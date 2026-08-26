package com.mokostudio.moko.ui.editor

import android.graphics.Bitmap
import android.net.Uri

data class EditorUiState(
    val originalImageUri: Uri? = null,
    val previewImage: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
