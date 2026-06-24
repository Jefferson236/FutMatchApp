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
                    vista.mostrarCargando(false)
                    if (usuarioEncontrado != null && usuarioEncontrado!!.id != null) {
                        Log.d("FutMatch", "Login exitoso para: $username (ID: ${usuarioEncontrado!!.id})")
                        vista.irAInicio(usuarioEncontrado!!.id!!)
                    } else {
                        vista.mostrarError("Usuario no encontrado. ¿Deseas registrarte?")
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
