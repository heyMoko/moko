package com.mokostudio.moko.data.image

import com.mokostudio.moko.domain.model.PersonMask
import kotlin.math.floor
import kotlin.math.min

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
                scaled[y * targetWidth + x] = lerp(top, bottom, fy).coerceIn(0f, 1f)
            }
        }

        val feathered = if (featherRadius > 0) {
            val blurred = boxBlur(scaled, targetWidth, targetHeight, featherRadius)
            // Blur is useful for smoothing ML Kit's low-resolution mask, but a normal blur
            // also raises confidence on the background side of a body edge. Cap it by the
            // original confidence so Flash can soften *inside* the person without spilling
            // its extra exposure onto the surrounding background.
            FloatArray(scaled.size) { index -> min(scaled[index], blurred[index]) }
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
        val clampedRadius = radius.coerceIn(1, 32)
        val horizontal = FloatArray(source.size)
        val output = FloatArray(source.size)
        val windowSize = clampedRadius * 2 + 1

        for (y in 0 until height) {
            var sum = 0f
            for (offset in -clampedRadius..clampedRadius) {
                sum += source[y * width + offset.coerceIn(0, width - 1)]
            }
            for (x in 0 until width) {
                horizontal[y * width + x] = sum / windowSize
                val removeX = (x - clampedRadius).coerceIn(0, width - 1)
                val addX = (x + clampedRadius + 1).coerceIn(0, width - 1)
                sum += source[y * width + addX] - source[y * width + removeX]
            }
        }

        for (x in 0 until width) {
            var sum = 0f
            for (offset in -clampedRadius..clampedRadius) {
                sum += horizontal[offset.coerceIn(0, height - 1) * width + x]
            }
            for (y in 0 until height) {
                output[y * width + x] = (sum / windowSize).coerceIn(0f, 1f)
                val removeY = (y - clampedRadius).coerceIn(0, height - 1)
                val addY = (y + clampedRadius + 1).coerceIn(0, height - 1)
                sum += horizontal[addY * width + x] - horizontal[removeY * width + x]
            }
        }

        return output
    }

    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount
    }
}
