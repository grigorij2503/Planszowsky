package pl.pointblank.planszowsky.util

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import androidx.core.graphics.scale

class PixelationTransformation(private val pixelSize: Int = 16) : Transformation {
    override val cacheKey: String = "PixelationTransformation($pixelSize)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height
        
        // Scale down
        val scaledWidth = (width / pixelSize).coerceAtLeast(1)
        val scaledHeight = (height / pixelSize).coerceAtLeast(1)
        
        val smallBitmap = input.scale(scaledWidth, scaledHeight, false)
        
        // Scale back up using Nearest Neighbor (bilinear = false)
        return smallBitmap.scale(width, height, false)
    }
}
