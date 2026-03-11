package com.example.calculator.presentation.calculator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.calculator.presentation.scanner.ScannerScreen
import com.example.calculator.presentation.security.LockViewModel
import com.example.calculator.ui.theme.CalcDarkText
import com.example.calculator.ui.theme.CalcGray
import com.example.calculator.ui.theme.CalcSpecial

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel, lockViewModel: LockViewModel) {
    val context = LocalContext.current

    // Состояния из ViewModel
    val history by viewModel.history.collectAsState()
    val availableThemes by viewModel.availableThemes.collectAsState()
    val themeColorString by viewModel.remoteThemeColor.collectAsState()

    // Парсинг цвета темы для UI
    val themeColor = remember(themeColorString) {
        try {
            Color(android.graphics.Color.parseColor(themeColorString))
        } catch (e: Exception) {
            Color(0xFFFEA00C) // Оранжевый по умолчанию, если в БД ошибка
        }
    }

    // Состояния для UI-элементов
    var showHistory by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- Разрешения (Уведомления и Камера) ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Уведомления отключены", Toast.LENGTH_SHORT).show()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleScanner(true)
        else Toast.makeText(context, "Нет доступа к камере", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun openScanner() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.toggleScanner(true)
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    // Основной контейнер
    Scaffold(
        topBar = {
            // Верхняя панель с кнопками настроек
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Row {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.Gray)
                    }
                    IconButton(onClick = { showThemePicker = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Themes", tint = Color.Gray)
                    }
                    Button(
                        onClick = {
                            lockViewModel.resetSecurity()
                            showThemePicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сбросить защиту (PIN)", color = Color.Red)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Дисплей
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = viewModel.displayText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = when {
                            viewModel.displayText.length > 12 -> 35.sp
                            viewModel.displayText.length > 10 -> 45.sp
                            viewModel.displayText.length > 8 -> 60.sp
                            else -> 80.sp
                        },
                        textAlign = TextAlign.End
                    ),
                    color = CalcDarkText,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка камеры
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { openScanner() }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = themeColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Сетка кнопок
            val buttons = listOf(
                listOf("AC", "+/-", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            Column(modifier = Modifier.weight(1.8f)) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { symbol ->
                            CalcButton(
                                symbol = symbol,
                                modifier = Modifier
                                    .weight(if (symbol == "0") 2.1f else 1f)
                                    .fillMaxHeight(),
                                containerColor = when {
                                    symbol in listOf("÷", "×", "-", "+", "=") -> themeColor
                                    symbol in listOf("AC", "+/-", "%") -> CalcSpecial
                                    else -> CalcGray
                                },
                                onClick = { viewModel.onAction(symbol) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Шторка Истории ---
    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).height(450.dp)) {
                Text("История вычислений", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { record ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(text = record.expression, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "= ${record.result}", color = Color.Black, style = MaterialTheme.typography.headlineMedium)
                            HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }

    // --- Шторка Выбора тем ---
    if (showThemePicker) {
        ModalBottomSheet(onDismissRequest = { showThemePicker = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Облачные темы", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(availableThemes) { theme ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.selectTheme(theme.color)
                                showThemePicker = false
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .background(Color(android.graphics.Color.parseColor(theme.color)), CircleShape)
                                    .border(2.dp, Color.LightGray, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(theme.name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Экран сканера (Step 2)
    if (viewModel.showScanner) {
        ScannerScreen(
            onNumberDetected = { number -> viewModel.onNumberScanned(number) },
            onClose = { viewModel.toggleScanner(false) }
        )
    }
}

@Composable
fun CalcButton(
    symbol: String,
    modifier: Modifier,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = if (containerColor == CalcGray || containerColor == CalcSpecial) CalcDarkText else Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp)
        )
    }
}