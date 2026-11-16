package community.auroville.usbtether

import org.junit.Assert.*
import org.junit.Test

class TetherStateTest {
    @Test fun rndisIsOn() = assertTrue(Tether.usbExtrasSayOn(true, false))
    @Test fun ncmIsOn() = assertTrue(Tether.usbExtrasSayOn(false, true))
    @Test fun offTransition() = assertFalse(Tether.usbExtrasSayOn(false, false))
    @Test fun ifaceNames() {
        assertTrue(Tether.isTetherIface("rndis0")); assertTrue(Tether.isTetherIface("ncm0")); assertTrue(Tether.isTetherIface("usb0"))
        assertFalse(Tether.isTetherIface("wlan0")); assertFalse(Tether.isTetherIface("ap0"))
    }
}
