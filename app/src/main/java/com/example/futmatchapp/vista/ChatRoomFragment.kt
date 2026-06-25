package com.example.futmatchapp.vista

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.example.futmatchapp.modelo.ChatMessage
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

class ChatRoomFragment : Fragment() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var ivUserPhoto: ShapeableImageView
    private lateinit var tvUserName: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var adapter: ChatMessagesAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private val apiService = RetrofitClient.create()
    
    private var matchId: Int = -1
    private var otroUsuarioId: Int = -1
    private var miPerfilId: Int = -1
    private var nombreOtro: String = ""
    private var avatarOtro: String? = null
    
    private var refreshJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat_room, container, false)
        
        matchId = arguments?.getInt("MATCH_ID") ?: -1
        otroUsuarioId = arguments?.getInt("OTRO_ID") ?: -1
        nombreOtro = arguments?.getString("NOMBRE_OTRO") ?: "Usuario"
        avatarOtro = arguments?.getString("AVATAR_OTRO")
        miPerfilId = SessionManager(requireContext()).getPerfilId()

        enlazarVistas(view)
        setupHeader()
        setupRecyclerView()
        
        cargarMensajesLocales()

        return view
    }

    override fun onResume() {
        super.onResume()
        iniciarSincronizacion()
    }

    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    private fun enlazarVistas(view: View) {
        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage = view.findViewById(R.id.etChatMessage)
        btnSend = view.findViewById(R.id.btnSendMessage)
        ivUserPhoto = view.findViewById(R.id.ivChatUserPhoto)
        tvUserName = view.findViewById(R.id.tvChatUserName)
        btnBack = view.findViewById(R.id.btnBackChat)

        btnSend.setOnClickListener { enviarMensajeRemoto() }
        btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupHeader() {
        tvUserName.text = nombreOtro
        if (!avatarOtro.isNullOrEmpty()) {
            Picasso.get().load(avatarOtro).placeholder(R.drawable.ic_launcher_background).into(ivUserPhoto)
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatMessagesAdapter(messageList)
        rvMessages.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
        rvMessages.adapter = adapter
    }

    private fun iniciarSincronizacion() {
        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                descargarMensajes()
                delay(3000)
            }
        }
    }

    private suspend fun descargarMensajes() {
        try {
            val response = apiService.getChats()
            if (response.isSuccessful && response.body() != null) {
                val todosLosMensajes = response.body()!!.data
                
                // 1. Filtrar mensajes para este match
                val misMensajesRemotos = todosLosMensajes.filter { it.matchId == matchId }
                if (misMensajesRemotos.isEmpty()) return

                withContext(Dispatchers.Main) {
                    var huboCambios = false
                    misMensajesRemotos.forEach { remoto ->
                        // Verificar si ya lo tenemos (por ID o por contenido si es un temporal enviado)
                        val yaExiste = messageList.any { local -> 
                            local.id == remoto.id || (local.id == null && local.text == remoto.text && local.emisorId == remoto.emisorId)
                        }

                        if (!yaExiste) {
                            messageList.add(remoto.copy(isSentByMe = remoto.emisorId == miPerfilId))
                            huboCambios = true
                        } else {
                            // Si existe pero no tenía ID (era temporal), actualizamos con el ID del servidor
                            val index = messageList.indexOfFirst { it.id == null && it.text == remoto.text && it.emisorId == remoto.emisorId }
                            if (index != -1) {
                                messageList[index] = remoto.copy(isSentByMe = true)
                                huboCambios = true
                            }
                        }
                    }

                    if (huboCambios) {
                        messageList.sortBy { it.id ?: Int.MAX_VALUE }
                        adapter.notifyDataSetChanged()
                        rvMessages.scrollToPosition(messageList.size - 1)
                        guardarMensajesLocales()
                    }
                }

                // 2. Lógica Temporal: Borrar del servidor los mensajes que YO recibí
                misMensajesRemotos.forEach { msg ->
                    if (msg.emisorId != miPerfilId) {
                        Log.d("ChatRoom", "Borrando mensaje recibido del servidor: ${msg.id}")
                        apiService.eliminarMensaje(msg.id ?: 0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRoom", "Error al sincronizar mensajes", e)
        }
    }

    private fun enviarMensajeRemoto() {
        val texto = etMessage.text.toString().trim()
        if (texto.isEmpty()) return

        // Optimistic UI: Mostrar de inmediato sin ID
        val mensajeTemp = ChatMessage(
            matchId = matchId,
            emisorId = miPerfilId,
            text = texto,
            isSentByMe = true
        )

        messageList.add(mensajeTemp)
        adapter.notifyItemInserted(messageList.size - 1)
        rvMessages.scrollToPosition(messageList.size - 1)
        etMessage.setText("")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Enviar al servidor (match_id, emisor_id, mensaje)
                val response = apiService.enviarMensaje(mensajeTemp)
                if (!response.isSuccessful) {
                    Log.e("ChatRoom", "Error al enviar: ${response.code()} - ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRoom", "Excepción al enviar", e)
            }
        }
    }

    private fun guardarMensajesLocales() {
        val prefs = requireContext().getSharedPreferences("FutMatch_Chats", Context.MODE_PRIVATE)
        val json = Gson().toJson(messageList)
        prefs.edit().putString("history_$matchId", json).apply()
    }

    private fun cargarMensajesLocales() {
        val prefs = requireContext().getSharedPreferences("FutMatch_Chats", Context.MODE_PRIVATE)
        val json = prefs.getString("history_$matchId", null)
        if (json != null) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            val history: List<ChatMessage> = Gson().fromJson(json, type)
            messageList.clear()
            messageList.addAll(history)
            adapter.notifyDataSetChanged()
            rvMessages.scrollToPosition(messageList.size - 1)
        }
    }
}
