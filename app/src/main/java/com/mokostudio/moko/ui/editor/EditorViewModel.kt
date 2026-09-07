package com.mokostudio.moko.ui.editor

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.mokostudio.moko.data.repository.EditorImageRepository
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.PersonMask
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
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
    private var sourceBitmap: Bitmap? = null
    private var cachedMaskUri: Uri? = null
    private var cachedPersonMask: PersonMask? = null
    private var loadJob: Job? = null
    private var filterJob: Job? = null
    private var thumbnailJob: Job? = null
    private var imageRequestToken = 0
    private var filterRequestToken = 0

    fun loadImage(uriString: String?) {
        val uri = uriString?.let(Uri::parse)
        if (uri == null || uri == _uiState.value.originalImageUri) {
            return
        }

        imageRequestToken++
        val token = imageRequestToken
        loadJob?.cancel()
        filterJob?.cancel()
        thumbnailJob?.cancel()
        cachedMaskUri = null
        cachedPersonMask = null
        sourceBitmap = null

        _uiState.update {
            it.copy(
                originalImageUri = uri,
                originalPreviewImage = null,
                previewImage = null,
                filterThumbnails = emptyMap(),
                isLoading = true,
                isThumbnailLoading = false,
                loadingMessage = "Loading",
                error = null
            )
        }

        loadJob = viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    imageRepository.loadOriginalPreview(uri)
                }
            }

            if (token != imageRequestToken) return@launch

            result.fold(
                onSuccess = { processedImage ->
                    sourceBitmap = processedImage.bitmap
                    _uiState.update { state ->
                        state.copy(
                            originalPreviewImage = processedImage.bitmap,
                            previewImage = processedImage.bitmap,
                            selectedFilter = processedImage.filter,
                            isLoading = false,
                            loadingMessage = null,
                            error = null
                        )
                    }
                    createFilterThumbnails(uri, processedImage.bitmap, token)
                },
                onFailure = {
                    Log.e(TAG, "Could not load photo: $uri", it)
                    _uiState.update { state ->
                        state.copy(
                            originalPreviewImage = null,
                            previewImage = null,
                            isLoading = false,
                            loadingMessage = null,
                            error = "Could not load this photo."
                        )
                    }
                }
            )
        }
    }

    private fun createFilterThumbnails(uri: Uri, bitmap: Bitmap, token: Int) {
        thumbnailJob?.cancel()
        _uiState.update { it.copy(isThumbnailLoading = true) }

        thumbnailJob = viewModelScope.launch {
            val thumbnails = runCatching {
                withContext(Dispatchers.Default) {
                    val sourceThumbnail = bitmap.createThumbnail()
                    buildMap {
                        put(FilterDefinition.Original, sourceThumbnail)
                        FilterDefinition.EditorFilters
                            .filterNot { it == FilterDefinition.Original }
                            .forEach { filter ->
                                val personMask = if (filter.requiresPersonMask) {
                                    imageRepository.createPersonMask(sourceThumbnail)
                                        .takeIf(PersonMask::hasPerson)
                                } else {
                                    null
                                }
                                put(
                                    filter,
                                    imageRepository.applyFilter(
                                        uri = uri,
                                        bitmap = sourceThumbnail,
                                        filter = filter,
                                        personMask = personMask
                                    ).bitmap
                                )
                            }
                    }
                }
            }

            if (token != imageRequestToken) return@launch

            _uiState.update { state ->
                thumbnails.fold(
                    onSuccess = { generated ->
                        state.copy(
                            filterThumbnails = generated,
                            isThumbnailLoading = false
                        )
                    },
                    onFailure = {
                        Log.w(TAG, "Could not generate filter thumbnails", it)
                        state.copy(isThumbnailLoading = false)
                    }
                )
            }
        }
    }

    private fun Bitmap.createThumbnail(): Bitmap {
        val longestEdge = maxOf(width, height)
        if (longestEdge <= THUMBNAIL_MAX_DIMENSION) return this

        val scale = THUMBNAIL_MAX_DIMENSION.toFloat() / longestEdge
        return Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    fun selectFilter(filter: FilterDefinition) {
        val uri = _uiState.value.originalImageUri ?: return
        val bitmap = sourceBitmap ?: return
        if (filter == _uiState.value.selectedFilter && !_uiState.value.isLoading) {
            return
        }

        filterRequestToken++
        val token = filterRequestToken
        filterJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = if (filter.requiresPersonMask) "Finding person" else "Processing",
                error = null
            )
        }

        filterJob = viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val personMask = if (filter.requiresPersonMask) {
                        getOrCreatePersonMask(uri, bitmap)
                    } else {
                        null
                    }
                    imageRepository.applyFilter(
                        uri = uri,
                        bitmap = bitmap,
                        filter = filter,
                        personMask = personMask
                    )
                }
            }

            if (token != filterRequestToken) return@launch

            _uiState.update { state ->
                result.fold(
                    onSuccess = { processedImage ->
                        state.copy(
                            previewImage = processedImage.bitmap,
                            selectedFilter = processedImage.filter,
                            isLoading = false,
                            loadingMessage = null,
                            error = null
                        )
                    },
                    onFailure = {
                        if (it is CancellationException) {
                            return@fold state
                        }
                        Log.e(TAG, "Could not apply filter: ${filter.id}", it)
                        state.copy(
                            isLoading = false,
                            loadingMessage = null,
                            error = "Could not apply this filter."
                        )
                    }
                )
            }
        }
    }

    private suspend fun getOrCreatePersonMask(uri: Uri, bitmap: Bitmap): PersonMask? {
        val cached = cachedPersonMask
        if (cachedMaskUri == uri && cached != null) {
            return cached
        }

        return runCatching {
            imageRepository.createPersonMask(bitmap)
        }.fold(
            onSuccess = { mask ->
                if (mask.hasPerson()) {
                    cachedMaskUri = uri
                    cachedPersonMask = mask
                    mask
                } else {
                    Log.w(TAG, "ML Kit mask has no confident person area for $uri")
                    null
                }
            },
            onFailure = {
                if (it is CancellationException) throw it
                Log.e(TAG, "ML Kit segmentation failed for $uri. Falling back to weak flash.", it)
                null
            }
        )
    }

    override fun onCleared() {
        loadJob?.cancel()
        filterJob?.cancel()
        thumbnailJob?.cancel()
        cachedPersonMask = null
        sourceBitmap = null
        super.onCleared()
    }

    private companion object {
        const val TAG = "EditorViewModel"
        const val THUMBNAIL_MAX_DIMENSION = 240
    }
}
