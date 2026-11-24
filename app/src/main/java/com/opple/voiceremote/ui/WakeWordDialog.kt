package com.opple.voiceremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opple.voiceremote.voice.VoiceCatalog

/**
 * 灯键唤醒词编辑器。
 *
 * 用组合内的浮层而不是 Dialog：不额外开窗口，行为与主界面一致，
 * 也便于无设备时做渲染与交互验证。
 */
@Composable
fun WakeWordEditor(
    slotIndex: Int,
    initial: String,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val p = LocalPalette.current
    var value by remember(slotIndex) { mutableStateOf(initial) }
    val preview = VoiceCatalog.labelFor(value.trim())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(p.bodyHigh)
                .border(1.dp, p.hairline, RoundedCornerShape(24.dp))
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Text(
                "灯键 ${slotIndex + 1}",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, letterSpacing = 0.8.sp),
                color = p.text,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "灯具只认唤醒词，唤醒词就是这盏灯的身份；按钮名称由它自动推导。",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 0.3.sp,
                    lineHeight = 15.sp,
                ),
                color = p.textFaint,
            )

            Spacer(Modifier.height(14.dp))

            Column(
                Modifier
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                VoiceCatalog.wakePresets.chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        row.forEach { wake ->
                            SceneChip(
                                label = if (wake == VoiceCatalog.WAKE_DEFAULT) "默认" else VoiceCatalog.labelFor(wake),
                                active = value.trim() == wake,
                                modifier = Modifier.weight(1f),
                                onClick = { value = wake },
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    label = { Text("唤醒词", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.accent,
                        unfocusedBorderColor = p.hairline,
                        focusedTextColor = p.text,
                        unfocusedTextColor = p.text,
                        cursorColor = p.accent,
                        focusedLabelColor = p.accent,
                        unfocusedLabelColor = p.textFaint,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "按钮名称 · $preview",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.6.sp),
                    color = p.accent,
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete) {
                    Text(
                        "删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.danger,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onDelete() }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "取消",
                    style = MaterialTheme.typography.bodyMedium,
                    color = p.textDim,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "保存",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isNotBlank()) p.accent else p.textFaint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = value.isNotBlank()) { onConfirm(value.trim()) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
