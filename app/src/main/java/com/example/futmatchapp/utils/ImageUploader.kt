package com.example.futmatchapp.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object ImageUploader {
    // API Key de ImgBB proporcionada por el usuario
    private const val IMGBB_API_KEY = "959537489fe2ade9204c5457c99771bc"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun uploadImage(context: Context, imageUri: Uri, onResult: (String?) -> Unit) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null || bytes.isEmpty()) {
                Log.e("ImageUploader", "No se pudieron obtener los bytes de la imagen")
                onResult(null)
                return
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", IMGBB_API_KEY)
                .addFormDataPart("image", "upload.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()

            Log.d("ImageUploader", "Iniciando subida binaria a ImgBB...")
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ImageUploader", "Fallo en la petición: ${e.message}", e)
                    onResult(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()
                    Log.d("ImageUploader", "Respuesta recibida: $responseString")
                    
                    if (response.isSuccessful && responseString != null) {
                        try {
                            val json = JSONObject(responseString)
                            if (json.getBoolean("success")) {
                                val url = json.getJSONObject("data").getString("url")
                                Log.d("ImageUploader", "Subida exitosa: $url")
                                onResult(url)
                            } else {
                                Log.e("ImageUploader", "ImgBB error: ${json.optString("message")}")
                                onResult(null)
                            }
                        } catch (e: Exception) {
                            Log.e("ImageUploader", "Error parseando JSON: ${e.message}")
                            onResult(null)
                        }
                    } else {
                        Log.e("ImageUploader", "Respuesta no exitosa: ${response.code} - $responseString")
                        onResult(null)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("ImageUploader", "Excepción al preparar subida: ${e.message}")
            onResult(null)
        }
    }
}
