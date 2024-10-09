package io.github.dracula101.jetscan.presentation.platform.feature.document

import android.text.format.DateFormat
import io.github.dracula101.jetscan.data.document.models.IllegalFilenameChar
import java.util.Date

enum class DocumentDatePattern {
    SHORT_DATE,
    MEDIUM_DATE,
    LONG_DATE,
    DD_MM_YYYY,
    MM_DD_YYYY,
    YYYY_MM_DD;

    override fun toString(): String {
        return when(this){
            DD_MM_YYYY -> "dd-mm-yyyy"
            MM_DD_YYYY -> "mm-dd-yyyy"
            YYYY_MM_DD -> "yyyy-mm-dd"
            SHORT_DATE -> "Short Date"
            MEDIUM_DATE -> "Medium Date"
            LONG_DATE -> "Long Date"
        }
    }

    fun format(date: Date): String {
        val pattern = when(this){
            DD_MM_YYYY -> "dd-MM-yyyy"
            MM_DD_YYYY -> "MM-dd-yyyy"
            YYYY_MM_DD -> "yyyy-MM-dd"
            SHORT_DATE -> "dd-MM-yy"
            MEDIUM_DATE -> "dd MMM yyyy"
            LONG_DATE -> "dd MMMM yyyy"
        }
        return DateFormat.format(pattern, date)
            .toString()
    }
}