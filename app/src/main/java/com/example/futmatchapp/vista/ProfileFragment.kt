package com.example.futmatchapp.vista

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futmatchapp.R
import com.example.futmatchapp.SessionManager
import com.example.futmatchapp.controlador.ProfileController
import com.example.futmatchapp.controlador.SettingsController
import com.example.futmatchapp.modelo.Perfil
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.utils.ImageUploader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profileController: ProfileController
    private lateinit var settingsController: SettingsController
    private lateinit var sessionManager: SessionManager
    
    private var usuarioLogueadoId: Int = -1 
    private var dbPerfilId: Int = -1
    private var modoEdicion: Boolean = false

    // Photos
    private var newAvatarUri: Uri? = null
    private var newBannerUri: Uri? = null
    private var avatarUrlActual: String? = null
    private var bannerUrlActual: String? = null

    // Media Launchers
    private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            newAvatarUri = it
            imgProfile.setImageURI(it)
        }
    }

    private val pickBanner = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            newBannerUri = it
            imgBannerProfile.setImageURI(it)
        }
    }

    private val pickGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { subirImagenAGaleria(it) }
    }

    // Header Display (In Card)
    private lateinit var txtOvr: TextView
    private lateinit var txtPosicionCard: TextView
    private lateinit var txtDisplayNameInCard: TextView
    private lateinit var imgProfile: ShapeableImageView
    private lateinit var btnEditPhoto: View
    private lateinit var imgBannerProfile: ImageView
    
    // Stats Display
    private lateinit var txtPac: TextView
    private lateinit var txtSho: TextView
    private lateinit var txtPas: TextView
    private lateinit var txtDri: TextView
    private lateinit var txtDef: TextView
    private lateinit var txtPhy: TextView

    // Header Display (Original)
    private lateinit var txtDisplayName: TextView
    private lateinit var txtDisplayPosicion: TextView
    
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
    
    private lateinit var galleryContainer: LinearLayout
    private lateinit var btnAddPhoto: ImageButton

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
            // Cargar estadísticas si tenemos el ID del perfil más adelante
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
        
        // Card Views
        txtOvr = view.findViewById(R.id.txtOvr)
        txtPosicionCard = view.findViewById(R.id.txtPosicion)
        txtDisplayNameInCard = view.findViewById(R.id.txtDisplayNameInCard)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto)
        imgBannerProfile = view.findViewById(R.id.imgBannerProfile)
        
        // Stats
        txtPac = view.findViewById(R.id.txtPac)
        txtSho = view.findViewById(R.id.txtSho)
        txtPas = view.findViewById(R.id.txtPas)
        txtDri = view.findViewById(R.id.txtDri)
        txtDef = view.findViewById(R.id.txtDef)
        txtPhy = view.findViewById(R.id.txtPhy)
        
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
        
        galleryContainer = view.findViewById(R.id.galleryContainerProfile)
        btnAddPhoto = view.findViewById(R.id.btnAddPhotoProfile)
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

        btnEditPhoto.setOnClickListener { if (modoEdicion) pickAvatar.launch("image/*") }
        imgBannerProfile.setOnClickListener { if (modoEdicion) pickBanner.launch("image/*") }
        btnAddPhoto.setOnClickListener { pickGallery.launch("image/*") }

        btnActualizarPerfil.setOnClickListener {
            if (dbPerfilId == -1) {
                mostrarError("No se puede actualizar: ID de perfil no obtenido.")
                return@setOnClickListener
            }

            mostrarCargando(true)
            
            // 1. Subir imágenes si han cambiado
            var avatarUrlFinal: String? = avatarUrlActual
            var bannerUrlFinal: String? = bannerUrlActual
            
            val totalUploads = (if (newAvatarUri != null) 1 else 0) + (if (newBannerUri != null) 1 else 0)
            if (totalUploads == 0) {
                enviarActualizacion(avatarUrlFinal, bannerUrlFinal)
                return@setOnClickListener
            }

            var uploadsDone = 0
            newAvatarUri?.let { uri ->
                ImageUploader.uploadImage(requireContext(), uri) { url: String? ->
                    if (url != null) avatarUrlFinal = url
                    uploadsDone++
                    if (uploadsDone == totalUploads) enviarActualizacion(avatarUrlFinal, bannerUrlFinal)
                }
            }
            newBannerUri?.let { uri ->
                ImageUploader.uploadImage(requireContext(), uri) { url: String? ->
                    if (url != null) bannerUrlFinal = url
                    uploadsDone++
                    if (uploadsDone == totalUploads) enviarActualizacion(avatarUrlFinal, bannerUrlFinal)
                }
            }
        }

        btnEliminarPerfil.setOnClickListener {
            mostrarCargando(true)
            settingsController.eliminarCuenta(usuarioLogueadoId)
        }
    }

    private fun enviarActualizacion(avatarUrl: String?, bannerUrl: String?) {
        Log.d("FutMatch", "ProfileFragment - Enviando actualización: Avatar=$avatarUrl, Banner=$bannerUrl")
        // Usamos Perfil de Models.kt que es el que espera profileController.actualizarTodo
        val perfilModels = com.example.futmatchapp.modelo.Perfil(
            id = dbPerfilId,
            usuario_id = usuarioLogueadoId,
            nombre = edtNombre.text.toString().trim(),
            apellido = edtApellido.text.toString().trim(),
            altura = edtAltura.text.toString().toDoubleOrNull(),
            peso = edtPeso.text.toString().toDoubleOrNull(),
            tipo_juego = spinnerTipoJuego.selectedItem.toString(),
            posicion_juego = spinnerPosicion.selectedItem.toString(),
            avatar_url = avatarUrl,
            banner_url = bannerUrl
        )
        
        profileController.actualizarTodo(dbPerfilId, perfilModels, usuarioLogueadoId)
    }

    private fun subirImagenAGaleria(uri: Uri) {
        mostrarCargando(true)
        ImageUploader.uploadImage(requireContext(), uri) { url ->
            if (url != null) {
                val g = com.example.futmatchapp.modelo.Galeria(perfil_id = dbPerfilId, url_foto = url)
                // Usamos el controlador para subir a la API
                CoroutineScope(Dispatchers.IO).launch {
                    val response = RetrofitClient.create().subirFoto(g)
                    withContext(Dispatchers.Main) {
                        mostrarCargando(false)
                        if (response.isSuccessful) {
                            agregarVistaGaleria(url)
                            mostrarMensajeExito("Imagen añadida a la galería.")
                        } else {
                            mostrarError("Error al guardar en galería.")
                        }
                    }
                }
            } else {
                mostrarCargando(false)
                mostrarError("Error al subir la imagen.")
            }
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
        btnAddPhoto.visibility = visibility
        
        // El botón de editar foto (en la tarjeta) y el banner ahora responden al click
        btnEditPhoto.isClickable = true 
        imgBannerProfile.isClickable = true

        fabEditToggle.setImageResource(
            if (modoEdicion) android.R.drawable.ic_menu_close_clear_cancel 
            else android.R.drawable.ic_menu_edit
        )
    }

    fun actualizarUI(perfil: Perfil) {
        mostrarCargando(false)
        this.dbPerfilId = perfil.id ?: -1
        this.avatarUrlActual = perfil.avatar_url
        this.bannerUrlActual = perfil.banner_url
        
        // Cargar fotos con Picasso
        if (!perfil.avatar_url.isNullOrEmpty()) {
            Picasso.get().load(perfil.avatar_url).placeholder(R.drawable.ic_launcher_background).into(imgProfile)
        }
        if (!perfil.banner_url.isNullOrEmpty()) {
            Picasso.get().load(perfil.banner_url).into(imgBannerProfile)
        }

        // Cargar galería
        cargarGaleria()
        
        // Cargar estadísticas vinculadas al perfil
        if (dbPerfilId != -1) {
            profileController.cargarEstadisticas(dbPerfilId)
        }
        
        // Cabecera
        val nombre = perfil.nombre ?: ""
        val apellido = perfil.apellido ?: ""
        val nombreCompleto = if ("$nombre $apellido".trim().isEmpty()) "Sin Nombre" else "$nombre $apellido"
        
        txtDisplayName.text = nombreCompleto
        txtDisplayNameInCard.text = nombre.uppercase()
        
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

    private fun cargarGaleria() {
        galleryContainer.removeAllViews()
        galleryContainer.addView(btnAddPhoto)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.create().getGaleria()
                if (response.isSuccessful && response.body() != null) {
                    val fotos = response.body()!!.data.filter { it.perfil_id == dbPerfilId }
                    withContext(Dispatchers.Main) {
                        fotos.forEach { agregarVistaGaleria(it.url_foto) }
                    }
                }
            } catch (e: Exception) { Log.e("Profile", "Error galeria", e) }
        }
    }

    private fun agregarVistaGaleria(url: String) {
        val iv = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(250, 250).apply { setMargins(0,0,15,0) }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Picasso.get().load(url).into(iv)
        galleryContainer.addView(iv, galleryContainer.childCount - 1)
    }

    fun actualizarEstadisticas(stats: com.example.futmatchapp.modelo.Estadistica) {
        Log.d("FutMatch", "ProfileFragment - Actualizando UI con estadísticas: OVR=${stats.ovr}")
        txtOvr.text = stats.ovr.toString()
        txtPac.text = "PAC ${stats.pac}"
        txtSho.text = "SHO ${stats.sho}"
        txtPas.text = "PAS ${stats.pas}"
        txtDri.text = "DRI ${stats.dri}"
        txtDef.text = "DEF ${stats.def}"
        txtPhy.text = "PHY ${stats.phy}"
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
