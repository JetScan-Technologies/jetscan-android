package io.github.dracula101.jetscan.presentation.theme

import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF6F86FF)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDEE0FF)
val md_theme_light_onPrimaryContainer = Color(0xFF00105A)
val md_theme_light_secondary = Color(0xFF3A52CA)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDEE0FF)
val md_theme_light_onSecondaryContainer = Color(0xFF00115A)
val md_theme_light_tertiary = Color(0xFF6C4EA2)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEBDCFF)
val md_theme_light_onTertiaryContainer = Color(0xFF260058)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFEFBFF)
val md_theme_light_onBackground = Color(0xFF1B1B1F)
val md_theme_light_surface = Color(0xFFFEFBFF)
val md_theme_light_onSurface = Color(0xFF1B1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE3E1EC)
val md_theme_light_onSurfaceVariant = Color(0xFF46464F)
val md_theme_light_outline = Color(0xFF767680)
val md_theme_light_inverseOnSurface = Color(0xFFF3F0F4)
val md_theme_light_inverseSurface = Color(0xFF303034)
val md_theme_light_inversePrimary = Color(0xFFBAC3FF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF2E4DDF)
val md_theme_light_outlineVariant = Color(0xFFC6C5D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFF526EFF)
val md_theme_dark_onPrimary = Color(0xFFFFFFFF)
val md_theme_dark_primaryContainer = Color(0xFF0030C8)
val md_theme_dark_onPrimaryContainer = Color(0xFFDEE0FF)
val md_theme_dark_secondary = Color(0xFFBAC3FF)
val md_theme_dark_onSecondary = Color(0xFF00208E)
val md_theme_dark_secondaryContainer = Color(0xFF1C38B1)
val md_theme_dark_onSecondaryContainer = Color(0xFFDEE0FF)
val md_theme_dark_tertiary = Color(0xFFD4BBFF)
val md_theme_dark_onTertiary = Color(0xFF3C1D70)
val md_theme_dark_tertiaryContainer = Color(0xFF533688)
val md_theme_dark_onTertiaryContainer = Color(0xFFEBDCFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1B1B1F)
val md_theme_dark_onBackground = Color(0xFFE4E1E6)
val md_theme_dark_surface = Color(0xFF1B1B1F)
val md_theme_dark_onSurface = Color(0xFFE4E1E6)
val md_theme_dark_surfaceVariant = Color(0xFF46464F)
val md_theme_dark_onSurfaceVariant = Color(0xFFC6C5D0)
val md_theme_dark_outline = Color(0xFF90909A)
val md_theme_dark_inverseOnSurface = Color(0xFF1B1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE4E1E6)
val md_theme_dark_inversePrimary = Color(0xFF2E4DDF)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFFBAC3FF)
val md_theme_dark_outlineVariant = Color(0xFF46464F)
val md_theme_dark_scrim = Color(0xFF000000)


val seed = Color(0xFF526EFF)
val light_526EFF = Color(0xFF4250CD)
val light_on526EFF = Color(0xFFFFFFFF)
val light_526EFFContainer = Color(0xFFDFE0FF)
val light_on526EFFContainer = Color(0xFF000865)
val dark_526EFF = Color(0xFFBDC2FF)
val dark_on526EFF = Color(0xFF00139F)
val dark_526EFFContainer = Color(0xFF2635B4)
val dark_on526EFFContainer = Color(0xFFDFE0FF)


object AppColors {
    fun getRandomColor(extension: String?): Color {
        if (extension.isNullOrEmpty()) {
            return Color.Gray
        }
        val hashCode = extension.hashCode()
        val red = (hashCode and 0xFF0000 shr 16) / 255.0f
        val green = (hashCode and 0x00FF00 shr 8) / 255.0f
        val blue = (hashCode and 0x0000FF) / 255.0f
        return Color(red, green, blue, alpha = 1.0f)
    }

    val green = Color(0xFF39D9A1)
    val green_light = Color(0xFFE3F9F2)
    val green_dark = Color(0xFF1E8A6E)
    val on_green = Color(0xFF001A13)


    val red = Color(0xFFF95658)
    val red_light = Color(0xFFFFE9E9)
    val red_dark = Color(0xFFC53E41)


}

object MainComponentListColor {
    val qr_code = Color(0xFFFEBA57)
    val watermark = Color(0xFFA9715E)
    val esign_pdf = Color(0xFFF95658)
    val split_pdf = Color(0xFF7B5EFF)
    val merge_pdf = Color(0xFFF95658)
    val protect_pdf = Color(0xFF39D9A1)
    val compress_pdf = Color(0xFFFBA323)
    val all_tools = Color(0xFF506CFF)
}