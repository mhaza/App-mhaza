package com.example.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.LandAreaUnits
import com.example.model.SurveyPoint
import com.example.model.SurveyProject
import com.example.ui.components.PolygonPreview
import com.example.ui.theme.HamdanPrimary
import com.example.ui.theme.HamdanSecondary
import com.example.util.SurveyMath
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamdanApp(
    viewModel: HamdanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // معالج أذونات الموقع
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.captureCurrentGpsPoint(context)
        } else {
            Toast.makeText(context, "يلزم السماح بإذن الموقع لتسجيل إحداثيات GPS", Toast.LENGTH_LONG).show()
        }
    }

    // إظهار الرسائل التنبيهية في سناكبار
    LaunchedEffect(uiState.lastGpsStatusMessage) {
        uiState.lastGpsStatusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // تأكيد الاتجاه العربي RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "همدان",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = HamdanSecondary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "المساحة",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مساحة الأراضي وحساب مساحاتها",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HamdanPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("app_top_bar")
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                // حاوية استجابة مرنة بعرض متكيف لجميع الشاشات
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .testTag("survey_content_scroll"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // بطاقة الترحيب وإدارة المشروع
                    item {
                        HeroProjectCard(
                            projectName = uiState.projectName,
                            onProjectNameChange = { viewModel.updateProjectName(it) },
                            onNewProject = { viewModel.startNewProject() },
                            onSaveProject = { viewModel.saveCurrentProject() },
                            pointsCount = uiState.points.size
                        )
                    }

                    // أداة حساب مساحة المستطيل
                    item {
                        RectangleCalculatorCard(
                            length = uiState.rectLengthInput,
                            width = uiState.rectWidthInput,
                            onLengthChange = { viewModel.updateRectLength(it) },
                            onWidthChange = { viewModel.updateRectWidth(it) },
                            area = viewModel.getRectangleArea()
                        )
                    }

                    // أداة حساب مساحة المثلث
                    item {
                        TriangleCalculatorCard(
                            sideA = uiState.triSideAInput,
                            sideB = uiState.triSideBInput,
                            sideC = uiState.triSideCInput,
                            onSideAChange = { viewModel.updateTriSideA(it) },
                            onSideBChange = { viewModel.updateTriSideB(it) },
                            onSideCChange = { viewModel.updateTriSideC(it) },
                            area = viewModel.getTriangleArea()
                        )
                    }

                    // أداة المسح بالإحداثيات وGPS
                    item {
                        GpsSurveyCard(
                            points = uiState.points,
                            polygonArea = viewModel.getPolygonArea(),
                            polygonPerimeter = viewModel.getPolygonPerimeter(),
                            isGpsSearching = uiState.isGpsSearching,
                            onRecordGpsPoint = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            onAddManualPoint = { viewModel.showManualPointDialog(true) },
                            onClearPoints = { viewModel.openClearPointsDialog() },
                            onDeletePoint = { index -> viewModel.removePointAt(index) },
                            onExportCsv = { viewModel.exportAndShareCsv(context) }
                        )
                    }

                    // قسم المشاريع المحفوظة
                    item {
                        SavedProjectsSection(
                            projects = uiState.savedProjects,
                            currentProjectName = uiState.projectName,
                            onLoadProject = { viewModel.loadProject(it) },
                            onDeleteProject = { viewModel.confirmDeleteProject(it) }
                        )
                    }

                    // ملاحظة وإخلاء المسؤولية المساحية
                    item {
                        SurveyDisclaimerCard()
                    }
                }
            }
        }

        // مربع حوار إدخال نقطة يدوية
        if (uiState.showManualPointDialog) {
            ManualPointInputDialog(
                onDismiss = { viewModel.showManualPointDialog(false) },
                onAddPoint = { lat, lon, alt ->
                    val success = viewModel.addManualPoint(lat, lon, alt)
                    if (!success) {
                        Toast.makeText(context, "يرجى إدخال إحداثيات صحيحة (-90 إلى 90 للعرض، و -180 إلى 180 للطول)", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // مربع حوار تأكيد مسح النقاط
        if (uiState.showClearPointsDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissClearPointsDialog() },
                title = { Text("مسح جميع النقاط", fontWeight = FontWeight.Bold) },
                text = { Text("هل أنت متأكد من رغبتك في حذف جميع النقاط المسجلة (${uiState.points.size} نقطة) في المشروع الحالي؟") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmClearPoints() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("مسح النقاط")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissClearPointsDialog() }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // مربع حوار تأكيد حذف مشروع محفوظ
        uiState.showDeleteProjectDialog?.let { projectToDelete ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteProjectDialog() },
                title = { Text("حذف المشروع المحفوظ", fontWeight = FontWeight.Bold) },
                text = { Text("هل أنت متأكد من حذف المشروع '${projectToDelete.name}' الذي يحتوي على ${projectToDelete.points.size} نقطة؟ لن يمكن استرجاعه.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteProject(projectToDelete) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("حذف نهائياً")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteProjectDialog() }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// مكونات واجهة المستخدم المخصصة
// -----------------------------------------------------------------------------

@Composable
private fun HeroProjectCard(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    onNewProject: () -> Unit,
    onSaveProject: () -> Unit,
    pointsCount: Int
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_project_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "مرحباً بك في همدان",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HamdanPrimary
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$pointsCount نقطة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "احسب المساحات، سجل نقاط GPS، واحفظ أعمال المساحة محلياً بدقة متناهية.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = projectName,
                onValueChange = onProjectNameChange,
                label = { Text("اسم المشروع") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HamdanPrimary,
                    focusedLabelColor = HamdanPrimary
                )
            )

            // أزرار التحكم بدون تداخل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNewProject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("new_project_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HamdanPrimary)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشروع جديد", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onSaveProject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_project_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HamdanPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ المشروع", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RectangleCalculatorCard(
    length: String,
    width: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    area: Double?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title = "حساب مساحة مستطيل", icon = Icons.Default.Calculate)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rectangle_calculator_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = length,
                        onValueChange = onLengthChange,
                        label = { Text("الطول (متر)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rect_length_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = width,
                        onValueChange = onWidthChange,
                        label = { Text("العرض (متر)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rect_width_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // عرض النتيجة
                AreaResultBadge(
                    areaSquareMeters = area,
                    emptyMessage = "أدخل الطول والعرض كقيم موجبة لحساب المساحة"
                )
            }
        }
    }
}

@Composable
private fun TriangleCalculatorCard(
    sideA: String,
    sideB: String,
    sideC: String,
    onSideAChange: (String) -> Unit,
    onSideBChange: (String) -> Unit,
    onSideCChange: (String) -> Unit,
    area: Double?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title = "حساب مساحة مثلث (صيغة هيرون)", icon = Icons.Default.Straighten)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("triangle_calculator_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sideA,
                        onValueChange = onSideAChange,
                        label = { Text("الضلع أ (م)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tri_side_a_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sideB,
                        onValueChange = onSideBChange,
                        label = { Text("الضلع ب (م)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tri_side_b_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sideC,
                        onValueChange = onSideCChange,
                        label = { Text("الضلع ج (م)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tri_side_c_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // عرض النتيجة
                AreaResultBadge(
                    areaSquareMeters = area,
                    emptyMessage = "أدخل 3 أطوال أضلاع صالحة تحقق متباينة المثلث"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GpsSurveyCard(
    points: List<SurveyPoint>,
    polygonArea: Double?,
    polygonPerimeter: Double?,
    isGpsSearching: Boolean,
    onRecordGpsPoint: () -> Unit,
    onAddManualPoint: () -> Unit,
    onClearPoints: () -> Unit,
    onDeletePoint: (Int) -> Unit,
    onExportCsv: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title = "المسح بالإحداثيات وGPS", icon = Icons.Default.GpsFixed)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gps_survey_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // إحصائيات النقاط والمساحة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "النقاط المسجلة: ${points.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        polygonPerimeter?.let { p ->
                            Text(
                                text = "المحيط التقريبي: ${SurveyMath.formatNumber(p)} متر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isGpsSearching) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = HamdanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "جاري الالتقاط...",
                                fontSize = 12.sp,
                                color = HamdanPrimary
                            )
                        }
                    }
                }

                // عرض مساحة المضلع
                AreaResultBadge(
                    areaSquareMeters = polygonArea,
                    emptyMessage = "أضف 3 نقاط أو أكثر لحساب مساحة المضلع المساحي"
                )

                // رسم المخطط المساحي التفاعلي
                PolygonPreview(
                    points = points,
                    modifier = Modifier.testTag("polygon_canvas_preview")
                )

                // أزرار التحكم بالنقاط
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordGpsPoint,
                        enabled = !isGpsSearching,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("record_gps_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HamdanPrimary)
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسجيل GPS", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onAddManualPoint,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("add_manual_point_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نقطة يدوية", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onClearPoints,
                        enabled = points.isNotEmpty(),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("clear_points_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح", maxLines = 1)
                    }
                }

                // قائمة النقاط المسجلة
                if (points.isNotEmpty()) {
                    Text(
                        text = "تفاصيل الإحداثيات:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        points.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = HamdanPrimary,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.6f", point.latitude)} ، ${String.format(Locale.US, "%.6f", point.longitude)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(
                                    onClick = { onDeletePoint(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "حذف النقطة",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (index < points.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }

                // زر تصدير CSV ومشاركته
                OutlinedButton(
                    onClick = onExportCsv,
                    enabled = points.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_csv_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HamdanPrimary
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تصدير ومشاركة النقاط بصيغة CSV", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SavedProjectsSection(
    projects: List<SurveyProject>,
    currentProjectName: String,
    onLoadProject: (SurveyProject) -> Unit,
    onDeleteProject: (SurveyProject) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title = "المشاريع المحفوظة", icon = Icons.Default.Save)

        if (projects.isEmpty()) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_projects_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد مشاريع مساحية محفوظة بعد.\nاضغط 'حفظ المشروع' في الأعلى لحفظ عملك الحالي.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { project ->
                    val isCurrent = project.name == currentProjectName
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_item_${project.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HamdanPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "النقاط: ${project.points.size} • ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(project.savedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { onLoadProject(project) },
                                    modifier = Modifier.testTag("open_project_${project.id}")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فتح")
                                }

                                IconButton(
                                    onClick = { onDeleteProject(project) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف المشروع",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaResultBadge(
    areaSquareMeters: Double?,
    emptyMessage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (areaSquareMeters != null) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (areaSquareMeters != null) {
                val units = LandAreaUnits(areaSquareMeters)
                Text(
                    text = "المساحة: ${SurveyMath.formatNumber(areaSquareMeters)} م²",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HamdanPrimary
                )

                // وحدات تفصيلية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "• ${String.format(Locale.US, "%.3f", units.dunams)} دونم",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• ${String.format(Locale.US, "%.4f", units.hectares)} هكتار",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• ${String.format(Locale.US, "%.2f", units.labnah)} لبنة تقريبية",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HamdanPrimary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SurveyDisclaimerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = HamdanSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "تنبيه: حساب GPS تقريبي، ويتأثر بدقة الجهاز وعدد النقاط وطريقة تسجيل حدود الأرض ميدانياً.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ManualPointInputDialog(
    onDismiss: () -> Unit,
    onAddPoint: (String, String, String) -> Unit
) {
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var altInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة نقطة يدوياً", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "أدخل إحداثيات النقطة المساحية (درجات عشرية):",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("خط العرض (Latitude مثل 15.3694)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("خط الطول (Longitude مثل 44.1910)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = altInput,
                    onValueChange = { altInput = it },
                    label = { Text("الارتفاع اختياري بالمتر (Altitude)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddPoint(latInput, lonInput, altInput) },
                colors = ButtonDefaults.buttonColors(containerColor = HamdanPrimary)
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
