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
