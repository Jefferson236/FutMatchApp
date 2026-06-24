package com.example.futmatchapp.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object ImageUploader {
    private const val IMGBB_API_KEY = "6790518659560f69a53820257e841285" // API Key de ejemplo (ImgBB)
    private val client = OkHttpClient()

    fun uploadImage(context: Context, imageUri: Uri, onResult: (String?) -> Unit) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes == null) {
            onResult(null)
            return
        }

        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val formBody = FormBody.Builder()
            .add("key", IMGBB_API_KEY)
            .add("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ImageUploader", "Error al subir imagen", e)
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val url = json.getJSONObject("data").getString("url")
                    onResult(url)
                } else {
                    Log.e("ImageUploader", "Respuesta fallida: $body")
                    onResult(null)
                }
            }
        })
    }
}
