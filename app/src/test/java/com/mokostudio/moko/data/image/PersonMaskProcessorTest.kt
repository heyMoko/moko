package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonMaskProcessorTest {
    @Test
    fun scaleAndFeather_returnsTargetSize() {
        val mask = PersonMask(
            width = 2,
            height = 2,
            confidence = floatArrayOf(
                0f, 1f,
                1f, 0f
            )
        )

        val scaled = PersonMaskProcessor.scaleAndFeather(
            source = mask,
            targetWidth = 4,
            targetHeight = 4,
            featherRadius = 0
        )

        assertEquals(4, scaled.width)
        assertEquals(4, scaled.height)
        assertEquals(16, scaled.confidence.size)
    }

    @Test
    fun scaleAndFeather_keepsSoftConfidenceValues() {
        val mask = PersonMask(
            width = 2,
            height = 2,
            confidence = floatArrayOf(
                0f, 1f,
                1f, 0f
            )
        )

        val scaled = PersonMaskProcessor.scaleAndFeather(
            source = mask,
            targetWidth = 3,
            targetHeight = 3,
            featherRadius = 0
        )

        val center = scaled.confidence[4]
        assertTrue(center > 0f)
        assertTrue(center < 1f)
    }
}
