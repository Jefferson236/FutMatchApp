package com.example.futmatchapp.modelo

data class StatsModel(
    val id: Int? = null,
    val perfilId: Int,
    val ovr: Int,
    val pac: Int,
    val sho: Int,
    val pas: Int,
    val dri: Int,
    val def: Int,
    val phy: Int
)