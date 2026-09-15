-- DO NOT run this file before completing the Supabase Auth migration.
-- With no matching policies, enabling RLS intentionally blocks anonymous access.
-- Add owner_id/couple membership policies before enabling production traffic.

alter table public.user_profiles enable row level security;
alter table public.learning_roadmaps enable row level security;
alter table public.roadmap_lessons enable row level security;
alter table public.learning_tasks enable row level security;
alter table public.expense_entries enable row level security;
alter table public.saving_tasks enable row level security;
alter table public.calendar_tasks enable row level security;
alter table public.user_accounts enable row level security;
alter table public.device_status_telemetry enable row level security;

-- No permissive anon policies are included deliberately. The Android client
-- currently has no Supabase Auth JWT and must be migrated before these tables
-- can support authenticated read/write policies.
