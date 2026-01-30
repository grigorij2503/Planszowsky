package com.planszowsky.android.util

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
            val imageWidth = image.width
            val imageHeight = image.height
            
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
                                // Filter: only consider blocks that are relatively "large"
                                // (e.g., width > 20% of image width)
                                val bestBlock = visionText.textBlocks
                                    .filter { block ->
                                        val rect = block.boundingBox ?: return@filter false
                                        rect.width() > imageWidth * 0.2
                                    }
                                    .maxByOrNull { block -> 
                                        val rect = block.boundingBox
                                        if (rect != null) rect.width() * rect.height() else 0
                                    }
                                
                                val detectedText = bestBlock?.text?.replace("\n", " ")?.trim()
                                
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
