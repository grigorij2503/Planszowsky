package pl.pointblank.planszowsky.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

class GameScannerAnalyzer(
    private val onResultDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    // --- STABILIZACJA ---
    private val recentDetections = mutableListOf<String>()
    private val HISTORY_SIZE = 5 
    private val CONFIDENCE_THRESHOLD = 3 

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)

            val imageWidth = if (rotation == 90 || rotation == 270) image.height.toFloat() else image.width.toFloat()
            val imageHeight = if (rotation == 90 || rotation == 270) image.width.toFloat() else image.height.toFloat()
            val imageCenterX = imageWidth / 2f
            val imageCenterY = imageHeight / 2f

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes: List<Barcode> ->
                    val qrCode = barcodes.find { it.format == Barcode.FORMAT_QR_CODE }
                    val url = qrCode?.url?.url ?: qrCode?.displayValue
                    
                    if (url != null && url.startsWith("http")) {
                        onResultDetected(url)
                        imageProxy.close()
                    } else {
                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val bestCandidate = visionText.textBlocks.mapNotNull { block ->
                                    val rect = block.boundingBox ?: return@mapNotNull null
                                    val text = block.text.cleanOcrText()

                                    if (text.length < 3) return@mapNotNull null
                                    if (text.isLikelyGameMetadata()) return@mapNotNull null
                                    if (rect.height() < imageHeight * 0.05f) return@mapNotNull null

                                    val areaScore = (rect.width() * rect.height()).toFloat() / (imageWidth * imageHeight)
                                    val distX = abs(rect.centerX() - imageCenterX) / imageWidth
                                    val distY = abs(rect.centerY() - imageCenterY) / imageHeight
                                    val centralityScore = 1.0f - (distX + distY)
                                    val totalScore = areaScore * 2.0f + centralityScore

                                    Candidate(text, totalScore)
                                }.maxByOrNull { it.score }

                                if (bestCandidate != null && bestCandidate.score > 0.8f) {
                                    processStabilityFuzzy(bestCandidate.text)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                            .addOnFailureListener {
                                imageProxy.close()
                            }
                    }
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processStabilityFuzzy(newText: String) {
        recentDetections.add(newText)
        if (recentDetections.size > HISTORY_SIZE) {
            recentDetections.removeAt(0)
        }

        val similarCount = recentDetections.count { historyItem ->
            calculateSimilarity(newText, historyItem) > 0.8
        }

        if (similarCount >= CONFIDENCE_THRESHOLD) {
            val bestVersion = recentDetections
                .filter { calculateSimilarity(newText, it) > 0.8 }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: newText

            onResultDetected(bestVersion)
        }
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1
        if (longer.isEmpty()) return 1.0
        val distance = levenshtein(longer, shorter).toDouble()
        return (longer.length - distance) / longer.length
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }

    private data class Candidate(val text: String, val score: Float)

    private fun String.cleanOcrText(): String {
        return this.replace("\n", " ")
            .replace(Regex("[^a-zA-Z0-9 ]"), "")
            .trim()
            .replace(Regex(" +"), " ")
    }

    private fun String.isLikelyGameMetadata(): Boolean {
        val metaKeywords = listOf("players", "graczy", "wiek", "ages", "minutes", "minut", "time", "czas", "author", "autor", "spiel", "game")
        val lower = this.lowercase(Locale.getDefault())
        return metaKeywords.any { lower.contains(it) } && this.any { it.isDigit() }
    }
}
