package io.github.dracula101.jetscan.presentation.features.home.main

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.presentation.features.home.files_view.HomeFilesScreen
import io.github.dracula101.jetscan.presentation.features.home.files_view.HomeFilesViewModel
import io.github.dracula101.jetscan.presentation.features.home.home_view.HomeScreen
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeTopAppbar
import io.github.dracula101.jetscan.presentation.features.home.main.components.PdfActionPage
import io.github.dracula101.jetscan.presentation.features.home.settings_view.SettingsScreen
import io.github.dracula101.jetscan.presentation.features.home.settings_view.SettingsViewModel
import io.github.dracula101.jetscan.presentation.features.home.subscription_view.SubscriptionViewModel
import io.github.dracula101.jetscan.presentation.features.settings.document.DocumentSettingScreen
import io.github.dracula101.jetscan.presentation.platform.component.dialog.ConfirmAlertDialog
import io.github.dracula101.jetscan.presentation.platform.component.dialog.IconAlertDialog
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showErrorSnackBar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showSuccessSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showWarningSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    mainViewModel: MainHomeViewModel = hiltViewModel(),
    filesViewModel: HomeFilesViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateDocument: (Document) -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    navigateTo: (PdfActionPage) -> Unit,
    onNavigateToFolder: (DocumentFolder) -> Unit = {},
    onNavigateToAboutPage: () -> Unit = {},
    onNavigateToDocumentSettings: (DocumentSettingScreen) -> Unit = {},
) {
    val state = mainViewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bottomBarVisibleAnimation = remember { Animatable(1f) }

    state.value.snackbarState?.let { snackbarState ->
        MainHomeAlertSnackbar(
            snackbarHostState = snackbarHostState,
            snackbarState = snackbarState,
            onDismiss = {
                mainViewModel.trySendAction(MainHomeAction.Ui.DismissSnackbar)
            }
        )
    }

    LaunchedEffect(state.value.navigateTo) {
        if (state.value.navigateTo != null) {
            navigateTo(state.value.navigateTo!!)
            mainViewModel.trySendAction(MainHomeAction.MainHomeClearNavigate)
        }
    }

    state.value.dialogState?.let { dialogState ->
        MainHomeDialog(
            dialogState = dialogState,
            onDismiss = {
                mainViewModel.trySendAction(MainHomeAction.Ui.DismissDialog)
            },
            importQuality = state.value.importQuality,
            onQualityChanged = { quality ->
                mainViewModel.trySendAction(MainHomeAction.Ui.ChangeImportQuality(quality))
            },
            onImportDocument = {
                mainViewModel.trySendAction(MainHomeAction.Ui.AddDocument)
            },
            onDocumentDelete = { document ->
                mainViewModel.trySendAction(MainHomeAction.Ui.DeleteDocument(document))
            }
        )
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrolledDown = delta > 0
                scope.launch {
                    if(
                        !bottomBarVisibleAnimation.isRunning &&
                        (isScrolledDown && bottomBarVisibleAnimation.value == 0f) ||
                        (!isScrolledDown && bottomBarVisibleAnimation.value == 1f)
                    ){
                        bottomBarVisibleAnimation.animateTo(if(isScrolledDown) 1f else 0f)
                    }
                }
                return Offset.Zero
            }
        }
    }

    JetScanScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(nestedScrollConnection),
        snackbarHostState = snackbarHostState,
        useImePadding = false,
        topBar = {
            MainHomeTopAppbar(
                scrollBehavior = scrollBehavior,
                onLogoutClicked = {
                    mainViewModel.trySendAction(MainHomeAction.Ui.Logout)
                },
                state = state.value,
            )
        },
        floatingActionButton = {
            if(state.value.currentTab != MainHomeTabs.SETTINGS) {
                MainHomeFloatingActionButton(
                    onClick = { onNavigateToScanner() },
                    isExtended = state.value.currentTab == MainHomeTabs.HOME,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = 0,
                                y = ((1 - bottomBarVisibleAnimation.value) * 250).toInt()
                            )
                        }
                )
            }
        },
        bottomBar = {
            MainHomeBottomBar(
                state = state.value,
                onTabSelected = { tab ->
                    mainViewModel.trySendAction(MainHomeAction.Ui.ChangeTab(tab))
                },
                horizontalModifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = ((1 - bottomBarVisibleAnimation.value) * 300).toInt()
                        )
                    }
            )
        }
    ) { padding, windowSize ->
        Row {
            if(windowSize != ScaffoldSize.COMPACT){
                MainHomeBottomBar(
                    state = state.value,
                    onTabSelected = { tab ->
                        mainViewModel.trySendAction(MainHomeAction.Ui.ChangeTab(tab))
                    },
                    isVertical = true,
                    verticalModifier  = Modifier
                        .padding(padding)
                        .padding(top = 16.dp)
                )
            }
            when(state.value.currentTab) {
                MainHomeTabs.HOME -> {
                    HomeScreen(
                        viewModel = mainViewModel,
                        windowSize = windowSize,
                        padding = padding,
                        onDocumentClick = onNavigateDocument,
                        onNavigateToPdfActions = { document, page ->
                            mainViewModel.trySendAction(
                                MainHomeAction.MainHomeNavigate(
                                    document = document,
                                    navigatePage = page,
                                )
                            )
                        },
                    )
                }
                MainHomeTabs.FILES -> {
                    HomeFilesScreen(
                        windowSize = windowSize,
                        padding = padding,
                        viewModel = filesViewModel,
                        mainHomeState = state.value,
                        onShowSnackbar = { snackbarState ->
                            mainViewModel.trySendAction(MainHomeAction.Ui.ShowSnackbar(snackbarState))
                        },
                        onDocumentClick = { document ->
                            onNavigateDocument(document)
                        },
                        onNavigateToFolder = { folder->
                            onNavigateToFolder(folder)
                        },
                        onNavigateToPdfActions = { document, page ->
                            mainViewModel.trySendAction(
                                MainHomeAction.MainHomeNavigate(
                                    document = document,
                                    navigatePage = page,
                                )
                            )
                        },
                    )
                }
//            MainHomeTabs.SUBSCRIPTION -> {
//                SubscriptionScreen(
//                    windowSize = windowSize,
//                    padding = padding,
//                    viewModel = subscriptionViewModel,
//                    mainHomeState = state.value
//                )
//            }
                MainHomeTabs.SETTINGS -> {
                    SettingsScreen(
                        windowSize = windowSize,
                        padding = padding,
                        viewModel = settingsViewModel,
                        mainHomeState = state.value,
                        onNavigateToAboutPage = onNavigateToAboutPage,
                        onNavigateToDocumentSettings = onNavigateToDocumentSettings
                    )
                }
            }
        }
    }
}


@Composable
fun MainHomeFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isExtended: Boolean = true,
) {

    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = "Scan Document",
            )
        },
        text = {
            Text(text = "Scan")
        },
        expanded = isExtended,
        modifier = modifier,
    )
}

@Composable
fun MainHomeBottomBar(
    horizontalModifier: Modifier = Modifier,
    verticalModifier: Modifier = Modifier,
    state: MainHomeState,
    isVertical: Boolean = false,
    onTabSelected: (MainHomeTabs) -> Unit = {},
) {
    if (isVertical) {
        Column(
            verticalModifier
                .fillMaxHeight()
                .widthIn(min = 80.dp)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainHomeNavRailItem(
                icons = Pair(
                    Icons.Rounded.Home,
                    Icons.Outlined.Home
                ),
                label = "Home",
                selected = state.currentTab == MainHomeTabs.HOME,
                onClick = { onTabSelected(MainHomeTabs.HOME) },
            )
            MainHomeNavRailItem(
                icons = Pair(
                    Icons.Rounded.Folder,
                    Icons.Outlined.FolderOpen
                ),
                label = "Files",
                selected = state.currentTab == MainHomeTabs.FILES,
                onClick = { onTabSelected(MainHomeTabs.FILES) },
            )
//            MainHomeNavRailItem(
//                icons = Pair(
//                    Icons.Rounded.Star,
//                    Icons.Outlined.StarOutline
//                ),
//                label = "Premium",
//                selected = state.currentTab == MainHomeTabs.SUBSCRIPTION,
//                onClick = { onTabSelected(MainHomeTabs.SUBSCRIPTION) },
//            )
            MainHomeNavRailItem(
                icons = Pair(
                    Icons.Rounded.Settings,
                    Icons.Outlined.Settings
                ),
                label = "Settings",
                selected = state.currentTab == MainHomeTabs.SETTINGS,
                onClick = { onTabSelected(MainHomeTabs.SETTINGS) },
            )
        }
    } else {
        BottomAppBar(
            modifier = horizontalModifier,
        ) {
            MainHomeNavbarItem(
                icons = Pair(
                    Icons.Rounded.Home,
                    Icons.Outlined.Home
                ),
                label = "Home",
                selected = state.currentTab == MainHomeTabs.HOME,
                onClick = { onTabSelected(MainHomeTabs.HOME) },
            )
            MainHomeNavbarItem(
                icons = Pair(
                    Icons.Rounded.Folder,
                    Icons.Outlined.FolderOpen
                ),
                label = "Files",
                selected = state.currentTab == MainHomeTabs.FILES,
                onClick = { onTabSelected(MainHomeTabs.FILES) },
            )
//            MainHomeNavbarItem(
//                icons = Pair(
//                    Icons.Rounded.Star,
//                    Icons.Outlined.StarOutline
//                ),
//                label = "Premium",
//                selected = state.currentTab == MainHomeTabs.SUBSCRIPTION,
//                onClick = { onTabSelected(MainHomeTabs.SUBSCRIPTION) },
//            )
            MainHomeNavbarItem(
                icons = Pair(
                    Icons.Rounded.Settings,
                    Icons.Outlined.Settings
                ),
                label = "Settings",
                selected = state.currentTab == MainHomeTabs.SETTINGS,
                onClick = { onTabSelected(MainHomeTabs.SETTINGS) },
            )
        }
    }
}

@Composable
fun RowScope.MainHomeNavbarItem(
    icons: Pair<ImageVector, ImageVector>,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (selected) icons.first else icons.second,
                contentDescription = label,
                modifier = Modifier.size(26.dp)
            )
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
fun ColumnScope.MainHomeNavRailItem(
    icons: Pair<ImageVector, ImageVector>,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationRailItem(
        icon = {
            Icon(
                if (selected) icons.first else icons.second,
                contentDescription = label
            )
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
fun MainHomeAlertSnackbar(
    snackbarHostState: SnackbarHostState,
    snackbarState: SnackbarState,
    onDismiss: () -> Unit = {},
) {
    LaunchedEffect(snackbarState) {
        when (snackbarState) {
            is SnackbarState.ShowSuccess -> {
                snackbarHostState.showSuccessSnackbar(
                    message = snackbarState.title,
                    onDismiss = onDismiss
                )
            }

            is SnackbarState.ShowWarning -> {
                snackbarHostState.showWarningSnackbar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = onDismiss
                )
            }

            is SnackbarState.ShowError -> {
                snackbarHostState.showErrorSnackBar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun MainHomeDialog(
    dialogState: MainHomeState.MainHomeDialogState,
    importQuality: ImageQuality,
    onDismiss: () -> Unit = {},
    onQualityChanged: (ImageQuality) -> Unit = {},
    onImportDocument: () -> Unit = {},
    onDocumentDelete: (Document) -> Unit = {},
) {
    when (dialogState) {
        is MainHomeState.MainHomeDialogState.ShowImportQuality -> {
            val selectedQuality = remember { mutableStateOf(importQuality) }
            ConfirmAlertDialog(
                title = "Select Import Quality",
                onDismiss = onDismiss,
                onConfirm = {
                    onDismiss()
                    onImportDocument()
                },
                onCancel = { onDismiss() },
            ) {
                key(selectedQuality.value) {
                    Column {
                        ImageQuality.entries
                            .map { quality ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedQuality.value == quality,
                                        modifier = Modifier
                                            .size(40.dp),
                                        onClick = {
                                            selectedQuality.value = quality
                                            onQualityChanged(quality)
                                        }
                                    )
                                    Text(
                                        text = quality.toFormattedString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                    }
                }
            }
        }

        is MainHomeState.MainHomeDialogState.ShowDeleteDocument -> {
            IconAlertDialog(
                icon = Icons.Rounded.DeleteForever,
                onDismiss = onDismiss,
                onConfirm = {
                    onDocumentDelete(dialogState.document)
                    onDismiss()
                },
            ) {
                Text("Delete Document")
                Text(
                    text = "Are you sure you want to delete this document?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        is MainHomeState.MainHomeDialogState.ShowPasswordDialog -> {
            val password = remember { mutableStateOf("") }
            IconAlertDialog(
                icon = Icons.Rounded.Lock,
                onDismiss = onDismiss,
                onConfirm = {
                    onDismiss()
                    dialogState.onPasswordEntered(password.value)
                },
            ) {
                Text(
                    "Password Protected",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please enter the password to import.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = "Password",
                    placeholder = {
                        Text("Enter Password")
                    },
                )
            }
        }
    }
}