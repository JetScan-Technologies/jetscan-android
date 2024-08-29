package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentTypeSelector(
    documentType: DocumentType,
    onDocumentTypeChanged: (DocumentType) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = documentType.ordinal,
    ) {
        DocumentType.entries.forEachIndexed { index, type ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = { onDocumentTypeChanged(type) })
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = type.toFormattedString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }

}
