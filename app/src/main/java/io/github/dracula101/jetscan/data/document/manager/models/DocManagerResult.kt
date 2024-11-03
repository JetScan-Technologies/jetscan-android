package io.github.dracula101.jetscan.data.document.manager.models

sealed class DocManagerResult<out R> {

    data class Success<R>(val data: R) : DocManagerResult<R>()

    data class Error(
        val message: String,
        val error: Exception? = null,
        val type: DocManagerErrorType,
    ) : DocManagerResult<Nothing>()

}

enum class DocManagerErrorType {
    UNKNOWN,
    IO_EXCEPTION,
    FILE_NOT_CREATED,
    FILE_NOT_UPDATED,
    INVALID_EXTENSION,
}