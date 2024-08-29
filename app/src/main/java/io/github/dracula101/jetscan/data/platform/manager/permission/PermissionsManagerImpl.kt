package io.github.dracula101.jetscan.data.platform.manager.permission


import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

/**
 * Primary implementation of [PermissionsManager].
 */
class PermissionsManagerImpl(
    private val activity: Activity,
) : PermissionsManager {

    @Composable
    override fun getLauncher(
        onResult: (Boolean) -> Unit,
    ): ManagedActivityResultLauncher<String, Boolean> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = onResult,
        )

    @Composable
    override fun getPermissionsLauncher(
        onResult: (Map<String, Boolean>) -> Unit,
    ): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = onResult,
        )

    override fun checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED

    override fun checkPermissions(permissions: Array<String>): Boolean =
        permissions.map { checkPermission(it) }.all { isGranted -> isGranted }

    override fun shouldShouldRequestPermissionRationale(
        permission: String,
    ): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
}
