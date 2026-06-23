package com.example.futmatchapp.controlador

import com.example.futmatchapp.modelo.BurbujaData
import com.example.futmatchapp.modelo.BubbleModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BubbleController(private val view: BubbleCallbackInterface) {

    private val bubbleModel = BubbleModel()

    interface BubbleCallbackInterface {
        fun onBurbujaCreadaExitosamente()
        fun onErrorCreacion(mensaje: String)
    }

    fun procesarNuevaBurbuja(
        creadorId: Int, tipoJuego: String, ubicacion: String,
        fechaHora: String, cuotaStr: String, posicion: String, mensaje: String
    ) {
        if (ubicacion.trim().isEmpty() || fechaHora.trim().isEmpty()) {
            view.onErrorCreacion("La ubicación y la fecha son campos obligatorios.")
            return
        }

        val cuota = cuotaStr.toDoubleOrNull()
        val burbuja = BurbujaData(
            creadorId = creadorId,
            tipoJuego = tipoJuego,
            mensajePersonalizado = mensaje.ifEmpty { null },
            ubicacionLugar = ubicacion,
            cuotaAPagar = cuota,
            posicionNecesitada = posicion.ifEmpty { null },
            fechaHora = fechaHora
        )

        bubbleModel.insertarBurbuja(burbuja, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) view.onBurbujaCreadaExitosamente()
                else view.onErrorCreacion("Error del servidor al publicar la burbuja.")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                view.onErrorCreacion("Error de red: ${t.localizedMessage}")
            }
        })
    }
}