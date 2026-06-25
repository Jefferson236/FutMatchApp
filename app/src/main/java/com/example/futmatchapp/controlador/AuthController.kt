package com.example.futmatchapp.controlador

import android.util.Log
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.vista.LoginFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthController(private val vista: LoginFragment) {
    private val apiService = RetrofitClient.create()

    fun ejecutarLogin(username: String) {
        if (username.isBlank()) {
            vista.mostrarError("Por favor, ingresa tu usuario.")
            return
        }

        vista.mostrarCargando(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var usuarioEncontrado: com.example.futmatchapp.modelo.Usuario? = null
                
                // Buscamos exhaustivamente en todas las páginas si es necesario
                var currentUrl: String? = "api/usuarios"
                
                while (currentUrl != null) {
                    val response = apiService.getUsuarios() // Si el API soporta páginas vía URL, mejor
                    if (response.isSuccessful && response.body() != null) {
                        val paginated = response.body()!!
                        usuarioEncontrado = paginated.data.find { it.email.lowercase() == username.lowercase() }
                        
                        if (usuarioEncontrado != null) break
                        
                        // Si no hay más páginas, salir. 
                        // Nota: El API actual getUsuarios() no recibe parámetros.
                        // Si el backend devuelve siempre la misma página 1, esto entraría en bucle si no cortamos.
                        currentUrl = paginated.links?.next
                        if (currentUrl == null) break
                        
                        // Como getUsuarios() no acepta URL, salimos para evitar bucle infinito
                        break
                    } else {
                        break
                    }
                }

                withContext(Dispatchers.Main) {
                    if (usuarioEncontrado != null && usuarioEncontrado!!.id != null) {
                        val realUsuarioId = usuarioEncontrado!!.id!!
                        Log.d("FutMatch", "Usuario encontrado en BD: ID Usuario $realUsuarioId (Email: ${usuarioEncontrado!!.email})")
                        
                        // Buscamos el perfil asociado para obtener el ID de la tabla perfiles
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Buscamos en la lista de todos los perfiles el que tenga usuario_id == realUsuarioId
                                val responsePerfiles = apiService.getPerfiles()
                                val perfilesList = responsePerfiles.body()?.data ?: emptyList()
                                val perfilEncontrado = perfilesList.find { it.usuario_id == realUsuarioId }

                                withContext(Dispatchers.Main) {
                                    vista.mostrarCargando(false)
                                    if (perfilEncontrado != null) {
                                        val realPerfilId = perfilEncontrado.id!!
                                        Log.d("FutMatch", "¡ID DE PERFIL ENCONTRADO! Perfil ID: $realPerfilId (Usuario ID: $realUsuarioId)")
                                        vista.irAInicio(realUsuarioId, realPerfilId)
                                    } else {
                                        // Intento 2: Buscar por el endpoint específico si falló el find
                                        val responseEspecífico = apiService.getPerfilPorUsuario(realUsuarioId)
                                        if (responseEspecífico.isSuccessful && responseEspecífico.body()?.data != null) {
                                            val perfilId = responseEspecífico.body()!!.data!!.id!!
                                            vista.irAInicio(realUsuarioId, perfilId)
                                        } else {
                                            Log.e("FutMatch", "Error: No se encontró registro en tabla 'perfiles' para usuario_id $realUsuarioId")
                                            vista.mostrarError("No tienes un perfil creado con este usuario. Por favor regístrate.")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    vista.mostrarCargando(false)
                                    vista.mostrarError("Error de red al buscar perfil: ${e.message}")
                                }
                            }
                        }
                    } else {
                        vista.mostrarCargando(false)
                        vista.mostrarError("Usuario no encontrado.")
                    }
                }
            } catch (e: Exception) {
                Log.e("FutMatch", "Error en login", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarCargando(false)
                    vista.mostrarError("Error de red: ${e.message}")
                }
            }
        }
    }
}
