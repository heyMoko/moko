package com.mokostudio.moko.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mokostudio.moko.ui.navigation.MokoNavHost

@Composable
fun MokoApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MokoNavHost(contentPadding = innerPadding)
    }
}
