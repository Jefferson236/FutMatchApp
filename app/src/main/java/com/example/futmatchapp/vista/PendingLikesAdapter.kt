package com.example.futmatchapp.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.SwipeEnriquecido
import com.squareup.picasso.Picasso

class PendingLikesAdapter(
    private val likes: List<SwipeEnriquecido>,
    private val onAccept: (SwipeEnriquecido) -> Unit,
    private val onReject: (SwipeEnriquecido) -> Unit
) : RecyclerView.Adapter<PendingLikesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivLikerPhoto)
        val tvName: TextView = view.findViewById(R.id.tvLikerName)
        val tvInfo: TextView = view.findViewById(R.id.tvLikerInfo)
        val btnAccept: ImageButton = view.findViewById(R.id.btnAccept)
        val btnReject: ImageButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val swipe = likes[position]
        holder.tvName.text = swipe.nombreEmisor
        
        val prefijo = if (swipe.tipoReceptor == "burbuja") "A tu Burbuja" else "A ti"
        holder.tvInfo.text = "$prefijo | ${swipe.infoAdicional ?: ""}"

        if (!swipe.avatarUrl.isNullOrEmpty()) {
            Picasso.get().load(swipe.avatarUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivPhoto)
        }

        holder.btnAccept.setOnClickListener { onAccept(swipe) }
        holder.btnReject.setOnClickListener { onReject(swipe) }
    }

    override fun getItemCount() = likes.size
}