package io.github.dracula101.jetscan.presentation.features.home.main.components


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.extensions.formatDate
import io.github.dracula101.jetscan.data.document.models.extensions.getReadableFileSize
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.document.preview.PDFIcon
import io.github.dracula101.jetscan.presentation.platform.component.document.preview.PreviewIcon
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.fadingEdge

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentItem(
    document: Document,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    ui: DocumentItemUI = DocumentItemUI.Compact(),
) {
    val isChildrenShown = remember { mutableStateOf(false) }
    val isMenuOpen = remember { mutableStateOf(false) }
    val animationSpec = TweenSpec<Float>(
        durationMillis = 1000,
        delay = 0,
        easing = LinearOutSlowInEasing
    )
    val degreeAnimation = animateFloatAsState(
        targetValue = if (!isChildrenShown.value) -90f else -270f,
        animationSpec = animationSpec,
        label = "Arrow Animation"
    )
    val opacityAnimation = animateFloatAsState(
        targetValue = if (!isChildrenShown.value) 0f else 1f,
        animationSpec = animationSpec,
        label = "Opacity Animation"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .customContainer(
                shape = MaterialTheme.shapes.large
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        when(ui){
            is DocumentItemUI.Vertical -> {
                Column(
                    modifier = Modifier
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    document.previewImageUri?.let { uri->
                        PreviewIcon(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .padding(horizontal = 12.dp)
                                .aspectRatio(3 / 4f)
                                .clip(MaterialTheme.shapes.medium),
                            uri = uri,
                        )
                        Box(
                            modifier = Modifier
                                .height(
                                    MaterialTheme.typography.titleSmall.fontSize.value.dp * 2
                                )
                        ){
                            Text(
                                text = document.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center,
                                softWrap = true
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .then(
                            if (ui is DocumentItemUI.Expanded) Modifier
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        document.previewImageUri?.let { uri->
                            PreviewIcon(
                                modifier = Modifier
                                    .aspectRatio(3 / 4f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .weight(0.55f),
                                uri = uri,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(2.35f)
                        ) {
                            DocumentInfo(document)
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                        ) {
                            when(ui){
                                is DocumentItemUI.Compact -> {
                                    CircleButton(
                                        onClick = ui.onDetailClicked
                                    )
                                }
                                is DocumentItemUI.Expanded -> {
                                    IconButton(
                                        onClick = {
                                            isChildrenShown.value = !isChildrenShown.value
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.ArrowBackIosNew,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(degreeAnimation.value),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    if (isChildrenShown.value && (ui is DocumentItemUI.Expanded)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Row {
                            PreviewImageSlider(opacityAnimation.value, document)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AppDropDown(
                                    expanded = isMenuOpen.value,
                                    offset = DpOffset(-(120).dp, -(50).dp),
                                    onDismissRequest = {
                                        isMenuOpen.value = false
                                    },
                                    modifier = Modifier
                                        .width(120.dp),
                                    items = listOf(
                                        MenuItem(
                                            title = "Delete",
                                            icon = Icons.Rounded.Delete,
                                            onClick = {
                                                ui.deleteClicked()
                                            }
                                        ),
                                        MenuItem(
                                            title = "Share",
                                            icon = Icons.Rounded.IosShare,
                                            onClick = {
                                                ui.onShareClicked()
                                            }
                                        )
                                    )
                                )
                                CircleButton(
                                    onClick = {
                                        isMenuOpen.value = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun PreviewImageSlider(
    opacityAnimation: Float,
    document: Document
) {
    LazyRow(
        state = rememberLazyListState(),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .height(65.dp)
            .fillMaxWidth(0.85f)
            .padding(bottom = 4.dp)
            .alpha(opacityAnimation)
            .fadingEdge(
                Brush.horizontalGradient(
                    0.85f to MaterialTheme.colorScheme.surface,
                    1f to Color.Transparent,
                )
            ),
        contentPadding = PaddingValues(top = 8.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(document.scannedImages.size) {
            PDFIcon(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(3 / 4f),
                uri = document.scannedImages[it].scannedUri
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Pages: ${document.scannedImages.size}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Size: ${document.scannedImages.firstOrNull()?.width}x${document.scannedImages.firstOrNull()?.height}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

    }
}

@Composable
private fun DocumentInfo(document: Document) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = document.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Visible,
            softWrap = true
        )
        Row(
            modifier = Modifier.alpha(0.75f)
        ) {
            Text(
                text = document.getReadableFileSize(document.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier.padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = document.formatDate(document.dateCreated),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

sealed class DocumentItemUI {
    data class Expanded(
        val onShareClicked: () -> Unit = {},
        val deleteClicked: () -> Unit = {},
    ) : DocumentItemUI()

    data class Compact(
        val onDetailClicked: () -> Unit = {},
    ) : DocumentItemUI()

    data object Vertical : DocumentItemUI()
}