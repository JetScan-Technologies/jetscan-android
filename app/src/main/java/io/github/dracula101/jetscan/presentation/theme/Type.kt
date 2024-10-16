package io.github.dracula101.jetscan.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.dracula101.jetscan.R

val nunito_light = FontFamily(Font(R.font.nunito_sans_light))
val nunito_regular = FontFamily(Font(R.font.nunito_sans))
val nunito_semi_bold = FontFamily(Font(R.font.nunito_sans_semibold))
val nunito_bold = FontFamily(Font(R.font.nunito_sans_bold))

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 12.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_semi_bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_regular,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_regular,
        fontSize = 16.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_bold,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_semi_bold,
        fontSize = 24.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = nunito_semi_bold,
        fontSize = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 16.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 12.sp
    ),
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_bold,
        fontSize = 22.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_semi_bold,
        fontSize = 20.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = nunito_regular,
        fontSize = 18.sp
    ),
)