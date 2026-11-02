package com.example.model

import java.util.UUID

/**
 * يمثل نقطة مساحية جغرافية مع الإحداثيات
 */
data class SurveyPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * يمثل مشروع مساحي كامل يتضمن اسم المشروع والنقاط وتاريخ الحفظ
 */
data class SurveyProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val points: List<SurveyPoint>,
    val savedAt: Long = System.currentTimeMillis()
)

/**
 * وحدات قياس الأراضي الشائعة
 */
data class LandAreaUnits(
    val squareMeters: Double
) {
    val hectares: Double get() = squareMeters / 10_000.0
    val dunams: Double get() = squareMeters / 1_000.0
    // اللبنة العشارية اليمنية الشائعة (~44.44 م²)
    val labnah: Double get() = squareMeters / 44.4444
    // القصبة (~12.5 م²)
    val qasabah: Double get() = squareMeters / 12.5
}
