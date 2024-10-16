package io.github.dracula101.jetscan.data.ocr.repository.exception

import java.lang.Exception

class TokenNotGeneratedException: Exception() {

    override fun toString(): String {
        return "Token not generated"
    }

}