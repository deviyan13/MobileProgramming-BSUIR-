package com.example.calculator.presentation.scanner

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onNumberDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Стабильный объект PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val detectedNumbers = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    // 2. Инициализация камеры
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImageForNumbers(imageProxy, recognizer) { numbers ->
                    scope.launch(Dispatchers.Main) {
                        numbers.forEach { number ->
                            if (number !in detectedNumbers) {
                                detectedNumbers.add(number)
                            }
                        }
                    }
                }
                // Важно закрыть прокси, чтобы получить следующий кадр
                imageProxy.close()
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    // 3. UI Слой
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Камера на заднем плане
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Кнопка закрытия (Крестик)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(top = 40.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Text("✕", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }

        // Панель найденных чисел
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (detectedNumbers.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Нажмите на число для ввода:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(detectedNumbers) { number ->
                                Button(
                                    onClick = { onNumberDetected(number) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE0E7FF),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(number)
                                }
                            }
                        }
                    }
                }
            } else {
                // Подсказка, пока ничего не найдено
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "Наведите камеру на текст с числами",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageForNumbers(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onNumbersFound: (List<String>) -> Unit
) {
    val mediaImage = imageProxy.image ?: return
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val numbers = extractAllNumbers(visionText.text)
            if (numbers.isNotEmpty()) {
                onNumbersFound(numbers)
            }
        }
        .addOnFailureListener { it.printStackTrace() }
}

private fun extractAllNumbers(text: String): List<String> {
    // Регулярное выражение для поиска целых и дробных чисел
    val pattern = Regex("""-?\d+(?:[.,]\d+)?""")
    return pattern.findAll(text)
        .map { it.value.replace(',', '.') } // Стандартизируем точку как разделитель
        .filter { it.length < 15 } // Убираем слишком длинный "мусор"
        .distinct()
        .toList()
}