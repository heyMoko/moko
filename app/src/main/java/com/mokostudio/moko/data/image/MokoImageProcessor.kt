package com.mokostudio.moko.data.image

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.core.image.ImageProcessor
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.ProcessedImage
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class MokoImageProcessor @Inject constructor() : ImageProcessor {
    override fun process(
        sourceUri: Uri,
        source: Bitmap,
        filter: FilterDefinition,
        strength: Float
    ): ProcessedImage {
        val bitmap = when (filter.id) {
            FilterDefinition.Flash.id -> applyFlashLook(source, strength)
            else -> source
        }

        return ProcessedImage(
            sourceUri = sourceUri,
            bitmap = bitmap,
            filter = filter
        )
    }

    private fun applyFlashLook(source: Bitmap, strength: Float): Bitmap {
        if (strength <= 0f) return source

        val width = source.width
        val height = source.height
        val original = IntArray(width * height)
        val flashed = IntArray(width * height)
        source.getPixels(original, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            val ny = y.toFloat() / max(1, height - 1) - 0.46f
            for (x in 0 until width) {
                val index = y * width + x
                val color = original[index]
                val alpha = color ushr 24
                val originalR = ((color ushr 16) and 0xFF) / 255f
                val originalG = ((color ushr 8) and 0xFF) / 255f
                val originalB = (color and 0xFF) / 255f

                val nx = x.toFloat() / max(1, width - 1) - 0.5f
                val distance = sqrt(nx * nx * 1.1f + ny * ny * 0.85f)
                val centerFlash = (1f - (distance / 0.76f).coerceIn(0f, 1f)).pow(1.65f)
                val luma = luma(originalR, originalG, originalB)
                val shadowMask = (1f - luma).coerceIn(0f, 1f).pow(1.35f)
                val highlightMask = smoothStep(0.62f, 1f, luma)
                val edgeMask = (1f - centerFlash).pow(1.6f)

                var r = originalR
                var g = originalG
                var b = originalB

                val exposureLift = 0.045f + centerFlash * 0.15f + shadowMask * 0.055f
                r += exposureLift
                g += exposureLift * 1.02f
                b += exposureLift * 1.08f

                r = applyContrast(r, 1.14f, 0.48f)
                g = applyContrast(g, 1.14f, 0.48f)
                b = applyContrast(b, 1.14f, 0.48f)

                val currentLuma = luma(r, g, b)
                val saturationScale = when {
                    isWarmSkinLike(originalR, originalG, originalB) -> 0.88f
                    highlightMask > 0.2f -> 0.9f
                    else -> 1.08f
                }
                r = currentLuma + (r - currentLuma) * saturationScale
                g = currentLuma + (g - currentLuma) * saturationScale
                b = currentLuma + (b - currentLuma) * saturationScale

                r = r * 0.985f + 0.006f
                g = g * 1.005f + 0.004f
                b = b * 1.045f + 0.012f

                val highlightControl = highlightMask * 0.035f
                r -= highlightControl * 0.25f
                g += highlightControl * 0.1f
                b += highlightControl * 0.35f

                val vignette = edgeMask * 0.052f
                r -= vignette
                g -= vignette
                b -= vignette

                val grain = deterministicNoise(x, y) * 0.012f
                r += grain
                g += grain
                b += grain

                val fullR = r.coerceIn(0f, 1f)
                val fullG = g.coerceIn(0f, 1f)
                val fullB = b.coerceIn(0f, 1f)

                val blendedR = lerp(originalR, fullR, strength)
                val blendedG = lerp(originalG, fullG, strength)
                val blendedB = lerp(originalB, fullB, strength)
                flashed[index] = packColor(alpha, blendedR, blendedG, blendedB)
            }
        }

        sharpen(flashed, width, height, amount = 0.24f * strength)

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(flashed, 0, width, 0, 0, width, height)
        }
    }

    private fun sharpen(pixels: IntArray, width: Int, height: Int, amount: Float) {
        if (amount <= 0f || width < 3 || height < 3) return

        val source = pixels.copyOf()
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                val color = source[index]
                val alpha = color ushr 24
                val centerR = ((color ushr 16) and 0xFF) / 255f
                val centerG = ((color ushr 8) and 0xFF) / 255f
                val centerB = (color and 0xFF) / 255f

                val blur = averageNeighbors(source, width, x, y)
                val r = centerR + (centerR - blur[0]) * amount
                val g = centerG + (centerG - blur[1]) * amount
                val b = centerB + (centerB - blur[2]) * amount
                pixels[index] = packColor(alpha, r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
            }
        }
    }

    private fun averageNeighbors(pixels: IntArray, width: Int, x: Int, y: Int): FloatArray {
        var r = 0f
        var g = 0f
        var b = 0f

        for (offsetY in -1..1) {
            for (offsetX in -1..1) {
                val color = pixels[(y + offsetY) * width + x + offsetX]
                r += ((color ushr 16) and 0xFF) / 255f
                g += ((color ushr 8) and 0xFF) / 255f
                b += (color and 0xFF) / 255f
            }
        }

        return floatArrayOf(r / 9f, g / 9f, b / 9f)
    }

    private fun applyContrast(value: Float, contrast: Float, pivot: Float): Float {
        return (value - pivot) * contrast + pivot
    }

    private fun luma(r: Float, g: Float, b: Float): Float {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f
    }

    private fun smoothStep(edge0: Float, edge1: Float, value: Float): Float {
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun isWarmSkinLike(r: Float, g: Float, b: Float): Boolean {
        return r > 0.32f && r > g && g > b && r - b > 0.08f
    }

    private fun deterministicNoise(x: Int, y: Int): Float {
        val hash = (x * 73_856_093) xor (y * 19_349_663)
        return (((hash and 0xFF) / 255f) - 0.5f)
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount
    }

    private fun packColor(alpha: Int, r: Float, g: Float, b: Float): Int {
        return (alpha shl 24) or
            ((r * 255f).toInt().coerceIn(0, 255) shl 16) or
            ((g * 255f).toInt().coerceIn(0, 255) shl 8) or
            (b * 255f).toInt().coerceIn(0, 255)
    }
}
