package com.mokostudio.moko.core.image

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.ProcessedImage
import javax.inject.Inject

class FilterEngine @Inject constructor(
    private val imageProcessor: ImageProcessor
) {
    fun applyFilter(
        sourceUri: Uri,
        source: Bitmap,
        filter: FilterDefinition,
        strength: Float = 1f
    ): ProcessedImage {
        val normalizedStrength = strength.coerceIn(0f, 1f)
        return imageProcessor.process(
            sourceUri = sourceUri,
            source = source,
            filter = filter,
            strength = normalizedStrength
        )
    }
}
