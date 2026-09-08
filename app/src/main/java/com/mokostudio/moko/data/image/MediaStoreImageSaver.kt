package com.mokostudio.moko.data.image

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MediaStoreImageSaver @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun save(bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, createDisplayName())
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_JPEG)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, RELATIVE_PATH)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val destination = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: throw IOException("Could not create a gallery image")

        return try {
            resolver.openOutputStream(destination)?.use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    throw IOException("Could not encode the edited photo")
                }
            } ?: throw IOException("Could not open the gallery image")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(destination, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
            }
            destination
        } catch (error: Throwable) {
            resolver.delete(destination, null, null)
            throw error
        }
    }

    private fun createDisplayName(): String =
        "moko_${dateFormatter.format(Date())}.jpg"

    private companion object {
        const val MIME_TYPE_JPEG = "image/jpeg"
        const val JPEG_QUALITY = 95
        const val RELATIVE_PATH = "Pictures/moko"
        val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    }
}
