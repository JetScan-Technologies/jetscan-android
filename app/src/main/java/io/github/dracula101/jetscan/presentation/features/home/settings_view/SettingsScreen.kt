package io.github.dracula101.jetscan.presentation.features.home.settings_view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NoAccounts
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import io.github.dracula101.jetscan.data.auth.model.UserState
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.settings_view.components.SettingsItem
import io.github.dracula101.jetscan.presentation.features.home.settings_view.components.SettingsSection
import io.github.dracula101.jetscan.presentation.features.settings.document.DocumentSettingScreen
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import io.github.dracula101.jetscan.presentation.platform.component.switch.AppSwitch
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSize: ScaffoldSize,
    viewModel: SettingsViewModel,
    padding: PaddingValues,
    mainHomeState: MainHomeState,
    onNavigateToAboutPage: () -> Unit,
    onNavigateToDocumentSettings: (DocumentSettingScreen) -> Unit,
) {
    val context = LocalContext.current
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState()
    val layoutDirection = LocalLayoutDirection.current

    state.value.bottomSheetState?.let {
        SettingsBottomSheet(
            state = it,
            bottomSheetState = bottomSheetState,
            viewModel = viewModel
        )
    }
    LazyColumn(
        modifier = Modifier
            .padding(
                top = padding.calculateTopPadding(),
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
            ),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        if (state.value.user?.displayName != "") {
            item {
                state.value.user?.let { AccountInfo(it) }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            }
        }
        item{
            SettingsSection(
                title = "Account"
            ) {
                SettingsItem(
                    title = "Edit Profile",
                    icon = Icons.Rounded.Person,
                    onClick = {}
                )
                SettingsItem(
                    title = "Delete Account",
                    icon = Icons.Rounded.NoAccounts,
                    onClick = {},
                    showDivider = false
                )
            }
        }
        item{
            SettingsSection(
                title = "Preferences"
            ) {
                SettingsItem(
                    title = "Language",
                    icon = Icons.Rounded.Language,
                    trailing = {
                        Text(
                            text = "English (US)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        Toast.makeText(
                            context,
                            "Other Languages are coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                SettingsItem(
                    title = "Dark Mode",
                    icon = Icons.Rounded.DarkMode,
                    trailing = {
                        AppSwitch(
                            checked = state.value.isDarkTheme,
                            onCheckedChange = {
                                viewModel.trySendAction(SettingsAction.Ui.ChangeTheme)
                            }
                        )
                    },
                    showDivider = false
                )
            }
        }
        item{
            SettingsSection(
                title = "Document"
            ) {
                DocumentSettingScreen.entries.forEach { screen ->
                    SettingsItem(
                        title = screen.toString(),
                        icon = screen.toIcon(),
                        onClick = {
                            onNavigateToDocumentSettings(screen)
                        },
                        showDivider = screen != DocumentSettingScreen.entries.last()
                    )
                }
            }
        }
        item{
            SettingsSection(
                title = "Help"
            ) {
                SettingsItem(
                    title = "Help",
                    icon = Icons.Rounded.Description,
                    onClick = {}
                )
                SettingsItem(
                    title = "About JetScan",
                    icon = Icons.Outlined.Info,
                    onClick = {
                        onNavigateToAboutPage()
                    },
                    showDivider = false
                )
            }
        }
        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        MaterialTheme.shapes.large
                    )
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            ){
                SettingsItem(
                    title = "Logout",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        viewModel.trySendAction(SettingsAction.Alerts.ShowLogoutConfirmation)
                    },
                    showDivider = false
                )
            }
        }
        item {
            Spacer(modifier = Modifier.size(120.dp))
        }
    }
}

@Composable
fun AccountInfo(user: UserState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .customContainer(shape = MaterialTheme.shapes.large)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = user.photoUrl,
                error = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                contentDescription = "User Photo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: SettingsBottomSheetState,
    bottomSheetState: SheetState,
    viewModel: SettingsViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
            coroutineScope.launch {
                bottomSheetState.hide()
                viewModel.trySendAction(SettingsAction.Alerts.DismissAlert(bottomSheet = true))
            }
        },
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .size(40.dp, 4.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
            }
        }
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Are you sure you want to logout?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                            viewModel.trySendAction(SettingsAction.Alerts.DismissAlert(bottomSheet = true))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.trySendAction(SettingsAction.Ui.Logout)
                            delay(1000L)
                            bottomSheetState.hide()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}