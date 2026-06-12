package com.example.Consultaitems.network

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class SolicitudSoap(private val context: Context) {

    private lateinit var apiBaseUrl: String
    private lateinit var clientId: String
    private lateinit var usuario: String
    private lateinit var clave: String

    private lateinit var str_id: String
    private lateinit var str_cadena: String

    private lateinit var llenarControles: ClsLLenarControles

    companion object {
        @Volatile
        private var accessTokenCompartido: String? = null

        @Volatile
        private var tokenExpiraEnMillisCompartido: Long = 0

        private val tokenLock = Any()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun initializeVariables(id: Int, Cadena: String) {
        str_id = id.toString()
        str_cadena = Cadena

        llenarControles = ClsLLenarControles(context)
        llenarControles.fnCrearTablasApiSiNoExisten()

        cargarConfiguracionApiDesdeSQLite()
    }

    private fun cargarConfiguracionApiDesdeSQLite() {
        val db: SQLiteDatabase = context.openOrCreateDatabase(
            "db_vendedor.db",
            Context.MODE_PRIVATE,
            null
        )

        try {
            val cursorConfig = db.rawQuery(
                """
                SELECT 
                    ac_base_url,
                    ac_client_id,
                    ac_usuario
                FROM se_ws_apiConfig
                WHERE ac_activo = 1
                LIMIT 1
                """.trimIndent(),
                null
            )

            if (!cursorConfig.moveToFirst()) {
                cursorConfig.close()
                throw IOException("No existe configuración activa en se_ws_apiConfig.")
            }

            apiBaseUrl = cursorConfig.getString(cursorConfig.getColumnIndexOrThrow("ac_base_url"))
            clientId = cursorConfig.getString(cursorConfig.getColumnIndexOrThrow("ac_client_id"))
            usuario = cursorConfig.getString(cursorConfig.getColumnIndexOrThrow("ac_usuario"))

            cursorConfig.close()

            val cursorCredencial = db.rawQuery(
                """
                SELECT 
                    cr_clave
                FROM se_ws_apiCredencial
                WHERE cr_client_id = ?
                  AND cr_usuario = ?
                  AND cr_activo = 1
                LIMIT 1
                """.trimIndent(),
                arrayOf(clientId, usuario)
            )

            if (!cursorCredencial.moveToFirst()) {
                cursorCredencial.close()
                throw IOException("No existe credencial activa en se_ws_apiCredencial.")
            }

            clave = cursorCredencial.getString(cursorCredencial.getColumnIndexOrThrow("cr_clave"))

            cursorCredencial.close()

            apiBaseUrl = apiBaseUrl.trimEnd('/')

        } finally {
            db.close()
        }
    }

    fun realizarSolicitudSoap(): InputStream? {
        return try {
            ejecutarApiXml(reintentarSi401 = true)
        } catch (e: Exception) {
            val mensaje = e.message ?: "Ocurrió un error desconocido."

            mostrarErrorDialog(
                titulo = "Error en la solicitud",
                mensaje = mensaje
            )

            throw IOException("Excepción en la solicitud API: $mensaje", e)
        }
    }

    private fun obtenerToken(): String {
        val ahora = System.currentTimeMillis()
        val tokenActual = accessTokenCompartido

        if (!tokenActual.isNullOrBlank() && ahora < tokenExpiraEnMillisCompartido) {
            return tokenActual
        }

        synchronized(tokenLock) {
            val tokenRevisado = accessTokenCompartido
            val ahoraRevisado = System.currentTimeMillis()

            if (!tokenRevisado.isNullOrBlank() && ahoraRevisado < tokenExpiraEnMillisCompartido) {
                return tokenRevisado
            }

            val jsonLogin = JSONObject().apply {
                put("clientId", clientId)
                put("usuario", usuario)
                put("clave", clave)
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonLogin.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$apiBaseUrl/api/auth/login")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val detalle = extraerMensajeRespuesta(bodyString)

                    throw IOException(
                        "No fue posible iniciar sesión.\n" +
                                "Código: ${response.code}\n" +
                                "Mensaje: ${response.message}\n" +
                                detalle
                    )
                }

                if (bodyString.isBlank()) {
                    throw IOException("La API no devolvió respuesta al iniciar sesión.")
                }

                val jsonResponse = JSONObject(bodyString)

                val nuevoToken = jsonResponse.getString("accessToken")
                val expiresIn = jsonResponse.optInt("expiresIn", 900)

                accessTokenCompartido = nuevoToken

                val segundosValidos = if (expiresIn > 60) expiresIn - 60 else expiresIn

                tokenExpiraEnMillisCompartido =
                    System.currentTimeMillis() + (segundosValidos * 1000L)

                return nuevoToken
            }
        }
    }

    private fun ejecutarApiXml(reintentarSi401: Boolean): InputStream? {
        val token = obtenerToken()

        val jsonRequest = JSONObject().apply {
            put("id", str_id.toInt())
            put("cadena", str_cadena)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$apiBaseUrl/api/v1/legacy/ejecutar-xml")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/xml")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()

            if (response.code == 401 && reintentarSi401) {
                limpiarTokenCompartido()
                return ejecutarApiXml(reintentarSi401 = false)
            }

            if (response.isSuccessful) {
                if (bodyString.isBlank()) {
                    throw IOException("La API respondió correctamente, pero no devolvió datos.")
                }

                return ByteArrayInputStream(bodyString.toByteArray(Charsets.UTF_8))
            }

            val detalle = extraerMensajeRespuesta(bodyString)

            throw IOException(
                "Error en la API.\n" +
                        "Código: ${response.code}\n" +
                        "Mensaje: ${response.message}\n" +
                        detalle
            )
        }
    }

    private fun limpiarTokenCompartido() {
        synchronized(tokenLock) {
            accessTokenCompartido = null
            tokenExpiraEnMillisCompartido = 0
        }
    }

    private fun mostrarErrorDialog(titulo: String, mensaje: String) {
        Handler(Looper.getMainLooper()).post {
            val activity = context as? Activity

            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                AlertDialog.Builder(activity)
                    .setTitle(titulo)
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                Toast.makeText(
                    context.applicationContext,
                    "$titulo: $mensaje",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun extraerMensajeRespuesta(body: String): String {
        if (body.isBlank()) {
            return ""
        }

        return try {
            val json = JSONObject(body)

            when {
                json.has("mensaje") -> "\nDetalle: ${json.optString("mensaje")}"
                json.has("message") -> "\nDetalle: ${json.optString("message")}"
                json.has("error") -> "\nDetalle: ${json.optString("error")}"
                else -> "\nRespuesta: ${body.take(800)}"
            }
        } catch (e: Exception) {
            "\nRespuesta: ${body.take(800)}"
        }
    }
}