package com.example.futmatchapp

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object FutMatchDatabaseModule {
    private const val BASE_URL = "https://api.futmatchapp.com/v1/" // Cambiar por tu URL de desarrollo o producción

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Permite crear de forma dinámica cualquier interfaz de la API del paquete modelo
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofitInstance.create(serviceClass)
    }
}