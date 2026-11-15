package community.auroville.usbtether

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class TetherWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) = updateAll(c)

    companion object {
        fun updateAll(c: Context) {
            val m = AppWidgetManager.getInstance(c)
            val ids = m.getAppWidgetIds(ComponentName(c, TetherWidget::class.java))
            if (ids.isEmpty()) return
            val on = Tether.known(c)
            val v = RemoteViews(c.packageName, R.layout.widget)
            v.setTextViewText(R.id.state, if (on) "ON" else "OFF")
            v.setInt(R.id.root, "setBackgroundColor", if (on) Color.parseColor("#FF1B8A3E") else Color.parseColor("#FF222222"))
            val tap = PendingIntent.getActivity(
                c, 0, Intent(c, TapActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            v.setOnClickPendingIntent(R.id.root, tap)
            m.updateAppWidget(ids, v)
        }
    }
}
