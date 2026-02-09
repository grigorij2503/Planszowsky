package pl.pointblank.planszowsky.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor() {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.POLISH)
        .build()

    private val translator: Translator = Translation.getClient(options)
    private val modelManager = RemoteModelManager.getInstance()

    suspend fun translate(text: String): Result<String> {
        return try {
            ensureModelDownloaded()
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureModelDownloaded() {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions).await()
    }

    fun close() {
        translator.close()
    }
}
