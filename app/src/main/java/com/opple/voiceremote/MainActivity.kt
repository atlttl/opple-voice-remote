package com.opple.voiceremote

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.opple.voiceremote.data.Prefs
import com.opple.voiceremote.ui.OppleTheme
import com.opple.voiceremote.ui.RemoteScreen
import com.opple.voiceremote.voice.VoiceEngine

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs
    private lateinit var voice: VoiceEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = Prefs(this)
        LauncherIcon.syncIfNeeded(this, prefs.iconStyle)
        voice = VoiceEngine(this)

        setContent {
            var themeMode by remember { mutableIntStateOf(prefs.themeMode) }
            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                1 -> true
                2 -> false
                else -> systemDark
            }

            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }

            OppleTheme(dark = dark) {
                RemoteScreen(
                    engine = voice,
                    prefs = prefs,
                    dark = dark,
                    themeMode = themeMode,
                    onThemeModeChange = {
                        prefs.themeMode = it
                        themeMode = it
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::voice.isInitialized) voice.refreshAudioState()
    }

    override fun onDestroy() {
        if (::voice.isInitialized) voice.shutdown()
        super.onDestroy()
    }
}
