package com.example.futmatchapp.controlador

import android.util.Log
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.modelo.Perfil
import com.example.futmatchapp.modelo.Usuario
import com.example.futmatchapp.vista.RegisterDataFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.net.SocketTimeoutException

class OnboardingController(private val vista: RegisterDataFragment) {
    private val apiService = RetrofitClient.create()
    private val validadorUsername = "^[a-z]{1,8}$".toRegex()

    fun guardarDatosCompletos(
        username: String, nombre: String, apellido: String,
        alturaStr: String, pesoStr: String,
        edadMin: Int, edadMax: Int,
        posicion: String, tipoJuego: String,
        avatarUrl: String, bannerUrl: String
    ) {
        val altura = alturaStr.toDoubleOrNull()
        val peso = pesoStr.toDoubleOrNull()

        if (!username.matches(validadorUsername)) {
            vista.mostrarError("Username inválido (máx 8 letras minúsculas).")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FutMatch", "Intentando crear usuario: $username")
                // 1. Intentar crear usuario
                val userResponse = apiService.crearUsuario(Usuario(email = username))
                val userId = if (userResponse.isSuccessful && userResponse.body() != null) {
                    Log.d("FutMatch", "Usuario creado con ID: ${userResponse.body()?.id}")
                    userResponse.body()!!.id!!
                } else {
                    Log.e("FutMatch", "Error al crear usuario: ${userResponse.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        vista.mostrarError("Error al registrar usuario o el username ya existe.")
                    }
                    return@launch
                }

                // 2. Crear Perfil con usuario_id
                Log.d("FutMatch", "Creando perfil para usuario ID: $userId")
                val nuevoPerfil = Perfil(
                    usuario_id = userId, // ¡Asegúrate de que este campo se envíe!
                    nombre = if (nombre.isBlank()) null else nombre,
                    apellido = if (apellido.isBlank()) null else apellido,
                    altura = altura,
                    peso = peso,
                    posicion_juego = posicion,
                    tipo_juego = tipoJuego,
                    pie_dominante = "Diestro",
                    edad_min_preferida = edadMin,
                    edad_max_preferida = edadMax,
                    avatar_url = if (avatarUrl.isBlank()) null else avatarUrl,
                    banner_url = if (bannerUrl.isBlank()) null else bannerUrl
                )
                
                Log.d("FutMatch", "Datos del perfil a enviar: $nuevoPerfil")

                val profileResponse = apiService.crearPerfil(nuevoPerfil)
                withContext(Dispatchers.Main) {
                    if (profileResponse.isSuccessful) {
                        Log.d("FutMatch", "Perfil creado exitosamente")
                        vista.irAInicio(userId)
                    } else {
                        Log.e("FutMatch", "Error perfil: ${profileResponse.errorBody()?.string()}")
                        vista.mostrarError("Error al crear perfil.")
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e("FutMatch", "Timeout de red: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarError("Tiempo de espera agotado. El servidor tarda mucho en responder.")
                }
            } catch (e: Exception) {
                Log.e("FutMatch", "Excepción de red: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarError("Error de red: ${e.message}")
                }
            }
        }
    }

    fun saltarRegistro() {
        // Lógica opcional aquí (ej: guardar preferencia de "perfil incompleto")
        vista.irAlExploradorTinder()
    }
}
