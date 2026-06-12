package com.example.Consultaitems.utils.cls

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ClsRegImagen(
    private val context: Context
) {

    init {
        // Deshabilitar la verificación de SSL para propósitos de prueba
        disableSSLCertificateChecking()
    }

    private fun disableSSLCertificateChecking() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enviarDatos(
        nombreArchivo: String,
        archivoBase64: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onNoResponse: () -> Unit
    ) {
        enviarArchivoPdf(
            url = "https://app.cotzul.com/sitenet/18/senDatosGestionVendedor.php",
            nombreArchivo = nombreArchivo,
            archivoBase64 = archivoBase64,
            onSuccess = onSuccess,
            onError = onError,
            onNoResponse = onNoResponse
        )
    }

    fun enviarDatosInventario(
        nombreArchivo: String,
        archivoBase64: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onNoResponse: () -> Unit
    ) {
        enviarArchivoPdf(
            url = "https://app.cotzul.com/sitenet/37/senDatosInventarioObservacion.php",
            nombreArchivo = nombreArchivo,
            archivoBase64 = archivoBase64,
            onSuccess = onSuccess,
            onError = onError,
            onNoResponse = onNoResponse
        )
    }

    private fun enviarArchivoPdf(
        url: String,
        nombreArchivo: String,
        archivoBase64: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onNoResponse: () -> Unit
    ) {
        try {
            val handler = Handler(Looper.getMainLooper())
            var requestCompleted = false
            var timeoutRunnable: Runnable? = null

            fun finalizar(callback: () -> Unit) {
                if (!requestCompleted) {
                    requestCompleted = true
                    timeoutRunnable?.let { handler.removeCallbacks(it) }
                    callback()
                }
            }

            val strRequest = object : StringRequest(
                Request.Method.POST,
                url,
                Response.Listener<String> { response ->

                    finalizar {
                        if (response.isBlank()) {
                            onNoResponse()
                            return@finalizar
                        }

                        if (response.contains("El archivo PDF se ha guardado correctamente")) {
                            onSuccess(response)
                        } else {
                            Log.e("ClsRegImagen", "Respuesta no exitosa: $response")
                            onError("La respuesta del servidor no fue exitosa.")
                        }
                    }
                },
                Response.ErrorListener { error ->

                    finalizar {
                        val mensajeError = error.message
                            ?: error.localizedMessage
                            ?: error.toString()

                        Log.e("ClsRegImagen", "Error en la solicitud: $mensajeError", error)
                        onError("Error en la solicitud: $mensajeError")
                    }
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()

                    params["nombreArchivo"] = nombreArchivo
                    params["archivoBase64"] = archivoBase64

                    Log.d("ClsRegImagen", "nombreArchivo: $nombreArchivo")
                    Log.d("ClsRegImagen", "archivoBase64: ${archivoBase64.take(50)}...")

                    return params
                }
            }

            strRequest.retryPolicy = DefaultRetryPolicy(
                120 * 1000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            Volley.newRequestQueue(context.applicationContext).add(strRequest)

            timeoutRunnable = Runnable {
                finalizar {
                    onError("Verifique su conexión a internet. Intente de nuevo enviando la imagen")
                }
            }

            handler.postDelayed(timeoutRunnable, 10_000)

        } catch (e: Exception) {
            Log.e("ClsRegImagen", "Error en enviarArchivoPdf: ${e.message}", e)
            onError("Error al enviar los datos: ${e.message}")
        }
    }
}