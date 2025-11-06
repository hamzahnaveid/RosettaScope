package com.example.rosettascope.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //accessing API through ngrok tunnel
    private const val BASE_URL = "https://subopaquely-unirradiative-bradley.ngrok-free.dev"

    val api: TranslationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApiService::class.java)
    }
}