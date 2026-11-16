package community.auroville.usbtether

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { UsbWatch.start(c); UsbWatch.schedule(c); UsbWatch.refreshAll(c) }
}
