package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.futmatchapp.R
import com.example.futmatchapp.controlador.ExploreController
import com.example.futmatchapp.modelo.BurbujaEnriquecida
import com.example.futmatchapp.modelo.MatchResponse
import com.example.futmatchapp.modelo.PerfilEntidad

class SwipePlayersFragment : Fragment(), ExploreController.ExploreCallbackInterface {

    private lateinit var controller: ExploreController
    private var miId: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe_players, container, false)
        controller = ExploreController(this)

        // Aquí se implementaría el RecyclerView con ItemTouchHelper para el efecto Tinder (Swipe a la derecha/izquierda)
        // Ejemplo de uso al deslizar a un jugador (ID = 5) a la derecha (Like):
        // controller.procesarDeslizamiento(emisorId = miId, receptorId = 5, esLike = true, esHaciaJugador = true)

        return view
    }

    // Un Capitán puede abrir el BottomSheet para ver las stats del jugador antes de darle Like
    private fun abrirDetalleJugador(jugadorId: Int) {
        val bottomSheet = PlayerDetailBottomSheet.newInstance(jugadorId)
        bottomSheet.show(parentFragmentManager, "PlayerDetail")
    }

    override fun onMatchGenerado(matchData: MatchResponse) {
        Toast.makeText(context, "¡IT'S A MATCH! Fichaje completado.", Toast.LENGTH_LONG).show()
        // Navegar a la sala de chat...
    }

    override fun onBurbujasCargadas(burbujas: List<BurbujaEnriquecida>) {} // No se usa en esta vista
    override fun onPerfilesCargados(perfiles: List<PerfilEntidad>) {} // Implementar feed de jugadores
    override fun onBurbujaCreada() {} // No se usa aquí

    override fun onErrorExploracion(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}