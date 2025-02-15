package io.github.dracula101.jetscan.data.platform.repository.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class JetScanMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushNotificationRepository: PushNotificationRepository

    override fun onMessageReceived(message: RemoteMessage) {
        pushNotificationRepository.onMessageReceived(
            title = message.notification?.title ?: "",
            body = message.notification?.title ?: "",
            data = message.data
        )
    }

    override fun onNewToken(token: String) {
        pushNotificationRepository.onNewToken(token)
    }

    override fun onDeletedMessages() {
        // remove all notifications
        pushNotificationRepository.removeAllNotifications()
    }

}