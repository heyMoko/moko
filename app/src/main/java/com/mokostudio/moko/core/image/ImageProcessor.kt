package com.mokostudio.moko.core.image

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.ProcessedImage

interface ImageProcessor {
    fun process(
        sourceUri: Uri,
        source: Bitmap,
        filter: FilterDefinition,
        strength: Float = 1f
    ): ProcessedImage
}
