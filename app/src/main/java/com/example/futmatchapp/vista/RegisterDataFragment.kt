package com.example.futmatchapp.vista

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futmatchapp.R
import com.example.futmatchapp.MainActivity
import com.example.futmatchapp.controlador.OnboardingController
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.button.MaterialButtonToggleGroup

class RegisterDataFragment : Fragment(R.layout.fragment_register_data) {

    private lateinit var controlador: OnboardingController

    // Variables de estado
    private var posicionSeleccionada: String = ""
    private var tipoJuegoSeleccionado: String = "Fútbol 11"
    private var avatarUri: Uri? = null
    private var bannerUri: Uri? = null
    private val galleryUris = mutableListOf<Uri>()

    // Views
    private lateinit var tvAlturaLabel: TextView
    private lateinit var tvPesoLabel: TextView
    private lateinit var tvEdadLabel: TextView
    private lateinit var sliderAltura: Slider
    private lateinit var sliderPeso: Slider
    private lateinit var sliderEdad: RangeSlider
    private lateinit var toggleTipoJuego: MaterialButtonToggleGroup
    private lateinit var canchaContainer: FrameLayout
    private lateinit var galleryContainer: LinearLayout
    private lateinit var imgAvatar: ImageView
    private lateinit var imgBanner: ImageView
    private lateinit var loadingOverlay: View

    // Colores tácticos
    private val colorPortero = Color.parseColor("#9E9E9E") // Gris
    private val colorDefensa = Color.parseColor("#304FFE") // Azul
    private val colorMedio = Color.parseColor("#FFD600")   // Amarillo intenso
    private val colorAtaque = Color.parseColor("#D50000")  // Rojo intenso

    // Media Launchers
    private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            avatarUri = it
            imgAvatar.setImageURI(it)
        }
    }

    private val pickBanner = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            bannerUri = it
            imgBanner.setImageURI(it)
        }
    }

    private val pickGallery = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri ->
            agregarImagenAGaleria(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controlador = OnboardingController(this)

        enlazarVistas(view)
        configurarSliders()
        configurarSelectorCancha()
        configurarFotos()

        view.findViewById<Button>(R.id.btnGuardarPerfil).setOnClickListener {
            enviarDatosAlControlador(view)
        }

        view.findViewById<Button>(R.id.btnSaltarRegistro).setOnClickListener {
            irAlExploradorTinder()
        }
    }

    private fun enlazarVistas(view: View) {
        tvAlturaLabel = view.findViewById(R.id.tvAlturaLabel)
        tvPesoLabel = view.findViewById(R.id.tvPesoLabel)
        tvEdadLabel = view.findViewById(R.id.tvEdadLabel)
        sliderAltura = view.findViewById(R.id.sliderAltura)
        sliderPeso = view.findViewById(R.id.sliderPeso)
        sliderEdad = view.findViewById(R.id.sliderEdad)
        toggleTipoJuego = view.findViewById(R.id.toggleTipoJuego)
        canchaContainer = view.findViewById(R.id.canchaContainer)
        galleryContainer = view.findViewById(R.id.galleryContainer)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        imgBanner = view.findViewById(R.id.imgBanner)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }

    private fun configurarSliders() {
        sliderEdad.values = listOf(18f, 35f)
        sliderAltura.addOnChangeListener { _, value, _ ->
            tvAlturaLabel.text = "Altura: ${value.toInt()} cm"
        }
        sliderPeso.addOnChangeListener { _, value, _ ->
            tvPesoLabel.text = "Peso: ${value.toInt()} kg"
        }
        sliderEdad.addOnChangeListener { slider, _, _ ->
            val min = slider.values[0].toInt()
            val max = slider.values[1].toInt()
            tvEdadLabel.text = "Edades con las que jugarías: $min - $max"
        }
    }

    private fun configurarSelectorCancha() {
        dibujarCanchaF11()

        toggleTipoJuego.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                canchaContainer.removeAllViews()
                posicionSeleccionada = ""
                when (checkedId) {
                    R.id.btnF11 -> {
                        tipoJuegoSeleccionado = "Fútbol 11"
                        dibujarCanchaF11()
                    }
                    R.id.btnF7 -> {
                        tipoJuegoSeleccionado = "Fútbol 7"
                        dibujarCanchaF7()
                    }
                    R.id.btnFutsal -> {
                        tipoJuegoSeleccionado = "Futsal"
                        dibujarCanchaFutsal()
                    }
                }
            }
        }
    }

    private fun dibujarCanchaF11() {
        // Portero
        agregarBotonPosicion("POR", colorPortero, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 15)
        
        // Defensas (4) - Más separados
        agregarBotonPosicion("LD", colorDefensa, Gravity.BOTTOM or Gravity.END, marginBottom = 120, marginSide = 25)
        agregarBotonPosicion("DFC", colorDefensa, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 100, marginSide = -85)
        agregarBotonPosicion("DFC", colorDefensa, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 100, marginSide = 85)
        agregarBotonPosicion("LI", colorDefensa, Gravity.BOTTOM or Gravity.START, marginBottom = 120, marginSide = 25)

        // Mediocampo (3) - Más separados
        agregarBotonPosicion("MC", colorMedio, Gravity.CENTER, marginTop = 40)
        agregarBotonPosicion("MCD", colorMedio, Gravity.CENTER, marginTop = 40, marginSide = -110)
        agregarBotonPosicion("MCI", colorMedio, Gravity.CENTER, marginTop = 40, marginSide = 110)

        // Delantera (3) - Más separados
        agregarBotonPosicion("ED", colorAtaque, Gravity.TOP or Gravity.END, marginTop = 100, marginSide = 25)
        agregarBotonPosicion("DC", colorAtaque, Gravity.TOP or Gravity.CENTER_HORIZONTAL, marginTop = 50)
        agregarBotonPosicion("EI", colorAtaque, Gravity.TOP or Gravity.START, marginTop = 100, marginSide = 25)
    }

    private fun dibujarCanchaF7() {
        agregarBotonPosicion("POR", colorPortero, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 20)
        agregarBotonPosicion("DFC", colorDefensa, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 120, marginSide = -90)
        agregarBotonPosicion("DFC", colorDefensa, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 120, marginSide = 90)
        agregarBotonPosicion("MC", colorMedio, Gravity.CENTER)
        agregarBotonPosicion("MD", colorMedio, Gravity.CENTER_VERTICAL or Gravity.END, marginSide = 30)
        agregarBotonPosicion("MI", colorMedio, Gravity.CENTER_VERTICAL or Gravity.START, marginSide = 30)
        agregarBotonPosicion("DC", colorAtaque, Gravity.TOP or Gravity.CENTER_HORIZONTAL, marginTop = 80)
    }

    private fun dibujarCanchaFutsal() {
        agregarBotonPosicion("POR", colorPortero, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 20)
        agregarBotonPosicion("CIERRE", colorDefensa, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, marginBottom = 140)
        agregarBotonPosicion("ALA", colorMedio, Gravity.CENTER_VERTICAL or Gravity.START, marginSide = 40)
        agregarBotonPosicion("ALA", colorMedio, Gravity.CENTER_VERTICAL or Gravity.END, marginSide = 40)
        agregarBotonPosicion("PÍVOT", colorAtaque, Gravity.TOP or Gravity.CENTER_HORIZONTAL, marginTop = 80)
    }

    private fun agregarBotonPosicion(nombrePos: String, colorBase: Int, posGravity: Int, marginTop: Int = 0, marginBottom: Int = 0, marginSide: Int = 0) {
        val size = 110
        val btn = TextView(context).apply {
            text = nombrePos
            textSize = 11f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.circle_position)
            backgroundTintList = ColorStateList.valueOf(colorBase)
            
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                this.gravity = posGravity
                topMargin = marginTop
                bottomMargin = marginBottom
                
                if ((posGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == Gravity.START) {
                    marginStart = marginSide
                } else if ((posGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == Gravity.END) {
                    marginEnd = marginSide
                } else {
                    if (marginSide > 0) marginStart = marginSide
                    if (marginSide < 0) marginEnd = -marginSide
                }
            }
            
            setOnClickListener {
                seleccionarPosicion(this, nombrePos, colorBase)
            }
            tag = colorBase
        }
        canchaContainer.addView(btn)
    }

    private fun seleccionarPosicion(view: TextView, nombrePos: String, colorBase: Int) {
        for (i in 0 until canchaContainer.childCount) {
            val child = canchaContainer.getChildAt(i) as TextView
            child.isSelected = false
            child.backgroundTintList = ColorStateList.valueOf(child.tag as Int)
            child.setTextColor(Color.WHITE)
        }
        
        view.isSelected = true
        view.backgroundTintList = null // Use selector white color
        view.setTextColor(Color.BLACK)
        posicionSeleccionada = nombrePos
        Toast.makeText(context, "Posición: $nombrePos", Toast.LENGTH_SHORT).show()
    }

    private fun configurarFotos() {
        imgAvatar.setOnClickListener { pickAvatar.launch("image/*") }
        imgBanner.setOnClickListener { pickBanner.launch("image/*") }
        view?.findViewById<View>(R.id.btnAddPhoto)?.setOnClickListener { pickGallery.launch("image/*") }
    }

    private fun agregarImagenAGaleria(uri: Uri) {
        galleryUris.add(uri)
        val iv = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(250, 250).apply {
                setMargins(0, 0, 15, 0)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }
        galleryContainer.addView(iv, galleryContainer.childCount - 1)
    }

    private fun enviarDatosAlControlador(view: View) {
        val username = view.findViewById<EditText>(R.id.edtUsernameNuevo).text.toString().trim()
        val nombre = view.findViewById<EditText>(R.id.edtNombre).text.toString().trim()
        val apellido = view.findViewById<EditText>(R.id.edtApellido).text.toString().trim()

        if (posicionSeleccionada.isEmpty()) {
            mostrarError("¡Toca la cancha para elegir tu posición!")
            return
        }

        controlador.guardarDatosCompletos(
            context = requireContext(),
            username = username,
            nombre = nombre,
            apellido = apellido,
            alturaStr = sliderAltura.value.toString(),
            pesoStr = sliderPeso.value.toString(),
            edadMin = sliderEdad.values[0].toInt(),
            edadMax = sliderEdad.values[1].toInt(),
            posicion = posicionSeleccionada,
            tipoJuego = tipoJuegoSeleccionado,
            avatarUri = avatarUri,
            bannerUri = bannerUri,
            galleryUris = galleryUris
        )
    }

    fun mostrarCargando(visible: Boolean) {
        loadingOverlay.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun mostrarError(mensaje: String) { Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show() }
    fun mostrarError(resId: Int) { Toast.makeText(context, getString(resId), Toast.LENGTH_LONG).show() }

    fun irAlExploradorTinder() {
        (activity as? MainActivity)?.mostrarBottomNavigation()
        findNavController().navigate(R.id.action_register_to_swipeBubbles)
    }

    fun irAInicio(userId: Int) {
        val sessionManager = com.example.futmatchapp.SessionManager(requireContext())
        sessionManager.saveUserId(userId)

        val bundle = Bundle().apply { 
            putInt("USUARIO_ID", userId)
            putInt("PERFIL_ID", userId)
        }
        (activity as? MainActivity)?.mostrarBottomNavigation()
        findNavController().navigate(R.id.action_register_to_swipeBubbles, bundle)
    }
}
