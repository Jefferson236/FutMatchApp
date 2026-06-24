package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule // Importación desde la raíz
import com.example.futmatchapp.modelo.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class PerfilEntidad(
    val id: Int,
    @com.google.gson.annotations.SerializedName("usuario_id") val usuarioId: Int?,
    val nombre: String?,
    val apellido: String?,
    val altura: Double?,
    val peso: Double?,
    val latitud: Double?,
    val longitud: Double?,
    @com.google.gson.annotations.SerializedName("posicion_juego") val posicion_juego: String?,
    @com.google.gson.annotations.SerializedName("tipo_juego") val tipo_juego: String?,
    @com.google.gson.annotations.SerializedName("pie_dominante") val pie_dominante: String?,
    @com.google.gson.annotations.SerializedName("edad_min_preferida") val edad_min_preferida: Int? = 18,
    @com.google.gson.annotations.SerializedName("edad_max_preferida") val edad_max_preferida: Int? = 50,
    @com.google.gson.annotations.SerializedName("avatar_url") val avatar_url: String? = null,
    @com.google.gson.annotations.SerializedName("banner_url") val banner_url: String? = null,
    val ovr: Int = 60,
    val pac: Int = 60,
    val sho: Int = 60
)

data class PerfilResponse(
    val exito: Boolean,
    val mensaje: String
)

interface ProfileApiService {
    @POST("api/perfiles")
    fun guardarPerfil(@Body perfil: PerfilEntidad): Call<PerfilResponse>

    @PUT("api/perfiles/{id}")
    fun actualizarPreferencias(@Path("id") id: Int, @Body perfil: PerfilEntidad): Call<PerfilResponse>

    @retrofit2.http.GET("api/perfiles")
    fun obtenerTodosLosPerfiles(): Call<ApiResponse<List<PerfilEntidad>>>
}

class ProfileModel {
    private val apiService = FutMatchDatabaseModule.createService(ProfileApiService::class.java)

    fun descargarPerfiles(callback: Callback<List<PerfilEntidad>>) {
        apiService.obtenerTodosLosPerfiles().enqueue(object : Callback<ApiResponse<List<PerfilEntidad>>> {
            override fun onResponse(call: Call<ApiResponse<List<PerfilEntidad>>>, response: retrofit2.Response<ApiResponse<List<PerfilEntidad>>>) {
                @Suppress("UNCHECKED_CAST")
                val dummyCall = call as Call<List<PerfilEntidad>>
                if (response.isSuccessful && response.body()?.data != null) {
                    callback.onResponse(dummyCall, retrofit2.Response.success(response.body()!!.data))
                } else {
                    val errorBody = response.errorBody() ?: okhttp3.ResponseBody.create(null, "")
                    callback.onResponse(dummyCall, retrofit2.Response.error(response.code(), errorBody))
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<PerfilEntidad>>>, t: Throwable) {
                @Suppress("UNCHECKED_CAST")
                callback.onFailure(call as Call<List<PerfilEntidad>>, t)
            }
        })
    }

    fun registrarDatosPerfil(perfil: PerfilEntidad, callback: (PerfilResponse?) -> Unit) {
        apiService.guardarPerfil(perfil).enqueue(object : Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: retrofit2.Response<PerfilResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                callback(PerfilResponse(false, "Error de red: ${t.message}"))
            }
        })
    }

    fun actualizarPreferencias(id: Int, perfil: PerfilEntidad, callback: (PerfilResponse?) -> Unit) {
        apiService.actualizarPreferencias(id, perfil).enqueue(object : Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: retrofit2.Response<PerfilResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                callback(PerfilResponse(false, "Error de red: ${t.message}"))
            }
        })
    }
}