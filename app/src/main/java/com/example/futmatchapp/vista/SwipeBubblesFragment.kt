package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futmatchapp.R
import com.example.futmatchapp.controlador.ExploreController
import com.example.futmatchapp.modelo.BurbujaData
import com.example.futmatchapp.modelo.MatchResponse

class SwipeBubblesFragment : Fragment(), ExploreController.ExploreCallbackInterface {

    private lateinit var controller: ExploreController
    private lateinit var progressBar: ProgressBar
    private lateinit var rvCards: RecyclerView
    private lateinit var adapter: BubbleCardAdapter
    private var bubblesList = mutableListOf<BurbujaData>()
    private var miId: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe_bubbles, container, false)
        
        progressBar = view.findViewById(R.id.progressExplore)
        rvCards = view.findViewById(R.id.rvCards)
        
        setupRecyclerView()
        
        controller = ExploreController(this)
        
        // Cargar datos de prueba
        cargarDatosPrueba()

        return view
    }

    private fun setupRecyclerView() {
        adapter = BubbleCardAdapter(bubblesList)
        // Usamos un LayoutManager que no permita scroll manual para forzar el swipe
        rvCards.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean = false
            override fun canScrollHorizontally(): Boolean = false
        }
        rvCards.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val bubble = bubblesList[position]
                
                if (direction == ItemTouchHelper.RIGHT) {
                    Toast.makeText(context, "¡Like a ${bubble.tipoJuego}!", Toast.LENGTH_SHORT).show()
                    controller.procesarDeslizamiento(miId, bubble.id, true, false)
                } else {
                    Toast.makeText(context, "Descartado: ${bubble.tipoJuego}", Toast.LENGTH_SHORT).show()
                }
                
                bubblesList.removeAt(position)
                adapter.notifyItemRemoved(position)
                
                if (bubblesList.isEmpty()) {
                    Toast.makeText(context, "No hay más partidos cerca", Toast.LENGTH_LONG).show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(rvCards)
    }

    private fun cargarDatosPrueba() {
        bubblesList.clear()
        bubblesList.add(BurbujaData(id = 1, creadorId = 10, tipoJuego = "Fútbol 7 Amigos", mensajePersonalizado = "Falta uno!", ubicacionLugar = "Cancha Central", cuotaAPagar = 5.0, posicionNecesitada = "Portero", fechaHora = "Hoy 21:00"))
        bubblesList.add(BurbujaData(id = 2, creadorId = 11, tipoJuego = "Futsal Relámpago", mensajePersonalizado = "Nivel intermedio", ubicacionLugar = "Polideportivo", cuotaAPagar = 3.5, posicionNecesitada = "Cierre", fechaHora = "Mañana 19:00"))
        bubblesList.add(BurbujaData(id = 3, creadorId = 12, tipoJuego = "Fútbol 11 Clasico", mensajePersonalizado = "Traer camiseta blanca", ubicacionLugar = "Estadio Municipal", cuotaAPagar = 0.0, posicionNecesitada = "Delantero", fechaHora = "Sábado 10:00"))
        adapter.notifyDataSetChanged()
    }

    override fun onBurbujasCargadas(burbujas: List<BurbujaData>) {
        progressBar.visibility = View.GONE
        bubblesList.clear()
        bubblesList.addAll(burbujas)
        adapter.notifyDataSetChanged()
    }

    override fun onMatchGenerado(matchData: MatchResponse) {
        Toast.makeText(context, "¡MATCH! Has sido aceptado en el equipo.", Toast.LENGTH_LONG).show()
    }

    override fun onErrorExploracion(mensaje: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}