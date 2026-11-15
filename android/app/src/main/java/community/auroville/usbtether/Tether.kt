package community.auroville.usbtether

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import rikka.shizuku.Shizuku
import java.net.NetworkInterface

/**
 * The one mechanism. A normal app may not start tethering (TETHER_PRIVILEGED), but the shell may:
 * `svc usb setFunctions rndis` is USB tethering, `svc usb setFunctions` (no argument) puts it back.
 * Shizuku hands the app a shell without root. Without Shizuku the tap opens the Tethering settings page.
 */
object Tether {
    enum class Shiz { NOT_INSTALLED, NOT_RUNNING, NEEDS_PERMISSION, READY }

    private const val SHIZUKU_PKG = "moe.shizuku.privileged.api"
    private const val PREFS = "tether"

    fun shizuku(c: Context): Shiz {
        val installed = runCatching { c.packageManager.getPackageInfo(SHIZUKU_PKG, 0); true }.getOrDefault(false)
        if (!installed) return Shiz.NOT_INSTALLED
        if (!runCatching { Shizuku.pingBinder() }.getOrDefault(false)) return Shiz.NOT_RUNNING
        val ok = runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)
        return if (ok) Shiz.READY else Shiz.NEEDS_PERMISSION
    }

    /** Runs one shell command through Shizuku (newProcess is private in API 13, so by reflection). */
    private fun sh(cmd: String): String? = runCatching {
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
        ).apply { isAccessible = true }
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        val out = p.inputStream.bufferedReader().readText()
        p.waitFor()
        out
    }.getOrNull()

    private fun ifaceUp(): Boolean = runCatching {
        NetworkInterface.getNetworkInterfaces().toList().any {
            (it.name.startsWith("rndis") || it.name.startsWith("usb")) && it.isUp
        }
    }.getOrDefault(false)

    /** The last state we knew: what the widget and the tile draw without waiting for a shell. */
    fun known(c: Context): Boolean =
        ifaceUp() || c.getSharedPreferences(PREFS, 0).getBoolean("on", false)

    private fun remember(c: Context, on: Boolean) = c.getSharedPreferences(PREFS, 0).edit().putBoolean("on", on).apply()

    /** The true state: the USB function list says rndis. Needs Shizuku; otherwise the interface check. */
    fun isOn(c: Context): Boolean {
        val on = if (shizuku(c) == Shiz.READY) {
            sh("getprop sys.usb.state; getprop sys.usb.config")?.contains("rndis") ?: ifaceUp()
        } else ifaceUp()
        remember(c, on)
        return on
    }

    /** Flips it. Returns null when the real toggle is not available (the caller opens the settings page). */
    fun toggle(c: Context): Boolean? {
        if (shizuku(c) != Shiz.READY) return null
        val wasOn = isOn(c)
        sh(if (wasOn) "svc usb setFunctions" else "svc usb setFunctions rndis") ?: return null
        Thread.sleep(1200)
        val now = isOn(c)
        TetherWidget.updateAll(c)
        return now
    }

    /** The always-works path: the Tethering page itself, one switch away. */
    fun openSettings(c: Context) {
        val tries = listOf(
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.TetherSettings")),
            Intent("android.settings.TETHER_SETTINGS"),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (i in tries) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (runCatching { c.startActivity(i) }.isSuccess) return
        }
    }
}
