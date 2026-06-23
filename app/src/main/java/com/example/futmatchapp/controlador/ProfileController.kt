package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.ReviewHistoryModel
import com.example.futmatchapp.modelo.StatsModel

class ProfileController {

    fun obtenerHistorialCalificaciones(perfilId: Int): List<ReviewHistoryModel> {
        return listOf(
            ReviewHistoryModel(matchName = "Partido Lunes", rating = 45, date = "18 Jun 2026"),
            ReviewHistoryModel(matchName = "Cancha Chiriguano", rating = 52, date = "20 Jun 2026"),
            ReviewHistoryModel(matchName = "Liga FC", rating = 48, date = "21 Jun 2026")
        )
    }

    fun calcularOvrDinamico(historial: List<ReviewHistoryModel>): Int {
        if (historial.isEmpty()) return 50
        val suma = historial.sumOf { it.rating }
        val promedio = suma / historial.size
        return if (promedio < 50) 50 else promedio
    }

    fun obtainerEstadisticasJugador(perfilId: Int): StatsModel {
        return StatsModel(
            perfilId = perfilId,
            ovr = 0,
            pac = 84,
            sho = 79,
            pas = 81,
            dri = 82,
            def = 55,
            phy = 73
        )
    }
}