package com.example.capitalguard.network

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object LinkTokenRequester {
    // This value is setup to work with emulators. Modify this value to your PC's IP address if not.
    private val baseUrl = "http://10.0.2.2:8000"

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val api = retrofit.create(LinkSampleApi::class.java)

    /**
     * You can optionally curl for a link_token instead of running the node-quickstart server
     * programmatically, and copy and paste the link_token value below.
     *
     * The following curl request will output the necessary link token for CapitalGuard to function
     * correctly:
     *
     curl -X POST https://sandbox.plaid.com/link/token/create \
     -H 'Content-Type: application/json' \
     -d '{
        "client_id": "<Enter client id from Plaid API>",
        "secret": "<Enter secret from Plaid API>",
        "client_name": "Plaid App",
        "user": { "client_user_id": "UNIQUE_USER_ID" },
        "products": ["auth", "transactions", "assets", "investments"],
        "country_codes": ["US"],
        "language": "en",
        "android_package_name":"com.example.capitalguard"
     }'

     */

    val token
        get() = Single.just("<Enter link token from curl request>")

}