package com.mokostudio.moko.domain.model

data class PersonMask(
    val width: Int,
    val height: Int,
    val confidence: FloatArray
) {
    init {
        require(width > 0) { "Mask width must be positive" }
        require(height > 0) { "Mask height must be positive" }
        require(confidence.size == width * height) { "Mask confidence size must match dimensions" }
    }

    fun hasPerson(minAverageConfidence: Float = 0.015f): Boolean {
        var sum = 0f
        for (value in confidence) {
            sum += value.coerceIn(0f, 1f)
        }
        return sum / confidence.size >= minAverageConfidence
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersonMask) return false
        return width == other.width &&
            height == other.height &&
            confidence.contentEquals(other.confidence)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + confidence.contentHashCode()
        return result
    }
}
