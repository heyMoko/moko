package com.mokostudio.moko.ui.editor

import android.net.Uri
import com.mokostudio.moko.data.repository.EditorImageRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val imageRepository: EditorImageRepository
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
                    imageRepository.loadOriginalPreview(uri)
                }
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { processedImage ->
                        state.copy(
                            previewImage = processedImage.bitmap,
                            selectedFilter = processedImage.filter,
                            isLoading = false,
                            error = null
                        )
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
}
