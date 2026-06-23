package com.example.futmatchapp.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.modelo.BurbujaData

class BubbleCardAdapter(private var bubbles: List<BurbujaData>) :
    RecyclerView.Adapter<BubbleCardAdapter.BubbleViewHolder>() {

    class BubbleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Aquí están los IDs corregidos para que coincidan con tu diseño
        val ivBackground: ImageView = view.findViewById(R.id.imgCancha)
        val tvName: TextView = view.findViewById(R.id.tvTitulo)
        val tvDate: TextView = view.findViewById(R.id.tvFecha)
        val tvLocation: TextView = view.findViewById(R.id.tvUbicacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BubbleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bubble_card, parent, false)
        return BubbleViewHolder(view)
    }

    override fun onBindViewHolder(holder: BubbleViewHolder, position: Int) {
        val bubble = bubbles[position]
        holder.tvName.text = bubble.tipoJuego // Usamos tipoJuego como nombre para la prueba
        holder.tvDate.text = bubble.fechaHora
        holder.tvLocation.text = bubble.ubicacionLugar

        // Carga una imagen por defecto de los recursos (asegúrate de que pitch_background exista)
        holder.ivBackground.setImageResource(R.drawable.cancha_futbol)
    }

    override fun getItemCount(): Int = bubbles.size

    fun updateData(newBubbles: List<BurbujaData>) {
        this.bubbles = newBubbles
        notifyDataSetChanged()
    }
}