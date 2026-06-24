package com.example.futmatchapp.controlador

import android.util.Log
import com.example.futmatchapp.RetrofitClient
import com.example.futmatchapp.modelo.Perfil
import com.example.futmatchapp.vista.ProfileFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileController(private val vista: ProfileFragment) {
    private val apiService = RetrofitClient.create()

    fun cargarPerfil(usuarioId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FutMatch", "ProfileController - Iniciando carga de perfil para usuario: $usuarioId")
                
                // Intento 1: Endpoint por usuario_id
                val response = apiService.getPerfilPorUsuario(usuarioId)
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val perfil = apiResponse.data
                    
                    if (perfil != null) {
                        Log.d("FutMatch", "ProfileController - Perfil encontrado para usuario $usuarioId")
                        withContext(Dispatchers.Main) { vista.actualizarUI(perfil) }
                        return@launch
                    } else {
                        Log.w("FutMatch", "ProfileController - Respuesta exitosa pero 'data' es null. Intentando fallback...")
                    }
                }

                // Intento 2: Fallback - Buscar en la lista de todos los perfiles
                Log.w("FutMatch", "ProfileController - Fallback: Buscando en la lista global de perfiles...")
                val listResponse = apiService.getPerfiles()
                if (listResponse.isSuccessful && listResponse.body() != null) {
                    val perfiles = listResponse.body()!!.data
                    val match = perfiles.find { it.usuario_id == usuarioId }
                    if (match != null) {
                        Log.d("FutMatch", "ProfileController - Perfil encontrado en la lista global (ID: ${match.id})")
                        withContext(Dispatchers.Main) { vista.actualizarUI(match) }
                        return@launch
                    }
                }

                withContext(Dispatchers.Main) {
                    vista.mostrarError("No se encontraron datos para el perfil.")
                }
                
            } catch (e: Exception) {
                Log.e("FutMatch", "ProfileController - Error fatal al cargar", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    fun cargarEstadisticas(perfilId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FutMatch", "ProfileController - Cargando estadísticas para perfil: $perfilId")
                val response = apiService.getEstadisticasPorPerfil(perfilId)
                
                var stats: com.example.futmatchapp.modelo.Estadistica? = null
                
                if (response.isSuccessful && response.body()?.data != null) {
                    stats = response.body()!!.data
                    Log.d("FutMatch", "ProfileController - Estadísticas obtenidas por perfil_id")
                } else {
                    Log.w("FutMatch", "ProfileController - Falló getEstadisticasPorPerfil (Código: ${response.code()}). Intentando fallback...")
                    val listResponse = apiService.getEstadisticas()
                    if (listResponse.isSuccessful && listResponse.body() != null) {
                        stats = listResponse.body()!!.data.find { it.perfil_id == perfilId }
                        if (stats != null) Log.d("FutMatch", "ProfileController - Estadísticas encontradas en lista global")
                    }
                }

                if (stats != null) {
                    withContext(Dispatchers.Main) {
                        vista.actualizarEstadisticas(stats)
                    }
                } else {
                    Log.e("FutMatch", "ProfileController - No se encontraron estadísticas para el perfil $perfilId. Usando valores por defecto.")
                    withContext(Dispatchers.Main) {
                        vista.actualizarEstadisticas(com.example.futmatchapp.modelo.Estadistica(
                            perfil_id = perfilId, ovr = 60, pac = 60, sho = 60, pas = 60, dri = 60, def = 60, phy = 60
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("FutMatch", "ProfileController - Error cargando estadísticas", e)
            }
        }
    }

    fun actualizarTodo(dbId: Int, perfil: Perfil, usuarioId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FutMatch", "ProfileController - Actualizando perfil ID: $dbId con datos: $perfil")
                val response = apiService.actualizarPerfil(dbId, perfil)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("FutMatch", "ProfileController - Actualización exitosa: ${response.body()}")
                        vista.mostrarMensajeExito("Perfil actualizado correctamente.")
                        cargarPerfil(usuarioId)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Desconocido"
                        Log.e("FutMatch", "ProfileController - Error al actualizar: $errorMsg (Código: ${response.code()})")
                        vista.mostrarError("Error al actualizar: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FutMatch", "ProfileController - Error de red en actualización", e)
                withContext(Dispatchers.Main) {
                    vista.mostrarError("Error de red: ${e.message}")
                }
            }
        }
    }
}
