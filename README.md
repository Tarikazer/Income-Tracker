# Income Control

A modern, offline-first personal budget and expense tracking Android application built with Jetpack Compose and Room.

## Features

- **Per-Month Budgeting**: Track recurring and separate expenses categorized cleanly per month.
- **Advance Payment Coverage**: Manage multi-month prepaid expenses with automatic coverage badges.
- **Multi-Currency Support**: Household profile configuration with native MAD and custom currencies.
- **Privacy & Offline First**: 100% on-device SQLite / Room persistence with local JSON backup export and restore.
- **Material 3 Design**: Smooth dark and light theme palette, edge-to-edge support, and responsive layouts.

## Build and Testing

```bash
# Run unit and Robolectric tests
gradle testDebugUnitTest

# Assemble debug APK
gradle assembleDebug
```

## How to release

To create a signed release APK using GitHub Actions:

1. **Create the keystore once** (locally using `keytool`):
   ```bash
   keytool -genkeypair -v -keystore release.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
2. **Add the 4 repository secrets** under **Settings > Secrets and variables > Actions**:
   - `KEYSTORE_BASE64`: Base64 string of the `release.jks` file (`base64 -w 0 release.jks` or `base64 -i release.jks`).
   - `KEYSTORE_PASSWORD`: Password for the keystore.
   - `KEY_ALIAS`: Alias of the key (e.g. `upload`).
   - `KEY_PASSWORD`: Password for the key.
3. **Tag and push**:
   ```bash
   git tag v1.12
   git push origin v1.12
   ```
4. The GitHub Actions workflow will automatically run the test suite, build the minified release APK with R8, sign it with your keystore, and attach `IncomeControl-v1.12.apk` to the new GitHub Release.
