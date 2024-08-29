package io.github.dracula101.jetscan.data.document.models.extensions

import android.os.Build
import io.github.dracula101.jetscan.data.document.models.doc.Document
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow


fun Document.formatDate(time: Long): String {
    val calendar = Calendar.getInstance()
    val date = Date(time)
    val year = calendar.get(Calendar.YEAR)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val instant = Instant.ofEpochMilli(time)
        val formatter =
            DateTimeFormatter.ofPattern("dd MMM ${if (date.year == year) "" else "yyyy"}")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } else {
        val formatter = SimpleDateFormat(
            "dd MMM ${if (date.year == year) "" else "yyyy"}",
            Locale.getDefault()
        )
        val formattedDate = formatter.format(date)
        formattedDate
    }

}

fun Document.formatDateTime(time: Long): String {
    val calendar = Calendar.getInstance()
    val date = Date(time)
    val year = calendar.get(Calendar.YEAR)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val instant = Instant.ofEpochMilli(time)
        val formatter =
            DateTimeFormatter.ofPattern("hh:mm a dd MMM ${if (date.year == year) "" else "yyyy"}")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } else {
        val formatter = SimpleDateFormat(
            "hh:mm a dd MMM ${if (date.year == year) "" else "yyyy"}",
            Locale.getDefault()
        )
        val formattedDate = formatter.format(date)
        formattedDate
    }
}

private fun getReadableSizeUnit(digitGroups: Int): String {
    return when (digitGroups) {
        0 -> "B"
        1 -> "KB"
        2 -> "MB"
        3 -> "GB"
        4 -> "TB"
        5 -> "PB"
        6 -> "EB"
        7 -> "ZB"
        8 -> "YB"
        else -> "Unknown"
    }
}

fun Document.getReadableFileSize(length: Long): String {
    val size = length.toDouble()
    val sizeUnit = 1000
    if (size <= 0) {
        return "0"
    }
    val digitGroups =
        (kotlin.math.log10(size) / kotlin.math.log10(sizeUnit.toDouble())).toInt()
    return DecimalFormat("#,##0.##").format(
        size / sizeUnit.toDouble().pow(digitGroups.toDouble())
    ) + " " + getReadableSizeUnit(digitGroups)
}
