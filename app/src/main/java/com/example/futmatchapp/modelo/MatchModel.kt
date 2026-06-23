package com.example.futmatchapp.modelo

data class MatchModel(
    val id: Int? = null,
    val swipeId: Int,
    val estado: String = "pendiente",
    val fechaMatch: String? = null
)