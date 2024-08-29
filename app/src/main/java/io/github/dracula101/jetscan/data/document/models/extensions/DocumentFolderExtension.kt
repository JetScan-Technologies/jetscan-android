package io.github.dracula101.jetscan.data.document.models.extensions

import android.text.format.DateFormat
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun DocumentFolder.formatTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

fun DocumentFolder.formatDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(date)
}

fun DocumentFolder.formatDateTime(time: Long): String {
    // 1 sec ago, 1 min ago, 1 hour ago, yesterday 2:40 am, 2 days ago 2:40 am, 1 week ago 2:40 am, 24 Dec 2021 2:40 am
    val date = Date(time)
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    return when {
        seconds < 60 -> "1 sec ago"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hour ago"
        days == 1L -> "yesterday ${formatTime(time)}"
        days < 7 -> "$days days ago ${formatTime(time)}"
        weeks == 1L -> "1 week ago ${formatTime(time)}"
        else -> DateFormat.format("dd MMM yyyy hh:mm a", date).toString()
    }
}