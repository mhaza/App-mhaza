package com.example.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.SurveyPoint
import com.example.model.SurveyProject
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.Locale

class ProjectRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("hamdan_survey_prefs", Context.MODE_PRIVATE)
    private val PREF_KEY_PROJECTS = "saved_projects"

    fun loadProjects(): List<SurveyProject> {
        return try {
            val raw = prefs.getString(PREF_KEY_PROJECTS, "[]") ?: "[]"
            val array = JSONArray(raw)
            val list = mutableListOf<SurveyProject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", i.toString())
                val name = obj.getString("name")
                val savedAt = obj.optLong("savedAt", System.currentTimeMillis())
                val pointsArray = obj.getJSONArray("points")
                val points = mutableListOf<SurveyPoint>()
                for (j in 0 until pointsArray.length()) {
                    val p = pointsArray.getJSONObject(j)
                    points.add(
                        SurveyPoint(
                            latitude = p.getDouble("lat"),
                            longitude = p.getDouble("lon"),
                            altitude = p.optDouble("alt", 0.0),
                            timestamp = p.optLong("time", savedAt)
                        )
                    )
                }
                list.add(SurveyProject(id = id, name = name, points = points, savedAt = savedAt))
            }
            list.sortedByDescending { it.savedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveProject(project: SurveyProject): List<SurveyProject> {
        val current = loadProjects().filterNot { it.name.trim().equals(project.name.trim(), ignoreCase = true) }
        val updated = listOf(project) + current
        persistProjects(updated)
        return updated
    }

    fun deleteProject(projectId: String): List<SurveyProject> {
        val updated = loadProjects().filterNot { it.id == projectId }
        persistProjects(updated)
        return updated
    }

    private fun persistProjects(projects: List<SurveyProject>) {
        val array = JSONArray()
        projects.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("savedAt", p.savedAt)
            val pointsArray = JSONArray()
            p.points.forEach { pt ->
                val ptObj = JSONObject()
                ptObj.put("lat", pt.latitude)
                ptObj.put("lon", pt.longitude)
                ptObj.put("alt", pt.altitude)
                ptObj.put("time", pt.timestamp)
                pointsArray.put(ptObj)
            }
            obj.put("points", pointsArray)
            array.put(obj)
        }
        prefs.edit().putString(PREF_KEY_PROJECTS, array.toString()).apply()
    }

    /**
     * تصدير نقاط المشروع إلى ملف CSV وحفظه في مجلد التصدير
     */
    fun exportCsv(project: SurveyProject): File? {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val sanitizedName = project.name.replace(Regex("[^\\p{L}\\p{N}_-]"), "_").ifBlank { "hamdan_project" }
            val file = File(exportDir, "${sanitizedName}_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                // رأس الملف مع معلومات المشروع
                writer.append("# تطبيق مساحة همدان - تقرير إحداثيات المشروع\n")
                writer.append("# اسم المشروع: ${project.name}\n")
                writer.append("# عدد النقاط: ${project.points.size}\n")
                writer.append("# تاريخ التصدير: ${System.currentTimeMillis()}\n")
                writer.append("Index,Latitude,Longitude,Altitude\n")

                project.points.forEachIndexed { index, point ->
                    val latStr = String.format(Locale.US, "%.7f", point.latitude)
                    val lonStr = String.format(Locale.US, "%.7f", point.longitude)
                    val altStr = String.format(Locale.US, "%.2f", point.altitude)
                    writer.append("${index + 1},$latStr,$lonStr,$altStr\n")
                }
            }
            file
        } catch (_: Exception) {
            null
        }
    }

    /**
     * مشاركة ملف CSV عبر قائمة المشاركة في أندرويد
     */
    fun shareCsvFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تصدير نقاط مشروع مساحي - همدان")
                putExtra(Intent.EXTRA_TEXT, "مرفق تقرير إحداثيات ونقاط المشروع المساحي بصيغة CSV.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "مشاركة تقرير CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "تم حفظ الملف: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
