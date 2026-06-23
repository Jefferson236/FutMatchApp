package com.example.futmatchapp.modelo

data class ReviewModel(
    val id: Int? = null,
    val matchId: Int,
    val evaluadorId: Int,
    val evaluadoId: Int,
    val estrellas: Int,
    val boostStats: String
)