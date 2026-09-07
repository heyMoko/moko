package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.FilterParameters
import kotlin.math.sqrt

/** Applies parameterized camera-look recipes without relying on a GPU effect stack. */
object CameraLookComposer {
    fun compose(
        original: IntArray,
        width: Int,
        height: Int,
        parameters: FilterParameters,
        strength: Float
    ): IntArray {
        require(original.size == width * height) { "Pixel size must match dimensions" }
        val amount = strength.coerceIn(0f, 1f)
        if (amount == 0f) return original.copyOf()

        val output = IntArray(original.size)
        val maxX = (width - 1).coerceAtLeast(1).toFloat()
        val maxY = (height - 1).coerceAtLeast(1).toFloat()

        for (index in original.indices) {
            val source = original[index]
            val alpha = source ushr 24
            val sourceR = ((source ushr 16) and 0xFF) / 255f
            val sourceG = ((source ushr 8) and 0xFF) / 255f
            val sourceB = (source and 0xFF) / 255f
            val sourceLuma = luma(sourceR, sourceG, sourceB)
            val x = index % width
            val y = index / width

            val shadowWeight = 1f - smoothStep(0.06f, 0.46f, sourceLuma)
            val highlightWeight = smoothStep(0.52f, 0.96f, sourceLuma)
            val exposure = parameters.exposure +
                parameters.shadows * shadowWeight +
                parameters.highlights * highlightWeight

            var r = applyContrast(sourceR + exposure, parameters.contrast)
            var g = applyContrast(sourceG + exposure, parameters.contrast)
            var b = applyContrast(sourceB + exposure, parameters.contrast)

            r += parameters.temperature + parameters.tint * 0.45f
            g -= parameters.tint * 0.55f
            b -= parameters.temperature - parameters.tint * 0.45f

            val adjustedLuma = luma(r, g, b)
            r = adjustedLuma + (r - adjustedLuma) * parameters.saturation
            g = adjustedLuma + (g - adjustedLuma) * parameters.saturation
            b = adjustedLuma + (b - adjustedLuma) * parameters.saturation

            r = fade(r, parameters.fade)
            g = fade(g, parameters.fade)
            b = fade(b, parameters.fade)

            val vignette = vignette(x / maxX, y / maxY) * parameters.vignette
            val grain = grain(index) * parameters.grain
            r -= vignette - grain
            g -= vignette - grain
            b -= vignette - grain

            output[index] = packColor(
                alpha = alpha,
                r = lerp(sourceR, r.coerceIn(0f, 1f), amount),
                g = lerp(sourceG, g.coerceIn(0f, 1f), amount),
                b = lerp(sourceB, b.coerceIn(0f, 1f), amount)
            )
        }

        return output
    }

    private fun applyContrast(value: Float, contrast: Float): Float =
        (value - CONTRAST_PIVOT) * contrast + CONTRAST_PIVOT

    private fun fade(value: Float, amount: Float): Float =
        lerp(value, FADE_BLACK_POINT, amount)

    private fun vignette(normalizedX: Float, normalizedY: Float): Float {
        val distance = sqrt(
            (normalizedX - 0.5f) * (normalizedX - 0.5f) +
                (normalizedY - 0.5f) * (normalizedY - 0.5f)
        ) / 0.7071f
        return smoothStep(0.45f, 1f, distance)
    }

    private fun grain(index: Int): Float {
        val hash = (index * 1_103_515_245 + 12_345) and Int.MAX_VALUE
        return hash / Int.MAX_VALUE.toFloat() - 0.5f
    }

    private fun smoothStep(edge0: Float, edge1: Float, value: Float): Float {
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun luma(r: Float, g: Float, b: Float): Float =
        r * 0.2126f + g * 0.7152f + b * 0.0722f

    private fun lerp(start: Float, end: Float, amount: Float): Float =
        start + (end - start) * amount

    private fun packColor(alpha: Int, r: Float, g: Float, b: Float): Int =
        (alpha shl 24) or
            ((r * 255f).toInt().coerceIn(0, 255) shl 16) or
            ((g * 255f).toInt().coerceIn(0, 255) shl 8) or
            (b * 255f).toInt().coerceIn(0, 255)

    private const val CONTRAST_PIVOT = 0.5f
    private const val FADE_BLACK_POINT = 0.14f
}
