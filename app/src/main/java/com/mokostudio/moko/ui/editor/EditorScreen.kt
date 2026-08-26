@file:Suppress("DEPRECATION")

package com.mokostudio.moko.ui.editor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mokostudio.moko.ui.theme.MokoTheme

@Composable
fun EditorScreen(
    imageUri: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(imageUri) {
        viewModel.loadImage(imageUri)
    }

    EditorScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun EditorScreenContent(
    uiState: EditorUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Back")
                }

                Text(
                    text = "Editor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Save")
                }
            }

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.78f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    PhotoPreview(uiState = uiState)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterPill("Original", selected = true, enabled = uiState.previewImage != null, modifier = Modifier.weight(1f))
                    FilterPill("Flash", selected = false, enabled = false, modifier = Modifier.weight(1f))
                    FilterPill("Film", selected = false, enabled = false, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

@Composable
private fun BoxScope.PhotoPreview(uiState: EditorUiState) {
    when {
        uiState.isLoading -> {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        uiState.error != null -> {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        }

        uiState.previewImage != null -> {
            Image(
                bitmap = uiState.previewImage.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit
            )
        }

        else -> {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF111111),
                                Color(0xFF3F3B35),
                                Color(0xFFE2DED7)
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
private fun FilterPill(
    name: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val foreground = if (selected) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) background else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) foreground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorScreenPreview() {
    MokoTheme {
        EditorScreenContent(
            uiState = EditorUiState(),
            onBackClick = {}
        )
    }
}
