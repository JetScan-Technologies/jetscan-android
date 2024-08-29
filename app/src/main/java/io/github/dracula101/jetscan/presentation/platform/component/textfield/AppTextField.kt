package io.github.dracula101.jetscan.presentation.platform.component.textfield

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.composition.ThemePreviews

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = (if (isError) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ),
        label = if (label != null) {
            { Text( label, style = MaterialTheme.typography.bodyMedium) }
        } else null,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = if (isError) {
            { Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = "inputfield_error_warning",
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = errorText ?: "Error",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.error
                    ),
                )
            } }
        } else null,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
            // Removes the line below the textfield (Easy Solution)
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        )
    )
}


@ThemePreviews
@Composable
fun SimpleTextFieldPreview() {
    AppTextField(
        value = "",
        onValueChange = {},
        label = "Username",
    )
}

@ThemePreviews
@Composable
fun ErrorTextFieldPreview() {
    AppTextField(
        value = "",
        onValueChange = {},
        isError = true,
        label = "Username",
    )
}