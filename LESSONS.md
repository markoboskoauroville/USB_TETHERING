# Lessons

- 15.11.2026: the toggle is the shell command, not a tethering API; Shizuku is the only no-root way to hold a shell on a phone away from the Mac.
- Widget and tile open a no-UI translucent activity (TapActivity): background broadcast receivers may not start the settings page on Android 10+.
- 16.11.2026: a round widget is a vector oval drawable in an ImageView (a shape background would be a rectangle behind it); the state colour is setColorFilter on the symbol ImageView through RemoteViews, no text needed.
- 16.11.2026: an app cannot list network interfaces on Android 11+ (NetworkInterface returns nothing for rndis0), so the old reader always said OFF. Read the sticky android.hardware.usb.action.USB_STATE broadcast (extras rndis/ncm) with registerReceiver(null, filter). Manifest receivers do not get USB_STATE; a quiet foreground service (UsbWatch, specialUse) holds the dynamic receiver and redraws widget and tile. Nothing 2a uses rndis0 (tetherableNcmRegexs empty), ncm still accepted.
