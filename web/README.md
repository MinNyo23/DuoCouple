# DuoCouple Control Room

This is the Vercel-compatible web dashboard for the DuoCouple Android application. It lives in `web/` so the native Android project remains unchanged.

## Local development

```bash
cd web
npm install
npm run dev
```

## Vercel configuration

The linked Vercel project is configured from the repository root using `/vercel.json`. Add this environment variable in Vercel Project Settings → Environment Variables for Production, Preview, and Development:

```text
VITE_SUPABASE_ANON_KEY=<Supabase publishable/anon key>
```

`VITE_SUPABASE_URL` is optional because the dashboard defaults to the existing project URL. The dashboard only uses the publishable/anon key. Never add a Supabase service-role or `sb_secret_...` key to this frontend.

The dashboard currently provides a read-only overview. Before exposing private data, configure Supabase Auth and Row Level Security policies.

## Secure login and user list

The dashboard requires Supabase Auth email/password login. It stores the Supabase session using the official client library, refreshes tokens automatically, and signs out through Supabase Auth. Every dashboard view is hidden unless a valid session exists.

The **Load user list** button calls `/api/users`. The API validates the bearer token with Supabase Auth, checks `user.app_metadata.role === "admin"`, and only then uses the server-only Supabase service-role key to call `auth.admin.listUsers`. The service-role key must never be prefixed with `VITE_` and must never be exposed to the browser.

Add these Vercel environment variables for Production, Preview, and Development:

```text
VITE_SUPABASE_ANON_KEY=<Supabase publishable/anon key>
SUPABASE_ANON_KEY=<same publishable/anon key>
SUPABASE_SERVICE_ROLE_KEY=<Supabase service_role key; server-only>
```

To grant administrator access, use the Supabase Dashboard SQL Editor or an authenticated server-side admin workflow. Do not put a service-role key in the Android app, GitHub, or any `VITE_` variable. In Supabase SQL Editor, after replacing the email with the intended administrator, use a controlled server-side process to set the user’s `app_metadata` role to `admin`; never allow ordinary browser users to change their own `app_metadata`.

The user list is intentionally capped at the first 100 Auth users. Pagination can be added later if your project grows beyond that limit.
