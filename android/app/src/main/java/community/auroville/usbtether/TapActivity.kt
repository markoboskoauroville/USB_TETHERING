package community.auroville.usbtether

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import kotlin.concurrent.thread

/** No screen: toggle, say the result in one line, or open the Tethering page, and go. */
class TapActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val app = applicationContext
        thread {
            val r = Tether.toggle(app)
            runOnUiThread {
                when (r) {
                    null -> Tether.openSettings(this)
                    true -> Toast.makeText(this, "USB tethering ON", Toast.LENGTH_SHORT).show()
                    false -> Toast.makeText(this, "USB tethering OFF", Toast.LENGTH_SHORT).show()
                }
                TetherWidget.updateAll(app)
                finish()
            }
        }
    }
}
