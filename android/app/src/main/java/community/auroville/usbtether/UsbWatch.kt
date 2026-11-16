package community.auroville.usbtether

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.service.quicksettings.TileService

/**
 * Keeps the widget and the tile current when tethering changes from anywhere (Settings, the tile, adb):
 * a dynamic receiver for USB_STATE (manifest receivers do not get it), alive in a quiet foreground service.
 */
class UsbWatch : Service() {
    private val rx = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { refreshAll(c) }
    }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("watch", "Widget sync", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, "watch").setSmallIcon(R.drawable.ic_app).setContentTitle("USB tethering widget sync").build()
        runCatching { startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) }
        registerReceiver(rx, IntentFilter("android.hardware.usb.action.USB_STATE"))
        registerReceiver(rx, IntentFilter("android.net.conn.TETHER_STATE_CHANGED"))
        registerReceiver(rx, IntentFilter(Intent.ACTION_SCREEN_ON))   // v5: redraw when the screen comes on
        schedule(this)
        refreshAll(this)
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY

    override fun onDestroy() { runCatching { unregisterReceiver(rx) }; super.onDestroy() }

    companion object {
        /** v5: fallback in case UsbWatch is killed: an inexact repeat about every 15 minutes (no permission needed). */
        fun schedule(c: Context) {
            runCatching {
                val pi = PendingIntent.getBroadcast(c, 5, Intent(c, FallbackReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                c.getSystemService(AlarmManager::class.java).setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi)
            }
        }

        fun start(c: Context) { runCatching { c.startForegroundService(Intent(c, UsbWatch::class.java)) } }

        /** Redraw the widget and ask the tile to redraw. */
        fun refreshAll(c: Context) {
            TetherWidget.updateAll(c)
            runCatching { TileService.requestListeningState(c, ComponentName(c, TetherTile::class.java)) }
        }
    }
}
