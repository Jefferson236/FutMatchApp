package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.PerfilEntidad
import com.example.futmatchapp.modelo.ProfileModel
import com.example.futmatchapp.vista.SettingsFragment

class SettingsController(private val vista: SettingsFragment) {
    private val modelo = ProfileModel()

    fun actualizarFiltrosDeEdad(idUsuario: Int, minEdad: Int, maxEdad: Int) {
        if (minEdad > maxEdad) {
            vista.mostrarError("La edad mínima no puede ser mayor que la máxima.")
            return
        }

        // Armamos el objeto con las preferencias modificadas
        val perfilModificado = PerfilEntidad(
            id = idUsuario,
            nombre = null, apellido = null, altura = null, peso = null,
            latitud = null, longitud = null, posicion_juego = null, tipo_juego = null, pie_dominante = null,
            edad_min_preferida = minEdad,
            edad_max_preferida = maxEdad
        )

        vista.mostrarCargando(true)
        modelo.actualizarPreferencias(idUsuario, perfilModificado) { respuesta ->
            vista.mostrarCargando(false)
            if (respuesta != null && respuesta.exito) {
                vista.mostrarMensajeExito("Preferencias actualizadas correctamente.")
            } else {
                vista.mostrarError(respuesta?.mensaje ?: "Error al intentar actualizar.")
            }
        }
    }

    fun cerrarSesion() {
        // Lógica local para borrar tokens, caché, etc.
        vista.redirigirAlLogin()
    }
}