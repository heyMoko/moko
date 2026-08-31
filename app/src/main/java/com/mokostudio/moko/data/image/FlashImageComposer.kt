package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask

/**
 * A direct-flash treatment: the whole scene receives one coherent camera grade,
 * while the soft ML person mask adds extra exposure to the photographed person.
 */
object FlashImageComposer {
    fun compose(
        original: IntArray,
        width: Int,
        height: Int,
        strength: Float,
        personMask: PersonMask?
    ): IntArray {
        require(original.size == width * height) { "Pixel size must match dimensions" }
        val amount = strength.coerceIn(0f, 1f)
        if (amount == 0f) return original.copyOf()

        val mask = personMask?.takeIf {
            it.width == width && it.height == height && it.hasPerson()
        }
        val output = IntArray(original.size)

        for (index in original.indices) {
            val color = original[index]
            val alpha = color ushr 24
            val sourceR = ((color ushr 16) and 0xFF) / 255f
            val sourceG = ((color ushr 8) and 0xFF) / 255f
            val sourceB = (color and 0xFF) / 255f
            val sourceLuma = luma(sourceR, sourceG, sourceB)
            val highlight = smoothStep(0.48f, 0.94f, sourceLuma)
            val rawSubject = mask?.confidence?.get(index)?.coerceIn(0f, 1f) ?: 0f
            // Keep the flash transition inside the detected person. The S-curve begins
            // above zero, which removes the blurred mask's outer background spill without
            // introducing a cutout edge.
            val subject = smoothStep(0.18f, 0.98f, rawSubject)

            // The scene grade is intentionally subtle and applies everywhere. It keeps the
            // background from becoming brighter than a direct-flash subject.
            val sceneDarken = 0.050f + highlight * 0.135f
            var r = applyContrast(sourceR, 1.10f, 0.47f) - sceneDarken
            var g = applyContrast(sourceG, 1.10f, 0.47f) - sceneDarken
            var b = applyContrast(sourceB, 1.10f, 0.47f) - sceneDarken * 0.93f

            // The mask is a continuous exposure weight, never a binary person/background cut.
            val skin = skinScore(sourceR, sourceG, sourceB)
            val nonBlack = smoothStep(0.045f, 0.24f, sourceLuma)
            // Most of the lift is shared across the detected person. Skin contributes
            // only a small warm fill, preventing JPEG color blocks from being amplified.
            val materialLift = nonBlack * (0.075f + (1f - highlight) * 0.075f)
            val skinLift = skin * 0.08f
            val flashLift = subject * (0.018f + materialLift + skinLift)
            r += flashLift * (1.025f + skin * 0.035f)
            g += flashLift
            b += flashLift * (0.985f - skin * 0.02f)

            val gradedLuma = luma(r, g, b)
            val saturation = 1.015f - skin * 0.10f
            r = gradedLuma + (r - gradedLuma) * saturation
            g = gradedLuma + (g - gradedLuma) * saturation
            b = gradedLuma + (b - gradedLuma) * saturation

            // A restrained cool camera response, shared by subject and background.
            r *= 0.993f
            g *= 1.001f
            b *= 1.014f

            output[index] = packColor(
                alpha,
                lerp(sourceR, r.coerceIn(0f, 1f), amount),
                lerp(sourceG, g.coerceIn(0f, 1f), amount),
                lerp(sourceB, b.coerceIn(0f, 1f), amount)
            )
        }

        return output
    }

    private fun skinScore(r: Float, g: Float, b: Float): Float {
        // Only used inside the person mask. This keeps warm scene objects from being treated
        // as skin and still recognizes skin that starts in shadow.
        val redBlue = smoothStep(0.025f, 0.16f, r - b)
        val redGreen = smoothStep(0.005f, 0.12f, r - g)
        val greenBlue = smoothStep(0.002f, 0.08f, g - b)
        return minOf(redBlue, redGreen, greenBlue)
    }

    private fun applyContrast(value: Float, contrast: Float, pivot: Float): Float =
        (value - pivot) * contrast + pivot

    private fun luma(r: Float, g: Float, b: Float): Float =
        r * 0.2126f + g * 0.7152f + b * 0.0722f

    private fun smoothStep(edge0: Float, edge1: Float, value: Float): Float {
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float =
        start + (end - start) * amount

    private fun packColor(alpha: Int, r: Float, g: Float, b: Float): Int =
        (alpha shl 24) or
            ((r * 255f).toInt().coerceIn(0, 255) shl 16) or
            ((g * 255f).toInt().coerceIn(0, 255) shl 8) or
            (b * 255f).toInt().coerceIn(0, 255)
}
