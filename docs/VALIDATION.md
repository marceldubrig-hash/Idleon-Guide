# Validation · 2026-11-08

Successful CI: https://github.com/marceldubrig-hash/Idleon-Guide/actions/runs/42530900295

- 7 data/calculation tests passed: graph references, local icons, search filters, adjacency, probability edge cases, recursive recipes and import metadata.
- 5 Playwright browser tests passed: search/detail/bookmarks/persistence; recipe/drop calculators; main routes without JS errors; responsive widths 390/768/1440; offline app/data after service worker installation.
- Production build succeeded. The downloadable `idleon-atlas-web-app` artifact contains the built app and assets.
- Desktop and mobile home screenshots visually reviewed. Screenshot evidence is committed alongside this report.
- Real Fold/Android 17 device testing has not been performed. Android emulator validation was subsequently completed as recorded below.
- GitHub Pages deployment workflow is prepared but has NOT been executed or claimed live. The repository owner must enable Pages (Source: GitHub Actions) before running it; the available connector does not expose Pages administration.
- Initial CI failure was an Ubuntu 26 / Playwright compatibility issue; pinning ubuntu-24.04 fixed it. Local browser startup was restricted, so browser validation was performed successfully on GitHub Actions.

![Desktop](screenshots/desktop.png)

[Mobile screenshot](screenshots/mobile.png)

## Android APK · 2026-11-09

Successful Android build: https://github.com/marceldubrig-hash/Idleon-Guide/actions/runs/42561043176

- Android release build and lint passed.
- Two Android 15 instrumentation tests passed: offline startup without INTERNET permission, database search, native backup bridge availability, bookmarking, persistence across activity recreation, world 7, local graphics and probability calculator.
- The actual release APK was then installed and launched in the emulator. Screenshot visually reviewed.
- APK signature verification passed. Public unauthenticated download returned HTTP 200; 14,812,813 bytes; SHA-256 matched the published checksum.
- Native backup file-picker roundtrip and a physical Galaxy Fold 7 / Android 17 remain untested.
- This preview uses a local WebView for the UI, with Android navigation/insets/file picking. It is not a Compose rewrite.

[Download APK](https://github.com/marceldubrig-hash/Idleon-Guide/releases/download/android-preview-3/Idleon-Atlas-Android.apk) · [Release](https://github.com/marceldubrig-hash/Idleon-Guide/releases/tag/android-preview-3)
