package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document


enum class DocumentType {
    ID_CARD,
    BAR_CODE,
    QR_CODE,
    DOCUMENT,
    PHOTO,
    BOOK,
    SIGNATURE,;

    fun toFormattedString(): String {
        return when (this) {
            ID_CARD -> "ID Card"
            BAR_CODE -> "Bar Code"
            QR_CODE -> "QR Code"
            DOCUMENT -> "Document"
            PHOTO -> "Photo"
            BOOK -> "Book"
            SIGNATURE -> "Signature"
        }
    }
}

