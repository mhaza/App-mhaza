package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.LandAreaUnits
import com.example.model.SurveyPoint
import com.example.model.SurveyProject
import com.example.util.SurveyMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class HamdanUiState(
    val projectName: String = "مشروع جديد",
    val points: List<SurveyPoint> = emptyList(),
    val savedProjects: List<SurveyProject> = emptyList(),

    // مستطيل
    val rectLengthInput: String = "",
    val rectWidthInput: String = "",

    // مثلث
    val triSideAInput: String = "",
    val triSideBInput: String = "",
    val triSideCInput: String = "",

    // حالة GPS
    val isGpsSearching: Boolean = false,
    val lastGpsStatusMessage: String? = null,

    // حوارات
    val showManualPointDialog: Boolean = false,
    val showClearPointsDialog: Boolean = false,
    val showDeleteProjectDialog: SurveyProject? = null,
    val exportedCsvFile: File? = null
)

class HamdanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    private val _uiState = MutableStateFlow(HamdanUiState())
    val uiState: StateFlow<HamdanUiState> = _uiState.asStateFlow()

    init {
        loadSavedProjects()
    }

    fun loadSavedProjects() {
        val projects = repository.loadProjects()
        _uiState.update { it.copy(savedProjects = projects) }
    }

    fun updateProjectName(name: String) {
        _uiState.update { it.copy(projectName = name) }
    }

    fun startNewProject() {
        _uiState.update {
            it.copy(
                projectName = "مشروع جديد",
                points = emptyList(),
                lastGpsStatusMessage = "تم بدء مشروع جديد وتفريغ النقاط"
            )
        }
    }

    fun saveCurrentProject() {
        val state = _uiState.value
        val name = state.projectName.ifBlank { "مشروع بدون اسم" }
        val project = SurveyProject(
            name = name,
            points = state.points,
            savedAt = System.currentTimeMillis()
        )
        val updatedList = repository.saveProject(project)
        _uiState.update {
            it.copy(
                savedProjects = updatedList,
                lastGpsStatusMessage = "تم حفظ المشروع '$name' بنجاح"
            )
        }
    }

    fun loadProject(project: SurveyProject) {
        _uiState.update {
            it.copy(
                projectName = project.name,
                points = project.points,
                lastGpsStatusMessage = "تم فتح المشروع: ${project.name}"
            )
        }
    }

    fun confirmDeleteProject(project: SurveyProject) {
        _uiState.update { it.copy(showDeleteProjectDialog = project) }
    }

    fun dismissDeleteProjectDialog() {
        _uiState.update { it.copy(showDeleteProjectDialog = null) }
    }

    fun deleteProject(project: SurveyProject) {
        val updated = repository.deleteProject(project.id)
        _uiState.update {
            it.copy(
                savedProjects = updated,
                showDeleteProjectDialog = null,
                lastGpsStatusMessage = "تم حذف المشروع '${project.name}'"
            )
        }
    }

    // مستطيل
    fun updateRectLength(value: String) {
        _uiState.update { it.copy(rectLengthInput = value) }
    }

    fun updateRectWidth(value: String) {
        _uiState.update { it.copy(rectWidthInput = value) }
    }

    fun getRectangleArea(): Double? {
        val l = SurveyMath.parseLocalizedDouble(_uiState.value.rectLengthInput) ?: return null
        val w = SurveyMath.parseLocalizedDouble(_uiState.value.rectWidthInput) ?: return null
        return SurveyMath.calculateRectangleArea(l, w)
    }

    // مثلث
    fun updateTriSideA(value: String) {
        _uiState.update { it.copy(triSideAInput = value) }
    }

    fun updateTriSideB(value: String) {
        _uiState.update { it.copy(triSideBInput = value) }
    }

    fun updateTriSideC(value: String) {
        _uiState.update { it.copy(triSideCInput = value) }
    }

    fun getTriangleArea(): Double? {
        val a = SurveyMath.parseLocalizedDouble(_uiState.value.triSideAInput) ?: return null
        val b = SurveyMath.parseLocalizedDouble(_uiState.value.triSideBInput) ?: return null
        val c = SurveyMath.parseLocalizedDouble(_uiState.value.triSideCInput) ?: return null
        return SurveyMath.calculateTriangleAreaHeron(a, b, c)
    }

    // مضلع
    fun getPolygonArea(): Double? {
        return SurveyMath.calculatePolygonAreaSquareMeters(_uiState.value.points)
    }

    fun getPolygonPerimeter(): Double? {
        return SurveyMath.calculatePolygonPerimeterMeters(_uiState.value.points)
    }

    // النقاط
    fun addManualPoint(latitudeStr: String, longitudeStr: String, altitudeStr: String = ""): Boolean {
        val lat = SurveyMath.parseLocalizedDouble(latitudeStr) ?: return false
        val lon = SurveyMath.parseLocalizedDouble(longitudeStr) ?: return false
        val alt = SurveyMath.parseLocalizedDouble(altitudeStr) ?: 0.0

        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            return false
        }

        val newPoint = SurveyPoint(latitude = lat, longitude = lon, altitude = alt)
        _uiState.update {
            it.copy(
                points = it.points + newPoint,
                showManualPointDialog = false,
                lastGpsStatusMessage = "تمت إضافة النقطة رقم ${it.points.size + 1}"
            )
        }
        return true
    }

    fun removePointAt(index: Int) {
        _uiState.update {
            val list = it.points.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(points = list)
        }
    }

    fun openClearPointsDialog() {
        if (_uiState.value.points.isNotEmpty()) {
            _uiState.update { it.copy(showClearPointsDialog = true) }
        }
    }

    fun confirmClearPoints() {
        _uiState.update {
            it.copy(
                points = emptyList(),
                showClearPointsDialog = false,
                lastGpsStatusMessage = "تم مسح جميع النقاط"
            )
        }
    }

    fun dismissClearPointsDialog() {
        _uiState.update { it.copy(showClearPointsDialog = false) }
    }

    fun showManualPointDialog(show: Boolean) {
        _uiState.update { it.copy(showManualPointDialog = show) }
    }

    // التقاط GPS
    @SuppressLint("MissingPermission")
    fun captureCurrentGpsPoint(context: Context) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            _uiState.update { it.copy(lastGpsStatusMessage = "يلزم منح إذن الموقع لاستخدام GPS") }
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            _uiState.update { it.copy(lastGpsStatusMessage = "خدمة الموقع غير متوفرة على هذا الجهاز") }
            return
        }

        _uiState.update { it.copy(isGpsSearching = true, lastGpsStatusMessage = "جاري الاتصال بالأقمار الاصطناعية وتسجيل النقطة...") }

        // محاولة جلب آخر موقع معروف أولاً
        val providers = listOfNotNull(
            LocationManager.GPS_PROVIDER.takeIf { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) },
            LocationManager.NETWORK_PROVIDER.takeIf { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            } catch (_: Exception) {}
        }

        if (bestLocation != null) {
            val point = SurveyPoint(
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                altitude = bestLocation.altitude
            )
            _uiState.update {
                it.copy(
                    isGpsSearching = false,
                    points = it.points + point,
                    lastGpsStatusMessage = "تم تسجيل النقطة بنجاح: ${"%.6f".format(point.latitude)}, ${"%.6f".format(point.longitude)}"
                )
            }
        } else {
            // طلب تحديث موقع فردي
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    val point = SurveyPoint(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        altitude = loc.altitude
                    )
                    _uiState.update {
                        it.copy(
                            isGpsSearching = false,
                            points = it.points + point,
                            lastGpsStatusMessage = "تم التقاط النقطة: ${"%.6f".format(point.latitude)}, ${"%.6f".format(point.longitude)}"
                        )
                    }
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: Exception) {}
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                val providerToUse = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.NETWORK_PROVIDER
                }
                locationManager.requestSingleUpdate(providerToUse, listener, Looper.getMainLooper())
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGpsSearching = false,
                        lastGpsStatusMessage = "يرجى تفعيل خدمة GPS من إعدادات الهاتف والانتظار لثبات الإشارة"
                    )
                }
            }
        }
    }

    // تصدير CSV
    fun exportAndShareCsv(context: Context) {
        val state = _uiState.value
        if (state.points.isEmpty()) {
            Toast.makeText(context, "لا توجد نقاط مسجلة لتصديرها", Toast.LENGTH_SHORT).show()
            return
        }
        val currentProject = SurveyProject(
            name = state.projectName.ifBlank { "مشروع مساحي" },
            points = state.points,
            savedAt = System.currentTimeMillis()
        )
        val file = repository.exportCsv(currentProject)
        if (file != null) {
            _uiState.update { it.copy(exportedCsvFile = file) }
            repository.shareCsvFile(file)
        } else {
            Toast.makeText(context, "تعذر تصدير الملف", Toast.LENGTH_SHORT).show()
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(exportedCsvFile = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(lastGpsStatusMessage = null) }
    }
}
