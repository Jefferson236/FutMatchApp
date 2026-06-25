package com.example.futmatchapp.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.BurbujaEnriquecida
import com.example.futmatchapp.modelo.PerfilEntidad
import com.squareup.picasso.Picasso

class ExploreAdapter(private var items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PLAYER = 1
        private const val TYPE_BUBBLE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is PerfilEntidad) TYPE_PLAYER else TYPE_BUBBLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PLAYER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_card, parent, false)
            PlayerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bubble_card, parent, false)
            BubbleViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PlayerViewHolder) {
            val player = items[position] as PerfilEntidad
            holder.tvName.text = "${player.nombre ?: "N/A"} ${player.apellido ?: ""}"
            holder.tvProfileId.text = "ID Perfil: ${player.id}"
            holder.tvInfo.text = "${player.altura ?: 0}m | ${player.peso ?: 0}kg | ${player.posicion_juego ?: "N/A"} | ${player.pie_dominante ?: "N/A"}"
            holder.tvStats.text = "OVR: ${player.ovr} | PAC: ${player.pac} | SHO: ${player.sho}"
            
            val photoUrl = if (!player.banner_url.isNullOrEmpty()) player.banner_url else player.avatar_url

            if (!photoUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivPhoto)
            }
        } else if (holder is BubbleViewHolder) {
            val bubble = items[position] as BurbujaEnriquecida
            
            holder.tvSeekerName.text = bubble.nombreCreador
            holder.tvProfileId.text = "ID Perfil Creador: ${bubble.creadorId}"
            holder.tvRequirements.text = "Busca: ${bubble.posicion ?: "Cualquiera"}"
            holder.tvComment.text = bubble.mensaje ?: "Sin mensaje"
            holder.tvDetails.text = "${bubble.fechaHora} - ${bubble.ubicacion}"
            
            if (!bubble.avatarUrl.isNullOrEmpty()) {
                Picasso.get().load(bubble.avatarUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivPhoto)
            } else {
                holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Any>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        val tvProfileId: TextView = view.findViewById(R.id.tvProfileId)
        val tvStats: TextView = view.findViewById(R.id.tvPlayerStats)
        val tvInfo: TextView = view.findViewById(R.id.tvPlayerInfo)
        val ivLike: ImageView? = view.findViewById(R.id.ivOverlayLike)
        val ivDislike: ImageView? = view.findViewById(R.id.ivOverlayDislike)
    }

    class BubbleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivSeekerPhoto)
        val tvSeekerName: TextView = view.findViewById(R.id.tvSeekerName)
        val tvProfileId: TextView = view.findViewById(R.id.tvProfileId)
        val tvRequirements: TextView = view.findViewById(R.id.tvRequirements)
        val tvComment: TextView = view.findViewById(R.id.tvBubbleComment)
        val tvDetails: TextView = view.findViewById(R.id.tvBubbleDetails)
        val ivLike: ImageView? = view.findViewById(R.id.ivOverlayLike)
        val ivDislike: ImageView? = view.findViewById(R.id.ivOverlayDislike)
    }
}
