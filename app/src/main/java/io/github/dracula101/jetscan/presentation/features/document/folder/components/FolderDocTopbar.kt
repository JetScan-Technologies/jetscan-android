package io.github.dracula101.jetscan.presentation.features.document.folder.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDocTopbar(
    onBack: () -> Unit,
    folder: DocumentFolder?,
    onMenuOptionClick: (MenuOption) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val menuExpandedState = remember { mutableStateOf(false) }
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
            }
        },
        title = {
            Text(text = folder?.name ?: "Folder")
        },
        actions = {
            AppDropDown(
                expanded = menuExpandedState.value,
                offset = DpOffset(60.dp, -(20).dp),
                onDismissRequest = {
                    menuExpandedState.value = false
                },
                items = MenuOption.entries.map { option ->
                    MenuItem(
                        title = option.toString(),
                        icon = option.toImageVector(),
                        onClick = {
                            onMenuOptionClick(option)
                            menuExpandedState.value = false
                        }
                    )
                }
            )
            CircleButton(
                onClick = {
                    menuExpandedState.value = true
                },
            )
            Spacer(modifier = Modifier.padding(6.dp))
        }
    )
}

enum class MenuOption {
    IMPORT_PDF,
    SELECT,
    CREATE_FOLDER;

    override fun toString(): String {
        return when (this) {
            IMPORT_PDF -> "Import PDF"
            SELECT -> "Select"
            CREATE_FOLDER -> "Create Folder"
        }
    }

    fun toImageVector(): ImageVector {
        return when (this) {
            IMPORT_PDF -> Icons.Rounded.FileDownload
            SELECT -> Icons.Rounded.CheckCircleOutline
            CREATE_FOLDER -> Icons.Rounded.CreateNewFolder
        }
    }
}
