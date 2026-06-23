package com.example.futmatchapp.controlador

import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.PerfilEntidad
import com.example.futmatchapp.modelo.ProfileModel
import com.example.futmatchapp.vista.RegisterDataFragment

class OnboardingController(private val vista: RegisterDataFragment) {
    private val modelo = ProfileModel()
    private val validadorUsername = "^[a-z]{1,8}$".toRegex()

    fun guardarDatosCompletos(
        username: String,
        nombre: String,
        apellido: String,
        alturaStr: String,
        pesoStr: String,
        edadMin: Int,
        edadMax: Int,
        posicion: String,
        tipoJuego: String,
        avatarUrl: String,
        bannerUrl: String
    ) {
        val altura = alturaStr.toDoubleOrNull()
        val peso = pesoStr.toDoubleOrNull()

        if (!username.matches(validadorUsername)) {
            vista.mostrarError(R.string.error_username_formato)
            return
        }

        if ((nombre.isBlank() || apellido.isBlank()) || (altura == null || peso == null)) {
            vista.mostrarError(R.string.error_datos_incompletos)
            return
        }

        // Estructura completa lista para la BD
        val nuevoPerfil = PerfilEntidad(
            id = 0, // Lo maneja el Backend
            nombre = nombre,
            apellido = apellido,
            altura = altura,
            peso = peso,
            latitud = 0.0, longitud = 0.0, // GPS opcional después
            posicion_juego = posicion,
            tipo_juego = tipoJuego,
            pie_dominante = "Diestro", // Puedes agregarlo como ToggleButton también
            edad_min_preferida = edadMin,
            edad_max_preferida = edadMax,
            avatar_url = avatarUrl.ifEmpty { null },
            banner_url = bannerUrl.ifEmpty { null }
        )

        modelo.registrarDatosPerfil(nuevoPerfil) { respuesta ->
            if (respuesta != null && respuesta.exito) {
                vista.irAlExploradorTinder()
            } else {
                vista.mostrarError(respuesta?.mensaje ?: "Error en el servidor al registrar.")
            }
        }
    }

    fun saltarRegistro() {
        // Lógica opcional aquí (ej: guardar preferencia de "perfil incompleto")
        vista.irAlExploradorTinder()
    }
}