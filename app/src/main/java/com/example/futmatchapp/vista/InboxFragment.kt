package com.example.futmatchapp.vista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.SessionManager
import com.example.futmatchapp.modelo.Match
import com.example.futmatchapp.modelo.Swipe
import com.example.futmatchapp.modelo.SwipeEnriquecido
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InboxFragment : Fragment() {

    private lateinit var rvLikes: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private val apiService = RetrofitClient.create()
    private var miPerfilId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)
        rvLikes = view.findViewById(R.id.rvPendingLikes)
        tvEmpty = view.findViewById(R.id.tvEmptyInbox)
        btnBack = view.findViewById(R.id.btnBack)

        val sessionManager = SessionManager(requireContext())
        miPerfilId = sessionManager.getPerfilId()

        btnBack.setOnClickListener { findNavController().navigateUp() }

        setupRecyclerView()
        cargarLikesRecibidos()

        return view
    }

    private fun setupRecyclerView() {
        rvLikes.layoutManager = LinearLayoutManager(context)
    }

    private fun cargarLikesRecibidos() {
        if (miPerfilId == -1) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "No se pudo cargar tu perfil."
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseSwipes = apiService.getSwipes()
                if (responseSwipes.isSuccessful && responseSwipes.body() != null) {
                    val todosLosSwipes = responseSwipes.body()!!.data
                    val misLikes = todosLosSwipes.filter { it.receptor_id == miPerfilId && it.es_match == 0 }
                    
                    if (misLikes.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            tvEmpty.visibility = View.VISIBLE
                            rvLikes.visibility = View.GONE
                        }
                    } else {
                        val responsePerfiles = apiService.getPerfiles()
                        val perfiles = responsePerfiles.body()?.data ?: emptyList()
                        
                        val enrichedLikes = misLikes.mapNotNull { swipe ->
                            val emisor = perfiles.find { it.id == swipe.emisor_id }
                            if (emisor != null) {
                                SwipeEnriquecido(
                                    swipeId = swipe.id ?: 0,
                                    emisorId = swipe.emisor_id,
                                    nombreEmisor = "${emisor.nombre ?: "Usuario"} ${emisor.apellido ?: ""}",
                                    avatarUrl = emisor.avatar_url,
                                    tipoReceptor = if (swipe.burbuja_id != null) "burbuja" else "jugador",
                                    receptorId = miPerfilId,
                                    infoAdicional = "Enviado: ${swipe.created_at?.substringBefore(".") ?: "Recientemente"}"
                                )
                            } else null
                        }

                        withContext(Dispatchers.Main) {
                            if (enrichedLikes.isEmpty()) {
                                tvEmpty.visibility = View.VISIBLE
                                rvLikes.visibility = View.GONE
                            } else {
                                tvEmpty.visibility = View.GONE
                                rvLikes.visibility = View.VISIBLE
                                rvLikes.adapter = PendingLikesAdapter(enrichedLikes, 
                                    onAccept = { swipe -> aceptarMatch(swipe.swipeId) },
                                    onReject = { swipe -> rechazarMatch(swipe.swipeId) }
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("InboxFragment", "Error cargando likes", e)
            }
        }
    }

    private fun rechazarMatch(swipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.eliminarSwipe(swipeId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Like rechazado", Toast.LENGTH_SHORT).show()
                        cargarLikesRecibidos()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al rechazar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun aceptarMatch(swipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val currentDateTime = sdf.format(java.util.Date())

                // 1. CREAR EL MATCH EN LA TABLA matches
                val nuevoMatch = Match(
                    swipe_id = swipeId, 
                    estado = "aceptado",
                    fecha_match = currentDateTime
                )
                
                Log.d("InboxFragment", "Registrando match para swipe: $swipeId")
                val responseMatch = apiService.registrarMatch(nuevoMatch)
                
                if (responseMatch.isSuccessful) {
                    // 2. ACTUALIZAR EL SWIPE A es_match = 1
                    // Primero obtenemos el swipe para tener sus datos
                    val responseSwipes = apiService.getSwipes()
                    val swipeActual = responseSwipes.body()?.data?.find { it.id == swipeId }
                    
                    if (swipeActual != null) {
                        val swipeActualizado = swipeActual.copy(es_match = 1)
                        val responseUpdate = apiService.actualizarSwipe(swipeId, swipeActualizado)
                        
                        if (responseUpdate.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "¡Match aceptado! Ahora puedes chatear.", Toast.LENGTH_SHORT).show()
                                cargarLikesRecibidos()
                            }
                        }
                    }
                } else {
                    val errorMsg = responseMatch.errorBody()?.string() ?: "Error desconocido"
                    Log.e("InboxFragment", "Error al registrar match: ${responseMatch.code()} - $errorMsg")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al crear el match en el servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("InboxFragment", "Error procesando aceptación", e)
            }
        }
    }
}
