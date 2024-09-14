package io.github.dracula101.jetscan.presentation.platform.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


data class SegmentedItem(
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun SegmentedButton(
    items: List<SegmentedItem>,
    modifier: Modifier = Modifier,
    defaultSelectedItemIndex: Int = 0,
    cornerRadius: Int = 36,
) {
    val selectedIndex = remember { mutableStateOf(defaultSelectedItemIndex) }
    val itemIndex = remember { mutableStateOf(defaultSelectedItemIndex) }
    Card(
        modifier = modifier
            .height(38.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedIndex.value == itemIndex.value) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.secondary
            }
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            horizontalArrangement = Arrangement.Center
        ) {
            items.forEachIndexed { index, item ->
                Card(
                    modifier = modifier
                        .padding(horizontal = 4.dp),
                    onClick = {
                        selectedIndex.value = index
                        item.onClick()
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedIndex.value == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                    ),
                    shape = RoundedCornerShape(cornerRadius),
                ) {
                    Text(
                        text = item.title,
                        color = if (selectedIndex.value == index)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }

}