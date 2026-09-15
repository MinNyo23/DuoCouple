import { createClient } from '@supabase/supabase-js';

const send = (res, status, body) => res.status(status).setHeader('Content-Type', 'application/json').json(body);

export default async function handler(req, res) {
  if (req.method !== 'GET') return send(res, 405, { error: 'Method not allowed' });
  const authHeader = req.headers.authorization || '';
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : '';
  const url = process.env.SUPABASE_URL;
  const anonKey = process.env.SUPABASE_ANON_KEY || process.env.SUPABASE_PUBLISHABLE_KEY;
  const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!token) return send(res, 401, { error: 'A valid login session is required.' });
  if (!anonKey || !serviceKey) return send(res, 500, { error: 'Server authentication is not configured.' });
  try {
    const authClient = createClient(url, anonKey, { auth: { autoRefreshToken: false, persistSession: false } });
    const { data: { user }, error: userError } = await authClient.auth.getUser(token);
    if (userError || !user) return send(res, 401, { error: 'Your login session is invalid or expired.' });
    const role = user.app_metadata?.role;
    if (role !== 'admin') return send(res, 403, { error: 'Your account is authenticated but is not an administrator.' });
    const adminClient = createClient(url, serviceKey, { auth: { autoRefreshToken: false, persistSession: false } });
    const { data, error } = await adminClient.auth.admin.listUsers({ page: 1, perPage: 100 });
    if (error) return send(res, 502, { error: 'Supabase could not return the user list.' });
    return send(res, 200, { users: data.users.map((item) => ({ email: item.email, confirmed: Boolean(item.email_confirmed_at), created_at: item.created_at, last_sign_in_at: item.last_sign_in_at })) });
  } catch (error) {
    return send(res, 500, { error: 'Unexpected server error.' });
  }
}

