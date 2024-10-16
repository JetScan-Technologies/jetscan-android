package io.github.dracula101.jetscan.data.ocr.repository.exception

class OcrNetworkException : Exception() {
    override val message: String?
        get() = "Network error occurred"
}