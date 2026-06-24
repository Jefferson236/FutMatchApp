package com.example.futmatchapp.controlador

import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.modelo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreController(private val view: ExploreCallbackInterface) {

    private val bubbleModel = BubbleModel()
    private val swipeModel = SwipeModel()
    private val profileModel = ProfileModel()

    interface ExploreCallbackInterface {
        fun onBurbujasCargadas(burbujas: List<BurbujaData>)
        fun onPerfilesCargados(perfiles: List<PerfilEntidad>)
        fun onMatchGenerado(matchData: MatchResponse)
        fun onErrorExploracion(mensaje: String)
        fun onBurbujaCreada()
    }

    fun descargarFeedBurbujas() {
        bubbleModel.descargarBurbujas(object : Callback<List<BurbujaData>> {
            override fun onResponse(call: Call<List<BurbujaData>>, response: Response<List<BurbujaData>>) {
                if (response.isSuccessful && response.body() != null) {
                    view.onBurbujasCargadas(response.body()!!)
                } else {
                    view.onErrorExploracion("Error al descargar burbujas")
                }
            }

            override fun onFailure(call: Call<List<BurbujaData>>, t: Throwable) {
                view.onErrorExploracion("Fallo al descargar el feed de partidos.")
            }
        })
    }

    fun descargarFeedPerfiles() {
        // En una app real, aquí llamaríamos a un endpoint que devuelva PerfilEntidad
        // Por ahora adaptamos la respuesta de Perfil (de Models.kt) a PerfilEntidad (de ProfileModel.kt) si es necesario
        // O usamos directamente el ProfileModel que ya parece devolver PerfilEntidad
        profileModel.descargarPerfiles(object : Callback<List<PerfilEntidad>> {
            override fun onResponse(call: Call<List<PerfilEntidad>>, response: Response<List<PerfilEntidad>>) {
                if (response.isSuccessful && response.body() != null) {
                    view.onPerfilesCargados(response.body()!!)
                } else {
                    // Fallback: Si el endpoint de ProfileModel falla, intentamos con RetrofitClient directo
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val res = RetrofitClient.create().getPerfiles()
                            if (res.isSuccessful && res.body() != null) {
                                val perfilesApi = res.body()!!.data
                                val perfilesEntidad = perfilesApi.map { p: com.example.futmatchapp.modelo.Perfil ->
                                    // Buscamos las estadísticas para este perfil
                                    // En una app real, el backend debería devolver el perfil con sus stats
                                    val statsRes = RetrofitClient.create().getEstadisticasPorPerfil(p.id ?: 0)
                                    val stats = statsRes.body()?.data
                                    
                                    PerfilEntidad(
                                        id = p.id ?: 0,
                                        usuarioId = p.usuario_id,
                                        nombre = p.nombre,
                                        apellido = p.apellido,
                                        altura = p.altura,
                                        peso = p.peso,
                                        latitud = p.latitud,
                                        longitud = p.longitud,
                                        posicion_juego = p.posicion_juego,
                                        tipo_juego = p.tipo_juego,
                                        pie_dominante = p.pie_dominante,
                                        avatar_url = p.avatar_url,
                                        banner_url = p.banner_url,
                                        // Guardamos las stats en un campo temporal o similar si PerfilEntidad lo permite
                                        // O simplemente las usaremos en el adapter. 
                                        // Por ahora, como PerfilEntidad no tiene stats, las simularemos o extenderemos PerfilEntidad
                                        ovr = stats?.ovr ?: 60,
                                        pac = stats?.pac ?: 60,
                                        sho = stats?.sho ?: 60
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    view.onPerfilesCargados(perfilesEntidad)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    view.onErrorExploracion("Error al descargar perfiles (Fallback)")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                view.onErrorExploracion("Error de red: ${e.message}")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<PerfilEntidad>>, t: Throwable) {
                view.onErrorExploracion("Fallo al descargar el feed de jugadores.")
            }
        })
    }

    fun crearBurbuja(burbuja: BurbujaData) {
        bubbleModel.insertarBurbuja(burbuja, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    view.onBurbujaCreada()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: ""
                    android.util.Log.e("ExploreController", "Error al crear burbuja: ${response.code()} - $errorMsg")
                    view.onErrorExploracion("Error al crear el post (${response.code()})")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                android.util.Log.e("ExploreController", "Fallo de red al crear burbuja", t)
                view.onErrorExploracion("Error de red al crear post")
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