package com.example.futmatchapp.vista

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.futmatchapp.R
import com.example.futmatchapp.controlador.ProfileController
import com.example.futmatchapp.controlador.ReviewController

class ProfileFragment : Fragment() {

    private val profileController = ProfileController()
    private val reviewController = ReviewController()
    private val perfilIdMock = 1
    private val matchIdMock = 99
    private val currentUserId = 1

    private lateinit var imgProfile: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imgProfile.setImageURI(it)
            Toast.makeText(context, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val txtOvr = view.findViewById<TextView>(R.id.txtOvr)
        imgProfile = view.findViewById(R.id.imgProfile)
        val btnEditPhoto = view.findViewById<ImageView>(R.id.btnEditPhoto)
        val edtPlayerName = view.findViewById<EditText>(R.id.edtPlayerName)

        val txtPac = view.findViewById<TextView>(R.id.txtPac)
        val txtSho = view.findViewById<TextView>(R.id.txtSho)
        val txtPas = view.findViewById<TextView>(R.id.txtPas)
        val txtDri = view.findViewById<TextView>(R.id.txtDri)
        val txtDef = view.findViewById<TextView>(R.id.txtDef)
        val txtPhy = view.findViewById<TextView>(R.id.txtPhy)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmitReview = view.findViewById<Button>(R.id.btnSubmitReview)
        val sliderPace = view.findViewById<SeekBar>(R.id.sliderPace)
        val lblPace = view.findViewById<TextView>(R.id.lblPace)

        val historial = profileController.obtenerHistorialCalificaciones(perfilIdMock)
        val ovrCalculado = profileController.calcularOvrDinamico(historial)
        txtOvr.text = ovrCalculado.toString()

        val stats = profileController.obtainerEstadisticasJugador(perfilIdMock)
        txtPac.text = "${stats.pac} PAC"
        txtSho.text = "${stats.sho} SHO"
        txtPas.text = "${stats.pas} PAS"
        txtDri.text = "${stats.dri} DRI"
        txtDef.text = "${stats.def} DEF"
        txtPhy.text = "${stats.phy} PHY"

        btnEditPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        sliderPace.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lblPace.text = "Ritmo (PAC): +$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSubmitReview.setOnClickListener {
            val success = reviewController.procesarYEnviarResena(
                matchId = matchIdMock,
                evaluadorId = currentUserId,
                evaluadoId = perfilIdMock,
                estrellas = ratingBar.rating.toInt(),
                pac = sliderPace.progress,
                sho = 0,
                pas = 0
            )

            if (success) {
                Toast.makeText(context, "Calificación guardada y estadísticas actualizadas", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}