package com.example.futmatchapp.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.futmatchapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlayerDetailBottomSheet : BottomSheetDialogFragment() {

    private var jugadorId: Int = 0

    companion object {
        fun newInstance(id: Int): PlayerDetailBottomSheet {
            val fragment = PlayerDetailBottomSheet()
            val args = Bundle().apply { putInt("JUGADOR_ID", id) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_player_detail, container, false)
        jugadorId = arguments?.getInt("JUGADOR_ID") ?: 0

        val tvHexagonoStats = view.findViewById<TextView>(R.id.tvGraficoHexagonalStats)

        // Aquí se consumiría el controlador para descargar el StatsModel de este jugador específico
        // y se dibujaría el gráfico de radar (hexágono) basado en sus atributos (PAC, SHO, PAS, DRI, DEF, PHY).
        tvHexagonoStats.text = "Cargando radar de atributos para el jugador #$jugadorId..."

        return view
    }
}