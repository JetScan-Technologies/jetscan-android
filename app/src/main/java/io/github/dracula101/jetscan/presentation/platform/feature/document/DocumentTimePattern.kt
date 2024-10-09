package io.github.dracula101.jetscan.presentation.platform.feature.document

import android.text.format.DateFormat
import io.github.dracula101.jetscan.data.document.models.IllegalFilenameChar
import java.util.Date

enum class DocumentTimePattern {
    DEFAULT,
    HH_MM_SS,
    HH_MM;

    override fun toString(): String {
        return when(this){
            DEFAULT -> "Default"
            HH_MM_SS -> "hh:mm:ss"
            HH_MM -> "hh:mm"
        }
    }

    fun format(time: Date): String {
        val pattern = when(this){
            DEFAULT -> "hh:mm a"
            HH_MM_SS -> "HH:mm:ss"
            HH_MM -> "HH:mm"
        }
        return DateFormat.format(pattern, time).toString()
            .replace("AM", "am")
            .replace("PM", "pm")
    }
}