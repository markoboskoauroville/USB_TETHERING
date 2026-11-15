package community.auroville.usbtether

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import rikka.shizuku.Shizuku
import kotlin.concurrent.thread

/** One thing on the first screen: the state and the button. The setup help sits one tap below. */
class MainActivity : Activity() {
    private lateinit var state: TextView
    private lateinit var help: TextView
    private lateinit var helpButton: Button

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, _ -> runOnUiThread { refresh() } }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        fun dp(n: Int) = (n * d).toInt()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }
        state = TextView(this).apply { textSize = 40f; gravity = Gravity.CENTER; setPadding(0, dp(40), 0, dp(24)) }
        val toggle = Button(this).apply {
            text = "SWITCH USB TETHERING"
            setOnClickListener { startActivity(Intent(this@MainActivity, TapActivity::class.java)) }
        }
        helpButton = Button(this).apply {
            text = "SET UP ONE-TAP SWITCH"
            setOnClickListener { if (Tether.shizuku(applicationContext) == Tether.Shiz.NEEDS_PERMISSION) runCatching { Shizuku.requestPermission(7) }; help.visibility = if (help.visibility == View.GONE) View.VISIBLE else View.GONE; refresh() }
        }
        val settings = Button(this).apply { text = "OPEN TETHERING SETTINGS"; setOnClickListener { Tether.openSettings(this@MainActivity) } }
        help = TextView(this).apply { textSize = 16f; visibility = View.GONE; setPadding(0, dp(16), 0, 0) }
        listOf(state, toggle, settings, helpButton, help).forEach { col.addView(it) }
        setContentView(ScrollView(this).apply {
            fitsSystemWindows = true
            addView(col)
        })
        Shizuku.addRequestPermissionResultListener(permListener)
        AppUpdater.checkAtStart(this)
    }

    override fun onResume() {
        super.onResume()
        AppUpdater.resume(this)
        refresh()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }

    private fun refresh() {
        val app = applicationContext
        thread {
            val on = Tether.isOn(app)
            val s = Tether.shizuku(app)
            runOnUiThread {
                state.text = if (on) "ON" else "OFF"
                state.setTextColor(if (on) Color.parseColor("#1B8A3E") else Color.DKGRAY)
                help.text = when (s) {
                    Tether.Shiz.READY -> "Ready. The widget and the tile switch USB tethering with one tap.\n\nPlug the cable into the computer first; the computer then shares the phone's internet."
                    Tether.Shiz.NEEDS_PERMISSION -> "Tap SET UP again: Shizuku asks once whether this app may use it. Allow it."
                    Tether.Shiz.NOT_RUNNING -> "Shizuku is installed but not running. Open Shizuku and press Start (Wireless debugging). After every restart of the phone, once more."
                    Tether.Shiz.NOT_INSTALLED -> "Without Shizuku a tap opens the Tethering page, where one switch is left.\n\nFor the real one-tap switch: install Shizuku (shizuku.rikka.app), start it with Wireless debugging, come back here. After every restart of the phone, start Shizuku again."
                }
                helpButton.text = if (s == Tether.Shiz.READY) "ONE-TAP SWITCH: READY" else "SET UP ONE-TAP SWITCH"
            }
        }
    }
}
