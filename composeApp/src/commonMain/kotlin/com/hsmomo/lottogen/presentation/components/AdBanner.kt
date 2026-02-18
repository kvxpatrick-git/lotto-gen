package com.hsmomo.lottogen.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Placeholder for AdBanner - Platform specific implementation will be provided
 */
@Composable
expect fun AdBanner(modifier: Modifier = Modifier)

/**
 * Fallback banner when ads are not available
 */
@Composable
fun PlaceholderAdBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ad Space",
            fontSize = 12.sp,
            color = Color.DarkGray
        )
    }
}
