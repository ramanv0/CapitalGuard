package com.example.capitalguard.network

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.POST

/**
 * For Plaid API calls to localhost token server.
 */
interface LinkSampleApi {

    @POST("/api/create_link_token")
    fun getLinkToken(): Single<LinkToken>
}

data class LinkToken(
        @SerializedName("link_token") val link_token: String
)