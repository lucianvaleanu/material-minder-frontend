package com.lucianvaleanu.materialminder.service.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.1.132:8080/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val projectApi: ProjectApiService by lazy {
        retrofit.create(ProjectApiService::class.java)
    }

    val constructionItemApi: ConstructionItemApiService by lazy {
        retrofit.create(ConstructionItemApiService::class.java)
    }
}