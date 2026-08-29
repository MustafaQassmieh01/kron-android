# Kron Android MVP — Manual Setup

Only external/project-owner items remain here. App features are not intentionally left half-configured.

## 1. API / hidden environment configuration

The supplied iOS project ignores `Dev.xcconfig`, `Prod.xcconfig`, and `GoogleService-Info.plist`.
The active Android MVP currently uses:

`https://api.tumbl.dev`

The current iOS networking flow did not actively attach the `API_KEY` placeholder, so the Android MVP does not invent one. If the backend later requires a private token/key, add it through local/CI secrets rather than committing it.

## 2. Local development backend

If the backend is run locally instead of `api.tumbl.dev`:

- Android Emulator normally reaches the host machine at `10.0.2.2`.
- A physical phone needs the development computer's LAN address and network access.

## 3. Confirm application ID before publishing

Current Android application ID:

`dev.kron.app`

Confirm it before the first Google Play release. Changing it after release effectively creates a different app.

## 4. Production signing

Android Studio handles debug signing. The project owner must configure production signing / Play App Signing before release. No production keystore or password is included.

## 5. Real-device smoke test

Before release, verify on at least one physical Android device:

- university list loads
- programme search works
- schedule opens
- bookmark survives app restart
- manual refresh updates bookmarked schedules
- daily/weekly navigation behaves correctly across timezone/day boundaries
- cached schedules remain visible after reopening without network

## 6. Play Console privacy/data declarations

The iOS privacy manifest does not complete Google Play's Data Safety form. Fill that out based on the final Android app/backend behavior before publishing.

## Removed from this MVP

There is intentionally no Play Billing, Pro entitlement system, notifications, background sync, widget, Firebase integration, notes, or advanced network/storage configuration in this branch. Those are not setup TODOs; their Android code was deleted for the MVP.
