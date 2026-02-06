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
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

class GameScannerAnalyzer(
    private val onResultDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    // --- ULEPSZONA STABILIZACJA ---
    // Przechowujemy listę ostatnich wykryć z ostatnich ramek
    private val recentDetections = mutableListOf<String>()
    private val HISTORY_SIZE = 7 // Sprawdzamy więcej ramek wstecz
    private val CONFIDENCE_THRESHOLD = 4 // Ile razy podobny tekst musi wystąpić

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)

            // Pobieramy wymiary uwzględniając rotację (ważne dla orientacji portretowej!)
            val imageWidth = if (rotation == 90 || rotation == 270) image.height.toFloat() else image.width.toFloat()
            val imageHeight = if (rotation == 90 || rotation == 270) image.width.toFloat() else image.height.toFloat()
            val imageCenterX = imageWidth / 2f
            val imageCenterY = imageHeight / 2f

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
                                // 1. Zbieramy WSZYSTKIE linie tekstu, nie tylko bloki
                                val allLines = visionText.textBlocks.flatMap { it.lines }

                                val bestCandidate = allLines.mapNotNull { line ->
                                    val rect = line.boundingBox ?: return@mapNotNull null
                                    val text = line.text.cleanOcrText()

                                    // 2. Filtrowanie Śmieci (Garbage Collection)
                                    if (text.length < 3) return@mapNotNull null // Za krótkie
                                    if (text.isLikelyGameMetadata()) return@mapNotNull null
                                    // Zmniejszamy próg szerokości do 5% (było 15%)
                                    if (rect.width() < imageWidth * 0.05f) return@mapNotNull null

                                    // 3. System Punktacji (Heurystyka dla gier planszowych)

                                    // A. Wielkość czcionki (Wysokość prostokąta jest tu kluczowa - tytuły są wysokie)
                                    val heightScore = (rect.height().toFloat() / imageHeight) * 2.0f // Wysokość ma podwójną wagę

                                    // B. Centralność
                                    val distX = abs(rect.centerX() - imageCenterX)
                                    val distY = abs(rect.centerY() - imageCenterY)
                                    // Bardziej karzemy odchylenie w pionie (tytuł zazwyczaj jest wyżej lub na środku)
                                    val centralityScore = 1.0f - ((distX / imageWidth) + (distY / imageHeight))

                                    // C. Bonus za ALL CAPS (Tytuły często są wielkimi literami)
                                    val capsBonus = if (text.isUpperCase()) 0.3f else 0.0f

                                    // D. Kara za zbyt długi tekst (To pewnie opis fabuły, a nie tytuł)
                                    val lengthPenalty = if (text.length > 30) 0.5f else 0.0f

                                    val totalScore = heightScore + centralityScore + capsBonus - lengthPenalty

                                    Candidate(text, totalScore)
                                }.maxByOrNull { it.score }

                                if (bestCandidate != null && bestCandidate.score > 0.5f) {
                                    processStabilityFuzzy(bestCandidate.text)
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

    // --- LOGIKA FUZZY STABILITY ---
    // Zamiast szukać identycznego stringa, szukamy "podobnego"
    private fun processStabilityFuzzy(newText: String) {
        recentDetections.add(newText)
        if (recentDetections.size > HISTORY_SIZE) {
            recentDetections.removeAt(0)
        }

        // Sprawdzamy, czy w historii jest wystarczająco dużo tekstów podobnych do ostatniego
        val similarCount = recentDetections.count { historyItem ->
            calculateSimilarity(newText, historyItem) > 0.8 // 80% podobieństwa
        }

        if (similarCount >= CONFIDENCE_THRESHOLD) {
            // Znaleźliśmy stabilny wynik!
            // Wybieramy najczęstszą wersję tego tekstu z bufora (żeby uniknąć literówek)
            val bestVersion = recentDetections
                .filter { calculateSimilarity(newText, it) > 0.8 }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: newText

            onResultDetected(bestVersion)
            recentDetections.clear() // Reset po sukcesie
        }
    }

    // Prosta implementacja Levenshteina do oceny podobieństwa (0.0 - 1.0)
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
        var newCost = IntArray(lhsLength + 1) { 0 }

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

    // Helper Class
    private data class Candidate(val text: String, val score: Float)

    // Extensions
    private fun String.cleanOcrText(): String {
        return this.replace("\n", " ").trim()
    }

    private fun String.isUpperCase(): Boolean {
        return this.all { !it.isLowerCase() }
    }

    // Tu wstaw swoją logikę filtrowania (np. słowa kluczowe jak "Players", "Ages", "Minutes")
    private fun String.isLikelyGameMetadata(): Boolean {
        val metaKeywords = listOf("players", "graczy", "wiek", "ages", "minutes", "minut", "time", "czas", "author", "autor")
        val lower = this.lowercase(Locale.getDefault())
        // Jeśli tekst zawiera cyfry i słowa kluczowe, to pewnie metadane
        return metaKeywords.any { lower.contains(it) } && this.any { it.isDigit() }
    }
}