package io.github.dracula101.jetscan.data.ocr.util

import android.util.Base64
import timber.log.Timber
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

class PemUtils {

    // Function to parse a PEM-encoded private key and convert it into a PrivateKey object
    fun getPrivateKeyFromPem(privateKeyPem: String): PrivateKey {
        // Remove the PEM header and footer
        val pem = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----\\n", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "") // Remove any whitespace, including newlines
        // Decode the Base64 PEM string into a byte array
        val keyBytes = Base64.decode(pem, Base64.DEFAULT)

        // Generate a PrivateKey object from the PKCS#8 encoded key
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
}
