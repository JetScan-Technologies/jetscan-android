package io.github.dracula101.pdf.models

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
}