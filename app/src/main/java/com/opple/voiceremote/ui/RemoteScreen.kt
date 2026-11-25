package com.opple.voiceremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opple.voiceremote.data.Prefs
import com.opple.voiceremote.voice.VoiceCatalog
import com.opple.voiceremote.voice.VoiceEngine
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SendLogEntry(
    val time: String,
    val command: String,
    val target: String,
)

@Composable
fun RemoteScreen(
    engine: VoiceEngine,
    prefs: Prefs,
    dark: Boolean,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
) {
    val p = LocalPalette.current
    val status by engine.status
    val haptics = LocalHapticFeedback.current
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.CHINA) }
    val log = remember { mutableStateListOf<SendLogEntry>() }
    var showSettings by remember { mutableStateOf(false) }
    var flashScene by remember { mutableStateOf<String?>(null) }
    var lampCount by remember { mutableIntStateOf(prefs.lampCount) }
    var selectedSlot by remember { mutableStateOf(prefs.selectedSlot) }
    var editingSlot by remember { mutableStateOf<Int?>(null) }

    val slotWakes = List(lampCount) { prefs.slotWake(it) }
    val slotLabels = VoiceCatalog.labelsFor(slotWakes)
    val currentWake = slotWakes[selectedSlot]
    val currentLabel = slotLabels[selectedSlot]

    fun selectSlot(index: Int) {
        selectedSlot = index
        prefs.selectedSlot = index
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun addLamp() {
        if (lampCount >= 6) return
        prefs.lampCount = lampCount + 1
        lampCount = prefs.lampCount
        selectSlot(lampCount - 1)
    }

    fun send(command: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val queued = engine.speak(
            wakeWord = currentWake,
            command = command,
            rate = prefs.rate,
            gapMs = prefs.gapMs,
            repeatTimes = prefs.repeatTimes,
            boostPercent = if (prefs.autoBoostVolume) prefs.boostVolumePercent else null,
            continuousWindow = prefs.continuousWindow,
        )
        if (queued) {
            log.add(0, SendLogEntry(formatter.format(Date()), command, currentLabel))
            while (log.size > 24) log.removeAt(log.size - 1)
        }
    }

    LaunchedEffect(flashScene) {
        if (flashScene != null) {
            delay(900)
            flashScene = null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to lerp(p.canvas, p.canvasGlow, if (dark) 0.55f else 0.6f),
                    0.3f to p.canvas,
                    1f to p.canvas,
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 28.dp),
        ) {
            // ---------- 顶栏 ----------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, p.hairline, CircleShape)
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center,
                ) {
                    TuneGlyph(p.textDim, Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "小欧遥控",
                style = MaterialTheme.typography.displaySmall,
                color = p.text,
            )

            Spacer(Modifier.height(16.dp))

            // ---------- 引擎状态条 ----------
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.hairline))
            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !status.ready -> p.danger
                                status.playing -> p.accent
                                status.chineseAvailable -> p.accent.copy(alpha = 0.75f)
                                else -> p.danger
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    buildString {
                        append(status.engineName)
                        append(" · ")
                        append(
                            when {
                                !status.ready -> "未就绪"
                                status.chineseAvailable -> "中文就绪"
                                else -> "缺中文语音"
                            }
                        )
                    },
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = p.textDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        status.playing -> "正在外放"
                        status.windowLeft > 0 -> "连续接收 ${status.windowLeft}s"
                        else -> "待命"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = if (status.playing || status.windowLeft > 0) p.accent else p.textFaint,
                )
                if (status.windowLeft > 0 && !status.playing) {
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .height(22.dp)
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, p.hairlineStrong, RoundedCornerShape(50))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                engine.clearWindow()
                            }
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "清空",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp,
                            ),
                            color = p.textDim,
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.hairline))

            status.message?.let { message ->
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(p.danger.copy(alpha = 0.09f))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = p.danger,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "语音设置",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                        color = p.danger,
                        modifier = Modifier.clickable { engine.openTtsSettings() },
                    )
                }
            }

            status.externalOutput?.let { device ->
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, p.hairline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "检测到 $device，声音可能不外放，请先断开",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = p.textDim,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ---------- 遥控器本体 ----------
            RemoteBodySurface {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RemoteDial(
                        title = currentLabel,
                        subtitle = "唤醒词 · $currentWake",
                        windowProgress = status.windowLeft / 15f,
                        playing = status.playing,
                        onAction = { send(it.spoken) },
                        modifier = Modifier.fillMaxWidth(0.78f),
                    )

                    Spacer(Modifier.height(20.dp))

                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            repeat(lampCount) { index ->
                                LampNode(
                                    label = slotLabels[index],
                                    selected = selectedSlot == index,
                                    onClick = { selectSlot(index) },
                                    onLongClick = { editingSlot = index },
                                )
                            }
                            if (lampCount < 6) {
                                AddLampNode { addLamp() }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        VoiceCatalog.scenes.forEach { scene ->
                            SceneChip(
                                label = scene.label,
                                active = flashScene == scene.spoken,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    send(scene.spoken)
                                    flashScene = scene.spoken
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    PowerBar(
                        onAllOn = { send(VoiceCatalog.POWER_ON) },
                        onAllOff = { send(VoiceCatalog.POWER_OFF) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "手机扬声器正对灯具，1–3 米最稳。点按灯键切换灯具，长按改该键的唤醒词；唤醒后 15 秒内可直接点按。",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 0.3.sp,
                    lineHeight = 14.sp,
                ),
                color = p.textFaint,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(26.dp))

            // ---------- 更多口令 ----------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                SectionLabel("更多口令")
                Text(
                    "延伸键 · 同属遥控器语义",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.6.sp),
                    color = p.textFaint,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CommandPill("最亮", Modifier.weight(1f)) { send(VoiceCatalog.BRIGHTEST) }
                CommandPill("最暗", Modifier.weight(1f)) { send(VoiceCatalog.DARKEST) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CommandPill("白光", Modifier.weight(1f)) { send(VoiceCatalog.WHITE) }
                CommandPill("黄光", Modifier.weight(1f)) { send(VoiceCatalog.YELLOW) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CommandPill("打开氛围光", Modifier.weight(1f)) { send(VoiceCatalog.AMBIENT_ON) }
                CommandPill("关闭氛围光", Modifier.weight(1f)) { send(VoiceCatalog.AMBIENT_OFF) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CommandPill("延时关灯", Modifier.weight(1f)) { send(VoiceCatalog.DELAY_OFF) }
                CommandPill("安静模式", Modifier.weight(1f)) { send(VoiceCatalog.QUIET) }
            }

            Spacer(Modifier.height(28.dp))

            // ---------- 发送记录 ----------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                SectionLabel("发送记录")
                if (log.isNotEmpty()) {
                    Text(
                        "清空",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.6.sp),
                        color = p.textFaint,
                        modifier = Modifier.clickable { log.clear() },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            if (log.isEmpty()) {
                Text(
                    "还没有发送过口令。点按上方任意键即可记录。",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = p.textFaint,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                log.take(5).forEach { entry ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { send(entry.command) }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            entry.time,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.4.sp,
                            ),
                            color = p.textFaint,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            entry.command,
                            style = MaterialTheme.typography.bodyMedium,
                            color = p.text,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            entry.target,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.6.sp,
                            ),
                            color = p.textFaint,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "重播",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.6.sp,
                            ),
                            color = p.accent.copy(alpha = 0.8f),
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(p.hairline.copy(alpha = 0.6f)))
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                "口令依据 OPPLE 第二代离线语音模块说明 · 1.0.9",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                color = p.textFaint.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
        }

        editingSlot?.let { index ->
            WakeWordEditor(
                slotIndex = index,
                initial = slotWakes[index],
                canDelete = lampCount > 1,
                onDismiss = { editingSlot = null },
                onConfirm = { wake ->
                    prefs.setSlotWake(index, wake)
                    editingSlot = null
                },
                onDelete = {
                    prefs.removeSlot(index)
                    lampCount = prefs.lampCount
                    selectedSlot = selectedSlot.coerceAtMost(lampCount - 1)
                    editingSlot = null
                },
            )
        }
    }

    if (showSettings) {
        SettingsSheet(
            engine = engine,
            prefs = prefs,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onSendCommand = { command -> send(command) },
            onEditSlot = { index ->
                showSettings = false
                editingSlot = index
            },
            onDismiss = {
                showSettings = false
            },
        )
    }

}
