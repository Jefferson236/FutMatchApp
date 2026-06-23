package com.example.futmatchapp.vista

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futmatchapp.R
import com.example.futmatchapp.SessionManager
import com.example.futmatchapp.controlador.ProfileController
import com.example.futmatchapp.controlador.SettingsController
import com.example.futmatchapp.modelo.Perfil
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileFragment : Fragment() {

    private lateinit var profileController: ProfileController
    private lateinit var settingsController: SettingsController
    private lateinit var sessionManager: SessionManager
    
    private var usuarioLogueadoId: Int = -1 
    private var dbPerfilId: Int = -1
    private var modoEdicion: Boolean = false

    // Header Display
    private lateinit var txtDisplayName: TextView
    private lateinit var txtDisplayPosicion: TextView
    
    // Card Display
    private lateinit var txtOvr: TextView
    private lateinit var txtPosicionCard: TextView
    
    // Edit Form
    private lateinit var edtNombre: EditText
    private lateinit var edtApellido: EditText
    private lateinit var edtAltura: EditText
    private lateinit var edtPeso: EditText
    private lateinit var spinnerTipoJuego: Spinner
    private lateinit var spinnerPosicion: Spinner
    
    private lateinit var btnActualizarPerfil: Button
    private lateinit var btnEliminarPerfil: Button
    private lateinit var fabEditToggle: FloatingActionButton
    private lateinit var loadingOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        sessionManager = SessionManager(requireContext())
        usuarioLogueadoId = sessionManager.getUserId()
        
        val bundleId = arguments?.getInt("PERFIL_ID", -1) ?: -1
        val targetUsuarioId = if (bundleId != -1 && bundleId != 0) bundleId else usuarioLogueadoId
        
        Log.d("FutMatch", "ProfileFragment - Usuario Logueado: $usuarioLogueadoId, Viendo Usuario: $targetUsuarioId")
        
        profileController = ProfileController(this)
        settingsController = SettingsController(this)

        vincularVistas(view)
        configurarSpinners()
        configurarEventos()

        if (targetUsuarioId != -1) {
            mostrarCargando(true)
            profileController.cargarPerfil(targetUsuarioId)
        } else {
            mostrarError("No se detectó usuario logueado.")
        }

        fabEditToggle.visibility = if (targetUsuarioId == usuarioLogueadoId) View.VISIBLE else View.GONE
        actualizarModoEdicion()

        return view
    }

    private fun vincularVistas(view: View) {
        txtDisplayName = view.findViewById(R.id.txtDisplayName)
        txtDisplayPosicion = view.findViewById(R.id.txtDisplayPosicion)
        txtOvr = view.findViewById(R.id.txtOvr)
        txtPosicionCard = view.findViewById(R.id.txtPosicion)
        
        edtNombre = view.findViewById(R.id.edtNombre)
        edtApellido = view.findViewById(R.id.edtApellido)
        edtAltura = view.findViewById(R.id.edtAltura)
        edtPeso = view.findViewById(R.id.edtPeso)
        spinnerTipoJuego = view.findViewById(R.id.spinnerTipoJuego)
        spinnerPosicion = view.findViewById(R.id.spinnerPosicion)
        
        btnActualizarPerfil = view.findViewById(R.id.btnActualizarPerfil)
        btnEliminarPerfil = view.findViewById(R.id.btnEliminarPerfil)
        fabEditToggle = view.findViewById(R.id.fabEditToggle)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }

    private fun configurarSpinners() {
        // Estilo de Juego
        val estilos = arrayOf("Fútbol 11", "Fútbol 7", "Futsal")
        val adapterEstilos = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, estilos) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        spinnerTipoJuego.adapter = adapterEstilos

        // Posiciones
        val posiciones = arrayOf("POR", "DFC", "LD", "LI", "MC", "MCD", "MCI", "MD", "MI", "ED", "EI", "DC", "ALA", "CIERRE", "PÍVOT")
        val adapterPosiciones = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, posiciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        spinnerPosicion.adapter = adapterPosiciones
    }

    private fun configurarEventos() {
        fabEditToggle.setOnClickListener {
            modoEdicion = !modoEdicion
            actualizarModoEdicion()
        }

        btnActualizarPerfil.setOnClickListener {
            if (dbPerfilId == -1) {
                mostrarError("No se puede actualizar: ID de perfil no obtenido.")
                return@setOnClickListener
            }

            val p = Perfil(
                usuario_id = usuarioLogueadoId,
                nombre = edtNombre.text.toString().trim(),
                apellido = edtApellido.text.toString().trim(),
                altura = edtAltura.text.toString().toDoubleOrNull(),
                peso = edtPeso.text.toString().toDoubleOrNull(),
                tipo_juego = spinnerTipoJuego.selectedItem.toString(),
                posicion_juego = spinnerPosicion.selectedItem.toString()
            )
            mostrarCargando(true)
            profileController.actualizarTodo(dbPerfilId, p, usuarioLogueadoId)
        }

        btnEliminarPerfil.setOnClickListener {
            mostrarCargando(true)
            settingsController.eliminarCuenta(usuarioLogueadoId)
        }
    }

    private fun actualizarModoEdicion() {
        val visibility = if (modoEdicion) View.VISIBLE else View.GONE
        val isEnabled = modoEdicion
        
        edtNombre.isEnabled = isEnabled
        edtApellido.isEnabled = isEnabled
        edtAltura.isEnabled = isEnabled
        edtPeso.isEnabled = isEnabled
        spinnerTipoJuego.isEnabled = isEnabled
        spinnerPosicion.isEnabled = isEnabled
        
        btnActualizarPerfil.visibility = visibility
        btnEliminarPerfil.visibility = visibility
        
        fabEditToggle.setImageResource(
            if (modoEdicion) android.R.drawable.ic_menu_close_clear_cancel 
            else android.R.drawable.ic_menu_edit
        )
    }

    fun actualizarUI(perfil: Perfil) {
        mostrarCargando(false)
        this.dbPerfilId = perfil.id ?: -1
        
        // Cabecera
        val nombre = perfil.nombre ?: ""
        val apellido = perfil.apellido ?: ""
        txtDisplayName.text = if ("$nombre $apellido".trim().isEmpty()) "Sin Nombre" else "$nombre $apellido"
        
        val pos = perfil.posicion_juego ?: "---"
        txtDisplayPosicion.text = pos
        txtPosicionCard.text = if (pos.length > 3) pos.take(3).uppercase() else pos.uppercase()
        
        // Formulario
        edtNombre.setText(nombre)
        edtApellido.setText(apellido)
        edtAltura.setText(perfil.altura?.toInt()?.toString() ?: "")
        edtPeso.setText(perfil.peso?.toInt()?.toString() ?: "")
        
        // Spinners
        setSpinnerSelection(spinnerTipoJuego, perfil.tipo_juego ?: "Fútbol 11")
        setSpinnerSelection(spinnerPosicion, perfil.posicion_juego ?: "DC")
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as? ArrayAdapter<String>
        val pos = adapter?.getPosition(value) ?: 0
        if (pos >= 0) spinner.setSelection(pos)
    }

    fun mostrarError(mensaje: String) {
        mostrarCargando(false)
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    fun mostrarMensajeExito(mensaje: String) {
        mostrarCargando(false)
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        modoEdicion = false
        actualizarModoEdicion()
    }

    fun redirigirAlLogin() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, LoginFragment())
            .commit()
    }

    fun mostrarCargando(visible: Boolean) {
        loadingOverlay.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
