package com.opple.voiceremote

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.opple.voiceremote.voice.VoiceStatus
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.opple.voiceremote.data.Prefs
import com.opple.voiceremote.ui.OppleTheme
import com.opple.voiceremote.ui.RemoteScreen
import com.opple.voiceremote.voice.VoiceEngine
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun shoot(
        name: String,
        dark: Boolean,
        beforeCapture: () -> Unit = {},
        content: @Composable () -> Unit,
    ) {
        compose.setContent { content() }
        compose.waitForIdle()
        beforeCapture()
        compose.waitForIdle()

        val view: View = compose.activity.window.decorView
        val width = if (view.width > 0) view.width else 1233
        val height = if (view.height > 0) view.height else 2673
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(if (dark) 0xFF14120F.toInt() else 0xFFF1EDE4.toInt())
        view.draw(canvas)

        val out = File("build/screenshots/$name.png")
        out.parentFile?.mkdirs()
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SHOT " + out.absolutePath + " " + bitmap.width + "x" + bitmap.height)
    }

    @Test
    fun captureDark() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        shoot("dark", true) {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
    }

    @Test
    fun captureLight() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        shoot("light", false) {
            OppleTheme(dark = false) {
                RemoteScreen(engine = engine, prefs = prefs, dark = false, themeMode = 2, onThemeModeChange = {})
            }
        }
    }

    @Test
    fun selectingLampUpdatesWakeWord() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        prefs.lampCount = 2
        prefs.setSlotWake(0, "小欧客厅灯")
        prefs.setSlotWake(1, "小欧主卧灯")
        val engine = VoiceEngine(app)
        compose.setContent {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("唤醒词 · 小欧客厅灯").assertExists()

        compose.onNodeWithTag("lampNode-主卧").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("唤醒词 · 小欧主卧灯").assertExists()
    }

    @Test
    fun longPressLampOpensWakeWordDialog() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        compose.setContent {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("lampNode-默认").performTouchInput { longClick() }
        compose.waitForIdle()
        compose.onNodeWithText("灯键 1").assertExists()
    }

    @Test
    fun plusAddsAnotherLamp() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        compose.setContent {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("lampNode-默认").assertExists()

        compose.onNodeWithTag("addLamp").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("lampNode-默认 1").assertExists()
        compose.onNodeWithTag("lampNode-默认 2").assertExists()
    }

    @Test
    fun captureThreeLamps() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        prefs.lampCount = 3
        prefs.setSlotWake(0, "小欧客厅灯")
        prefs.setSlotWake(1, "小欧餐厅灯")
        prefs.setSlotWake(2, "小欧主卧灯")
        val engine = VoiceEngine(app)
        shoot("three-lamps", true) {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
    }

    @Test
    fun captureEditor() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        shoot("editor", true, beforeCapture = {
            compose.onNodeWithTag("lampNode-默认").performTouchInput { longClick() }
        }) {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
    }

    @Test
    fun captureWindowWithClearButton() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = Prefs(app)
        val engine = VoiceEngine(app)
        forceWindow(engine, 12)
        shoot("window", true) {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = prefs, dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
    }

    @Test
    fun clearWindowEndsCountdown() {
        val app = RuntimeEnvironment.getApplication()
        val engine = VoiceEngine(app)
        forceWindow(engine, 12)
        compose.setContent {
            OppleTheme(dark = true) {
                RemoteScreen(engine = engine, prefs = Prefs(app), dark = true, themeMode = 1, onThemeModeChange = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("清空").performClick()
        compose.waitForIdle()
        org.junit.Assert.assertEquals(0, engine.status.value.windowLeft)
    }

    @Suppress("UNCHECKED_CAST")
    private fun forceWindow(engine: VoiceEngine, seconds: Int) {
        val field = VoiceEngine::class.java.getDeclaredField("_status")
        field.isAccessible = true
        val state = field.get(engine) as MutableState<VoiceStatus>
        state.value = state.value.copy(windowLeft = seconds)
    }

    @Test
    fun launchesMainActivity() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }
}
