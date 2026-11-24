package com.opple.voiceremote.voice

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale
import kotlin.math.roundToInt

data class VoiceStatus(
    val ready: Boolean = false,
    val engineName: String = "检测中",
    val chineseAvailable: Boolean = false,
    val voiceLabel: String = "",
    val playing: Boolean = false,
    val lastSpoken: String = "",
    val volumePercent: Int = 100,
    val externalOutput: String? = null,
    val windowLeft: Int = 0,
    val message: String? = null,
)

/**
 * 语音外放引擎。
 *
 * 关键点：
 * 1. 使用 STREAM_MUSIC + USAGE_MEDIA，保证走扬声器而不是听筒；
 * 2. 通过 UtteranceProgressListener 串行排队“唤醒词 → 停顿 → 命令”，
 *    停顿用 playSilence 实现，模块更容易把它当成两次自然发音；
 * 3. 播完自动还原音量，避免污染系统音量。
 */
class VoiceEngine(context: Context) {

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val audio = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _status = mutableStateOf(VoiceStatus())
    val status: State<VoiceStatus> = _status

    private var tts: TextToSpeech? = null
    private var lastUtteranceId: String? = null
    private var windowSeconds = 0
    private var savedVolume: Int? = null
    private var focusHeld = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { }

    private val ticker = object : Runnable {
        override fun run() {
            if (windowSeconds > 0) {
                windowSeconds -= 1
                _status.value = _status.value.copy(windowLeft = windowSeconds)
                if (windowSeconds > 0) main.postDelayed(this, 1000)
            }
        }
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) {
            if (utteranceId != null && utteranceId == lastUtteranceId) finishSequence()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            if (utteranceId == null || utteranceId == lastUtteranceId) {
                failSequence("语音合成失败，请检查系统语音引擎")
            }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            // 只处理当前这条序列，避免上一条被 stop() 后的回调干扰
            if (utteranceId == null || utteranceId == lastUtteranceId) {
                failSequence("语音合成失败（错误码 $errorCode）")
            }
        }
    }

    init {
        refreshAudioState()
        tts = TextToSpeech(appContext) { code ->
            main.post {
                if (code == TextToSpeech.SUCCESS) onEngineReady()
                else _status.value = _status.value.copy(
                    ready = false,
                    engineName = "无可用语音引擎",
                    message = "系统未找到语音合成引擎，请先安装或启用",
                )
            }
        }
    }

    private fun onEngineReady() {
        val engine = tts ?: return
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        val result = engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
        val chinese = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

        if (chinese) {
            val voices = runCatching { engine.voices }.getOrNull()
            val chosen = voices
                ?.filter { it.locale.language.equals("zh", ignoreCase = true) }
                ?.filter { !it.name.contains("network", ignoreCase = true) }
                ?.minByOrNull { it.name }
                ?: voices?.firstOrNull { it.locale.language.equals("zh", ignoreCase = true) }
            if (chosen != null) runCatching { engine.voice = chosen }
        }
        engine.setOnUtteranceProgressListener(listener)

        _status.value = _status.value.copy(
            ready = true,
            engineName = engineLabel(),
            chineseAvailable = chinese,
            voiceLabel = runCatching { engine.voice?.locale?.getDisplayName(Locale.CHINA) }.getOrNull() ?: "系统默认音色",
            message = if (chinese) null else "当前引擎缺少中文语音包，请到系统语音设置中下载",
        )
        refreshAudioState()
    }

    private fun engineLabel(): String {
        val pkg = runCatching {
            Settings.Secure.getString(appContext.contentResolver, "tts_default_synth")
        }.getOrNull().orEmpty()
        return when {
            pkg.contains("google", true) -> "Google 语音服务"
            pkg.contains("iflytek", true) -> "讯飞语音"
            pkg.contains("baidu", true) -> "百度语音"
            pkg.contains("xiaomi", true) || pkg.contains("miui", true) -> "小米语音"
            pkg.contains("huawei", true) || pkg.contains("hisi", true) -> "华为语音"
            pkg.contains("samsung", true) -> "三星语音"
            pkg.contains("oppo", true) || pkg.contains("coloros", true) -> "OPPO 语音"
            pkg.contains("vivo", true) -> "vivo 语音"
            pkg.isNotBlank() -> pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            else -> "系统语音引擎"
        }
    }

    fun refreshAudioState() {
        runCatching {
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val external = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_HEARING_AID
            }
            _status.value = _status.value.copy(
                volumePercent = if (max > 0) (current * 100f / max).roundToInt() else 100,
                externalOutput = external?.productName?.toString()?.takeIf { it.isNotBlank() },
            )
        }
    }

    /** 立即结束 15 秒连续控制窗口，下一条命令会重新带唤醒词 */
    fun clearWindow() {
        main.removeCallbacks(ticker)
        windowSeconds = 0
        _status.value = _status.value.copy(windowLeft = 0)
    }

    fun setVolumePercent(percent: Float) {
        runCatching {
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (max * percent).roundToInt().coerceIn(0, max), 0)
        }
        refreshAudioState()
    }

    /**
     * @param useWake true = 先播唤醒词再播命令；false = 只播命令
     */
    fun speak(
        wakeWord: String,
        command: String,
        rate: Float,
        gapMs: Int,
        repeatTimes: Int,
        boostPercent: Int?,
        continuousWindow: Boolean,
        useWake: Boolean = true,
    ): Boolean {
        val engine = tts
        val snapshot = _status.value
        if (engine == null || !snapshot.ready) {
            _status.value = snapshot.copy(message = "语音引擎尚未就绪")
            return false
        }
        if (snapshot.playing) return false

        val needsWake = useWake && windowSeconds <= 0 && wakeWord.isNotBlank()

        requestFocus()
        boostPercent?.let { boostVolume(it) }
        runCatching { engine.setSpeechRate(rate.coerceIn(0.5f, 1.4f)) }
        runCatching { engine.stop() }

        val stamp = System.currentTimeMillis()
        val parts = mutableListOf<Pair<String, String>>()
        var index = 0
        if (needsWake) {
            parts += wakeWord.trim() to "u${stamp}_${index++}"
            parts += "" to "u${stamp}_${index++}"
        }
        repeat(repeatTimes.coerceIn(1, 3)) { round ->
            if (round > 0) parts += "" to "u${stamp}_${index++}"
            parts += command to "u${stamp}_${index++}"
        }
        lastUtteranceId = parts.last().second

        parts.forEachIndexed { i, part ->
            val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            if (part.first.isEmpty()) {
                val silenceParams = hashMapOf<String, String>(
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to part.second
                )
                engine.playSilence(gapMs.coerceIn(150, 2000).toLong(), mode, silenceParams)
            } else {
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, part.second)
                }
                engine.speak(part.first, mode, params, part.second)
            }
        }

        // 灯具的 15s 窗口从唤醒词起算，因此这里按发送时刻计时，
        // 播完时窗口已消耗掉唤醒词与命令的时长，更贴近真实行为。
        main.removeCallbacks(ticker)
        if (continuousWindow) {
            windowSeconds = 15
            main.postDelayed(ticker, 1000)
        } else {
            windowSeconds = 0
        }

        _status.value = _status.value.copy(
            playing = true,
            lastSpoken = if (needsWake) "$wakeWord · $command" else command,
            windowLeft = if (continuousWindow) 15 else 0,
            message = null,
        )
        return true
    }

    private fun requestFocus() {
        runCatching {
            val result = audio.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            )
            focusHeld = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonFocus() {
        if (!focusHeld) return
        focusHeld = false
        runCatching { audio.abandonAudioFocus(focusListener) }
    }

    private fun finishSequence() {
        main.post {
            _status.value = _status.value.copy(playing = false)
            restoreVolume()
            abandonFocus()
        }
    }

    private fun failSequence(message: String) {
        main.post {
            _status.value = _status.value.copy(playing = false, message = message)
            restoreVolume()
            abandonFocus()
        }
    }

    /**
     * 只往上提升，不压低用户当前音量；音量已经在目标之上时保持不动。
     */
    private fun boostVolume(targetPercent: Int) {
        runCatching {
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val desired = (max * targetPercent.coerceIn(10, 100) / 100f).roundToInt()
            if (savedVolume == null) savedVolume = current
            if (current < desired) audio.setStreamVolume(AudioManager.STREAM_MUSIC, desired, 0)
        }
        refreshAudioState()
    }

    private fun restoreVolume() {
        val target = savedVolume ?: return
        savedVolume = null
        main.postDelayed({
            runCatching { audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0) }
            refreshAudioState()
        }, 350)
    }

    fun openTtsSettings() {
        val direct = Intent("com.android.settings.TTS_SETTINGS")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(direct) }.onFailure {
            runCatching {
                appContext.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    fun clearMessage() {
        _status.value = _status.value.copy(message = null)
    }

    fun shutdown() {
        main.removeCallbacks(ticker)
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        abandonFocus()
    }
}
