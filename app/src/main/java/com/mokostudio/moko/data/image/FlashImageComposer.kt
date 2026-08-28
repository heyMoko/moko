package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object FlashImageComposer {
    fun compose(
        original: IntArray,
        width: Int,
        height: Int,
        strength: Float,
        personMask: PersonMask?
    ): IntArray {
        require(original.size == width * height) { "Pixel size must match dimensions" }
        val normalizedStrength = strength.coerceIn(0f, 1f)
        if (normalizedStrength <= 0f) return original.copyOf()

        val output = IntArray(original.size)
        val usableMask = personMask?.takeIf {
            it.width == width && it.height == height && it.hasPerson()
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val color = original[index]
                val alpha = color ushr 24
                val r = ((color ushr 16) and 0xFF) / 255f
                val g = ((color ushr 8) and 0xFF) / 255f
                val b = (color and 0xFF) / 255f

                val mask = usableMask?.confidence?.get(index)?.coerceIn(0f, 1f) ?: fallbackSubjectMask(
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    r = r,
                    g = g,
                    b = b
                )
                val effectiveMask = (mask * (0.76f + 0.24f * normalizedStrength)).coerceIn(0f, 1f)

                val person = adjustPerson(r, g, b, x, y, width, height, normalizedStrength)
                val background = adjustBackground(r, g, b, x, y, width, height, normalizedStrength)

                val composedR = person[0] * effectiveMask + background[0] * (1f - effectiveMask)
                val composedG = person[1] * effectiveMask + background[1] * (1f - effectiveMask)
                val composedB = person[2] * effectiveMask + background[2] * (1f - effectiveMask)

                output[index] = packColor(
                    alpha = alpha,
                    r = lerp(r, composedR, normalizedStrength),
                    g = lerp(g, composedG, normalizedStrength),
                    b = lerp(b, composedB, normalizedStrength)
                )
            }
        }

        sharpen(output, width, height, amount = 0.18f * normalizedStrength)
        return output
    }

    fun debugLayers(
        original: IntArray,
        width: Int,
        height: Int,
        strength: Float,
        personMask: PersonMask?
    ): FlashDebugLayers {
        val normalizedStrength = strength.coerceIn(0f, 1f)
        val maskPixels = IntArray(original.size)
        val personPixels = IntArray(original.size)
        val backgroundPixels = IntArray(original.size)
        val usableMask = personMask?.takeIf { it.width == width && it.height == height }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val color = original[index]
                val alpha = color ushr 24
                val r = ((color ushr 16) and 0xFF) / 255f
                val g = ((color ushr 8) and 0xFF) / 255f
                val b = (color and 0xFF) / 255f
                val mask = usableMask?.confidence?.get(index)?.coerceIn(0f, 1f) ?: 0f
                val person = adjustPerson(r, g, b, x, y, width, height, normalizedStrength)
                val background = adjustBackground(r, g, b, x, y, width, height, normalizedStrength)

                maskPixels[index] = packColor(alpha, mask, mask, mask)
                personPixels[index] = packColor(alpha, person[0], person[1], person[2])
                backgroundPixels[index] = packColor(alpha, background[0], background[1], background[2])
            }
        }

        return FlashDebugLayers(
            mask = maskPixels,
            personAdjusted = personPixels,
            backgroundAdjusted = backgroundPixels
        )
    }

    private fun adjustPerson(
        r: Float,
        g: Float,
        b: Float,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        strength: Float
    ): FloatArray {
        val luma = luma(r, g, b)
        val shadow = smoothStep(0.08f, 0.58f, 1f - luma)
        val midtone = (1f - kotlin.math.abs(luma - 0.42f) / 0.42f).coerceIn(0f, 1f)
        val highlight = smoothStep(0.68f, 1f, luma)
        val blackPreserve = smoothStep(0.02f, 0.22f, luma)
        val skin = if (isWarmSkinLike(r, g, b)) 1f else 0f

        var outR = r
        var outG = g
        var outB = b

        val lift = (0.09f * shadow + 0.068f * midtone) * blackPreserve * (1f - highlight * 0.74f)
        outR += lift * (1.035f + skin * 0.11f)
        outG += lift * (1.025f + skin * 0.05f)
        outB += lift * (0.998f - skin * 0.034f)

        outR = applyContrast(outR, 1.08f + 0.06f * blackPreserve, 0.45f)
        outG = applyContrast(outG, 1.08f + 0.06f * blackPreserve, 0.45f)
        outB = applyContrast(outB, 1.08f + 0.06f * blackPreserve, 0.45f)

        val currentLuma = luma(outR, outG, outB)
        val saturation = if (skin > 0f) 0.96f else 1.05f
        outR = currentLuma + (outR - currentLuma) * saturation
        outG = currentLuma + (outG - currentLuma) * saturation
        outB = currentLuma + (outB - currentLuma) * saturation

        outR = outR * 1.006f + skin * 0.006f
        outG = outG * 1.003f + skin * 0.0024f
        outB = outB * 1.016f + (1f - skin) * 0.003f

        val highlightControl = highlight * 0.07f
        outR -= highlightControl * 0.24f
        outG -= highlightControl * 0.16f
        outB -= highlightControl * 0.08f

        val grain = deterministicNoise(x, y) * 0.008f * strength
        outR += grain
        outG += grain
        outB += grain

        return floatArrayOf(
            lerp(r, outR.coerceIn(0f, 1f), strength),
            lerp(g, outG.coerceIn(0f, 1f), strength),
            lerp(b, outB.coerceIn(0f, 1f), strength)
        )
    }

    private fun adjustBackground(
        r: Float,
        g: Float,
        b: Float,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        strength: Float
    ): FloatArray {
        val nx = x.toFloat() / max(1, width - 1) - 0.5f
        val ny = y.toFloat() / max(1, height - 1) - 0.48f
        val edge = (sqrt(nx * nx + ny * ny) / 0.72f).coerceIn(0f, 1f).pow(1.35f)
        val luma = luma(r, g, b)
        val highlight = smoothStep(0.72f, 1f, luma)
        val darken = (0.012f + edge * 0.032f + highlight * 0.014f) * strength

        var outR = r - darken
        var outG = g - darken
        var outB = b - darken * 0.96f

        outR = applyContrast(outR, 1.02f, 0.48f)
        outG = applyContrast(outG, 1.02f, 0.48f)
        outB = applyContrast(outB, 1.02f, 0.48f)

        val currentLuma = luma(outR, outG, outB)
        outR = currentLuma + (outR - currentLuma) * 0.96f
        outG = currentLuma + (outG - currentLuma) * 0.96f
        outB = currentLuma + (outB - currentLuma) * 0.96f

        return floatArrayOf(
            lerp(r, outR.coerceIn(0f, 1f), strength),
            lerp(g, outG.coerceIn(0f, 1f), strength),
            lerp(b, outB.coerceIn(0f, 1f), strength)
        )
    }

    private fun fallbackSubjectMask(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        r: Float,
        g: Float,
        b: Float
    ): Float {
        val nx = x.toFloat() / max(1, width - 1) - 0.5f
        val ny = y.toFloat() / max(1, height - 1) - 0.43f
        val center = (1f - (sqrt(nx * nx * 1.18f + ny * ny * 0.8f) / 0.62f).coerceIn(0f, 1f)).pow(1.9f)
        val skin = if (isWarmSkinLike(r, g, b)) 0.16f else 0f
        return (center * 0.38f + skin).coerceIn(0f, 0.46f)
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
                pixels[index] = packColor(
                    alpha = alpha,
                    r = (centerR + (centerR - blur[0]) * amount).coerceIn(0f, 1f),
                    g = (centerG + (centerG - blur[1]) * amount).coerceIn(0f, 1f),
                    b = (centerB + (centerB - blur[2]) * amount).coerceIn(0f, 1f)
                )
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
        return r > 0.3f && r > g && g > b && r - b > 0.075f && g - b > 0.025f
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

data class FlashDebugLayers(
    val mask: IntArray,
    val personAdjusted: IntArray,
    val backgroundAdjusted: IntArray
)
