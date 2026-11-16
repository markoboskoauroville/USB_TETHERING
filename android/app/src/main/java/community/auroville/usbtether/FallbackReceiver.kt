package community.auroville.usbtether

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** v5: the 15-minute inexact alarm: redraw widget and tile, and try to bring UsbWatch back if it was killed. */
class FallbackReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { UsbWatch.refreshAll(c); UsbWatch.start(c) }
}
