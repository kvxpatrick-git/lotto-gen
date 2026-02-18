package com.hsmomo.lottogen.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF6200EE)
val PrimaryVariant = Color(0xFF3700B3)
val Secondary = Color(0xFF03DAC6)
val SecondaryVariant = Color(0xFF018786)

val Background = Color(0xFFF5F5F5)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF0F0F0)

val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFF1C1B1F)
val OnSurface = Color(0xFF1C1B1F)

// Lotto ball colors
val LottoBallYellow = Color(0xFFFFC107)
val LottoBallBlue = Color(0xFF2196F3)
val LottoBallRed = Color(0xFFF44336)
val LottoBallGray = Color(0xFF9E9E9E)
val LottoBallGreen = Color(0xFF4CAF50)

val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)

val BookmarkActive = Color(0xFFFFD700)
val BookmarkInactive = Color(0xFFBDBDBD)

fun getLottoBallColor(number: Int): Color {
    return when (number) {
        in 1..10 -> LottoBallYellow
        in 11..20 -> LottoBallBlue
        in 21..30 -> LottoBallRed
        in 31..40 -> LottoBallGray
        in 41..45 -> LottoBallGreen
        else -> LottoBallGray
    }
}
