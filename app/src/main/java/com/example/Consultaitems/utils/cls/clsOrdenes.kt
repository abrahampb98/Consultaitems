package com.example.Consultaitems.utils.cls

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ClsRegImagenOrden(
    private val context: Context
) {

    init {
        // Deshabilitar la verificación de SSL para propósitos de prueba
        disableSSLCertificateChecking()
    }

    private fun disableSSLCertificateChecking() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

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
        val url = "https://app.cotzul.com/sitenet/digital/senDatosGestionVendedor.php"
        try {
            val handler = Handler(Looper.getMainLooper())
            var requestCompleted = false

            val strRequest = object : StringRequest(
                Request.Method.POST, url,
                Response.Listener<String> { response ->
                    if (!requestCompleted) {
                        requestCompleted = true
                        handler.removeCallbacksAndMessages(null)
                        //Toast.makeText(context, response, Toast.LENGTH_SHORT).show()

                        // Verifica si la respuesta es exitosa
                        if (response.isNullOrEmpty()) {
                            onNoResponse()  // Si no hay respuesta, llamamos al callback correspondiente
                        } else {
                            // Verifica si la respuesta contiene el mensaje esperado de éxito
                            if (response.contains("El archivo PDF se ha guardado correctamente")) {
                                onSuccess(response)
                            } else {
                                onError("La respuesta del servidor no fue exitosa.")
                            }
                        }
                    }
                },
                Response.ErrorListener { error ->
                    if (!requestCompleted) {
                        requestCompleted = true
                        handler.removeCallbacksAndMessages(null)
                        Log.e("ClsRegImagen", "Error en la solicitud: ${error.message}")
                        onError("Error en la solicitud: ${error.message}")
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["nombreArchivo"] = nombreArchivo
                    params["archivoBase64"] = archivoBase64
                    Log.d("ClsRegImagen", "nombreArchivo: $nombreArchivo")
                   // Log.d("ClsRegImagen", "archivoBase64: ${archivoBase64.substring(0, 50)}...")
                    return params
                }
            }

            // Establece un RetryPolicy personalizado para 1 intento y tiempo de espera de 5 segundos
            strRequest.retryPolicy = DefaultRetryPolicy(
                120 * 1000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Factor de multiplicación para el tiempo de espera
            )

            Volley.newRequestQueue(context).add(strRequest)

            handler.postDelayed({
                if (!requestCompleted) {
                    requestCompleted = true
                    onError("Verifique su conexion a internet. Intente de nuevo enviando la imagen")
                }
            }, 10000)

        } catch (e: Exception) {
            Log.e("ClsRegImagen", "Error en enviarDatos: ${e.message}")
            onError("Error al enviar los datos: ${e.message}")
        }
    }
}