package io.github.dracula101.jetscan.presentation.features.home.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeTabs
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeTopAppbar(
    scrollBehavior: TopAppBarScrollBehavior,
    onLogoutClicked: () -> Unit,
    state: MainHomeState,
    modifier : Modifier = Modifier
) {
    val currentTab = remember { mutableStateOf(MainHomeTabs.HOME) }
    LaunchedEffect(state.currentTab) {
        delay(200)
        currentTab.value = state.currentTab
    }

    JetScanTopAppbar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon_foreground),
                    contentDescription = "JetScan",
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(40.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                AnimatedContent(
                    targetState = state.currentTab,
                    transitionSpec = {
                        if ( targetState.toIndex() > currentTab.value.toIndex() ) {
                            slideInVertically { height -> height/3 } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height/3 } + fadeOut()
                        } else {
                            slideInVertically { height -> -height/3 } + fadeIn() togetherWith
                                    slideOutVertically { height -> height/3 } + fadeOut()
                        }.using(
                            SizeTransform(false)
                        )
                    },
                    label = "Tab Animation"
                ) { targetTab ->
                    Text(
                        text =  targetTab.toLabel(),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        actions = {
            // IconButton(
            //     onClick = {}
            // ) {
            //     Icon(
            //         Icons.Rounded.Search,
            //         modifier = Modifier
            //             .padding(8.dp)
            //             .size(28.dp),
            //         contentDescription = "Search",
            //     )
            // }
        }
    )
}