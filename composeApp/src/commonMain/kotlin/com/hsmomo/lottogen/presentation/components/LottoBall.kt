package com.hsmomo.lottogen.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsmomo.lottogen.ui.theme.getLottoBallColor

@Composable
fun LottoBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    isSelected: Boolean = false,
    isSelectable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) {
        getLottoBallColor(number)
    } else if (isSelectable) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        getLottoBallColor(number)
    }

    val textColor = if (isSelected || !isSelectable) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelectable) {
                    Modifier
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onClick?.invoke() }
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            fontSize = when {
                size >= 40.dp -> 16.sp
                size >= 32.dp -> 14.sp
                else -> 12.sp
            },
            fontWeight = FontWeight.Bold
        )
    }
}
