package pl.pointblank.planszowsky.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageManager {

    private const val IMAGES_DIR = "game_covers"
    private const val MAX_IMAGE_SIZE = 1024

    suspend fun saveImage(context: Context, sourceUri: Uri, gameId: String): String? = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, IMAGES_DIR)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext null
            
            // First, decode with rotation fix
            val originalBitmap = decodeBitmapWithRotation(context, sourceUri) ?: return@withContext null
            inputStream.close()

            // Center-crop to Square and resize
            val finalBitmap = processBitmapToSquare(originalBitmap, MAX_IMAGE_SIZE)

            val fileName = "game_${gameId}_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            if (finalBitmap != originalBitmap) {
                finalBitmap.recycle()
            }
            originalBitmap.recycle()

            return@withContext file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeBitmapWithRotation(context: Context, uri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        
        if (bitmap == null) return null

        val exifInputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val exif = ExifInterface(exifInputStream)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        exifInputStream.close()

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it != bitmap) bitmap.recycle()
        }
    }

    private fun processBitmapToSquare(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val minEdge = minOf(width, height)
        
        val dx = (width - minEdge) / 2
        val dy = (height - minEdge) / 2
        
        // 1. Center crop to square
        val squareBitmap = Bitmap.createBitmap(bitmap, dx, dy, minEdge, minEdge)
        
        // 2. Resize to target size if needed
        return if (minEdge > targetSize) {
            Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true).also {
                if (it != squareBitmap) squareBitmap.recycle()
            }
        } else {
            squareBitmap
        }
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
