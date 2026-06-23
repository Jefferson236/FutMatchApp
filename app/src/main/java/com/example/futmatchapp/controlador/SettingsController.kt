package com.example.futmatchapp.controlador

import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.modelo.Perfil
import com.example.futmatchapp.vista.SettingsFragment
import com.example.futmatchapp.vista.ProfileFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import androidx.fragment.app.Fragment

class SettingsController(private val fragment: Fragment) {
    private val apiService = RetrofitClient.create()

    fun actualizarFiltrosDeEdad(usuarioId: Int, minEdad: Int, maxEdad: Int) {
        mostrarCargando(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Obtener el perfil real usando el usuarioId
                val profileRes = apiService.getPerfilPorUsuario(usuarioId)
                val dbPerfilId = profileRes.body()?.data?.id

                if (dbPerfilId != null) {
                    val perfilModificado = Perfil(
                        edad_min_preferida = minEdad,
                        edad_max_preferida = maxEdad
                    )
                    val response = apiService.actualizarPerfil(dbPerfilId, perfilModificado)
                    withContext(Dispatchers.Main) {
                        mostrarCargando(false)
                        if (response.isSuccessful) {
                            mostrarExito("Preferencias actualizadas.")
                        } else {
                            mostrarError("Error al actualizar.")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarCargando(false)
                    mostrarError("Error: ${e.message}")
                }
            }
        }
    }

    fun eliminarCuenta(usuarioId: Int) {
        mostrarCargando(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Obtener el perfil real usando el usuarioId
                val profileRes = apiService.getPerfilPorUsuario(usuarioId)
                val dbPerfilId = profileRes.body()?.data?.id

                // 2. Eliminar el perfil si existe
                if (dbPerfilId != null) {
                    apiService.eliminarPerfil(dbPerfilId)
                }
                
                // 3. Eliminar el usuario
                val responseUsuario = apiService.eliminarUsuario(usuarioId)
                
                withContext(Dispatchers.Main) {
                    mostrarCargando(false)
                    if (responseUsuario.isSuccessful) {
                        mostrarExito("Cuenta eliminada por completo.")
                        redirigirAlLogin()
                    } else {
                        mostrarError("Error al eliminar el usuario.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarCargando(false)
                    mostrarError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun mostrarCargando(visible: Boolean) {
        when (fragment) {
            is SettingsFragment -> fragment.mostrarCargando(visible)
            is ProfileFragment -> fragment.mostrarCargando(visible)
        }
    }

    private fun mostrarError(mensaje: String) {
        when (fragment) {
            is SettingsFragment -> fragment.mostrarError(mensaje)
            is ProfileFragment -> fragment.mostrarError(mensaje)
        }
    }

    private fun mostrarExito(mensaje: String) {
        when (fragment) {
            is SettingsFragment -> fragment.mostrarMensajeExito(mensaje)
            is ProfileFragment -> fragment.mostrarMensajeExito(mensaje)
        }
    }

    private fun redirigirAlLogin() {
        when (fragment) {
            is SettingsFragment -> fragment.redirigirAlLogin()
            is ProfileFragment -> fragment.redirigirAlLogin()
        }
    }

    fun cerrarSesion() {
        redirigirAlLogin()
    }
}
