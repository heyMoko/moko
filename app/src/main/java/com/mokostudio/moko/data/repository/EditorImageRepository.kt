package com.mokostudio.moko.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.core.image.FilterEngine
import com.mokostudio.moko.data.image.AndroidBitmapImageLoader
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.ProcessedImage
import javax.inject.Inject

class EditorImageRepository @Inject constructor(
    private val imageLoader: AndroidBitmapImageLoader,
    private val filterEngine: FilterEngine
) {
    fun loadOriginalPreview(uri: Uri): ProcessedImage {
        val bitmap = imageLoader.loadSampledBitmap(uri)
        return applyFilter(
            uri = uri,
            bitmap = bitmap,
            filter = FilterDefinition.Original
        )
    }

    fun applyFilter(
        uri: Uri,
        bitmap: Bitmap,
        filter: FilterDefinition,
        strength: Float = 1f
    ): ProcessedImage {
        return filterEngine.applyFilter(
            sourceUri = uri,
            source = bitmap,
            filter = filter,
            strength = strength
        )
    }
}
