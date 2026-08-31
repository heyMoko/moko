package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashImageComposerTest {
    @Test
    fun compose_returnsOriginalPixelsWhenStrengthIsZero() {
        val original = intArrayOf(argb(255, 42, 36, 34), argb(255, 82, 65, 54))

        val result = FlashImageComposer.compose(original, 2, 1, 0f, null)

        assertEquals(original.toList(), result.toList())
    }

    @Test
    fun compose_liftsThePositionSelectedByPersonMask() {
        val original = IntArray(9) { argb(255, 94, 67, 57) }
        val confidence = FloatArray(9)
        confidence[0] = 1f
        val mask = PersonMask(3, 3, confidence)

        val result = FlashImageComposer.compose(original, 3, 3, 1f, mask)

        assertTrue(luma(result[0]) > luma(result[4]) + 0.06f)
    }

    @Test
    fun compose_blendsHalfConfidenceBetweenBackgroundAndPerson() {
        val original = IntArray(3) { argb(255, 94, 67, 57) }
        val mask = PersonMask(3, 1, floatArrayOf(0f, 0.5f, 1f))

        val result = FlashImageComposer.compose(original, 3, 1, 1f, mask)

        assertTrue(luma(result[0]) < luma(result[1]))
        assertTrue(luma(result[1]) < luma(result[2]))
    }

    @Test
    fun compose_liftsSkinMoreThanBlackClothingInsidePersonMask() {
        val original = intArrayOf(argb(255, 16, 16, 16), argb(255, 94, 67, 57))
        val mask = PersonMask(2, 1, floatArrayOf(1f, 1f))

        val result = FlashImageComposer.compose(original, 2, 1, 1f, mask)

        val blackLift = luma(result[0]) - luma(original[0])
        val skinLift = luma(result[1]) - luma(original[1])
        assertTrue(skinLift > blackLift)
    }

    @Test
    fun compose_preservesImageSizeAndAlpha() {
        val original = intArrayOf(
            argb(127, 70, 52, 44), argb(255, 90, 70, 60),
            argb(255, 20, 24, 30), argb(255, 180, 160, 140)
        )

        val result = FlashImageComposer.compose(original, 2, 2, 1f, null)

        assertEquals(original.size, result.size)
        assertEquals(127, result[0] ushr 24)
    }

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    private fun luma(color: Int): Float {
        val r = ((color ushr 16) and 0xFF) / 255f
        val g = ((color ushr 8) and 0xFF) / 255f
        val b = (color and 0xFF) / 255f
        return r * 0.2126f + g * 0.7152f + b * 0.0722f
    }
}
