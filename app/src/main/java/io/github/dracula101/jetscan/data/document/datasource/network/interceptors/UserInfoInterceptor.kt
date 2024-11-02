package io.github.dracula101.jetscan.data.document.datasource.network.interceptors

import io.github.dracula101.jetscan.data.platform.datasource.network.util.HEADER_KEY_USER_ID
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Singleton

@Singleton
class UserInfoInterceptor : Interceptor {
    /**
     * The auth token to be added to API requests.
     */
    var userId: String? = null

    private val missingUserIdMessage = "User ID is missing!"

    override fun intercept(chain: Interceptor.Chain): Response {
        val userId = userId ?: throw IOException(IllegalStateException(missingUserIdMessage))
        val request = chain
            .request()
            .newBuilder()
            .addHeader(
                name = HEADER_KEY_USER_ID,
                value = userId,
            )
            .build()
        return chain
            .proceed(request)
    }
}
