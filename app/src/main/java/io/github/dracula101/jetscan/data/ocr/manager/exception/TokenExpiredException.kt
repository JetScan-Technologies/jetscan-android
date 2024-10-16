package io.github.dracula101.jetscan.data.ocr.manager.exception

/**
 * Exception thrown when the token has expired.
 */
class TokenExpiredException : Exception() {

    override val message: String
        get() = "Token has expired."

    override fun toString(): String {
        return "TokenExpiredException: Token has expired."
    }

}