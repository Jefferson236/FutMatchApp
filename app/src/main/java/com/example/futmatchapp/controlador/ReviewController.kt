package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.ReviewModel

class ReviewController {

    fun procesarYEnviarResena(matchId: Int, evaluadorId: Int, evaluadoId: Int, estrellas: Int, pac: Int, sho: Int, pas: Int): Boolean {
        val jsonString = "{\"pac\":$pac,\"sho\":$sho,\"pas\":$pas}"
        val resena = ReviewModel(
            matchId = matchId,
            evaluadorId = evaluadorId,
            evaluadoId = evaluadoId,
            estrellas = estrellas,
            boostStats = jsonString
        )
        return true
    }
}