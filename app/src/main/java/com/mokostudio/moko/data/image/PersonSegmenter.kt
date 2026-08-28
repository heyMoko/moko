package com.mokostudio.moko.data.image

import android.graphics.Bitmap
import com.mokostudio.moko.domain.model.PersonMask

interface PersonSegmenter {
    suspend fun createMask(bitmap: Bitmap): PersonMask
}
