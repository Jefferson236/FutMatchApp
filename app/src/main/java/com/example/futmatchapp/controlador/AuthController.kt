package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.UserModel
import com.example.futmatchapp.vista.LoginFragment

class AuthController(private val vista: LoginFragment) {
    private val modelo = UserModel()
    // Regex: Solo letras minúsculas, de 1 a 8 caracteres
    private val validadorUsername = "^[a-z]{1,8}$".toRegex()

    fun ejecutarLogin(username: String) {
        if (!username.matches(validadorUsername)) {
            vista.mostrarError("El usuario debe tener máximo 8 letras, solo minúsculas y sin espacios.")
            return
        }

        vista.mostrarCargando(true)
        modelo.login(username) { respuesta ->
            vista.mostrarCargando(false)
            if (respuesta != null && respuesta.exito) {
                val usuarioId = respuesta.usuario?.id ?: 0
                // Directo al Home/Ajustes porque ya existe
                vista.irAInicio(usuarioId)
            } else {
                vista.mostrarError("Usuario no encontrado. Crea uno nuevo.")
            }
        }
    }
}