package io.github.dracula101.jetscan.data.ocr.service
import android.util.Base64
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.ocr.util.PemUtils
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class JwtTokenServiceImpl(
    private val pemUtils: PemUtils
) : JwtTokenService {

    override fun getJwtToken(expiryTime: Long): String {
        val header = base64UrlEncode("""{"alg":"RS256","typ":"JWT"}""")
        val claim = createClaim(expiryTime)

        val unsignedToken = "$header.$claim"
        val signature = sign(unsignedToken)

        return "$unsignedToken.$signature"
    }

    private fun createClaim(expiryTime: Long): String {
        val systemTime = System.currentTimeMillis()
        val expiry = systemTime + expiryTime
        val claim = """
            {
              "iss": "${BuildConfig.SERVICE_ACCOUNT_CLIENT_EMAIL}",
              "scope": "https://www.googleapis.com/auth/cloud-platform",
              "aud": "${BuildConfig.SERVICE_ACCOUNT_TOKEN_URI}",
              "iat": "${(systemTime/1000).toInt()}",
              "exp": "${(expiry/1000).toInt()}"
            }
        """.trimIndent()
        return base64UrlEncode(claim)
    }

    private fun sign(unsignedToken: String): String {
        return try {
            val privateKeyPemBase64Encoded = BuildConfig.SERVICE_ACCOUNT_PRIVATE_KEY_ID_BASE64_ENCODED
            val privateKeyPem = String(Base64.decode(privateKeyPemBase64Encoded, Base64.DEFAULT))
            // Use PemUtils to get the private key from the decoded Base64 PEM string
            val privateKey = pemUtils.getPrivateKeyFromPem(privateKeyPem)
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(unsignedToken.toByteArray(StandardCharsets.UTF_8))
            val signedBytes = signature.sign()
            base64UrlEncode(signedBytes)
        } catch (e: Exception) {
            throw IllegalStateException("Error signing JWT: ${e.message}", e)
        }
    }

    private fun base64UrlEncode(data: String): String {
        return Base64.encodeToString(data.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
