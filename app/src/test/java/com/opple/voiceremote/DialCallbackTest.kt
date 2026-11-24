package com.opple.voiceremote

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.opple.voiceremote.ui.OppleTheme
import com.opple.voiceremote.ui.RemoteDial
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 回归测试：pointerInput(Unit) 不会随重组重启，
 * 切换目标灯具后圆盘必须调用最新的回调（否则唤醒词停留在旧值）。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DialCallbackTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun dialAlwaysUsesLatestCallback() {
        val version = mutableIntStateOf(0)
        val calls = mutableListOf<Int>()

        compose.setContent {
            OppleTheme(dark = true) {
                RemoteDial(
                    title = "卧室灯",
                    subtitle = "唤醒词 · v${version.intValue}",
                    windowProgress = 0f,
                    playing = false,
                    onAction = { calls.add(version.intValue) },
                    modifier = Modifier.size(300.dp).testTag("dial"),
                )
            }
        }

        compose.onNodeWithTag("dial").performTouchInput { click(Offset(width / 2f, 24f)) }
        compose.waitForIdle()

        version.intValue = 1
        compose.waitForIdle()

        compose.onNodeWithTag("dial").performTouchInput { click(Offset(width / 2f, 24f)) }
        compose.waitForIdle()

        assertEquals(listOf(0, 1), calls)
    }
}
