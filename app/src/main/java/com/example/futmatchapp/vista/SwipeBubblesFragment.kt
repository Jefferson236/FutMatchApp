package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import kotlin.math.abs
import com.example.futmatchapp.SessionManager
import com.example.futmatchapp.R
import com.example.futmatchapp.controlador.ExploreController
import com.example.futmatchapp.modelo.BurbujaData
import com.example.futmatchapp.modelo.BurbujaEnriquecida
import com.example.futmatchapp.modelo.MatchResponse
import com.example.futmatchapp.modelo.PerfilEntidad
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class SwipeBubblesFragment : Fragment(), ExploreController.ExploreCallbackInterface {

    private lateinit var controller: ExploreController
    private lateinit var progressBar: ProgressBar
    private lateinit var rvCards: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var fabAddPost: FloatingActionButton
    
    private lateinit var adapter: ExploreAdapter
    private var displayList = mutableListOf<Any>()
    private var miPerfilId: Int = -1
    private var isPlayerMode = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe_bubbles, container, false)
        
        val sessionManager = SessionManager(requireContext())
        miPerfilId = sessionManager.getPerfilId()
        
        // Fallback para pruebas si no hay sesión
        if (miPerfilId == -1) {
            miPerfilId = 1
        }
        
        progressBar = view.findViewById(R.id.progressExplore)
        rvCards = view.findViewById(R.id.rvCards)
        tabLayout = view.findViewById(R.id.tabLayoutMode)
        fabAddPost = view.findViewById(R.id.fabAddPost)
        
        setupRecyclerView()
        setupTabs()
        
        controller = ExploreController(this)
        
        cargarDatos()

        fabAddPost.setOnClickListener { mostrarDialogoCrearPost() }

        return view
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isPlayerMode = tab?.position == 0
                // El FAB siempre debe estar disponible en modo equipos, incluso si está vacío
                fabAddPost.visibility = if (isPlayerMode) View.GONE else View.VISIBLE
                cargarDatos()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Asegurar estado inicial del FAB
        fabAddPost.visibility = if (isPlayerMode) View.GONE else View.VISIBLE
    }

    private fun cargarDatos() {
        progressBar.visibility = View.VISIBLE
        if (isPlayerMode) {
            controller.descargarFeedPerfiles()
        } else {
            controller.descargarFeedBurbujas()
        }
    }

    private fun setupRecyclerView() {
        adapter = ExploreAdapter(displayList)
        rvCards.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean = false
            override fun canScrollHorizontally(): Boolean = false
        }
        rvCards.adapter = adapter
        
        // Si la lista está vacía, mostramos un mensaje o fondo negro (ya es negro por defecto)
        if (displayList.isEmpty()) {
            progressBar.visibility = View.GONE
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val ivLike: View? = when (viewHolder) {
                    is ExploreAdapter.PlayerViewHolder -> viewHolder.ivLike
                    is ExploreAdapter.BubbleViewHolder -> viewHolder.ivLike
                    else -> null
                }
                val ivDislike: View? = when (viewHolder) {
                    is ExploreAdapter.PlayerViewHolder -> viewHolder.ivDislike
                    is ExploreAdapter.BubbleViewHolder -> viewHolder.ivDislike
                    else -> null
                }

                val alpha = (abs(dX) / (recyclerView.width / 2)).coerceIn(0f, 1f)

                if (dX > 0) {
                    ivLike?.visibility = View.VISIBLE
                    ivLike?.alpha = alpha
                    ivDislike?.visibility = View.GONE
                } else if (dX < 0) {
                    ivDislike?.visibility = View.VISIBLE
                    ivDislike?.alpha = alpha
                    ivLike?.visibility = View.GONE
                } else {
                    ivLike?.visibility = View.GONE
                    ivDislike?.visibility = View.GONE
                }

                // Efecto de rotación cool
                val rotation = dX / 20
                viewHolder.itemView.rotation = rotation
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.rotation = 0f
                
                when (viewHolder) {
                    is ExploreAdapter.PlayerViewHolder -> {
                        viewHolder.ivLike?.visibility = View.GONE
                        viewHolder.ivDislike?.visibility = View.GONE
                    }
                    is ExploreAdapter.BubbleViewHolder -> {
                        viewHolder.ivLike?.visibility = View.GONE
                        viewHolder.ivDislike?.visibility = View.GONE
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION || position >= displayList.size) return

                val item = displayList[position]
                
                if (direction == ItemTouchHelper.RIGHT) {
                    val receptorPerfilId = when (item) {
                        is PerfilEntidad -> item.id
                        is BurbujaEnriquecida -> item.creadorId
                        else -> -1
                    }
                    
                    if (receptorPerfilId != -1) {
                        android.util.Log.d("FutMatch", "Enviando Like Real: Emisor(Perfil)=$miPerfilId -> Receptor(Perfil)=$receptorPerfilId")
                        controller.procesarDeslizamiento(miPerfilId, receptorPerfilId, true, isPlayerMode)
                    } else {
                        android.util.Log.e("FutMatch", "Error: ReceptorPerfilId es nulo o inválido")
                    }
                }
                
                displayList.removeAt(position)
                adapter.notifyItemRemoved(position)
                
                if (displayList.isEmpty()) {
                    Toast.makeText(context, "No hay más resultados", Toast.LENGTH_SHORT).show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(rvCards)
    }

    private fun mostrarDialogoCrearPost() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_post, null)
        val spinnerPosicion = dialogView.findViewById<Spinner>(R.id.spinnerPosicionPost)
        val etComentario = dialogView.findViewById<EditText>(R.id.etComentarioPost)
        val etUbicacion = dialogView.findViewById<EditText>(R.id.etUbicacionPost)
        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnTime = dialogView.findViewById<Button>(R.id.btnPickTime)
        val tvDateTime = dialogView.findViewById<TextView>(R.id.tvSelectedDateTime)

        var selectedDate = ""
        var selectedTime = ""

        // Configurar Spinner de posiciones
        val posiciones = arrayOf("Cualquiera", "POR", "DFC", "LD", "LI", "MC", "ED", "EI", "DC", "ALA", "CIERRE", "PÍVOT")
        val adapterPos = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, posiciones)
        spinnerPosicion.adapter = adapterPos

        btnDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "$y-${m + 1}-$d"
                tvDateTime.text = "$selectedDate $selectedTime".trim()
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        btnTime.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.TimePickerDialog(requireContext(), { _, h, min ->
                selectedTime = String.format("%02d:%02d", h, min)
                tvDateTime.text = "$selectedDate $selectedTime".trim()
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Publicar") { _, _ ->
                val fechaFinal = if (selectedDate.isEmpty()) "Hoy" else "$selectedDate $selectedTime"
                val nuevoPost = BurbujaData(
                    creadorId = miPerfilId,
                    tipoJuego = "Fútbol",
                    mensajePersonalizado = etComentario.text.toString(),
                    ubicacionLugar = etUbicacion.text.toString(),
                    cuotaAPagar = 0.0,
                    posicionNecesitada = spinnerPosicion.selectedItem.toString(),
                    fechaHora = fechaFinal
                )
                controller.crearBurbuja(nuevoPost)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onPerfilesCargados(perfiles: List<PerfilEntidad>) {
        progressBar.visibility = View.GONE
        displayList.clear()
        displayList.addAll(perfiles)
        adapter.notifyDataSetChanged()
    }

    override fun onBurbujasCargadas(burbujas: List<BurbujaEnriquecida>) {
        progressBar.visibility = View.GONE
        displayList.clear()
        displayList.addAll(burbujas)
        adapter.notifyDataSetChanged()
    }

    override fun onBurbujaCreada() {
        Toast.makeText(context, "Post publicado con éxito", Toast.LENGTH_SHORT).show()
        cargarDatos()
    }

    override fun onMatchGenerado(matchData: MatchResponse) {
        Toast.makeText(context, "¡MATCH!", Toast.LENGTH_LONG).show()
    }

    override fun onErrorExploracion(mensaje: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}