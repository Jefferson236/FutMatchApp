package com.example.futmatchapp.modelo

import com.google.gson.annotations.SerializedName

// --- ESTRUCTURA DE SOPORTE (PAGINACIÓN) ---
data class Link(
    val active: Boolean,
    val label: String,
    val page: Int?,
    val url: String?
)

data class Links(
    val first: String?,
    val last: String?,
    val next: String?,
    val prev: String?
)

data class Meta(
    @SerializedName("current_page") val current_page: Int,
    val from: Int?,
    @SerializedName("last_page") val last_page: Int,
    val links: List<Link>?,
    val path: String?,
    @SerializedName("per_page") val per_page: Int,
    val to: Int?,
    val total: Int
)

// --- RESPUESTAS GENÉRICAS ---
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: Meta?,
    val links: Links?
)

// --- USUARIOS ---
data class Usuario(
    val id: Int? = null,
    val email: String,
    @SerializedName("google_auth") val google_auth: Int = 0,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- PERFILES ---
data class Perfil(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("usuario_id") val usuario_id: Int? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido") val apellido: String? = null,
    @SerializedName("altura") val altura: Double? = null,
    @SerializedName("peso") val peso: Double? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("posicion_juego") val posicion_juego: String? = null,
    @SerializedName("tipo_juego") val tipo_juego: String? = null,
    @SerializedName("pie_dominante") val pie_dominante: String? = null,
    @SerializedName("edad_min_preferida") val edad_min_preferida: Int? = null,
    @SerializedName("edad_max_preferida") val edad_max_preferida: Int? = null,
    @SerializedName("avatar_url") val avatar_url: String? = null,
    @SerializedName("banner_url") val banner_url: String? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- GALERIA ---
data class Galeria(
    val id: Int? = null,
    @SerializedName("perfil_id") val perfil_id: Int,
    @SerializedName("url_foto") val url_foto: String,
    val etiquetas: String? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- ESTADISTICAS ---
data class Estadistica(
    val id: Int? = null,
    @SerializedName("perfil_id") val perfil_id: Int,
    @SerializedName("partidos_jugados") val partidos_jugados: Int = 0,
    val ovr: Int = 60,
    val pac: Int = 60,
    val sho: Int = 60,
    val pas: Int = 60,
    val dri: Int = 60,
    val def: Int = 60,
    val phy: Int = 60,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- BURBUJAS DE SOLICITUD ---
data class BurbujaSolicitud(
    val id: Int? = null,
    @SerializedName("creador_id") val creador_id: Int? = null,
    @SerializedName("tipo_juego") val tipo_juego: String? = null,
    @SerializedName("mensaje_personalizado") val mensaje_personalizado: String? = null,
    @SerializedName("ubicacion_lugar") val ubicacion_lugar: String? = null,
    @SerializedName("cuota_a_pagar") val cuota_a_pagar: Double? = null,
    @SerializedName("posicion_necesitada") val posicion_necesitada: String? = null,
    @SerializedName("fecha_hora") val fecha_hora: String? = null,
    val estado: String = "abierta",
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

data class BurbujaEnriquecida(
    @SerializedName("id") val id: Int,
    @SerializedName("creador_id") val creadorId: Int,
    @SerializedName("nombre_creador") val nombreCreador: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("tipo_juego") val tipoJuego: String?,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("ubicacion") val ubicacion: String?,
    @SerializedName("posicion") val posicion: String?,
    @SerializedName("fecha_hora") val fechaHora: String?
)

// --- SWIPE ---
data class Swipe(
    val id: Int? = null,
    @SerializedName("emisor_id") val emisor_id: Int,
    @SerializedName("receptor_id") val receptor_id: Int? = null,
    @SerializedName("burbuja_id") val burbuja_id: Int? = null,
    @SerializedName("es_match") val es_match: Int = 0,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- MATCHES ---
data class Match(
    val id: Int? = null,
    @SerializedName("swipe_id") val swipe_id: Int,
    val estado: String = "pendiente",
    @SerializedName("fecha_match") val fecha_match: String? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- CHATS ---
data class Chat(
    val id: Int? = null,
    @SerializedName("burbuja_id") val burbuja_id: Int,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- RESENAS ---
data class Resena(
    val id: Int? = null,
    @SerializedName("match_id") val match_id: Int,
    @SerializedName("evaluador_id") val evaluador_id: Int,
    @SerializedName("evaluado_id") val evaluado_id: Int,
    val estrellas: Int,
    @SerializedName("boost_stats") val boost_stats: String? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- COMENTARIOS PERFILES ---
data class ComentarioPerfil(
    val id: Int? = null,
    @SerializedName("perfil_id") val perfil_id: Int,
    @SerializedName("autor_id") val autor_id: Int,
    val comentario: String,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)

// --- CHAT MESSAGES (LOCAL & REMOTE) ---
data class ChatMessage(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("match_id") val matchId: Int,
    @SerializedName("emisor_id") val emisorId: Int,
    @SerializedName("mensaje") val text: String,
    @SerializedName("created_at") val createdAt: String? = null,
    val isSentByMe: Boolean = false // Se calcula dinámicamente
)
