package com.example

import com.example.model.SurveyPoint
import com.example.util.SurveyMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testParseLocalizedDouble() {
        // اختبار الأرقام الغربية
        assertEquals(123.45, SurveyMath.parseLocalizedDouble("123.45")!!, 0.001)

        // اختبار الأرقام العربية المشرقية
        assertEquals(123.45, SurveyMath.parseLocalizedDouble("١٢٣.٤٥")!!, 0.001)

        // اختبار الفاصلة العربية والإنجليزية
        assertEquals(50.5, SurveyMath.parseLocalizedDouble("٥٠,٥")!!, 0.001)
        assertEquals(75.25, SurveyMath.parseLocalizedDouble("٧٥،٢٥")!!, 0.001)

        // اختبار المدخلات غير الصالحة
        assertNull(SurveyMath.parseLocalizedDouble(""))
        assertNull(SurveyMath.parseLocalizedDouble("abc"))
    }

    @Test
    fun testRectangleArea() {
        val area = SurveyMath.calculateRectangleArea(10.0, 20.0)
        assertEquals(200.0, area!!, 0.001)

        // أبعاد سالبة أو صفرية
        assertNull(SurveyMath.calculateRectangleArea(-5.0, 10.0))
        assertNull(SurveyMath.calculateRectangleArea(0.0, 10.0))
    }

    @Test
    fun testTriangleAreaHeron() {
        // مثلث قائم أضلاعه 3، 4، 5 (مساحته 6)
        val area = SurveyMath.calculateTriangleAreaHeron(3.0, 4.0, 5.0)
        assertNotNull(area)
        assertEquals(6.0, area!!, 0.001)

        // مثلث غير صالح (لا يحقق متباينة المثلث: 1 + 2 = 3)
        assertNull(SurveyMath.calculateTriangleAreaHeron(1.0, 2.0, 5.0))
    }

    @Test
    fun testPolygonAreaSquareMeters() {
        // مضلع من 4 نقاط يشكل مربعاً تقريبياً
        val p1 = SurveyPoint(15.369400, 44.191000)
        val p2 = SurveyPoint(15.369400, 44.191932) // ~100m شرقاً
        val p3 = SurveyPoint(15.370304, 44.191932) // ~100m شمالاً
        val p4 = SurveyPoint(15.370304, 44.191000) // ~100m غرباً

        val points = listOf(p1, p2, p3, p4)
        val area = SurveyMath.calculatePolygonAreaSquareMeters(points)

        assertNotNull(area)
        assertTrue("المساحة يجب أن تكون قريبة من 10,000 م²", area!! > 5000.0)

        // مضلع بأقل من 3 نقاط
        assertNull(SurveyMath.calculatePolygonAreaSquareMeters(listOf(p1, p2)))
    }
}
