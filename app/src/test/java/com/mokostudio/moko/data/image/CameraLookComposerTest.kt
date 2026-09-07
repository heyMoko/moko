package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.FilterParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraLookComposerTest {
    @Test
    fun compose_returnsOriginalPixelsWhenStrengthIsZero() {
        val original = intArrayOf(argb(127, 42, 36, 34), argb(255, 82, 65, 54))

        val result = CameraLookComposer.compose(
            original = original,
            width = 2,
            height = 1,
            parameters = FilterParameters(contrast = 1.2f, grain = 0.02f),
            strength = 0f
        )

        assertEquals(original.toList(), result.toList())
    }

    @Test
    fun compose_appliesVignetteMoreStronglyAtTheEdge() {
        val original = IntArray(9) { argb(255, 120, 120, 120) }

        val result = CameraLookComposer.compose(
            original = original,
            width = 3,
            height = 3,
            parameters = FilterParameters(vignette = 0.2f),
            strength = 1f
        )

        assertTrue(luma(result[0]) < luma(result[4]))
    }

    @Test
    fun compose_keepsAlphaAndCreatesDistinctCameraLooks() {
        val original = intArrayOf(
            argb(127, 78, 55, 42), argb(255, 154, 112, 74),
            argb(255, 44, 68, 98), argb(255, 210, 196, 172)
        )
        val film = CameraLookComposer.compose(
            original = original,
            width = 2,
            height = 2,
            parameters = FilterParameters(
                contrast = 0.9f,
                saturation = 0.8f,
                temperature = 0.05f,
                fade = 0.12f,
                grain = 0.015f
            ),
            strength = 1f
        )
        val digital = CameraLookComposer.compose(
            original = original,
            width = 2,
            height = 2,
            parameters = FilterParameters(
                contrast = 1.23f,
                saturation = 1.13f,
                temperature = -0.035f,
                vignette = 0.06f
            ),
            strength = 1f
        )

        assertEquals(127, film[0] ushr 24)
        assertNotEquals(film.toList(), digital.toList())
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
