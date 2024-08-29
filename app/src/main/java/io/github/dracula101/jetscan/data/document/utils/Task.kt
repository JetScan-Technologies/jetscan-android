package io.github.dracula101.jetscan.data.document.utils

import java.util.UUID

sealed interface Task<T>  {
    data class Success<T>(val data: T) : Task<T>
    data class Error<T>(val error: Throwable, val message: String? = null) : Task<T>
    data class Cancelled<T>(val message: String? = null) : Task<T>
    data class Idle<T>(val processId: String = UUID.randomUUID().toString() ) : Task<T>
}