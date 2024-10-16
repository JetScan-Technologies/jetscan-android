package io.github.dracula101.jetscan.data.ocr.service

interface JwtTokenService {

    fun getJwtToken(
        expiryTime: Long = 60 * 60 * 1000
    ): String

}