package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SurveyPoint
import com.example.ui.theme.HamdanPrimary
import com.example.ui.theme.HamdanSecondary

@Composable
fun PolygonPreview(
    points: List<SurveyPoint>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (points.isEmpty()) {
            Text(
                text = "سجل أو أضف نقاطاً لرسم المخطط المساحي هنا",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Box
        }

        val primaryColor = HamdanPrimary
        val secondaryColor = HamdanSecondary
        val surfaceColor = MaterialTheme.colorScheme.surface

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLon = points.minOf { it.longitude }
            val maxLon = points.maxOf { it.longitude }

            val latSpan = (maxLat - minLat).coerceAtLeast(0.00001)
            val lonSpan = (maxLon - minLon).coerceAtLeast(0.00001)

            val canvasWidth = size.width
            val canvasHeight = size.height

            // تحويل خطوط الطول والعرض إلى إحداثيات شاشة (مع إبقاء الشمال لأعلى)
            val canvasPoints = points.map { pt ->
                val normX = ((pt.longitude - minLon) / lonSpan).toFloat()
                val normY = (1.0f - ((pt.latitude - minLat) / latSpan).toFloat()) // عكس Y لأن الشاشة تبدأ من أعلى
                Offset(
                    x = normX * (canvasWidth - 40f) + 20f,
                    y = normY * (canvasHeight - 40f) + 20f
                )
            }

            // رسم المضلع والخطوط إذا كان هناك 2 نقطة أو أكثر
            if (canvasPoints.size >= 2) {
                val path = Path().apply {
                    moveTo(canvasPoints[0].x, canvasPoints[0].y)
                    for (i in 1 until canvasPoints.size) {
                        lineTo(canvasPoints[i].x, canvasPoints[i].y)
                    }
                    if (canvasPoints.size >= 3) {
                        close()
                    }
                }

                // تعبئة المضلع إذا كان 3 نقاط أو أكثر
                if (canvasPoints.size >= 3) {
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.15f)
                    )
                }

                // رسم حدود المضلع
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // رسم النقاط وترقيمها
            canvasPoints.forEachIndexed { index, offset ->
                // دائرة خارجية بيضاء
                drawCircle(
                    color = surfaceColor,
                    radius = 9.dp.toPx(),
                    center = offset
                )
                // دائرة داخلية باللون الثانوي
                drawCircle(
                    color = secondaryColor,
                    radius = 7.dp.toPx(),
                    center = offset
                )
                // نقطة المركز
                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = offset
                )

                // رقم النقطة
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    (index + 1).toString(),
                    offset.x,
                    offset.y - 14.dp.toPx(),
                    paint
                )
            }

            // رسم مؤشر الشمال في أعلى اليمين
            val northCenterX = size.width - 20f
            val northCenterY = 25f
            drawLine(
                color = secondaryColor,
                start = Offset(northCenterX, northCenterY + 18f),
                end = Offset(northCenterX, northCenterY - 18f),
                strokeWidth = 3f
            )
            // سهم الشمال
            val arrowPath = Path().apply {
                moveTo(northCenterX, northCenterY - 22f)
                lineTo(northCenterX - 7f, northCenterY - 8f)
                lineTo(northCenterX + 7f, northCenterY - 8f)
                close()
            }
            drawPath(arrowPath, color = secondaryColor)
        }

        // بطاقة ملصق الحالة
        Text(
            text = "مخطط مساحي تقريبي",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
