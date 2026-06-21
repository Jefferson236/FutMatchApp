package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class UsuarioEntidad(
    val id: Int? = null,
    val username: String, // Cambiado de email a username
    val created_at: String? = null
)

data class AuthResponse(
    val exito: Boolean,
    val mensaje: String,
    val usuario: UsuarioEntidad?
)

interface UserApiService {
    @POST("usuarios/login")
    fun autenticarUsuario(@Body usuario: UsuarioEntidad): Call<AuthResponse>
}

class UserModel {
    private val apiService = FutMatchDatabaseModule.createService(UserApiService::class.java)

    fun login(username: String, callback: (AuthResponse?) -> Unit) {
        val payload = UsuarioEntidad(username = username)
        apiService.autenticarUsuario(payload).enqueue(object : retrofit2.Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                callback(AuthResponse(false, "Error de red: ${t.message}", null))
            }
        })
    }
}