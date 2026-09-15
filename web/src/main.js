import { createClient } from '@supabase/supabase-js';
import './style.css';

const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL || 'https://xmikjxgzxbdmfjxiqanx.supabase.co';
const SUPABASE_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY || '';
const supabase = SUPABASE_KEY ? createClient(SUPABASE_URL, SUPABASE_KEY) : null;

const tables = [
  ['user_accounts', 'Accounts', 'Users registered through the mobile app', 'users'],
  ['user_profiles', 'Profiles', 'Couple profile records', 'heart'],
  ['learning_roadmaps', 'Roadmaps', 'Learning plans created for couples', 'book-open'],
  ['learning_tasks', 'Tasks', 'Daily learning tasks', 'check-square'],
  ['expense_entries', 'Expenses', 'Income and spending entries', 'wallet'],
  ['saving_tasks', 'Savings', 'Savings goals and rewards', 'piggy-bank'],
];

const icon = (name) => ({ users: '♧', heart: '♡', 'book-open': '▱', 'check-square': '☑', wallet: '▣', 'piggy-bank': '◉' }[name] || '•');
const fmt = (value) => new Intl.NumberFormat('en-US').format(value);

async function countRows(table) {
  if (!supabase) return null;
  const { count, error } = await supabase.from(table).select('*', { count: 'exact', head: true });
  if (error) throw error;
  return count ?? 0;
}

async function loadDashboard() {
  const results = await Promise.allSettled(tables.map(([table]) => countRows(table)));
  const values = results.map((result) => result.status === 'fulfilled' ? result.value : null);
  const connected = Boolean(supabase) && results.some((result) => result.status === 'fulfilled');
  const error = results.find((result) => result.status === 'rejected')?.reason;
  return { values, connected, error };
}

function render({ values, connected, error }) {
  const total = values.filter(Number.isFinite).reduce((sum, value) => sum + value, 0);
  const cards = tables.map(([table, label, description, glyph], index) => `
    <article class="metric-card">
      <div class="metric-top"><span class="metric-icon">${icon(glyph)}</span><span class="metric-label">${label}</span></div>
      <strong>${values[index] === null ? '—' : fmt(values[index])}</strong>
      <p>${description}</p>
      <small>${table}</small>
    </article>`).join('');

  document.querySelector('#app').innerHTML = `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand"><div class="brand-mark">D<span>+</span>C</div><div><b>DuoCouple</b><small>Control room</small></div></div>
        <nav><a class="active" href="#overview">Overview</a><a href="#data">Data catalog</a><a href="#security">Security notes</a></nav>
        <div class="side-note"><span class="pulse"></span><div><b>Supabase backend</b><small>${connected ? 'Connected and responding' : 'Waiting for credentials'}</small></div></div>
      </aside>
      <main class="content">
        <header class="topbar"><div><p class="eyebrow">DuoCouple / Admin</p><h1>Good morning, Malson.</h1><p class="subtitle">A calm view of your couple app’s cloud activity.</p></div><button id="refresh" class="refresh">↻ Refresh data</button></header>
        ${!SUPABASE_KEY ? `<section class="notice warning"><span>!</span><div><b>Connect this dashboard to Supabase</b><p>Add <code>VITE_SUPABASE_ANON_KEY</code> in Vercel Project Settings → Environment Variables, then redeploy. The publishable key is safe for browser use; never add a service-role key.</p></div></section>` : ''}
        ${error ? `<section class="notice error"><span>!</span><div><b>Supabase returned an error</b><p>${error.message || 'Check your RLS policies and table permissions.'}</p></div></section>` : ''}
        <section id="overview" class="hero-grid"><div class="hero-card"><div><span class="kicker">Cloud snapshot</span><h2>Your shared life,<br /><em>in sync.</em></h2><p>${connected ? 'Live counts are being read from your Supabase project.' : 'The dashboard is deployed and ready for your Supabase publishable key.'}</p></div><div class="orb"><div class="orb-inner">D<span>+</span>C</div></div></div><div class="summary-card"><span class="kicker">Records across core tables</span><strong>${connected ? fmt(total) : '—'}</strong><p>Read-only overview of your current cloud data.</p><div class="summary-line"><span>Project</span><b>xmikjxgzxbdmfjxiqanx</b></div><div class="summary-line"><span>Environment</span><b class="tag">Vercel · Hobby</b></div></div></section>
        <div class="section-heading"><div><span class="kicker">Live data</span><h2>Core collections</h2></div><span class="updated">${connected ? 'Updated just now' : 'Not connected yet'}</span></div>
        <section id="data" class="metrics">${cards}</section>
        <section id="security" class="security"><div class="security-icon">✓</div><div><b>Production safety checkpoint</b><p>This dashboard only uses the Supabase publishable/anon key. Keep Row Level Security enabled and add authenticated policies before exposing private couple data.</p></div><a href="https://supabase.com/dashboard/project/xmikjxgzxbdmfjxiqanx/auth/policies" target="_blank" rel="noreferrer">Review policies ↗</a></section>
        <footer><span>DuoCouple web dashboard · deployed from GitHub</span><a href="https://github.com/MinNyo23/DuoCouple" target="_blank" rel="noreferrer">View repository ↗</a></footer>
      </main>
    </div>`;
  document.querySelector('#refresh').addEventListener('click', async () => { document.querySelector('#refresh').textContent = '↻ Loading…'; render(await loadDashboard()); });
}

render(await loadDashboard());

