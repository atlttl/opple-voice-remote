package com.opple.voiceremote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class DialAction(val spoken: String, val caption: String) {
    BRIGHTER("亮一点", "亮度+"),
    COOLER("白一点", "色温+"),
    DIMMER("暗一点", "亮度-"),
    WARMER("黄一点", "色温-"),
}

@Composable
fun LampGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 1.4.dp,
) {
    Canvas(modifier) {
        val s = size.minDimension
        val u = s / 24f
        val ox = (size.width - s) / 2f
        val oy = (size.height - s) / 2f
        fun x(v: Float) = ox + v * u
        fun y(v: Float) = oy + v * u
        val sw = strokeWidth.toPx()

        drawLine(color, Offset(x(12f), y(2.6f)), Offset(x(12f), y(6.2f)), sw, StrokeCap.Round)

        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(x(5.6f), y(6.2f)),
            size = Size(12.8f * u, 12.8f * u),
            style = Stroke(width = sw, cap = StrokeCap.Round),
        )
        drawLine(color, Offset(x(4.6f), y(12.6f)), Offset(x(19.4f), y(12.6f)), sw, StrokeCap.Round)

        drawLine(color, Offset(x(8.4f), y(14.8f)), Offset(x(8.4f), y(17.4f)), sw, StrokeCap.Round)
        drawLine(color, Offset(x(12f), y(15.2f)), Offset(x(12f), y(18.2f)), sw, StrokeCap.Round)
        drawLine(color, Offset(x(15.6f), y(14.8f)), Offset(x(15.6f), y(17.4f)), sw, StrokeCap.Round)
    }
}

@Composable
fun TuneGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = h * 0.06f
        val rows = listOf(0.32f to 0.68f, 0.68f to 0.36f)
        rows.forEach { (fy, fx) ->
            drawLine(
                color,
                Offset(w * 0.14f, h * fy),
                Offset(w * 0.86f, h * fy),
                sw,
                StrokeCap.Round,
            )
            drawCircle(color, radius = h * 0.105f, center = Offset(w * fx, h * fy))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LampNode(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val p = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, tween(120), label = "lampNode")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(78.dp),
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(54.dp)
                .clip(CircleShape)
                .background(if (selected) p.accentSoft else Color.Transparent, CircleShape)
                .border(if (selected) 1.2.dp else 1.dp, if (selected) p.accent else p.hairline, CircleShape)
                .testTag("lampNode-$label")
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onLongClick = onLongClick,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            LampGlyph(
                color = if (selected) p.accent else p.textDim,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                letterSpacing = 1.4.sp,
            ),
            color = if (selected) p.text else p.textDim,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(if (selected) p.accent else Color.Transparent)
        )
    }
}

@Composable
fun AddLampNode(onClick: () -> Unit) {
    val p = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, tween(120), label = "addLamp")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(78.dp),
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(54.dp)
                .clip(CircleShape)
                .border(1.dp, p.hairline, CircleShape)
                .testTag("addLamp")
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(15.dp)) {
                val sw = 1.4.dp.toPx()
                drawLine(p.textDim, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), sw, StrokeCap.Round)
                drawLine(p.textDim, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), sw, StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = "添加",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 1.4.sp),
            color = p.textFaint,
        )
        Spacer(Modifier.height(4.dp))
        Box(Modifier.size(3.dp))
    }
}

@Composable
fun SceneChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(120), label = "sceneChip")
    Box(
        modifier = modifier
            .scale(scale)
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .background(if (active) p.accentSoft else Color.Transparent, RoundedCornerShape(50))
            .border(1.dp, if (active) p.accent else p.hairline, RoundedCornerShape(50))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp, letterSpacing = 1.sp),
            color = if (active) p.accent else p.textDim,
        )
    }
}

@Composable
fun CommandPill(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(110), label = "commandPill")
    Box(
        modifier = modifier
            .scale(scale)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (pressed) p.bodyHigh else Color.Transparent, RoundedCornerShape(10.dp))
            .border(1.dp, p.hairline, RoundedCornerShape(10.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, letterSpacing = 0.4.sp),
            color = p.text,
        )
    }
}

@Composable
fun RemoteDial(
    title: String,
    subtitle: String,
    windowProgress: Float,
    playing: Boolean,
    onAction: (DialAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    var pressed by remember { mutableStateOf<DialAction?>(null) }
    val scope = rememberCoroutineScope()
    // pointerInput(Unit) 不会随重组重启，必须取最新回调，否则唤醒词会停留在首次组合时的目标灯
    val latestOnAction by rememberUpdatedState(onAction)

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        if (dx == 0f && dy == 0f) return@detectTapGestures
                        val action = if (abs(dx) > abs(dy)) {
                            if (dx > 0) DialAction.COOLER else DialAction.WARMER
                        } else {
                            if (dy < 0) DialAction.BRIGHTER else DialAction.DIMMER
                        }
                        pressed = action
                        latestOnAction(action)
                        scope.launch {
                            delay(240)
                            if (pressed == action) pressed = null
                        }
                    }
                }
        ) {
            val stroke = 1.dp.toPx()
            val maxR = size.minDimension / 2f - 7.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = p.hairline, radius = maxR, center = center, style = Stroke(stroke))

            val action = pressed
            if (action != null) {
                val start = when (action) {
                    DialAction.BRIGHTER -> 225f
                    DialAction.COOLER -> 315f
                    DialAction.DIMMER -> 45f
                    DialAction.WARMER -> 135f
                }
                val rr = maxR - 22.dp.toPx()
                drawArc(
                    color = p.accent.copy(alpha = 0.13f),
                    startAngle = start,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - rr, center.y - rr),
                    size = Size(rr * 2, rr * 2),
                    style = Stroke(width = 26.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            if (playing) {
                drawCircle(
                    color = p.accent.copy(alpha = 0.05f),
                    radius = maxR * 0.46f,
                    center = center,
                )
            }

            if (windowProgress > 0f) {
                val ringR = maxR + 3.dp.toPx()
                drawArc(
                    color = p.accent.copy(alpha = 0.55f),
                    startAngle = -90f,
                    sweepAngle = 360f * windowProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(center.x - ringR, center.y - ringR),
                    size = Size(ringR * 2, ringR * 2),
                    style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }

        Text(
            text = DialAction.BRIGHTER.caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.9.sp),
            color = p.textDim,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp),
        )
        Text(
            text = DialAction.DIMMER.caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.9.sp),
            color = p.textDim,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
        )
        Text(
            text = DialAction.WARMER.caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.5.sp),
            color = p.textDim,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp),
        )
        Text(
            text = DialAction.COOLER.caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.5.sp),
            color = p.textDim,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(126.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, letterSpacing = 1.sp),
                color = p.text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 0.8.sp,
                    lineHeight = 13.sp,
                ),
                color = if (playing) p.accent else p.textFaint,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PowerBar(
    onAllOn: () -> Unit,
    onAllOff: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(50))
            .border(1.dp, p.hairline, RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PowerHalf("开灯", onAllOn, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(16.dp).background(p.hairline))
        PowerHalf("关灯", onAllOff, Modifier.weight(1f))
    }
}

@Composable
private fun PowerHalf(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (pressed) p.accentSoft else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 1.2.sp),
            color = p.text,
        )
    }
}

@Composable
fun RemoteBodySurface(content: @Composable () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp))
            // 渐变必须是实色：半透明会让底层的投影透出来，在圆角处形成暗斑
            .background(
                Brush.verticalGradient(
                    listOf(lerp(p.body, p.bodyHigh, 0.5f), p.body, p.body)
                )
            )
            .border(1.dp, p.hairline, RoundedCornerShape(34.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        content()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, letterSpacing = 1.6.sp),
        color = p.textFaint,
        modifier = modifier,
    )
}
