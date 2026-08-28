package com.mokostudio.moko.ui.editor

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.domain.model.FilterDefinition

data class EditorUiState(
    val originalImageUri: Uri? = null,
    val previewImage: Bitmap? = null,
    val selectedFilter: FilterDefinition = FilterDefinition.Original,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val error: String? = null
)
