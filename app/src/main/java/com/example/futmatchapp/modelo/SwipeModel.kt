package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback

data class SwipeData(
    @SerializedName("emisor_id") val emisor_id: Int,
    @SerializedName("receptor_id") val receptor_id: Int,
    @SerializedName("es_match") val es_match: Int = 0
)

data class SwipeEnriquecido(
    val swipeId: Int,
    val emisorId: Int,
    val nombreEmisor: String,
    val avatarUrl: String?,
    val tipoReceptor: String, // "jugador" o "burbuja"
    val receptorId: Int?,
    val infoAdicional: String? // "Delantero" o "Burbuja: Fútbol 7"
)

data class ChatItem(
    val matchId: Int,
    val otroUsuarioId: Int,
    val nombreOtro: String,
    val avatarOtro: String?,
    val ultimoMensaje: String?,
    val fecha: String?
)

data class MatchResponse(
    val success: Boolean,
    val message: String?,
    val matchId: Int?,
    val chatRoomId: Int?
)

class SwipeModel {
    interface SwipeApiService {
        @retrofit2.http.POST("api/swipes")
        fun enviarSwipe(@retrofit2.http.Body swipe: SwipeData): Call<MatchResponse>

        @retrofit2.http.GET("api/swipes/pendientes/{usuario_id}")
        fun obtenerLikesPendientes(@retrofit2.http.Path("usuario_id") usuarioId: Int): Call<ApiResponse<List<SwipeEnriquecido>>>

        @retrofit2.http.POST("api/matches/aceptar")
        fun aceptarMatch(@retrofit2.http.Query("swipe_id") swipeId: Int): Call<MatchResponse>

        @retrofit2.http.GET("api/matches/chats/{usuario_id}")
        fun obtenerChatsActivos(@retrofit2.http.Path("usuario_id") usuarioId: Int): Call<ApiResponse<List<ChatItem>>>
    }

    private val api = FutMatchDatabaseModule.createService(SwipeApiService::class.java)

    fun enviarSwipe(swipe: SwipeData, callback: Callback<MatchResponse>) {
        api.enviarSwipe(swipe).enqueue(callback)
    }

    fun obtenerLikesPendientes(usuarioId: Int, callback: Callback<ApiResponse<List<SwipeEnriquecido>>>) {
        api.obtenerLikesPendientes(usuarioId).enqueue(callback)
    }

    fun aceptarMatch(swipeId: Int, callback: Callback<MatchResponse>) {
        api.aceptarMatch(swipeId).enqueue(callback)
    }

    fun obtenerChatsActivos(usuarioId: Int, callback: Callback<ApiResponse<List<ChatItem>>>) {
        api.obtenerChatsActivos(usuarioId).enqueue(callback)
    }
}