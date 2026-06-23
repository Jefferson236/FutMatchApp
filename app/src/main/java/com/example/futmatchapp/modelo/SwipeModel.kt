package com.example.futmatchapp.modelo

import com.example.futmatchapp.FutMatchDatabaseModule
import retrofit2.Call
import retrofit2.Callback

data class SwipeData(
    val emisorId: Int,
    val receptorId: Int?, // Puede ser el ID de un perfil o el de una burbuja
    val esMatch: Int = 0
)

data class MatchResponse(
    val esMatchMutuo: Boolean,
    val matchId: Int?,
    val chatRoomId: Int?
)

class SwipeModel {
    interface SwipeApiService {
        @retrofit2.http.POST("swipes/jugador")
        fun enviarSwipeJugador(@retrofit2.http.Body swipe: SwipeData): Call<MatchResponse>

        @retrofit2.http.POST("swipes/burbuja")
        fun enviarSwipeBurbuja(@retrofit2.http.Body swipe: SwipeData): Call<MatchResponse>
    }

    private val api = FutMatchDatabaseModule.createService(SwipeApiService::class.java)

    fun registrarSwipeAJugador(swipe: SwipeData, callback: Callback<MatchResponse>) {
        api.enviarSwipeJugador(swipe).enqueue(callback)
    }

    fun registrarSwipeABurbuja(swipe: SwipeData, callback: Callback<MatchResponse>) {
        api.enviarSwipeBurbuja(swipe).enqueue(callback)
    }
}