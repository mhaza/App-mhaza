package com.example.util

import com.example.model.SurveyPoint
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object SurveyMath {

    private const val EARTH_RADIUS_METERS = 6378137.0

    /**
     * تحويل الأرقام العربية المشرقية والفواصل إلى تنسيق رقمي قياسي
     */
    fun parseLocalizedDouble(input: String): Double? {
        if (input.isBlank()) return null
        val normalized = input.trim()
            .map { ch ->
                when (ch) {
                    in '٠'..'٩' -> ('0'.code + (ch - '٠')).toChar()
                    in '۰'..'۹' -> ('0'.code + (ch - '۰')).toChar()
                    '،', ',' -> '.'
                    else -> ch
                }
            }
            .joinToString("")
            .replace("\\s+".toRegex(), "")

        return normalized.toDoubleOrNull()
    }

    /**
     * تنسيق الأرقام العشرية مع الفواصل
     */
    fun formatNumber(value: Double, decimals: Int = 2): String {
        return try {
            String.format(Locale.US, "%,.${decimals}f", value)
        } catch (_: Exception) {
            value.toString()
        }
    }

    /**
     * حساب مساحة المستطيل
     */
    fun calculateRectangleArea(length: Double, width: Double): Double? {
        return if (length > 0 && width > 0) length * width else null
    }

    /**
     * حساب مساحة المثلث بصيغة هيرون
     */
    fun calculateTriangleAreaHeron(a: Double, b: Double, c: Double): Double? {
        if (a <= 0 || b <= 0 || c <= 0) return null
        // التحقق من متباينة المثلث
        if (a + b <= c || a + c <= b || b + c <= a) return null

        val s = (a + b + c) / 2.0
        val insideSqrt = s * (s - a) * (s - b) * (s - c)
        if (insideSqrt < 0) return null
        return sqrt(insideSqrt)
    }

    /**
     * حساب مساحة مضلع مساحي من 3 نقاط أو أكثر بالإسقاط المحلي (Shoelace on Local Projection)
     */
    fun calculatePolygonAreaSquareMeters(points: List<SurveyPoint>): Double? {
        if (points.size < 3) return null

        val avgLat = points.map { it.latitude }.average()
        val lat0Rad = avgLat * (PI / 180.0)
        val cosLat0 = cos(lat0Rad)

        // إسقاط خطوط العرض والطول إلى إحداثيات مترية x, y محلية
        val xy = points.map { p ->
            val lonRad = p.longitude * (PI / 180.0)
            val latRad = p.latitude * (PI / 180.0)
            val x = EARTH_RADIUS_METERS * lonRad * cosLat0
            val y = EARTH_RADIUS_METERS * latRad
            Pair(x, y)
        }

        var sum = 0.0
        val n = xy.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            sum += xy[i].first * xy[j].second - xy[j].first * xy[i].second
        }

        return abs(sum) / 2.0
    }

    /**
     * حساب محيط المضلع بالأمتار (مجموع المسافات بين النقاط المتتالية مع إغلاق المضلع)
     */
    fun calculatePolygonPerimeterMeters(points: List<SurveyPoint>): Double? {
        if (points.size < 2) return null
        var total = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            // لا نغلق إذا كانت النقطتان فقط
            if (n == 2 && j == 0) continue
            total += haversineDistanceMeters(points[i], points[j])
        }
        return total
    }

    /**
     * حساب المسافة بالهافرساين بين نقطتين بالأمتار
     */
    fun haversineDistanceMeters(p1: SurveyPoint, p2: SurveyPoint): Double {
        val lat1Rad = Math.toRadians(p1.latitude)
        val lat2Rad = Math.toRadians(p2.latitude)
        val deltaLat = Math.toRadians(p2.latitude - p1.latitude)
        val deltaLon = Math.toRadians(p2.longitude - p1.longitude)

        val sinDLat2 = sin(deltaLat / 2.0)
        val sinDLon2 = sin(deltaLon / 2.0)

        val a = sinDLat2 * sinDLat2 + cos(lat1Rad) * cos(lat2Rad) * sinDLon2 * sinDLon2
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }
}
