package com.example.futmatchapp.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.ChatItem
import com.squareup.picasso.Picasso

class ChatsAdapter(
    private val chats: List<ChatItem>,
    private val onClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivChatPhoto)
        val tvName: TextView = view.findViewById(R.id.tvChatName)
        val tvLastMsg: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvChatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvName.text = chat.nombreOtro
        holder.tvLastMsg.text = chat.ultimoMensaje ?: "Sin mensajes aún"
        holder.tvTime.text = chat.fecha

        if (!chat.avatarOtro.isNullOrEmpty()) {
            Picasso.get().load(chat.avatarOtro)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivPhoto)
        }

        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount() = chats.size
}