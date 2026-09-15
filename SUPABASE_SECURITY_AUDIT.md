# Supabase data and security audit

## What the Android app currently uploads

The bulk backup path in `SupabaseSyncManager.forcePushToSupabase()` serializes and upserts the following Room entities:

| Local entity | Supabase table | Sensitive fields | Current sync status |
|---|---|---|---|
| `UserProfile` | `user_profiles` | Names, budget and savings goals | Uploaded by bulk sync |
| `LearningRoadmap` | `learning_roadmaps` | Titles, descriptions and owner labels | Uploaded by bulk sync |
| `RoadmapLesson` | `roadmap_lessons` | Learning content and completion state | Uploaded by bulk sync |
| `LearningTask` | `learning_tasks` | Tasks, dates and time spent | Uploaded by bulk sync |
| `ExpenseEntry` | `expense_entries` | Amounts, categories, dates, notes | Uploaded by bulk sync |
| `SavingTask` | `saving_tasks` | Goals, reward amounts and dates | Uploaded by bulk sync |
| `CalendarTask` | `calendar_tasks` | Calendar titles, times and completion state | Uploaded by bulk sync |
| Account metadata | `user_accounts` | Email, name and emoji | Separate account call |
| Device telemetry | `device_status_telemetry` | Device identifier, CPU/RAM and email | Separate telemetry call |

The push path is now non-destructive: it upserts local rows and does not delete all remote rows first. Local Room data is persistent on the device.

## What is not uploaded by the bulk entity sync

The following values currently remain in Android preferences or are handled locally: the custom Gemini API key, couple code, relationship start date, partner configuration, active session state, and local account password hash. These values should not be copied into a general-purpose Supabase table without an explicit ownership and retention design.

## Critical security finding

The current application uses a custom email/password flow and historically sent a SHA-256 password hash to `user_accounts`. A password hash is still authentication material and must not be treated as ordinary profile data. The current client also calls Supabase REST with the publishable/anon key and does not establish a Supabase Auth session. Therefore, the current code cannot honestly claim that private profiles, expenses, or telemetry are protected by user-level access control.

**Do not release the current custom account flow as a secure authentication system.** Use Supabase Auth for email/password authentication, password reset, sessions, and token refresh. Remove password hashes from `user_accounts`; store only a profile record keyed to `auth.users.id`.

## Required production rollout

1. Rotate any Supabase secret key that was previously embedded in the repository.
2. Enable Supabase Auth and migrate account login/signup to Supabase Auth.
3. Add an `owner_id uuid` or couple-membership identifier to every private data table.
4. Require a valid Auth JWT in all data requests. The publishable/anon key is not an authorization credential.
5. Enable Row Level Security on every private table.
6. Add policies that allow a user to access only their own rows or rows belonging to their couple membership.
7. Restrict `device_status_telemetry` reads to an administrator policy or a server-side function.
8. Remove any existing password-hash rows from `user_accounts` after migrating users.
9. Keep API keys, passwords, tokens, and private relationship data out of logs and client-facing documentation.
10. Test with two separate users: verify that each user cannot read or modify the other user's profile, expenses, tasks, or telemetry.

The baseline table definitions are in `supabase/schema.sql`. That file intentionally does not create broad anonymous policies because doing so would expose private couple and financial data. RLS policies must be designed together with the Supabase Auth migration; applying permissive `anon` policies is not an acceptable shortcut.

## Update coverage

Most database mutations in the view model call the bulk Supabase push path after the local Room mutation. The app also performs a startup pull. This is a best-effort full snapshot rather than a conflict-aware change log: deletions and concurrent edits are not represented as per-row tombstones, and the current client has no user ownership filter on bulk reads. That is another reason to complete Auth/RLS and then replace the snapshot approach with user/couple-scoped upserts and deletion markers.
