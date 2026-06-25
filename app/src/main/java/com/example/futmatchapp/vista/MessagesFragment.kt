package com.example.futmatchapp.vista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.SessionManager
import com.example.futmatchapp.modelo.ChatItem
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesFragment : Fragment() {

    private lateinit var rvChats: RecyclerView
    private lateinit var tvNoChats: TextView
    private lateinit var btnInbox: MaterialButton
    private val apiService = RetrofitClient.create()
    private var miPerfilId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        rvChats = view.findViewById(R.id.rvChats)
        tvNoChats = view.findViewById(R.id.tvNoChats)
        btnInbox = view.findViewById(R.id.btnInbox)

        val sessionManager = SessionManager(requireContext())
        miPerfilId = sessionManager.getPerfilId()

        btnInbox.setOnClickListener {
            findNavController().navigate(R.id.action_messages_to_inbox)
        }

        setupRecyclerView()
        cargarChatsActivos()

        return view
    }

    private fun setupRecyclerView() {
        rvChats.layoutManager = LinearLayoutManager(context)
    }

    private fun cargarChatsActivos() {
        if (miPerfilId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Obtener datos necesarios
                val responseSwipes = apiService.getSwipes()
                val responseMatches = apiService.getMatches()
                val responsePerfiles = apiService.getPerfiles()

                if (responseSwipes.isSuccessful && responsePerfiles.isSuccessful) {
                    val swipes = responseSwipes.body()?.data ?: emptyList()
                    val perfiles = responsePerfiles.body()?.data ?: emptyList()
                    
                    // Manejar el posible error 500 de matches
                    val matches = if (responseMatches.isSuccessful) {
                        responseMatches.body()?.data ?: emptyList()
                    } else {
                        Log.e("MessagesFragment", "Error 500 en api/matches: ${responseMatches.errorBody()?.string()}")
                        emptyList()
                    }

                    // 2. Filtrar swipes donde YO soy el RECEPTOR y es_match = 1
                    val misMatchesSwipes = swipes.filter { 
                        it.es_match == 1 && it.receptor_id == miPerfilId 
                    }

                    val activeChats = misMatchesSwipes.mapNotNull { swipe ->
                        // BUSCAR EL ID DE LA TABLA matches QUE REFERENCIA A ESTE SWIPE
                        val matchRecord = matches.find { it.swipe_id == swipe.id }
                        val realMatchId = matchRecord?.id
                        
                        // Si no hay matchRecord, significa que el match no se ha creado en la DB
                        // o que el endpoint getMatches() falló/está incompleto.
                        if (realMatchId == null) {
                            Log.e("MessagesFragment", "CRÍTICO: No existe registro en tabla 'matches' para el swipe ${swipe.id}")
                        }

                        // El otro usuario es el emisor del swipe
                        val otroId = swipe.emisor_id
                        val otroPerfil = perfiles.find { it.id == otroId }
                        
                        if (otroPerfil != null) {
                            ChatItem(
                                matchId = realMatchId ?: -1, // Si es -1, saldrá "Match no registrado"
                                otroUsuarioId = otroId,
                                nombreOtro = "${otroPerfil.nombre ?: "Usuario"} ${otroPerfil.apellido ?: ""}",
                                avatarOtro = otroPerfil.avatar_url,
                                ultimoMensaje = if (realMatchId == null) "Error: Match no registrado en DB" else "Toca para chatear",
                                fecha = swipe.updated_at?.substringBefore(".") ?: ""
                            )
                        } else null
                    }

                    withContext(Dispatchers.Main) {
                        if (activeChats.isEmpty()) {
                            tvNoChats.visibility = View.VISIBLE
                            rvChats.visibility = View.GONE
                        } else {
                            tvNoChats.visibility = View.GONE
                            rvChats.visibility = View.VISIBLE
                            rvChats.adapter = ChatsAdapter(activeChats) { chat ->
                                if (chat.matchId != -1) {
                                    val bundle = Bundle().apply {
                                        putInt("MATCH_ID", chat.matchId)
                                        putInt("OTRO_ID", chat.otroUsuarioId)
                                        putString("NOMBRE_OTRO", chat.nombreOtro)
                                        putString("AVATAR_OTRO", chat.avatarOtro)
                                    }
                                    findNavController().navigate(R.id.action_messages_to_chatRoom, bundle)
                                } else {
                                    Toast.makeText(context, "No se puede abrir el chat: Falta ID de match", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } else {
                    Log.e("MessagesFragment", "Fallo en swipes o perfiles: S:${responseSwipes.code()} P:${responsePerfiles.code()}")
                }
            } catch (e: Exception) {
                Log.e("MessagesFragment", "Error fatal cargando chats", e)
            }
        }
    }
}
