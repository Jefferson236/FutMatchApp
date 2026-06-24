package com.example.futmatchapp

import com.example.futmatchapp.modelo.*
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    // --- USUARIOS ---
    @GET("api/usuarios")
    suspend fun getUsuarios(): Response<PaginatedResponse<Usuario>>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<Usuario>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<Unit>

    // --- PERFILES ---
    @GET("api/perfiles")
    suspend fun getPerfiles(): Response<PaginatedResponse<Perfil>>

    @GET("api/perfiles/{id}")
    suspend fun getPerfil(@Path("id") id: Int): Response<ApiResponse<Perfil>>

    @GET("api/perfiles/usuario/{usuario_id}")
    suspend fun getPerfilPorUsuario(@Path("usuario_id") usuarioId: Int): Response<ApiResponse<Perfil>>

    @POST("api/perfiles")
    suspend fun crearPerfil(@Body perfil: Perfil): Response<Perfil>

    @PUT("api/perfiles/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Int, @Body perfil: Perfil): Response<Perfil>

    @DELETE("api/perfiles/{id}")
    suspend fun eliminarPerfil(@Path("id") id: Int): Response<Unit>

    // --- GALERIA ---
    @GET("api/galeria")
    suspend fun getGaleria(): Response<PaginatedResponse<Galeria>>

    @POST("api/galeria")
    suspend fun subirFoto(@Body foto: Galeria): Response<ApiResponse<Galeria>>

    // --- ESTADISTICAS ---
    @GET("api/estadisticas")
    suspend fun getEstadisticas(): Response<PaginatedResponse<Estadistica>>

    @GET("api/estadisticas/perfil/{perfil_id}")
    suspend fun getEstadisticasPorPerfil(@Path("perfil_id") perfilId: Int): Response<ApiResponse<Estadistica>>

    @POST("api/estadisticas")
    suspend fun crearEstadistica(@Body estadistica: Estadistica): Response<ApiResponse<Estadistica>>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2/lask2/public/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun create(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
