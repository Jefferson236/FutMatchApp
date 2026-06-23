package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.futmatchapp.R
import com.example.futmatchapp.controlador.BubbleController

class CreateBubbleFragment : Fragment(), BubbleController.BubbleCallbackInterface {

    private lateinit var controller: BubbleController
    private var creadorId: Int = 1

    private lateinit var spTipoJuego: Spinner
    private lateinit var etUbicacion: EditText
    private lateinit var etFechaHora: EditText
    private lateinit var etCuota: EditText
    private lateinit var etPosicion: EditText
    private lateinit var etMensaje: EditText
    private lateinit var btnPublicar: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_bubble, container, false)
        creadorId = arguments?.getInt("USUARIO_ID") ?: 1
        controller = BubbleController(this)

        spTipoJuego = view.findViewById(R.id.spinnerTipoJuegoBurbuja)
        etUbicacion = view.findViewById(R.id.etUbicacionBurbuja)
        etFechaHora = view.findViewById(R.id.etFechaHoraBurbuja)
        etCuota = view.findViewById(R.id.etCuotaBurbuja)
        etPosicion = view.findViewById(R.id.etPosicionRequerida)
        etMensaje = view.findViewById(R.id.etMensajeCapitan)
        btnPublicar = view.findViewById(R.id.btnPublicarBurbuja)

        btnPublicar.setOnClickListener {
            controller.procesarNuevaBurbuja(
                creadorId, spTipoJuego.selectedItem.toString(), etUbicacion.text.toString(),
                etFechaHora.text.toString(), etCuota.text.toString(),
                etPosicion.text.toString(), etMensaje.text.toString()
            )
        }
        return view
    }

    override fun onBurbujaCreadaExitosamente() {
        Toast.makeText(context, "¡Partido publicado! Esperando jugadores...", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onErrorCreacion(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }
}