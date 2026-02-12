package pl.pointblank.planszowsky.ui.screens

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.util.GameScannerAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    appTheme: AppTheme = AppTheme.MODERN,
    onTextScanned: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val isRetro = appTheme == AppTheme.PIXEL_ART

    if (cameraPermissionState.status.isGranted) {
        CameraPreview(isRetro, onTextScanned, onBackClick)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isRetro) Modifier.retroBackground() else Modifier.background(MaterialTheme.colorScheme.background))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.camera_permission_req).let { if(isRetro) it.uppercase() else it },
                style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (isRetro) {
                RetroSquareButton(
                    text = stringResource(R.string.grant_permission).uppercase(),
                    color = RetroGreen,
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                )
            } else {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    isRetro: Boolean,
    onResultScanned: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var detectedValue by remember { mutableStateOf("") }

    LaunchedEffect(detectedValue) {
        if (detectedValue.isNotBlank()) {
            kotlinx.coroutines.delay(5000)
            detectedValue = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                GameScannerAnalyzer { result ->
                                    detectedValue = result
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Binding failed", e)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (isRetro) {
                RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
                    PixelBackIcon(color = RetroText)
                }
            } else {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.Black.copy(0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (detectedValue.isNotBlank()) {
                if (isRetro) {
                    RetroChunkyBox(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        accentColor = RetroGold
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val label = stringResource(R.string.title_detected)
                            
                            Text(
                                text = label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                            )
                            Text(
                                text = detectedValue.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroGold)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            RetroSquareButton(
                                text = stringResource(R.string.search_this_game).uppercase(),
                                color = RetroGreen,
                                onClick = { onResultScanned(detectedValue) }
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val label = stringResource(R.string.title_detected)
                            Text(label, style = MaterialTheme.typography.labelSmall)
                            Text(detectedValue, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onResultScanned(detectedValue) }) {
                                Text(stringResource(R.string.search_this_game))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
