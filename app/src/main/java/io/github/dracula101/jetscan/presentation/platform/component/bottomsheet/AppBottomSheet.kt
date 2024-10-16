package io.github.dracula101.jetscan.presentation.platform.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    containerColor: Color =  BottomSheetDefaults.ContainerColor,
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(
        securePolicy = SecureFlagPolicy.SecureOn,
        isFocusable = true,
        shouldDismissOnBackPress = true,
    ),
    content: @Composable (ColumnScope) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        ),
        containerColor = containerColor,
        properties = properties,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
               Box(
                 modifier = Modifier
                     .clip(RoundedCornerShape(2.dp))
                     .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                     .width(40.dp)
                     .height(4.dp)
               )
            }
        },
        content = content,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    containerColor: Color =  BottomSheetDefaults.ContainerColor,
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(
        securePolicy = SecureFlagPolicy.SecureOn,
        isFocusable = true,
        shouldDismissOnBackPress = true,
    ),
    content: @Composable (ColumnScope) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        properties = properties,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        .width(40.dp)
                        .height(4.dp)
                )
            }
        },
        content = content,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
        ),
        modifier = modifier
    )
}