package com.mokostudio.moko.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class EditorViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadImage(uriString: String?) {
        val uri = uriString?.let(Uri::parse)
        if (uri == null || uri == _uiState.value.originalImageUri) {
            return
        }

        _uiState.update {
            it.copy(
                originalImageUri = uri,
                previewImage = null,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    decodeSampledBitmap(uri)
                }
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { bitmap ->
                        state.copy(previewImage = bitmap, isLoading = false, error = null)
                    },
                    onFailure = {
                        state.copy(
                            previewImage = null,
                            isLoading = false,
                            error = "Could not load this photo."
                        )
                    }
                )
            }
        }
    }

    private fun decodeSampledBitmap(uri: Uri): Bitmap {
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
            inSampleSize = calculateInSampleSize(width, height)
        }

        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: throw IllegalArgumentException("Image stream is empty")
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        val maxDimension = 2_048
        var inSampleSize = 1
        var sampledWidth = width
        var sampledHeight = height

        while (sampledWidth / 2 >= maxDimension || sampledHeight / 2 >= maxDimension) {
            sampledWidth /= 2
            sampledHeight /= 2
            inSampleSize *= 2
        }

        return inSampleSize
    }
}
