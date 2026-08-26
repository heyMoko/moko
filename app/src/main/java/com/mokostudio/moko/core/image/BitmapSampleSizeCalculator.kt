package com.mokostudio.moko.core.image

object BitmapSampleSizeCalculator {
    fun calculate(
        width: Int,
        height: Int,
        maxDimension: Int
    ): Int {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
        require(maxDimension > 0) { "maxDimension must be positive" }

        var inSampleSize = 1
        var sampledWidth = width
        var sampledHeight = height

        while (sampledWidth / 2 >= maxDimension || sampledHeight / 2 >= maxDimension) {
            sampledWidth /= 2
            sampledHeight /= 2
            inSampleSize *= 2
        }

        return inSampleSize
    }
}
