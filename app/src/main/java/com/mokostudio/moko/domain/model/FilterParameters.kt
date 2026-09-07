package com.mokostudio.moko.domain.model

/**
 * A camera-look recipe. Values are intentionally small so the same recipe can
 * later be blended by the filter-strength control without changing its shape.
 */
data class FilterParameters(
    val exposure: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val fade: Float = 0f,
    val grain: Float = 0f,
    val vignette: Float = 0f
)
