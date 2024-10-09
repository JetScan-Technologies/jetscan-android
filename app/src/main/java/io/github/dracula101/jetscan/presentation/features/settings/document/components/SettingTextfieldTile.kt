package io.github.dracula101.jetscan.presentation.features.settings.document.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.switch.AppSwitch
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField

@Composable
fun SettingTextfieldTile(
    title: String,
    labelText: String,
    leading: (@Composable () -> Unit)? = null,
    value: String? = null,
    onValueChange: (String) -> Unit,
    hideDivider: Boolean = false
) {
    val isDialogOpen = remember { mutableStateOf(false) }
    val tempValue = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isDialogOpen.value) {
        if (isDialogOpen.value) {
            focusRequester.requestFocus()
            tempValue.value = value ?: ""
        }
    }
    if(isDialogOpen.value){
        AppBasicDialog(
            title = title,
            onDismiss = {
                isDialogOpen.value = false
                onValueChange(tempValue.value)
            },
        ){
            Column {
                AppTextField(
                    value = tempValue.value,
                    onValueChange = { screen ->
                        tempValue.value = screen
                    },
                    label = labelText,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            isDialogOpen.value = false
                            onValueChange(tempValue.value)
                        }
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        isDialogOpen.value = false
                        onValueChange(tempValue.value)
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Text(
                        text = "Done",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .clickable {
                isDialogOpen.value = true
            }
            .fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if(value != null && value != "") value else labelText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        if (!hideDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            )
        }
    }
}