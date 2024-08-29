package io.github.dracula101.jetscan.presentation.features.home.files_view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.features.home.files_view.HomeFilesState
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem

@Composable
fun FileTopbar(
    state: HomeFilesState,
    mainHomeState: MainHomeState,
    onFolderShowAlert: ()->Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    Row (
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.Transparent
                    ),
                    startY = 120f,
                )
            )
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            "Total: ${state.documents.size + state.folders.size} Files",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal
        )
        Spacer(
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { isMenuExpanded.value = true },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = "Filter",
                modifier = Modifier.size(28.dp)
            )
            AppDropDown(
                expanded = isMenuExpanded.value,
                offset = DpOffset(20.dp, 0.dp),
                items = listOf(
                    MenuItem(
                        "Sort by Name",
                        icon = Icons.Rounded.SortByAlpha,
                        onClick = {  }
                    ),
                    MenuItem(
                        "Sort by Date",
                        icon = Icons.Rounded.DateRange,
                        onClick = {  }
                    ),
                    MenuItem(
                        "Sort by Size",
                        icon = Icons.Rounded.PhotoSizeSelectLarge,
                        onClick = {  }
                    ),
                ),
                onDismissRequest = { isMenuExpanded.value = false }
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        IconButton(
            onClick = { onFolderShowAlert() }
        ) {
            Icon(
                imageVector = Icons.Rounded.CreateNewFolder,
                contentDescription = "Create new Folder",
                modifier = Modifier.size(28.dp)
            )
        }
    }

}

