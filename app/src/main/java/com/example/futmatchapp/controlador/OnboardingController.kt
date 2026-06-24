package com.example.futmatchapp.controlador

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.modelo.*
import com.example.futmatchapp.utils.ImageUploader
import com.example.futmatchapp.vista.RegisterDataFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch

class OnboardingController(private val vista: RegisterDataFragment) {
    private val apiService = RetrofitClient.create()
    private val validadorUsername = "^[a-z0-9]{1,12}$".toRegex()

    fun guardarDatosCompletos(
        context: Context,
        username: String, nombre: String, apellido: String,
        alturaStr: String, pesoStr: String,
        edadMin: Int, edadMax: Int,
        posicion: String, tipoJuego: String,
        avatarUri: Uri?, bannerUri: Uri?,
        galleryUris: List<Uri>
    ) {
        val altura = alturaStr.toDoubleOrNull()
        val peso = pesoStr.toDoubleOrNull()

        if (!username.matches(validadorUsername)) {
            vista.mostrarError("Username inválido (máx 12 letras/números minúsculas).")
            return
        }

        vista.mostrarCargando(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Subir imágenes
                var avatarUrlFinal: String? = null
                var bannerUrlFinal: String? = null
                val galleryUrlsFinal = mutableListOf<String>()

                val latch = CountDownLatch( (if (avatarUri != null) 1 else 0) + (if (bannerUri != null) 1 else 0) + galleryUris.size )

                avatarUri?.let { uri ->
                    ImageUploader.uploadImage(context, uri) { url: String? ->
                        if (url != null) {
                            avatarUrlFinal = url
                        } else {
                            Log.e("FutMatch", "Fallo subida avatar en onboarding")
                        }
                        latch.countDown()
                    }
                }
                bannerUri?.let { uri ->
                    ImageUploader.uploadImage(context, uri) { url: String? ->
                        if (url != null) {
                            bannerUrlFinal = url
                        } else {
                            Log.e("FutMatch", "Fallo subida banner en onboarding")
                        }
                        latch.countDown()
                    }
                }
                galleryUris.forEach { uri ->
                    ImageUploader.uploadImage(context, uri) { url: String? ->
                        if (url != null) {
                            galleryUrlsFinal.add(url)
                        } else {
                            Log.e("FutMatch", "Fallo subida imagen galeria en onboarding")
                        }
                        latch.countDown()
                    }
                }

                latch.await() // Esperar a que terminen las subidas

                // 2. Crear usuario
                val userResponse = apiService.crearUsuario(Usuario(email = username))
                if (!userResponse.isSuccessful || userResponse.body() == null) {
                    withContext(Dispatchers.Main) {
                        vista.mostrarCargando(false)
                        vista.mostrarError("Error al registrar usuario o ya existe.")
                    }
                    return@launch
                }
                val userId = userResponse.body()!!.id!!

                // 3. Crear Perfil
                val nuevoPerfil = Perfil(
                    usuario_id = userId,
                    nombre = nombre,
                    apellido = apellido,
                    altura = altura,
                    peso = peso,
                    posicion_juego = posicion,
                    tipo_juego = tipoJuego,
                    edad_min_preferida = edadMin,
                    edad_max_preferida = edadMax,
                    avatar_url = avatarUrlFinal,
                    banner_url = bannerUrlFinal
                )

                val profileResponse = apiService.crearPerfil(nuevoPerfil)
                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val perfilId = profileResponse.body()!!.id!!
                    Log.d("FutMatch", "Perfil creado exitosamente con ID: $perfilId")
                    
                    // 4. Crear Estadísticas (VALORES INICIALES 60)
                    Log.d("FutMatch", "Creando estadísticas iniciales para perfil ID: $perfilId")
                    val statsRes = apiService.crearEstadistica(Estadistica(perfil_id = perfilId, ovr = 60, pac = 60, sho = 60, pas = 60, dri = 60, def = 60, phy = 60))
                    if (statsRes.isSuccessful) {
                        Log.d("FutMatch", "Estadísticas iniciales creadas con éxito")
                    } else {
                        Log.e("FutMatch", "Error al crear estadísticas: ${statsRes.errorBody()?.string()}")
                    }

                    // 5. Subir Galería (opcional, si hay endpoint)
                    galleryUrlsFinal.forEach { url ->
                        apiService.subirFoto(Galeria(perfil_id = perfilId, url_foto = url))
                    }

                    withContext(Dispatchers.Main) {
                        vista.mostrarCargando(false)
                        vista.irAInicio(userId)
                    }
                } else {
                    val errorBody = profileResponse.errorBody()?.string()
                    Log.e("FutMatch", "Error al crear perfil: ${profileResponse.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        vista.mostrarCargando(false)
                        vista.mostrarError("Error al crear perfil: $errorBody")
                    }
                }
            } catch (e: Exception) {
                Log.e("FutMatch", "Error onboarding", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarCargando(false)
                    vista.mostrarError("Error: ${e.message}")
                }
            }
        }
    }

    fun saltarRegistro() {
        // Lógica opcional aquí (ej: guardar preferencia de "perfil incompleto")
        vista.irAlExploradorTinder()
    }
}
