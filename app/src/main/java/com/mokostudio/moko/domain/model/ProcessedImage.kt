package com.mokostudio.moko.domain.model

import android.graphics.Bitmap
import android.net.Uri

data class ProcessedImage(
    val sourceUri: Uri,
    val bitmap: Bitmap,
    val filter: FilterDefinition,
    val width: Int = bitmap.width,
    val height: Int = bitmap.height
)
