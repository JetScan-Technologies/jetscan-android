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
    TIMEOUT_EXCEEDED;

    override fun toString(): String {
        return when (this) {
            UNKNOWN -> "Unknown error"
            DUPLICATE_DOCUMENT -> "Document already exists"
            DB_EXCEPTION -> "Database exception"
            INVALID_FOLDER -> "Invalid folder"
            INVALID_DOCUMENT -> "Invalid document"
            SECURITY_EXCEPTION -> "Security exception"
            FOLDER_NOT_FOUND -> "Folder not found"
            DOCUMENT_NOT_FOUND -> "Document not found"
            TIMEOUT_EXCEEDED -> "Timeout exceeded"
        }
    }

    fun toCode() : String {
        return when (this) {
            UNKNOWN -> "UNKNOWN"
            DUPLICATE_DOCUMENT -> "DUPLICATE_DOCUMENT"
            DB_EXCEPTION -> "DB_EXCEPTION"
            INVALID_FOLDER -> "INVALID_FOLDER"
            INVALID_DOCUMENT -> "INVALID_DOCUMENT"
            SECURITY_EXCEPTION -> "SECURITY_EXCEPTION"
            FOLDER_NOT_FOUND -> "FOLDER_NOT_FOUND"
            DOCUMENT_NOT_FOUND -> "DOCUMENT_NOT_FOUND"
            TIMEOUT_EXCEEDED -> "TIMEOUT_EXCEEDED"
        }
    }
}