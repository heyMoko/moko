package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashImageComposerTest {
    @Test
    fun compose_returnsOriginalPixelsWhenStrengthIsZero() {
        val original = intArrayOf(
            argb(255, 42, 36, 34),
            argb(255, 82, 65, 54)
        )

        val result = FlashImageComposer.compose(
            original = original,
            width = 2,
            height = 1,
            strength = 0f,
            personMask = PersonMask(2, 1, floatArrayOf(1f, 0f))
        )

        assertEquals(original.toList(), result.toList())
    }

    @Test
    fun compose_appliesPersonAdjustmentMoreThanBackgroundAdjustment() {
        val original = intArrayOf(
            argb(255, 72, 52, 43),
            argb(255, 72, 52, 43)
        )

        val result = FlashImageComposer.compose(
            original = original,
            width = 2,
            height = 1,
            strength = 1f,
            personMask = PersonMask(2, 1, floatArrayOf(1f, 0f))
        )

        assertTrue(luma(result[0]) > luma(result[1]))
    }

    @Test
    fun compose_preservesBlackPointBetterThanMidtones() {
        val original = intArrayOf(
            argb(255, 7, 7, 7),
            argb(255, 70, 52, 44)
        )

        val result = FlashImageComposer.compose(
            original = original,
            width = 2,
            height = 1,
            strength = 1f,
            personMask = PersonMask(2, 1, floatArrayOf(1f, 1f))
        )

        val blackLift = luma(result[0]) - luma(original[0])
        val midtoneLift = luma(result[1]) - luma(original[1])
        assertTrue(blackLift < midtoneLift)
    }

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun luma(color: Int): Float {
        val r = ((color ushr 16) and 0xFF) / 255f
        val g = ((color ushr 8) and 0xFF) / 255f
        val b = (color and 0xFF) / 255f
        return r * 0.2126f + g * 0.7152f + b * 0.0722f
    }
}
