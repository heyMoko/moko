package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object PersonMaskProcessor {
    fun scaleAndFeather(
        source: PersonMask,
        targetWidth: Int,
        targetHeight: Int,
        featherRadius: Int = 2
    ): PersonMask {
        require(targetWidth > 0) { "Target width must be positive" }
        require(targetHeight > 0) { "Target height must be positive" }

        val scaled = FloatArray(targetWidth * targetHeight)
        val xScale = if (targetWidth == 1) 0f else (source.width - 1).toFloat() / (targetWidth - 1)
        val yScale = if (targetHeight == 1) 0f else (source.height - 1).toFloat() / (targetHeight - 1)

        for (y in 0 until targetHeight) {
            val sourceY = y * yScale
            val y0 = floor(sourceY).toInt().coerceIn(0, source.height - 1)
            val y1 = min(y0 + 1, source.height - 1)
            val fy = sourceY - y0

            for (x in 0 until targetWidth) {
                val sourceX = x * xScale
                val x0 = floor(sourceX).toInt().coerceIn(0, source.width - 1)
                val x1 = min(x0 + 1, source.width - 1)
                val fx = sourceX - x0

                val top = lerp(
                    source.confidence[y0 * source.width + x0],
                    source.confidence[y0 * source.width + x1],
                    fx
                )
                val bottom = lerp(
                    source.confidence[y1 * source.width + x0],
                    source.confidence[y1 * source.width + x1],
                    fx
                )
                scaled[y * targetWidth + x] = smoothMask(lerp(top, bottom, fy))
            }
        }

        val feathered = if (featherRadius > 0) {
            boxBlur(scaled, targetWidth, targetHeight, featherRadius)
        } else {
            scaled
        }

        return PersonMask(
            width = targetWidth,
            height = targetHeight,
            confidence = feathered
        )
    }

    private fun boxBlur(
        source: FloatArray,
        width: Int,
        height: Int,
        radius: Int
    ): FloatArray {
        val clampedRadius = radius.coerceIn(1, 8)
        val output = FloatArray(source.size)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0f
                var count = 0
                for (offsetY in -clampedRadius..clampedRadius) {
                    val sampleY = (y + offsetY).coerceIn(0, height - 1)
                    for (offsetX in -clampedRadius..clampedRadius) {
                        val sampleX = (x + offsetX).coerceIn(0, width - 1)
                        sum += source[sampleY * width + sampleX]
                        count++
                    }
                }
                output[y * width + x] = (sum / max(1, count)).coerceIn(0f, 1f)
            }
        }

        return output
    }

    private fun smoothMask(value: Float): Float {
        val clamped = value.coerceIn(0f, 1f)
        return (clamped * clamped * (3f - 2f * clamped) * 1_000f).roundToInt() / 1_000f
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount
    }
}
