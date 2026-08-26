package com.mokostudio.moko.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.mokostudio.moko.core.image.BitmapSampleSizeCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidBitmapImageLoader @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun loadSampledBitmap(
        uri: Uri,
        maxDimension: Int = DEFAULT_MAX_DIMENSION
    ): Bitmap {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }

        val width = boundsOptions.outWidth
        val height = boundsOptions.outHeight
        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Invalid image bounds")
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = BitmapSampleSizeCalculator.calculate(
                width = width,
                height = height,
                maxDimension = maxDimension
            )
        }

        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: throw IllegalArgumentException("Image stream is empty")
    }

    private companion object {
        const val DEFAULT_MAX_DIMENSION = 2_048
    }
}
