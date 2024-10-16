package io.github.dracula101.jetscan.data.platform.manager.file_action

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import java.io.File

interface FileActionManager {

    fun saveFileIntent(
        uri: Uri,
        title: String
    ): Intent

    @SuppressLint("ComposableNaming")
    @Composable
    fun saveFileWithLauncher(
        uri: () -> Uri
    ) : ManagedActivityResultLauncher<Intent, ActivityResult>

    fun shareFile(
        uri: Uri,
        title: String,
        subject: String,
        onActivityNotFound: () -> Unit
    )

    fun shareToApp(
        uri: Uri,
        packageName: String,
        title: String,
        subject: String,
        onActivityNotFound: () -> Unit
    )

    fun shareToGDrive(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    )

    fun shareToWhatsapp(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    )

    fun shareToEmail(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    )

    fun shareToPrinter(
        file: File,
        subject: String,
        activityNotFound: () -> Unit
    )
}