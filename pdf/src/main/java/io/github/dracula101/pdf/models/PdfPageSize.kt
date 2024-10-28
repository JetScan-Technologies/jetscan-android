package io.github.dracula101.pdf.models

import android.util.Size


enum class PdfPageSize {
    AUTO,
    A1,
    A2,
    A3,
    A4,
    B3,
    B4,
    B5,
    LETTER,
    NOTE;

    override fun toString(): String {
        return when(this) {
            AUTO -> "Auto"
            A1 -> "A1"
            A2 -> "A2"
            A3 -> "A3"
            A4 -> "A4"
            B3 -> "B3"
            B4 -> "B4"
            B5 -> "B5"
            LETTER -> "Letter"
            NOTE -> "Note"
        }
    }

    val width: Float
        get() = toSize().width.toFloat()

    val height: Float
        get() = toSize().height.toFloat()

    fun toSize(): Size {
        return when(this) {
            AUTO -> Size(0, 0)
            A1 -> Size(1684, 2384)
            A2 -> Size(1191, 1684)
            A3 -> Size(842, 1191)
            A4 -> Size(595, 842)
            B3 -> Size(1417, 2004)
            B4 -> Size(1001, 1417)
            B5 -> Size(709, 1001)
            LETTER -> Size(612, 792)
            NOTE -> Size(540, 720)
        }
    }
}