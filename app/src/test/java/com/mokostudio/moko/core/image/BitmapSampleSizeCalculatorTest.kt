package com.mokostudio.moko.core.image

import org.junit.Assert.assertEquals
import org.junit.Test

class BitmapSampleSizeCalculatorTest {
    @Test
    fun calculate_returnsOneWhenImageFitsMaxDimension() {
        val sampleSize = BitmapSampleSizeCalculator.calculate(
            width = 1_200,
            height = 1_600,
            maxDimension = 2_048
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun calculate_returnsPowerOfTwoSampleSizeForLargeImage() {
        val sampleSize = BitmapSampleSizeCalculator.calculate(
            width = 8_000,
            height = 6_000,
            maxDimension = 2_048
        )

        assertEquals(2, sampleSize)
    }
}
