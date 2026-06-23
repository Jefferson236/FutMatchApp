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
                // Buscamos en todos los usuarios para encontrar el que coincida con el "email" (que es el username por ahora)
                val response = apiService.getUsuarios()
                withContext(Dispatchers.Main) {
                    vista.mostrarCargando(false)
                    if (response.isSuccessful && response.body() != null) {
                        val usuarios = response.body()!!.data
                        val usuarioEncontrado = usuarios.find { it.email.lowercase() == username.lowercase() }
                        
                        if (usuarioEncontrado != null && usuarioEncontrado.id != null) {
                            Log.d("FutMatch", "Login exitoso para: $username (ID: ${usuarioEncontrado.id})")
                            vista.irAInicio(usuarioEncontrado.id)
                        } else {
                            vista.mostrarError("Usuario no encontrado. ¿Deseas registrarte?")
                        }
                    } else {
                        vista.mostrarError("Error al conectar con el servidor.")
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
