package dev.opencode.mobile.custom.context

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.theme.openCodeColor

/**
 * Кольцо заполнения контекста вокруг кнопки шапки.
 *
 * Цвет нейтральный до предупреждения: постоянный цветной акцент в шапке раздражает, а хороший
 * дизайн не замечаешь. Трек — тот же цвет с прозрачностью, чтобы кольцо читалось и на пустом месте.
 */
@Composable
fun ContextRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    lineWidth: Dp = 2.dp,
    animated: Boolean = true,
) {
    val target = fraction.coerceIn(0f, 1f)
    val shown by
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = if (animated) 300 else 0),
            label = "contextRing",
        )
    val color = contextRingColor(CompactionCeiling.severity(target))
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Round)
        val diameter = size.minDimension - lineWidth.toPx()
        if (diameter <= 0f) return@Canvas
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = color.copy(alpha = 0.18f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        if (shown > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * shown,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
    }
}

@Composable
fun contextRingColor(severity: CompactionCeiling.Severity): Color =
    when (severity) {
        CompactionCeiling.Severity.Neutral -> openCodeColor("icon-strong-base")
        CompactionCeiling.Severity.Warn -> openCodeColor("warning")
        CompactionCeiling.Severity.Error -> openCodeColor("error")
    }
