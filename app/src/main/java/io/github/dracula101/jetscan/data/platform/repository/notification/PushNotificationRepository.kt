package io.github.dracula101.jetscan.data.platform.repository.notification

interface PushNotificationRepository {

    fun onMessageReceived(
        title: String,
        body: String,
        data: Map<String, String>,
    )

    fun removeAllNotifications()

    fun onNewToken(token: String)

}