# USB Tethering (Mantra) - takeover

Public, unpromoted repository: no README, no description (MANTRA_MANIFEST modules/repo-privacy.md).

**What it is.** An Android app for Marko's Nothing Phone 2a (and the Pixel 7). A 1x1 home-screen widget and a Quick Settings tile show ON/OFF and switch USB tethering with one tap, instead of digging through Settings several times a day. Package `community.auroville.usbtether`, name "USB Tethering (Mantra)". Request of 15.11.2026 is in momentaryupdates.md.

**How the toggle works.** A normal app cannot start tethering (TETHER_PRIVILEGED). The shell can: `svc usb setFunctions rndis` is USB tethering, `svc usb setFunctions` (no argument) restores the default. The app gets a shell through Shizuku (no root; started once per phone boot with Wireless debugging), calling Shizuku's newProcess by reflection (private since API 13). State is read from `sys.usb.state/config` (rndis) or, without Shizuku, from a rndis/usb network interface. Chosen over a Mac-side adb toggle because it works away from the Mac, and over TetheringManager reflection because that needs a privileged permission on Android 14/15.

**Fallback.** Without Shizuku (not installed, not running, not allowed) a tap on the widget or tile opens com.android.settings.TetherSettings, so it is one tap and the switch.

**Once (Marko).** Add the widget (long-press home, Widgets, USB Tethering) and the tile (edit Quick Settings, drag USB tethering in). Install Shizuku, start it with Wireless debugging, open the app, SET UP ONE-TAP SWITCH, allow. After each reboot, start Shizuku again.

**Build.** Only GitHub Actions (.github/workflows/android.yml), release vN Latest, appVersion in android/gradle.properties, whole numbers. Updater: AppUpdater.kt (kit android-updater) asks at start. Keystore secret ANDROID_DEBUG_KEYSTORE_B64 set.

**v2 (15.11.2026):** status-bar icons readable on the light screen.

**v3 (16.11.2026):** widget is a black circle with the white (ON) or grey #808080 (OFF) USB symbol, no text; the tile keeps the same USB symbol. Request in momentaryupdates.md.

**Not proven.** The Shizuku toggle on the Nothing (no hand on it yet); on the emulator only the fallback, the widget and the tile were tried.
