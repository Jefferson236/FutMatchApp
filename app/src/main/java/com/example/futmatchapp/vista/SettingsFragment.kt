package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.futmatchapp.R
import com.example.futmatchapp.MainActivity
import com.example.futmatchapp.controlador.SettingsController

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var controlador: SettingsController
    private var usuarioId: Int = 1

    private lateinit var btnLogOut: Button
    private lateinit var progressSettings: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Volver a mostrar el menú al entrar a la sección principal
        (activity as? MainActivity)?.mostrarNavegacionSoporte()

        controlador = SettingsController(this)
        usuarioId = arguments?.getInt("USUARIO_ID") ?: 1

        btnLogOut = view.findViewById(R.id.btnLogOut)
        progressSettings = view.findViewById(R.id.progressSettings)

        btnLogOut.setOnClickListener {
            controlador.cerrarSesion()
        }
    }

    fun mostrarCargando(visible: Boolean) {
        progressSettings.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    fun mostrarMensajeExito(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun redirigirAlLogin() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, LoginFragment())
            .commit()
    }
}