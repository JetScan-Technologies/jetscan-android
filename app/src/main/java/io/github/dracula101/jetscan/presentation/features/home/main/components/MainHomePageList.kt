package io.github.dracula101.jetscan.presentation.features.home.main.components


import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import kotlinx.parcelize.Parcelize

data class HomePageActionsItem(
    val icon: Int,
    val title: String,
    val color: Color,
    val item: MainHomeSubPage
)

enum class MainHomeSubPage {
    QR_CODE,
    WATERMARK,
    ESIGN_PDF,
    SPLIT_PDF,
    MERGE_PDF,
    PROTECT_PDF,
    COMPRESS_PDF,
    ALL_TOOLS
}

@Parcelize
data class PdfActionPage(
    val page: MainHomeSubPage,
    val document: Document? = null,
) : Parcelable

val homePageActionsItems = listOf(
    // HomePageActionsItem(
    //     icon = R.drawable.qr_code,
    //     title = "QR Code",
    //     color = MainComponentListColor.qr_code,
    //     item = MainHomeSubPage.QR_CODE
    // ),
    // HomePageActionsItem(
    //     icon = R.drawable.watermark_pdf,
    //     title = "Watermark",
    //     color = MainComponentListColor.watermark,
    //     item = MainHomeSubPage.WATERMARK
    // ),
    // HomePageActionsItem(
    //     icon = R.drawable.esign_pdf,
    //     title = "eSign PDF",
    //     color = MainComponentListColor.esign_pdf,
    //     item = MainHomeSubPage.ESIGN_PDF
    // ),
    HomePageActionsItem(
        icon = R.drawable.split_pdf,
        title = "Split",
        color = MainComponentListColor.split_pdf,
        item = MainHomeSubPage.SPLIT_PDF
    ),
    HomePageActionsItem(
        icon = R.drawable.merge_pdf,
        title = "Merge",
        color = MainComponentListColor.merge_pdf,
        item = MainHomeSubPage.MERGE_PDF
    ),
    HomePageActionsItem(
        icon = R.drawable.protect_pdf,
        title = "Protect",
        color = MainComponentListColor.protect_pdf,
        item = MainHomeSubPage.PROTECT_PDF
    ),
    HomePageActionsItem(
        icon = R.drawable.compress_pdf,
        title = "Compress",
        color = MainComponentListColor.compress_pdf,
        item = MainHomeSubPage.COMPRESS_PDF
    ),
    // HomePageActionsItem(
    //     icon = R.drawable.all_tools,
    //     title = "All Tools",
    //     color = MainComponentListColor.all_tools,
    //     item = MainHomeSubPage.ALL_TOOLS
    // ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHomePageComponent(
    viewModel: MainHomeViewModel,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    if (isExpanded) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.4f),
        ) {
            items(homePageActionsItems.size) { index ->
                MainHomeExpandedComponentList(
                    icon = painterResource(id = homePageActionsItems[index].icon),
                    title = homePageActionsItems[index].title,
                    color = homePageActionsItems[index].color,
                    onClick = {
                        viewModel.trySendAction(MainHomeAction.MainHomeNavigate(homePageActionsItems[index].item))
                    }
                )
            }
        }
    }else {
        Text(
            "Pdf Tools",
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ){
            List(homePageActionsItems.size) { index ->
                MainHomeComponentList(
                    icon = painterResource(id = homePageActionsItems[index].icon),
                    title = homePageActionsItems[index].title,
                    color = homePageActionsItems[index].color,
                    onClick = {
                        viewModel.trySendAction(MainHomeAction.MainHomeNavigate(homePageActionsItems[index].item))
                    },
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }
    }

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