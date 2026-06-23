package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futmatchapp.R
import com.example.futmatchapp.MainActivity
import com.example.futmatchapp.controlador.AuthController

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var controlador: AuthController
    private lateinit var edtUsername: EditText
    private lateinit var btnEntrar: Button
    private lateinit var btnCrearNuevo: Button
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.ocultarNavegacionSoporte()

        // Aplicamos un fondo negro base desde código si no está en XML
        view.setBackgroundColor(resources.getColor(R.color.black_pitch, null))

        controlador = AuthController(this)

        edtUsername = view.findViewById(R.id.edtUsername)
        btnEntrar = view.findViewById(R.id.btnEntrar)
        btnCrearNuevo = view.findViewById(R.id.btnCrearNuevo)
        progressBar = view.findViewById(R.id.progressLogin)

        btnEntrar.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            controlador.ejecutarLogin(username)
        }

        btnCrearNuevo.setOnClickListener {
            irAlRegistro()
        }
    }

    fun mostrarCargando(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun mostrarError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

    fun irAInicio(usuarioId: Int) {
        val sessionManager = com.example.futmatchapp.SessionManager(requireContext())
        sessionManager.saveUserId(usuarioId)
        
        val bundle = Bundle().apply { 
            putInt("USUARIO_ID", usuarioId) 
            putInt("PERFIL_ID", usuarioId)
        }
        (activity as? MainActivity)?.mostrarBottomNavigation()
        findNavController().navigate(R.id.swipeBubblesFragment, bundle)
    }

    private fun irAlRegistro() {
        findNavController().navigate(R.id.action_login_to_registerData)
    }
}