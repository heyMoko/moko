package com.mokostudio.moko.ui.editor

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.domain.model.FilterDefinition

data class EditorUiState(
    val originalImageUri: Uri? = null,
    val originalPreviewImage: Bitmap? = null,
    val previewImage: Bitmap? = null,
    val filterThumbnails: Map<FilterDefinition, Bitmap> = emptyMap(),
    val selectedFilter: FilterDefinition = FilterDefinition.Original,
    val isLoading: Boolean = false,
    val isThumbnailLoading: Boolean = false,
    val loadingMessage: String? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val saveError: String? = null
)
