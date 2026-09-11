package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.FilterDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraRecipeTest {
    private val looks = listOf(FilterDefinition.IPhone6, FilterDefinition.Film,
        FilterDefinition.Disposable, FilterDefinition.Digital)

    private fun render(filter: FilterDefinition, pixel: Int, strength: Float = 1f): Int =
        CameraLookComposer.compose(IntArray(9) { pixel }, 3, 3,
            requireNotNull(filter.parameters), strength)[4]

    private fun channels(pixel: Int) = listOf(pixel shr 16 and 255, pixel shr 8 and 255, pixel and 255)

    @Test fun recipes_preserveHighlightSeparationAndSkinColorOrdering() {
        looks.forEach { look ->
            val skin = channels(render(look, 0xFFC28E73.toInt()))
            assertTrue("${look.id}: skin should remain warm", skin[0] > skin[1] && skin[1] > skin[2])
            val light = channels(render(look, 0xFFE0E0E0.toInt())).sum()
            val white = channels(render(look, 0xFFF8F8F8.toInt())).sum()
            assertTrue("${look.id}: retain bright detail", white > light + 15)
        }
    }

    @Test fun recipes_haveDistinctNeutralAndShadowRendering() {
        val warm = channels(render(FilterDefinition.Disposable, 0xFF808080.toInt()))
        val cool = channels(render(FilterDefinition.Digital, 0xFF808080.toInt()))
        assertTrue(warm[0] - warm[2] > 15)
        assertTrue(cool[2] - cool[0] > 8)
        val filmBlack = channels(render(FilterDefinition.Film, 0xFF202020.toInt())).sum()
        val digitalBlack = channels(render(FilterDefinition.Digital, 0xFF202020.toInt())).sum()
        assertTrue("Film should have softer blacks", filmBlack > digitalBlack + 30)
        val clean = channels(render(FilterDefinition.IPhone6, 0xFF808080.toInt()))
        assertTrue(clean.max() - clean.min() < 10)
        assertTrue(clean.sum() > 128 * 3)
    }

    @Test fun recipes_preserveOriginalAndBlendAtHalfStrength() {
        val source = 0x7FB28C72
        looks.forEach { look ->
            assertEquals(source, render(look, source, 0f))
            val full = channels(render(look, source))
            val half = channels(render(look, source, 0.5f))
            channels(source).forEachIndexed { i, value ->
                assertTrue(kotlin.math.abs(half[i] - (value + full[i]) / 2f) <= 1f)
            }
            assertEquals(127, render(look, source) ushr 24)
        }
    }
}
