-- Auth, couple_pairs, sync columns, RLS, and RPC helpers for DuoCouple.
-- Run AFTER baseline schema.sql on your Supabase project.

-- ---------- couple membership ----------
create table if not exists public.couple_pairs (
  id uuid primary key default gen_random_uuid(),
  invite_code text not null unique,
  user_a_id uuid not null references auth.users (id) on delete cascade,
  user_b_id uuid references auth.users (id) on delete set null,
  status text not null default 'inviting',
  creator_name text not null,
  creator_emoji text not null,
  joiner_name text,
  joiner_emoji text,
  created_at timestamptz not null default now(),
  updated_at bigint not null default (extract(epoch from now()) * 1000)::bigint,
  constraint couple_pairs_status_check check (
    status in ('inviting', 'pending_accept', 'active', 'cancelled')
  )
);

create index if not exists couple_pairs_user_a_idx on public.couple_pairs (user_a_id);
create index if not exists couple_pairs_user_b_idx on public.couple_pairs (user_b_id);

-- ---------- sync metadata on app tables ----------
alter table public.user_profiles add column if not exists "coupleId" uuid;
alter table public.user_profiles add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.user_profiles add column if not exists "deletedAt" bigint;

alter table public.learning_roadmaps add column if not exists "coupleId" uuid;
alter table public.learning_roadmaps add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.learning_roadmaps add column if not exists "deletedAt" bigint;

alter table public.roadmap_lessons add column if not exists "coupleId" uuid;
alter table public.roadmap_lessons add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.roadmap_lessons add column if not exists "deletedAt" bigint;

alter table public.learning_tasks add column if not exists "coupleId" uuid;
alter table public.learning_tasks add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.learning_tasks add column if not exists "deletedAt" bigint;

alter table public.expense_entries add column if not exists "coupleId" uuid;
alter table public.expense_entries add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.expense_entries add column if not exists "deletedAt" bigint;

alter table public.saving_tasks add column if not exists "coupleId" uuid;
alter table public.saving_tasks add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.saving_tasks add column if not exists "deletedAt" bigint;

alter table public.calendar_tasks add column if not exists "coupleId" uuid;
alter table public.calendar_tasks add column if not exists "updatedAt" bigint not null default (extract(epoch from now()) * 1000)::bigint;
alter table public.calendar_tasks add column if not exists "deletedAt" bigint;

alter table public.couple_location_updates add column if not exists "coupleId" uuid;
alter table public.couple_location_updates add column if not exists "deletedAt" bigint;

alter table public.user_accounts add column if not exists user_id uuid unique references auth.users (id) on delete cascade;

-- ---------- helper: active couple for auth.uid() ----------
create or replace function public.my_active_couple_id()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select id
  from public.couple_pairs
  where status = 'active'
    and (user_a_id = auth.uid() or user_b_id = auth.uid())
  order by updated_at desc
  limit 1;
$$;

revoke all on function public.my_active_couple_id() from public;
grant execute on function public.my_active_couple_id() to authenticated;

-- ---------- RPC: couple linking ----------
create or replace function public.create_couple_invite(
  p_invite_code text,
  p_creator_name text,
  p_creator_emoji text
)
returns public.couple_pairs
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_row public.couple_pairs;
begin
  if v_uid is null then
    raise exception 'not authenticated';
  end if;
  if exists (
    select 1 from public.couple_pairs
    where status = 'active' and (user_a_id = v_uid or user_b_id = v_uid)
  ) then
    raise exception 'already in an active couple';
  end if;
  insert into public.couple_pairs (
    invite_code, user_a_id, creator_name, creator_emoji, status, updated_at
  )
  values (
    upper(trim(p_invite_code)),
    v_uid,
    p_creator_name,
    p_creator_emoji,
    'inviting',
    (extract(epoch from now()) * 1000)::bigint
  )
  returning * into v_row;
  return v_row;
end;
$$;

create or replace function public.request_couple_join(
  p_invite_code text,
  p_joiner_name text,
  p_joiner_emoji text
)
returns public.couple_pairs
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_row public.couple_pairs;
begin
  if v_uid is null then
    raise exception 'not authenticated';
  end if;
  select * into v_row
  from public.couple_pairs
  where invite_code = upper(trim(p_invite_code))
    and status = 'inviting'
    and user_b_id is null
  for update;
  if not found then
    raise exception 'invite not found or already used';
  end if;
  if v_row.user_a_id = v_uid then
    raise exception 'cannot join your own invite';
  end if;
  update public.couple_pairs
  set user_b_id = v_uid,
      joiner_name = p_joiner_name,
      joiner_emoji = p_joiner_emoji,
      status = 'pending_accept',
      updated_at = (extract(epoch from now()) * 1000)::bigint
  where id = v_row.id
  returning * into v_row;
  return v_row;
end;
$$;

create or replace function public.accept_couple_partnership(p_pair_id uuid)
returns public.couple_pairs
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_row public.couple_pairs;
begin
  if v_uid is null then
    raise exception 'not authenticated';
  end if;
  select * into v_row
  from public.couple_pairs
  where id = p_pair_id
  for update;
  if not found then
    raise exception 'pair not found';
  end if;
  if v_row.user_b_id is distinct from v_uid then
    raise exception 'only the joining partner can accept';
  end if;
  if v_row.status <> 'pending_accept' then
    raise exception 'pair is not awaiting acceptance';
  end if;
  update public.couple_pairs
  set status = 'active',
      updated_at = (extract(epoch from now()) * 1000)::bigint
  where id = p_pair_id
  returning * into v_row;
  return v_row;
end;
$$;

create or replace function public.get_my_couple_pair()
returns public.couple_pairs
language sql
stable
security definer
set search_path = public
as $$
  select *
  from public.couple_pairs
  where user_a_id = auth.uid() or user_b_id = auth.uid()
  order by updated_at desc
  limit 1;
$$;

grant execute on function public.create_couple_invite(text, text, text) to authenticated;
grant execute on function public.request_couple_join(text, text, text) to authenticated;
grant execute on function public.accept_couple_partnership(uuid) to authenticated;
grant execute on function public.get_my_couple_pair() to authenticated;

-- ---------- RLS ----------
alter table public.couple_pairs enable row level security;

create policy couple_pairs_select_member on public.couple_pairs
for select to authenticated
using (user_a_id = auth.uid() or user_b_id = auth.uid());

create policy couple_pairs_update_member on public.couple_pairs
for update to authenticated
using (user_a_id = auth.uid() or user_b_id = auth.uid())
with check (user_a_id = auth.uid() or user_b_id = auth.uid());

-- Data tables: couple-scoped read/write for authenticated users in an active couple.
do $$
declare
  tbl text;
begin
  foreach tbl in array array[
    'user_profiles',
    'learning_roadmaps',
    'roadmap_lessons',
    'learning_tasks',
    'expense_entries',
    'saving_tasks',
    'calendar_tasks',
    'couple_location_updates'
  ]
  loop
    execute format('alter table public.%I enable row level security', tbl);
    execute format('drop policy if exists %I_couple_rw on public.%I', tbl, tbl);
    execute format(
      'create policy %I_couple_rw on public.%I for all to authenticated using ("coupleId" = public.my_active_couple_id()) with check ("coupleId" = public.my_active_couple_id())',
      tbl, tbl
    );
  end loop;
end $$;

-- Profile metadata tied to auth user
alter table public.user_accounts enable row level security;
drop policy if exists user_accounts_self on public.user_accounts;
create policy user_accounts_self on public.user_accounts
for all to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());
