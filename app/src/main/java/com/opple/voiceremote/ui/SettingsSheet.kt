package com.opple.voiceremote.ui

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opple.voiceremote.LauncherIcon
import com.opple.voiceremote.R
import com.opple.voiceremote.data.Prefs
import com.opple.voiceremote.voice.VoiceCatalog
import com.opple.voiceremote.voice.VoiceEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    engine: VoiceEngine,
    prefs: Prefs,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    onSendCommand: (String) -> Unit,
    onEditSlot: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    val status by engine.status
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var rate by remember { mutableFloatStateOf(prefs.rate) }
    var gap by remember { mutableIntStateOf(prefs.gapMs) }
    var repeatTimes by remember { mutableIntStateOf(prefs.repeatTimes) }
    var autoBoost by remember { mutableStateOf(prefs.autoBoostVolume) }
    var boostPercent by remember { mutableIntStateOf(prefs.boostVolumePercent) }
    var continuous by remember { mutableStateOf(prefs.continuousWindow) }
    var iconStyle by remember { mutableStateOf(prefs.iconStyle) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.body,
        contentColor = p.text,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(34.dp).height(3.dp).clip(RoundedCornerShape(50)).background(p.hairlineStrong))
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 30.dp),
        ) {
            Text("设置", style = MaterialTheme.typography.headlineSmall, color = p.text)
            Spacer(Modifier.height(4.dp))
            Text(
                "只调整外放方式，不改变灯具本身的语音逻辑。",
                style = MaterialTheme.typography.bodySmall,
                color = p.textDim,
            )

            // 外放参数
            SheetSection("外放与识别") {
                SliderRow(
                    title = "语速",
                    valueText = String.format("%.2f×", rate),
                    value = rate,
                    range = 0.7f..1.2f,
                    steps = 4,
                    onValueChange = {
                        rate = it
                        prefs.rate = it
                    },
                )
                SliderRow(
                    title = "唤醒与命令间隔",
                    valueText = "$gap ms",
                    value = gap.toFloat(),
                    range = 300f..1500f,
                    steps = 11,
                    onValueChange = {
                        gap = it.toInt()
                        prefs.gapMs = gap
                    },
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    "命令重复",
                    style = MaterialTheme.typography.bodyMedium,
                    color = p.textDim,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2).forEach { count ->
                        SceneChip(
                            label = if (count == 1) "1 次" else "2 次",
                            active = repeatTimes == count,
                            modifier = Modifier.width(96.dp),
                            onClick = {
                                repeatTimes = count
                                prefs.repeatTimes = count
                            },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    title = "自动提升媒体音量",
                    desc = if (autoBoost) "播放前提升到 ${boostPercent}%，结束后还原" else "关闭后不改变系统音量",
                    checked = autoBoost,
                    onCheckedChange = {
                        autoBoost = it
                        prefs.autoBoostVolume = it
                    },
                )
                if (autoBoost) {
                    SliderRow(
                        title = "目标音量",
                        valueText = "$boostPercent%",
                        value = boostPercent.toFloat(),
                        range = 30f..100f,
                        steps = 6,
                        onValueChange = {
                            boostPercent = it.toInt()
                            prefs.boostVolumePercent = boostPercent
                        },
                    )
                }
                SwitchRow(
                    title = "15 秒连续控制",
                    desc = "唤醒后的窗口内只播命令，不重复唤醒词",
                    checked = continuous,
                    onCheckedChange = {
                        continuous = it
                        prefs.continuousWindow = it
                    },
                )
            }

            // 引擎
            SheetSection("朗读引擎") {
                InfoRow("引擎", status.engineName)
                InfoRow("中文语音", if (status.chineseAvailable) "可用" else "缺失")
                InfoRow("音色", status.voiceLabel.ifBlank { "系统默认" })
                InfoRow("当前音量", "${status.volumePercent}%")
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, p.hairline, RoundedCornerShape(10.dp))
                        .clickable { engine.openTtsSettings() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "打开系统语音设置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = p.text,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "若提示缺少中文语音，请在语音设置里安装或下载中文语音包；部分机型需先启用系统自带语音引擎。",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 14.sp,
                    ),
                    color = p.textFaint,
                )
            }

            // 唤醒词
            SheetSection("灯键与唤醒词") {
                val count = prefs.lampCount
                val wakes = List(count) { prefs.slotWake(it) }
                val labels = VoiceCatalog.labelsFor(wakes)
                labels.forEachIndexed { index, label ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onEditSlot(index) }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = p.text,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            wakes[index],
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.3.sp,
                            ),
                            color = p.textFaint,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "修改",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.6.sp,
                            ),
                            color = p.accent,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(p.hairline.copy(alpha = 0.6f))
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "点按灯键切换当前灯具，长按（或点上方任一行）改该键唤醒词；名称由唤醒词自动推导。新灯键在主界面用「＋」添加。",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 14.sp,
                    ),
                    color = p.textFaint,
                )
            }

            // 房间配对
            SheetSection("房间配对 · 上电 30 秒内") {
                VoiceCatalog.rooms.chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { room ->
                            CommandPill(
                                label = room.name,
                                modifier = Modifier.weight(1f),
                            ) {
                                onSendCommand(room.pairCommand)
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Text(
                    "点按会先唤醒再下发「设为 X 灯」；配对成功后，长按对应灯键把它的唤醒词改为「小欧 X 灯」即可。",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 14.sp,
                    ),
                    color = p.textFaint,
                )
            }

            // 外观
            SheetSection("外观") {
                Text(
                    "主题",
                    style = MaterialTheme.typography.bodyMedium,
                    color = p.textDim,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "跟随系统", 1 to "深色", 2 to "浅色").forEach { (mode, label) ->
                        SceneChip(
                            label = label,
                            active = themeMode == mode,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeModeChange(mode) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "默认跟随手机的深色开关；选定深色或浅色后，应用不再随系统变化。",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 14.sp,
                    ),
                    color = p.textFaint,
                )

                Spacer(Modifier.height(18.dp))
                Text(
                    "应用图标",
                    style = MaterialTheme.typography.bodyMedium,
                    color = p.textDim,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconChoice(
                        iconRes = R.drawable.ic_android_preview,
                        label = "Android",
                        selected = iconStyle == 0,
                    ) {
                        if (iconStyle != 0) {
                            iconStyle = 0
                            prefs.iconStyle = 0
                            LauncherIcon.apply(context, 0)
                            Toast.makeText(context, "已切换为 Android 图标", Toast.LENGTH_SHORT).show()
                        }
                    }
                    IconChoice(
                        iconRes = R.drawable.ic_warm_preview,
                        label = "Apple",
                        selected = iconStyle == 1,
                    ) {
                        if (iconStyle != 1) {
                            iconStyle = 1
                            prefs.iconStyle = 1
                            LauncherIcon.apply(context, 1)
                            Toast.makeText(context, "已切换为 Apple 图标", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "切换后桌面图标立即更新；个别桌面需稍等片刻，或重启桌面后才刷新。",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 14.sp,
                    ),
                    color = p.textFaint,
                )
            }

            // 关于
            SheetSection("关于") {
                InfoRow("版本", "1.1.0 (11)")
                Spacer(Modifier.height(8.dp))
                Text(
                    "口令词表：开灯 / 关灯 · 亮一点 / 暗一点 / 最亮 / 最暗 · 白一点 / 黄一点 / 白光 / 黄光 · 会客 / 娱乐 / 影院 / 夜灯模式 · 打开 / 关闭氛围光 · 延时关灯 · 安静模式",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.3.sp,
                        lineHeight = 15.sp,
                    ),
                    color = p.textFaint.copy(alpha = 0.85f),
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "小欧遥控 · 卧室吸顶灯",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                color = p.textFaint.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SheetSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    Spacer(Modifier.height(26.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(title)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(1.dp).background(p.hairline.copy(alpha = 0.7f)))
    }
    Spacer(Modifier.height(14.dp))
    Column { content() }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = p.textDim)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = p.text,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = p.text)
            Spacer(Modifier.height(3.dp))
            Text(
                desc,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 0.2.sp,
                    lineHeight = 14.sp,
                ),
                color = p.textFaint,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = p.canvas,
                checkedTrackColor = p.accent,
                checkedBorderColor = p.accent,
                uncheckedThumbColor = p.textFaint,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = p.hairlineStrong,
            ),
        )
    }
}

@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    val p = LocalPalette.current
    Column(Modifier.padding(top = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = p.textDim)
            Spacer(Modifier.weight(1f))
            Text(
                valueText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                color = p.text,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = p.accent,
                activeTrackColor = p.accent,
                inactiveTrackColor = p.hairline,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun IconChoice(
    @DrawableRes iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(6.dp),
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (selected) 1.4.dp else 1.dp,
                    color = if (selected) p.accent else p.hairline,
                    shape = RoundedCornerShape(16.dp),
                )
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = if (selected) p.accent else p.textDim,
        )
    }
}
