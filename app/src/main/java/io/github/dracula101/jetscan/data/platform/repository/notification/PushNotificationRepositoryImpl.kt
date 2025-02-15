package io.github.dracula101.jetscan.data.platform.repository.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.MainActivity
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PushNotificationRepositoryImpl(
    @ApplicationContext private val context: Context,
    private val authDiskSource: AuthDiskSource,
) : PushNotificationRepository {

    private val notificationManager: NotificationManagerCompat by lazy { NotificationManagerCompat.from(context) }
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(title: String, body: String, data: Map<String, String>) {
        try {
            buildNotificationChannel()
            if (!notificationManager.areNotificationsEnabled(NOTIFICATION_CHANNEL_ID)) {
                Timber.w("Notifications are disabled for channel: $NOTIFICATION_CHANNEL_ID")
                return
            }
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val appIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, appIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(body)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(NOTIFICATION_DEFAULT_TIMEOUT_MILLIS)
                .setSound(soundUri)
                .build()
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Timber.e(e, "Failed to display notification")
        }
    }

    override fun removeAllNotifications() {
        notificationManager.cancelAll()
    }

    override fun onNewToken(token: String) {
        scope.launch {
            authDiskSource.saveFirebaseToken(token)
        }
    }

    private fun buildNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(context, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun NotificationManagerCompat.areNotificationsEnabled(
        channelId: String,
    ): Boolean = areNotificationsEnabled() && isChannelEnabled(channelId)

    private fun NotificationManagerCompat.isChannelEnabled(
        channelId: String,
    ): Boolean = getChannelImportance(channelId) != NotificationManagerCompat.IMPORTANCE_NONE

    private fun NotificationManagerCompat.getChannelImportance(
        channelId: String,
    ): Int = this
        .getNotificationChannelCompat(channelId)
        ?.importance
        ?: NotificationManagerCompat.IMPORTANCE_DEFAULT
}

private const val NOTIFICATION_CHANNEL_ID: String = "jetscan_notification_channel"
private const val NOTIFICATION_CHANNEL_NAME: String = "General Notification"
private const val NOTIFICATION_ID: Int = 2_6072_022
private const val NOTIFICATION_REQUEST_CODE: Int = 20220801
private const val NOTIFICATION_DEFAULT_TIMEOUT_MILLIS: Long = 15L * 60L * 1_000L