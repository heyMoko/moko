package com.mokostudio.moko.data.image

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.core.image.ImageProcessor
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.ProcessedImage
import javax.inject.Inject

class OriginalImageProcessor @Inject constructor() : ImageProcessor {
    override fun process(
        sourceUri: Uri,
        source: Bitmap,
        filter: FilterDefinition,
        strength: Float
    ): ProcessedImage {
        return ProcessedImage(
            sourceUri = sourceUri,
            bitmap = source,
            filter = filter
        )
    }
}
