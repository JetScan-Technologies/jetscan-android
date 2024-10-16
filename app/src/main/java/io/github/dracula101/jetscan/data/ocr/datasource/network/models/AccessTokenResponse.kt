package io.github.dracula101.jetscan.data.ocr.datasource.network.models


import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(

    @SerializedName("access_token")
    val accessToken: String?,

    @SerializedName("expires_in")
    val expiresIn: Long?,

    @SerializedName("token_type")
    val tokenType: String?
)