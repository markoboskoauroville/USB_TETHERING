package community.auroville.usbtether

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class TetherTile : TileService() {
    override fun onStartListening() {
        UsbWatch.start(applicationContext)
        draw(Tether.known(this))
        Thread { val on = Tether.isOn(applicationContext); mainExecutor.execute { draw(on) } }.start()
    }

    private fun draw(on: Boolean) {
        val t = qsTile ?: return
        t.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.label = "USB tethering"
        t.updateTile()
    }

    @Suppress("DEPRECATION")
    override fun onClick() {
        val i = Intent(this, TapActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 1, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        } else startActivityAndCollapse(i)
    }
}
