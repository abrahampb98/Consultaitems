package com.example.Consultaitems.utils.cls

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.Consultaitems.network.SolicitudSoap
import java.io.BufferedReader
import java.io.InputStreamReader

class clsObtenerDatos(
    private val context: Context,
    private val solicitudSoap: SolicitudSoap,
    private val id: Int,
    private val cadena: String,
    private val onSuccess: (xml: String) -> Unit,
    private val onError: (exception: Exception) -> Unit = {}
) : AsyncTask<Void, Void, String?>() {

    private var error: Exception? = null

    override fun doInBackground(vararg params: Void?): String? {
        return try {
            solicitudSoap.initializeVariables(id, cadena)
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            }
            result?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            error = e
            Log.e("clsObtenerDatos", "Error en doInBackground: ${e.message}", e)
            null
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        val ex = error
        if (ex != null) {
            onError(ex)
        } else if (!result.isNullOrBlank()) {
            onSuccess(result)
        } else {
            onError(IllegalStateException("Respuesta vacía del SOAP"))
        }
    }
}
