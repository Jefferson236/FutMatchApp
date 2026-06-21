package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule // Importación desde la raíz
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class PerfilEntidad(
    val id: Int,
    val nombre: String?,
    val apellido: String?,
    val altura: Double?,
    val peso: Double?,
    val latitud: Double?,
    val longitud: Double?,
    val posicion_juego: String?,
    val tipo_juego: String?,
    val pie_dominante: String?,
    val edad_min_preferida: Int? = 18,
    val edad_max_preferida: Int? = 50,
    val avatar_url: String? = null,
    val banner_url: String? = null
)

data class PerfilResponse(
    val exito: Boolean,
    val mensaje: String
)

interface ProfileApiService {
    @POST("perfiles/crear")
    fun guardarPerfil(@Body perfil: PerfilEntidad): Call<PerfilResponse>

    @PUT("perfiles/actualizar/{id}")
    fun actualizarPreferencias(@Path("id") id: Int, @Body perfil: PerfilEntidad): Call<PerfilResponse>
}

class ProfileModel {
    private val apiService = FutMatchDatabaseModule.createService(ProfileApiService::class.java)

    fun registrarDatosPerfil(perfil: PerfilEntidad, callback: (PerfilResponse?) -> Unit) {
        apiService.guardarPerfil(perfil).enqueue(object : retrofit2.Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: retrofit2.Response<PerfilResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                callback(PerfilResponse(false, "Error de red: ${t.message}"))
            }
        })
    }

    fun actualizarPreferencias(id: Int, perfil: PerfilEntidad, callback: (PerfilResponse?) -> Unit) {
        apiService.actualizarPreferencias(id, perfil).enqueue(object : retrofit2.Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: retrofit2.Response<PerfilResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                callback(PerfilResponse(false, "Error de red: ${t.message}"))
            }
        })
    }
}