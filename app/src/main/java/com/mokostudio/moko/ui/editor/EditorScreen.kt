@file:Suppress("DEPRECATION")

package com.mokostudio.moko.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.selected
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.hilt.navigation.compose.hiltViewModel
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.ui.theme.MokoTheme

@Composable
fun EditorScreen(
    imageUri: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSavingNotice by remember { mutableStateOf(false) }
    val navigateBack = {
        if (uiState.isSaving) showSavingNotice = true else onBackClick()
    }
    BackHandler(enabled = uiState.isSaving) { showSavingNotice = true }
    if (showSavingNotice && uiState.isSaving) {
        AlertDialog(onDismissRequest = { showSavingNotice = false },
            title = { Text("Saving your photo") },
            text = { Text("Keep this editor open for a moment. Your photo is being saved to the gallery.") },
            confirmButton = { TextButton(onClick = { showSavingNotice = false }) { Text("Got it") } })
    }
    LaunchedEffect(uiState.isSaving) { if (!uiState.isSaving) showSavingNotice = false }
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.saveImage()
        } else {
            viewModel.onSavePermissionDenied()
        }
    }

    LaunchedEffect(imageUri) {
        viewModel.loadImage(imageUri)
    }

    EditorScreenContent(
        uiState = uiState,
        onBackClick = navigateBack,
        onRetryClick = viewModel::retryImage,
        onFilterSelected = viewModel::selectFilter,
        onSaveClick = {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED
            ) {
                writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                viewModel.saveImage()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun EditorScreenContent(
    uiState: EditorUiState,
    onBackClick: () -> Unit,
    onFilterSelected: (FilterDefinition) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val photoHeight = (maxHeight - 330.dp).coerceIn(180.dp, 720.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .semantics { contentDescription = "Back to home" }
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            RoundedCornerShape(15.dp)
                        )
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "moko studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = uiState.selectedFilter.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.size(44.dp))
            }

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(photoHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF252323))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PhotoPreview(uiState = uiState, onRetryClick = onRetryClick)
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Camera looks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "SWIPE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterDefinition.EditorFilters.forEach { filter ->
                        FilterThumbnail(
                            filter = filter,
                            thumbnail = uiState.filterThumbnails[filter],
                            selected = uiState.selectedFilter == filter,
                            enabled = uiState.previewImage != null && !uiState.isLoading && !uiState.isSaving,
                            isLoading = uiState.isThumbnailLoading &&
                                uiState.filterThumbnails[filter] == null,
                            onClick = { onFilterSelected(filter) },
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val saveFeedback = uiState.saveError ?: uiState.saveMessage
                if (saveFeedback != null) {
                    Text(
                        text = saveFeedback,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.saveError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = onSaveClick,
                    enabled = uiState.previewImage != null && !uiState.isLoading && !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = if (uiState.isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f)
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                    Text(
                        text = if (uiState.isSaving) "Saving your photo…" else if (uiState.saveError != null) "Try saving again" else "Save to gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PhotoPreview(uiState: EditorUiState, onRetryClick: () -> Unit) {
    when {
        uiState.isLoading -> {
            uiState.previewImage?.let { preview ->
                Image(preview.asImageBitmap(), null, Modifier.matchParentSize(), contentScale = ContentScale.Fit)
                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.38f)))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                uiState.loadingMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        uiState.error != null -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
            TextButton(onClick = onRetryClick) { Text("Reload photo", color = Color.White) }
            }
        }

        uiState.previewImage != null && uiState.originalPreviewImage != null &&
            uiState.selectedFilter != FilterDefinition.Original ->
            BeforeAfterPreview(
                original = uiState.originalPreviewImage,
                processed = uiState.previewImage
            )

        uiState.previewImage != null -> Image(
            bitmap = uiState.previewImage.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )

        else -> {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a photo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BoxScope.BeforeAfterPreview(
    original: android.graphics.Bitmap,
    processed: android.graphics.Bitmap
) {
    var position by rememberSaveable { mutableFloatStateOf(DEFAULT_COMPARISON_POSITION) }
    val dividerStroke = with(LocalDensity.current) { 2.dp.toPx() }
    val handleRadius = with(LocalDensity.current) { 18.dp.toPx() }

    Box(
        modifier = Modifier
            .matchParentSize()
            .semantics {
                contentDescription = "Before and after comparison"
                progressBarRangeInfo = ProgressBarRangeInfo(position, 0f..1f)
                setProgress { value -> position = value.coerceIn(0f, 1f); true }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        position = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        position = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Image(
            bitmap = processed.asImageBitmap(),
            contentDescription = "After applying filter",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )

        Image(
            bitmap = original.asImageBitmap(),
            contentDescription = "Before applying filter",
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    clipRect(right = size.width * position) {
                        this@drawWithContent.drawContent()
                    }
                },
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val dividerX = size.width * position
            drawLine(
                color = Color.White,
                start = Offset(dividerX, 0f),
                end = Offset(dividerX, size.height),
                strokeWidth = dividerStroke
            )
            drawCircle(
                color = Color.White,
                radius = handleRadius,
                center = Offset(dividerX, size.height / 2f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.72f),
                radius = handleRadius,
                center = Offset(dividerX, size.height / 2f),
                style = Stroke(width = dividerStroke)
            )
        }

        Text(
            text = "Before",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Text(
            text = "After",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Text(
            text = "Drag to compare",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun FilterThumbnail(
    filter: FilterDefinition,
    thumbnail: android.graphics.Bitmap?,
    selected: Boolean,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .semantics { this.selected = selected }
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(78.dp)
                .aspectRatio(0.82f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                thumbnail != null -> Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = "${filter.displayName} filter preview",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = filter.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorScreenPreview() {
    MokoTheme {
        EditorScreenContent(
            uiState = EditorUiState(),
            onBackClick = {},
            onFilterSelected = {},
            onSaveClick = {}
        )
    }
}

private const val DEFAULT_COMPARISON_POSITION = 0.5f
