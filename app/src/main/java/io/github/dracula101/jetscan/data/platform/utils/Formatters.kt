package io.github.dracula101.jetscan.data.platform.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {

    fun getReadableDate(date: Long): String {
        val currentDate = System.currentTimeMillis()
        val diff = currentDate - date
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30
        val years = months / 12
        return when {
            years > 0 -> "$years years ago"
            months > 0 -> "$months months ago"
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> {
                if (seconds < 30) {
                    "Just now"
                } else {
                    "$seconds seconds ago"
                }
            }
        }
    }

    fun formatCurrentDate(): String {
        // dd/MM/yyyy h:mm:ss a
        val date = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy h:mm:ss a", Locale.getDefault())
        val netDate = java.util.Date(date)
        return sdf.format(netDate)
    }

}