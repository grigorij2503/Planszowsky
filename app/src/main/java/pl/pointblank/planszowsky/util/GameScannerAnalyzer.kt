package pl.pointblank.planszowsky.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class GameScannerAnalyzer(
    private val onResultDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    // Stability buffer
    private val detectionHistory = mutableMapOf<String, Int>()
    private var frameCount = 0
    private val MAX_HISTORY_FRAMES = 5
    private val MIN_STABILITY_THRESHOLD = 3

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val imageWidth = image.width.toFloat()
            val imageHeight = image.height.toFloat()
            val imageCenterRawX = imageWidth / 2f
            val imageCenterRawY = imageHeight / 2f
            
            var barcodeFound = false

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.firstOrNull { 
                        it.format == Barcode.FORMAT_EAN_13 || it.format == Barcode.FORMAT_EAN_8 || it.format == Barcode.FORMAT_UPC_A 
                    }
                    if (barcode != null) {
                        barcode.rawValue?.let { 
                            onResultDetected(it)
                            barcodeFound = true
                        }
                    }
                }
                .addOnCompleteListener {
                    if (!barcodeFound) {
                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val validBlocks = visionText.textBlocks.filter { block ->
                                    val rect = block.boundingBox ?: return@filter false
                                    val text = block.text
                                    // Initial heavy filter: must be somewhat visible and not obvious metadata
                                    rect.width() > imageWidth * 0.15 && !text.isLikelyGameMetadata()
                                }

                                if (validBlocks.isEmpty()) return@addOnSuccessListener

                                // Scoring System:
                                // Score = (Area / MaxArea) * 0.4 + (1 - DistanceFromCenter / MaxDist) * 0.6
                                // We favor centrality slightly more than pure size.
                                val maxDist = Math.hypot(imageCenterRawX.toDouble(), imageCenterRawY.toDouble()).toFloat()
                                val maxArea = (imageWidth * imageHeight)

                                val bestBlock = validBlocks.maxByOrNull { block ->
                                    val rect = block.boundingBox!!
                                    val blockCenterX = rect.centerX()
                                    val blockCenterY = rect.centerY()
                                    
                                    val distX = blockCenterX - imageCenterRawX
                                    val distY = blockCenterY - imageCenterRawY
                                    val dist = Math.hypot(distX.toDouble(), distY.toDouble()).toFloat()

                                    val area = rect.width() * rect.height()
                                    
                                    val areaScore = area / maxArea
                                    val centerScore = 1.0f - (dist / maxDist)

                                    // Weighting: Center is King (0.7), Size is Queen (0.3)
                                    (areaScore * 0.3f) + (centerScore * 0.7f)
                                }
                                
                                val detectedText = bestBlock?.text?.cleanOcrText()
                                
                                if (detectedText != null && detectedText.length > 2) {
                                    updateStabilityAndEmit(detectedText)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
        } else {
            imageProxy.close()
        }
    }

    private fun updateStabilityAndEmit(text: String) {
        detectionHistory[text] = (detectionHistory[text] ?: 0) + 1
        frameCount++

        if (frameCount >= MAX_HISTORY_FRAMES) {
            val mostStable = detectionHistory.maxByOrNull { it.value }
            if (mostStable != null && mostStable.value >= MIN_STABILITY_THRESHOLD) {
                onResultDetected(mostStable.key)
            }
            detectionHistory.clear()
            frameCount = 0
        }
    }
}
