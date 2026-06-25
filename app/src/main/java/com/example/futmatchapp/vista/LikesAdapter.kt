package com.example.futmatchapp.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.PerfilEntidad
import com.squareup.picasso.Picasso

class LikesAdapter(
    private var likes: List<PerfilEntidad>,
    private val onAcceptClick: (PerfilEntidad) -> Unit
) : RecyclerView.Adapter<LikesAdapter.LikeViewHolder>() {

    class LikeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivLikerPhoto)
        val tvName: TextView = view.findViewById(R.id.tvLikerName)
        val tvInfo: TextView = view.findViewById(R.id.tvLikerInfo)
        val btnAccept: ImageButton = view.findViewById(R.id.btnAccept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like, parent, false)
        return LikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val liker = likes[position]
        holder.tvName.text = "${liker.nombre} ${liker.apellido}"
        holder.tvInfo.text = "${liker.posicion_juego} | OVR: ${liker.ovr}"

        if (!liker.avatar_url.isNullOrEmpty()) {
            Picasso.get()
                .load(liker.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivPhoto)
        }

        holder.btnAccept.setOnClickListener { onAcceptClick(liker) }
    }

    override fun getItemCount() = likes.size

    fun updateData(newLikes: List<PerfilEntidad>) {
        this.likes = newLikes
        notifyDataSetChanged()
    }
}