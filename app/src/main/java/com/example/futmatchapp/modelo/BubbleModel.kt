package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule
import retrofit2.Call
import retrofit2.Callback
import com.google.gson.annotations.SerializedName

// DTO mapeado de la tabla burbujas_solicitud
data class BurbujaData(
    val id: Int = 0,
    @SerializedName("creador_id") val creadorId: Int,
    @SerializedName("tipo_juego") val tipoJuego: String,
    @SerializedName("mensaje_personalizado") val mensajePersonalizado: String?,
    @SerializedName("ubicacion_lugar") val ubicacionLugar: String,
    @SerializedName("cuota_a_pagar") val cuotaAPagar: Double?,
    @SerializedName("posicion_necesitada") val posicionNecesitada: String?,
    @SerializedName("fecha_hora") val fechaHora: String, // Formato ISO 8601
    val estado: String = "abierta"
)

class BubbleModel {
    // Interfaz local para la API de burbujas (burbujas_solicitud)
    interface BubbleApiService {
        @retrofit2.http.POST("api/burbujas-solicitudes")
        fun crearBurbuja(@retrofit2.http.Body burbuja: BurbujaData): Call<Void>

        @retrofit2.http.GET("api/burbujas-solicitudes")
        fun obtenerBurbujasActivas(): Call<ApiResponse<List<BurbujaData>>>
    }

    private val api = FutMatchDatabaseModule.createService(BubbleApiService::class.java)

    fun insertarBurbuja(burbuja: BurbujaData, callback: Callback<Void>) {
        api.crearBurbuja(burbuja).enqueue(callback)
    }

    fun descargarBurbujas(callback: Callback<List<BurbujaData>>) {
        api.obtenerBurbujasActivas().enqueue(object : Callback<ApiResponse<List<BurbujaData>>> {
            override fun onResponse(call: Call<ApiResponse<List<BurbujaData>>>, response: retrofit2.Response<ApiResponse<List<BurbujaData>>>) {
                // Creamos un Call "dummy" o pasamos el original mediante un cast inseguro para satisfacer la firma
                @Suppress("UNCHECKED_CAST")
                val dummyCall = call as Call<List<BurbujaData>>
                
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    callback.onResponse(dummyCall, retrofit2.Response.success(data))
                } else {
                    val errorBody = response.errorBody() ?: okhttp3.ResponseBody.create(null, "")
                    callback.onResponse(dummyCall, retrofit2.Response.error(response.code(), errorBody))
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<BurbujaData>>>, t: Throwable) {
                @Suppress("UNCHECKED_CAST")
                callback.onFailure(call as Call<List<BurbujaData>>, t)
            }
        })
    }
}