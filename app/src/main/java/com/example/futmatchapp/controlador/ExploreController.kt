package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreController(private val view: ExploreCallbackInterface) {

    private val bubbleModel = BubbleModel()
    private val swipeModel = SwipeModel()

    interface ExploreCallbackInterface {
        fun onBurbujasCargadas(burbujas: List<BurbujaData>)
        fun onMatchGenerado(matchData: MatchResponse)
        fun onErrorExploracion(mensaje: String)
    }

    fun descargarFeedBurbujas() {
        bubbleModel.descargarBurbujas(object : Callback<List<BurbujaData>> {
            override fun onResponse(call: Call<List<BurbujaData>>, response: Response<List<BurbujaData>>) {
                if (response.isSuccessful && response.body() != null) {
                    view.onBurbujasCargadas(response.body()!!)
                }
            }

            override fun onFailure(call: Call<List<BurbujaData>>, t: Throwable) {
                view.onErrorExploracion("Fallo al descargar el feed de partidos.")
            }
        })
    }

    fun procesarDeslizamiento(emisorId: Int, receptorId: Int, esLike: Boolean, esHaciaJugador: Boolean) {
        if (!esLike) return // Si es un "Dislike" (Swipe Izquierda), lo ignoramos en BD o lo guardamos como descarte (según política)

        val swipe = SwipeData(emisorId = emisorId, receptorId = receptorId, esMatch = 0)

        val callback = object : Callback<MatchResponse> {
            override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                if (response.isSuccessful && response.body()?.esMatchMutuo == true) {
                    view.onMatchGenerado(response.body()!!) // ¡Es un Match!
                }
            }
            override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                view.onErrorExploracion("Error al registrar el Swipe.")
            }
        }

        if (esHaciaJugador) swipeModel.registrarSwipeAJugador(swipe, callback)
        else swipeModel.registrarSwipeABurbuja(swipe, callback)
    }
}