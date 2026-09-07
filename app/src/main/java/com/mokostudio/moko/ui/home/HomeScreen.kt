package com.mokostudio.moko.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.mokostudio.moko.R
import com.mokostudio.moko.ui.theme.MokoTheme

@Composable
fun HomeScreen(
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onPhotoSelected(uri)
            }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BrandDot()
                    Text(
                        text = "moko",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "YOUR PHOTO MOOD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                MoodHero()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Find your mood",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                ExampleFilterStrip()

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Choose a photo  ✦",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "m", color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MoodHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(252.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(start = 24.dp, top = 26.dp, bottom = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.47f)) {
            Text(
                text = "Your photo,\nyour mood.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Give everyday snaps a new energy.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.moko_camera_charm_hero),
            contentDescription = "moko camera charm",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(172.dp)
                .clip(RoundedCornerShape(24.dp))
                .padding(end = 4.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ExampleFilterStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExampleFilterPreview(
            name = "Flash",
            colors = listOf(Color(0xFFFF6E52), Color(0xFFFFB35C), Color(0xFFE7453D)),
            modifier = Modifier.weight(1f)
        )
        ExampleFilterPreview(
            name = "Film",
            colors = listOf(Color(0xFF7E2520), Color(0xFFF39A6C), Color(0xFFFFD6B6)),
            modifier = Modifier.weight(1f)
        )
        ExampleFilterPreview(
            name = "Digital",
            colors = listOf(Color(0xFF51212A), Color(0xFFD74B40), Color(0xFFFFB968)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExampleFilterPreview(
    name: String,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(colors))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp)
                )
        )

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MokoTheme {
        HomeScreen(onPhotoSelected = {})
    }
}
