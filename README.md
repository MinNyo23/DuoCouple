# DuoCouple Android App

DuoCouple is a native Android application built with Kotlin, Jetpack Compose, Room, and direct Supabase REST access. The Android application itself does **not** require Vercel hosting. Supabase provides the remote database/API; Vercel is only needed if a separate web dashboard or server-side API is added later.

## Safety changes in this branch

The local Room database now persists to `duo_couple.db` instead of using an in-memory database. Supabase credentials are no longer hardcoded in the APK source, placeholder credentials are rejected, and the push-sync path no longer deletes every row in the remote tables before uploading.

If the old repository version was ever built or distributed, rotate the exposed Supabase key in the Supabase dashboard. A mobile application may contain only the publishable/anon key. Never put a `service_role` or `sb_secret_...` key in `.env`, source control, or an APK.

## Configure Supabase

1. Create or select the Supabase project.
2. Run the table definitions in [`supabase/schema.sql`](supabase/schema.sql).
3. Configure authentication and Row Level Security policies before production use. The current Android code does not yet establish a Supabase Auth session, so do not enable broad anonymous read/write policies for real user data.
4. Copy `.env.example` to `.env` locally and set `SUPABASE_URL` plus the publishable/anon key. `.env` is ignored by Git.
5. Alternatively, users can enter the project URL and publishable/anon key in the app's connection settings.

## Build locally

Use the checked-in Gradle wrapper:

```bash
cp .env.example .env
# Edit .env with a real project URL and publishable/anon key.
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Local builds require Android Studio or the Android command-line SDK with the Android 36 platform and build tools installed. Set `ANDROID_HOME` or add an uncommitted `local.properties` file containing `sdk.dir=/absolute/path/to/android-sdk`. The repository also includes GitHub Actions CI, which installs the SDK automatically for pull requests and pushes to `main`.

For a release bundle, configure a private upload keystore and these environment variables:

```bash
KEYSTORE_PATH=/absolute/path/to/upload-key.jks
STORE_PASSWORD=...
KEY_PASSWORD=...
./gradlew bundleRelease
```

Do not commit the keystore or passwords. The generated release bundle is placed under `app/build/outputs/bundle/release/`.

## Deployment decision

| Component | Hosting | Required? |
|---|---|---:|
| Android APK/AAB | Google Play Console or APK distribution | Yes |
| Supabase database/API | Supabase | Yes for cloud sync |
| Web admin dashboard | Vercel or another web host | No, unless a web dashboard is added |
| Protected Gemini proxy | Supabase Edge Function or another server | Recommended if a shared Gemini key is used |

## Before publishing

Verify that the Supabase tables and policies are correct, test fresh install and upgrade flows, test offline use and reconnect synchronization, confirm the release is signed with a private keystore, and run the unit tests plus a release build. The current app accepts a user-supplied Gemini key locally; do not ship a shared Gemini secret inside the application.

## Repository layout

- `app/src/main`: Android application code and resources
- `supabase/schema.sql`: starting database schema for the entities used by the app
- `.env.example`: local build variable template without real credentials
- `app/build.gradle.kts`: Android build and release signing configuration

## License and ownership

Add the project's license and privacy policy before distributing the application publicly.
