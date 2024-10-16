package io.github.dracula101.jetscan.data.ocr.datasource.network.api

import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.AccessTokenResponse
import io.github.dracula101.jetscan.data.platform.datasource.network.util.BODY_ASSERTION_PREFIX
import io.github.dracula101.jetscan.data.platform.datasource.network.util.BODY_GRANT_TYPE_PREFIX
import io.github.dracula101.jetscan.data.platform.datasource.network.util.BODY_GRANT_TYPE_VALUE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OcrTokenApi {

    @FormUrlEncoded
    @POST("/token")
    suspend fun getAccessToken(
        @Field(BODY_GRANT_TYPE_PREFIX) grantType: String = BODY_GRANT_TYPE_VALUE,
        @Field(BODY_ASSERTION_PREFIX) assertion: String
    ): Result<AccessTokenResponse>

}