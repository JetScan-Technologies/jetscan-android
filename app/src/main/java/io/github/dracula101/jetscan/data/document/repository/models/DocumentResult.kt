package io.github.dracula101.jetscan.data.document.repository.models

sealed class DocumentResult<out T> {

    data class Success<T>(val data: T? = null) : DocumentResult<T>()

    data class Error(
        val message: String,
        val error: Exception? = null,
        val type: DocumentErrorType,
    ) : DocumentResult<Nothing>()

}

enum class DocumentErrorType {
    UNKNOWN,
    DUPLICATE_DOCUMENT,
    DB_EXCEPTION,
    INVALID_FOLDER,
    INVALID_DOCUMENT,
    SECURITY_EXCEPTION,
    FOLDER_NOT_FOUND,
    DOCUMENT_NOT_FOUND,
}