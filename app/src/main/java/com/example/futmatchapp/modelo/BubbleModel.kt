package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule
import retrofit2.Call
import retrofit2.Callback

// DTO mapeado de la tabla burbujas_solicitud
data class BurbujaData(
    val id: Int = 0,
    val creadorId: Int,
    val tipoJuego: String,
    val mensajePersonalizado: String?,
    val ubicacionLugar: String,
    val cuotaAPagar: Double?,
    val posicionNecesitada: String?,
    val fechaHora: String, // Formato ISO 8601
    val estado: String = "abierta"
)

class BubbleModel {
    // Interfaz local simulada para ilustrar la expansión del API Service
    interface BubbleApiService {
        @retrofit2.http.POST("burbujas")
        fun crearBurbuja(@retrofit2.http.Body burbuja: BurbujaData): Call<Void>

        @retrofit2.http.GET("burbujas/activas")
        fun obtenerBurbujasActivas(): Call<List<BurbujaData>>
    }

    private val api = FutMatchDatabaseModule.createService(BubbleApiService::class.java)

    fun insertarBurbuja(burbuja: BurbujaData, callback: Callback<Void>) {
        api.crearBurbuja(burbuja).enqueue(callback)
    }

    fun descargarBurbujas(callback: Callback<List<BurbujaData>>) {
        api.obtenerBurbujasActivas().enqueue(callback)
    }
}