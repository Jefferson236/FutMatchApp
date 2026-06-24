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
        fun onBurbujasCargadas(burbujas: List<BurbujaEnriquecida>)
        fun onPerfilesCargados(perfiles: List<PerfilEntidad>)
        fun onMatchGenerado(matchData: MatchResponse)
        fun onErrorExploracion(mensaje: String)
        fun onBurbujaCreada()
    }

    fun descargarFeedBurbujas() {
        bubbleModel.descargarBurbujas(object : Callback<List<BurbujaData>> {
            override fun onResponse(call: Call<List<BurbujaData>>, response: Response<List<BurbujaData>>) {
                if (response.isSuccessful && response.body() != null) {
                    val burbujasRaw = response.body()!!
                    
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val perfilesRes = RetrofitClient.create().getPerfiles()
                            val perfiles = perfilesRes.body()?.data ?: emptyList()
                            
                            val usuariosRes = RetrofitClient.create().getUsuarios()
                            val usuarios = usuariosRes.body()?.data ?: emptyList()

                            val enriquecidas = burbujasRaw.map { b ->
                                val perfil = perfiles.find { it.usuario_id == b.creadorId }
                                val usuario = usuarios.find { it.id == b.creadorId }
                                
                                val nombre = when {
                                    !perfil?.nombre.isNullOrBlank() -> "${perfil?.nombre} ${perfil?.apellido ?: ""}"
                                    usuario != null -> usuario.email
                                    else -> "Usuario #${b.creadorId}"
                                }

                                BurbujaEnriquecida(
                                    id = b.id,
                                    creadorId = b.creadorId,
                                    nombreCreador = nombre,
                                    avatarUrl = perfil?.avatar_url,
                                    tipoJuego = b.tipoJuego,
                                    mensaje = b.mensajePersonalizado,
                                    ubicacion = b.ubicacionLugar,
                                    posicion = b.posicionNecesitada,
                                    fechaHora = b.fechaHora
                                )
                            }
                            
                            withContext(Dispatchers.Main) {
                                view.onBurbujasCargadas(enriquecidas)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                view.onErrorExploracion("Error al enriquecer datos")
                            }
                        }
                    }
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
        profileModel.descargarPerfiles(object : Callback<List<PerfilEntidad>> {
            override fun onResponse(call: Call<List<PerfilEntidad>>, response: Response<List<PerfilEntidad>>) {
                if (response.isSuccessful && response.body() != null) {
                    view.onPerfilesCargados(response.body()!!)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val res = RetrofitClient.create().getPerfiles()
                            if (res.isSuccessful && res.body() != null) {
                                val perfilesApi = res.body()!!.data
                                val perfilesEntidad = perfilesApi.map { p: com.example.futmatchapp.modelo.Perfil ->
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
        if (!esLike) return 

        val swipe = SwipeData(emisorId = emisorId, receptorId = receptorId, esMatch = 0)

        val callback = object : Callback<MatchResponse> {
            override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                if (response.isSuccessful && response.body()?.esMatchMutuo == true) {
                    view.onMatchGenerado(response.body()!!) 
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
