package io.github.dracula101.jetscan.presentation.platform.component.button

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f,0.0f,0.0f,0.0f)
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.clickableWithoutRipple(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) = composed(
    factory = {
        this.then(
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick() }
            )
        )
    }
)

@Composable
fun NoRippleButton(
    onClick: () -> Unit,
    containerColor: Color = Color.Transparent,
    content: @Composable RowScope.() -> Unit,
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            content = content,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
            ),
        )
    }
}