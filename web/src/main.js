import { createClient } from '@supabase/supabase-js';
import './style.css';

const url = (import.meta.env.VITE_SUPABASE_URL || '').trim();
const key = (import.meta.env.VITE_SUPABASE_ANON_KEY || '').trim();
const supabase = url && key ? createClient(url, key, { auth: { persistSession: true, autoRefreshToken: true, detectSessionInUrl: true } }) : null;
const collections = [
  ['user_accounts', 'Accounts', 'Registered people', 'users'],
  ['user_profiles', 'Couples', 'Shared profiles', 'heart'],
  ['learning_roadmaps', 'Roadmaps', 'Learning plans', 'path'],
  ['learning_tasks', 'Tasks', 'Daily actions', 'check'],
  ['expense_entries', 'Expenses', 'Money entries', 'wallet'],
  ['saving_tasks', 'Savings', 'Goals in motion', 'target'],
];
const icon = (name) => ({ users: '◌', heart: '♡', path: '⌁', check: '✓', wallet: '▣', target: '◎' }[name] || '•');
const esc = (value = '') => String(value).replace(/[&<>'"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[c]));
const number = (value) => new Intl.NumberFormat('en-US').format(value);

function loginScreen(message = '') {
  document.querySelector('#app').innerHTML = `<main class="login-page"><div class="login-visual"><div class="mini-orbit"><span></span><b>DC</b></div><p class="eyebrow">DuoCouple / secure workspace</p><h1>Build a better<br><em>life together.</em></h1><p>One calm command center for your relationship, routines, money, and plans.</p></div><section class="login-card"><div class="brand"><span class="brand-mark">D<span>+</span>C</span><span><b>DuoCouple</b><small>Control room</small></span></div><p class="eyebrow">Welcome back</p><h2>Sign in to your dashboard</h2><p class="login-copy">Your private workspace is protected by Supabase Auth.</p>${message ? `<div class="error-box">${esc(message)}</div>` : ''}<form id="login-form"><label>Email<input id="email" type="email" autocomplete="username" required placeholder="you@example.com"></label><label>Password<input id="password" type="password" autocomplete="current-password" required minlength="6" placeholder="Your password"></label><button class="button primary" type="submit">Enter workspace <span>→</span></button></form><p class="login-foot">Need access? Ask your workspace administrator.</p></section></main>`;
  document.querySelector('#login-form').addEventListener('submit', async (event) => {
    event.preventDefault(); const button = event.currentTarget.querySelector('button'); button.disabled = true; button.textContent = 'Checking access…';
    const { error } = await supabase.auth.signInWithPassword({ email: document.querySelector('#email').value.trim(), password: document.querySelector('#password').value });
    if (error) return loginScreen(error.status === 429 ? 'Too many attempts. Please wait and try again.' : 'Invalid email or password.');
    boot();
  });
}

async function readCount(table) { const { count, error } = await supabase.from(table).select('id', { count: 'exact', head: true }); if (error) throw error; return count ?? 0; }
async function getCounts() { const results = await Promise.allSettled(collections.map(([table]) => readCount(table))); return { values: results.map((r) => r.status === 'fulfilled' ? r.value : null), error: results.find((r) => r.status === 'rejected')?.reason }; }

function render({ values, error, user }) {
  const total = values.reduce((sum, value) => sum + (Number.isFinite(value) ? value : 0), 0);
  const cards = collections.map(([table, label, description, glyph], index) => `<article class="metric-card"><div class="metric-heading"><span class="metric-icon">${icon(glyph)}</span><span>${label}</span></div><strong>${values[index] === null ? '—' : number(values[index])}</strong><p>${description}</p><small>${table}</small></article>`).join('');
  document.querySelector('#app').innerHTML = `<div class="app-shell"><aside class="sidebar"><div class="brand"><span class="brand-mark">D<span>+</span>C</span><span><b>DuoCouple</b><small>Control room</small></span></div><nav><a class="active" href="#overview">Overview</a><a href="#collections">Collections</a><a href="#users">People</a><a href="#security">Security</a></nav><div class="sidebar-bottom"><div class="connection"><span></span><div><b>Connected</b><small>Supabase + Vercel</small></div></div><button id="logout" class="text-button">Sign out</button></div></aside><main class="dashboard"><header class="topbar"><div><p class="eyebrow">DuoCouple / Admin</p><h1>Your shared life, <em>in sync.</em></h1><p class="subtitle">A useful, live view of the data powering your mobile application.</p></div><div class="top-actions"><span class="secure-pill">● Secure session</span><button class="button outline" id="refresh">↻ Refresh</button></div></header>${error ? `<div class="notice"><b>Some collections need attention</b><span>${esc(error.message || 'Check RLS policies and table permissions.')}</span></div>` : ''}<section id="overview" class="overview-grid"><div class="welcome-card"><div><p class="eyebrow">Cloud snapshot</p><h2>Make every<br><em>moment count.</em></h2><p>Real-time counts from your authenticated Supabase session. Use this control room to understand what is happening in the app at a glance.</p><div class="card-actions"><a href="#collections" class="button primary">Explore data <span>→</span></a><a href="#users" class="button outline">View people</a></div></div><div class="orbit-art"><div class="orbit-ring ring-one"></div><div class="orbit-ring ring-two"></div><div class="orbit-core">D<span>+</span>C</div></div></div><div class="summary-card"><p class="eyebrow">Total records</p><strong>${number(total)}</strong><p class="muted">Across six connected collections</p><div class="summary-row"><span>Signed in as</span><b>${esc(user.email)}</b></div><div class="summary-row"><span>Environment</span><b class="green">Production ready</b></div></div></section><section id="collections" class="section-block"><div class="section-heading"><div><p class="eyebrow">Live data</p><h2>Core collections</h2></div><span class="updated">Updated just now</span></div><div class="metrics">${cards}</div></section><section id="users" class="people-panel"><div class="section-heading"><div><p class="eyebrow">Team access</p><h2>People in your workspace</h2><p class="muted">Securely loaded through the protected Vercel API route.</p></div><button id="load-users" class="button outline">Load people</button></div><div id="users-result" class="users-result"><div class="empty-state">Load the user list when you need it. Admin permission is checked server-side.</div></div></section><section id="security" class="security-panel"><span class="security-icon">✓</span><div><b>Security is part of the product</b><p>Supabase Auth handles sessions, row-level security protects records, and the user directory never exposes a service key to the browser.</p></div></section><footer><span>© ${new Date().getFullYear()} DuoCouple</span><span>Built for two, managed with care.</span></footer></main></div>`;
  document.querySelector('#logout').addEventListener('click', async () => { await supabase.auth.signOut(); loginScreen(); });
  document.querySelector('#refresh').addEventListener('click', async () => { const button = document.querySelector('#refresh'); button.textContent = '↻ Loading…'; render({ ...(await getCounts()), user }); });
  document.querySelector('#load-users').addEventListener('click', loadUsers);
}

async function loadUsers() {
  const target = document.querySelector('#users-result'); const button = document.querySelector('#load-users'); button.disabled = true; button.textContent = 'Checking…';
  const { data: { session } } = await supabase.auth.getSession(); const response = await fetch('/api/users', { headers: { Authorization: `Bearer ${session?.access_token || ''}` } }); const body = await response.json().catch(() => ({})); button.disabled = false; button.textContent = 'Refresh people';
  if (!response.ok) { target.innerHTML = `<div class="users-error"><b>${response.status === 403 ? 'Admin permission required' : 'Could not load people'}</b><p>${esc(body.error || 'The protected endpoint rejected this request.')}</p></div>`; return; }
  target.innerHTML = `<div class="table-wrap"><table><thead><tr><th>Email</th><th>Status</th><th>Created</th><th>Last sign-in</th></tr></thead><tbody>${body.users.map((item) => `<tr><td>${esc(item.email || '—')}</td><td><span class="status ${item.confirmed ? 'confirmed' : ''}">${item.confirmed ? 'Confirmed' : 'Unconfirmed'}</span></td><td>${esc(item.created_at ? new Date(item.created_at).toLocaleDateString() : '—')}</td><td>${esc(item.last_sign_in_at ? new Date(item.last_sign_in_at).toLocaleString() : 'Never')}</td></tr>`).join('')}</tbody></table></div>`;
}

async function boot() { if (!supabase) return loginScreen('The dashboard is not configured. Add the Supabase public variables in Vercel.'); const { data: { session } } = await supabase.auth.getSession(); if (!session) return loginScreen(); render({ ...(await getCounts()), user: session.user }); supabase.auth.onAuthStateChange((_event, next) => { if (!next) loginScreen(); }); }
boot();
