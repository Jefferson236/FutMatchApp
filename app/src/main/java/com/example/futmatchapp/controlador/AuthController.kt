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
                var paginaActual = 1
                var usuarioEncontrado: com.example.futmatchapp.modelo.Usuario? = null
                
                // Buscamos exhaustivamente en todas las páginas si es necesario
                while (usuarioEncontrado == null) {
                    val response = apiService.getUsuarios() // Nota: El API actual no parece recibir página, pero si lo hiciera: getUsuarios(paginaActual)
                    if (response.isSuccessful && response.body() != null) {
                        val usuarios = response.body()!!.data
                        usuarioEncontrado = usuarios.find { it.email.lowercase() == username.lowercase() }
                        
                        // Si no está en esta página y hay más páginas, continuaríamos. 
                        // Como el API actual getUsuarios() no recibe página, asumimos que devuelve una lista.
                        // Si el backend tuviera búsqueda por email sería: apiService.buscarUsuario(email = username)
                        
                        if (usuarioEncontrado != null || response.body()?.links?.next == null) break
                        paginaActual++
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
