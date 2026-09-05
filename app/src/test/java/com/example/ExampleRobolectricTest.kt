package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DuckyScriptManager
import com.example.data.FirmwareManager
import com.example.data.SubGhzLabEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Flipper Droid", appName)
  }

  @Test
  fun `ducky script validation works`() {
    val manager = DuckyScriptManager()
    val script = "DELAY 500\nSTRING Hello Flipper\nENTER"
    val errors = manager.validateSyntax(script)
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `subghz signal synthesis works`() {
    val signal = SubGhzLabEngine.synthesizeSignal(433.92, "KeeLoq 64bit", "AM650", "1A2B3C4D5E")
    assertNotNull(signal)
    assertEquals(433.92, signal.frequencyMhz, 0.001)
    assertTrue(signal.pulses.isNotEmpty())
  }

  @Test
  fun `firmware manager loads stock pack`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fwManager = FirmwareManager(context)
    val pack = fwManager.currentFirmware.value
    assertNotNull(pack)
    assertEquals("Stock Flipper", pack.name)
  }
}
