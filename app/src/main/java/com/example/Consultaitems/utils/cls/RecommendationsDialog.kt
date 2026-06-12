package com.example.Consultaitems.utils.cls

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.Consultaitems.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private var totalPrecioRecomendacion = 0.0
private var totalCostoPromedioRecomendacion = 0.0

class RecommendationsDialog(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var llenarControles: ClsLLenarControles? = null

    private fun calcularRentabilidad(precio: Double, costoPromedio: Double): Double {
        return if (costoPromedio == 0.0) 0.0 else precio / costoPromedio
    }

    fun crearPrompt(
        pedido: Pedido,
        historialVentas: HistorialVenta,
        productosDisponibles: List<ProductoDisponible>
    ): String {
        val pedidoDetails = pedido.items.joinToString(", ") {
            "referencia: ${it.referencia} - descripcion: ${it.descripcion} " +
                "(Precio: ${it.precio}, Rentabilidad: ${calcularRentabilidad(it.precio, it.costoPromedio)})"
        }

        totalPrecioRecomendacion = pedido.items.sumOf { it.precio }
        totalCostoPromedioRecomendacion = pedido.items.sumOf { it.costoPromedio }

        val historialDetails = historialVentas.ventas.take(100).joinToString(", ") {
            "referencia: ${it.referencia} - descripcion: ${it.descripcion} " +
                "(Cantidad: ${it.stock}, Precio: ${it.precio}, Rentabilidad: ${calcularRentabilidad(it.precio, it.costoPromedio)})"
        }

        val productosDisponiblesRentabilidad = productosDisponibles
            .filter {
                calcularRentabilidad(
                    it.precio + totalPrecioRecomendacion,
                    it.costoPromedio + totalCostoPromedioRecomendacion
                ) >= 1.6
            }
            .take(250)

        val productosDisponiblesStr = productosDisponiblesRentabilidad.joinToString(", ") {
            "codigo: ${it.itCodigo} - descripcion: ${it.descripcion} " +
                "(Precio: ${it.precio}, Rentabilidad: ${calcularRentabilidad(it.precio + totalPrecioRecomendacion, it.costoPromedio + totalCostoPromedioRecomendacion)})"
        }

        val userMessage = """
            El cliente tiene un pedido con los siguientes productos: $pedidoDetails.

            Historial del cliente: $historialDetails.

            De estos productos disponibles muéstrame sugerencias de productos complementarios basándote en las descripciones,
            siempre que tengan que ver con los items del pedido:
            $productosDisponiblesStr.

            Por favor, solo devuélveme los códigos de los productos separados por coma, nada más.
        """.trimIndent()

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        return JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messagesArray)
        }.toString()
    }

    private suspend fun obtenerRecomendaciones(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val body = prompt.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        var attempt = 0
        while (attempt < 3) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext response.body?.string().orEmpty()
                    }
                    Log.e("OpenAI Error", "Error: ${response.message}")
                    return@withContext "Error en la solicitud: ${response.message}"
                }
            } catch (e: Exception) {
                attempt++
                Log.e("OpenAI Error", "Excepción: ${e.message}")
                if (attempt == 3) return@withContext "Error en la conexión después de 3 intentos"
            }
        }
        "Error al hacer la solicitud"
    }

    fun procesarRespuesta(recommendations: String): String {
        return try {
            val jsonObject = JSONObject(recommendations)
            if (jsonObject.has("choices")) {
                jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                Log.e("API Error", "'choices' no está presente en la respuesta")
                ""
            }
        } catch (e: Exception) {
            Log.e("API Error", "Error al procesar la respuesta: ${e.message}")
            ""
        }
    }

    private fun showDialog(codigos: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Recomendaciones de Productos")
        llenarControles = ClsLLenarControles(context)

        val productos = llenarControles?.obtenerProductosRecomendados(codigos).orEmpty()
        if (productos.isEmpty()) {
            builder.setMessage("No hay recomendaciones para mostrar")
            builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            builder.show()
            return
        }

        val productosAgrupados = productos.groupBy { it.itCodigo }
        val listView = ListView(context)
        val productosStr = productosAgrupados.flatMap { (codigoCombo, productosCombo) ->
            val sumaPrecios = productosCombo.sumOf { it.precio }
            val sumaCostos = productosCombo.sumOf { it.costoPromedio }
            val rentabilidadCombo = calcularRentabilidad(
                sumaPrecios + totalPrecioRecomendacion,
                sumaCostos + totalCostoPromedioRecomendacion
            )

            val encabezadoTexto = "Código del Combo: $codigoCombo - Rentabilidad: ${String.format("%.2f", rentabilidadCombo)}"
            val encabezadoSpannable = SpannableString(encabezadoTexto).apply {
                val startCode = encabezadoTexto.indexOf(codigoCombo).coerceAtLeast(0)
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    startCode,
                    startCode + codigoCombo.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val startRent = encabezadoTexto.indexOf("Rentabilidad").coerceAtLeast(0)
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    startRent,
                    encabezadoTexto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            listOf<CharSequence>(encabezadoSpannable) + productosCombo.map {
                val rentabilidadProducto = calcularRentabilidad(
                    it.precio + totalPrecioRecomendacion,
                    it.costoPromedio + totalCostoPromedioRecomendacion
                )
                "Referencia: ${it.referencia} - Descripción: ${it.descripcion} - Precio: ${String.format("%.2f", it.precio)} - Rentabilidad: ${String.format("%.2f", rentabilidadProducto)}"
            }
        }

        listView.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, productosStr)
        builder.setView(listView)
        builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun showRecommendationsDialog(
        pedido: Pedido,
        historialVentas: HistorialVenta,
        productosDisponibles: List<ProductoDisponible>,
        onFinished: (() -> Unit)? = null
    ) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage("Cargando, por favor espere...")
            setCancelable(false)
            show()
        }

        val prompt = crearPrompt(pedido, historialVentas, productosDisponibles)
        lifecycleOwner.lifecycleScope.launch {
            try {
                val apiKey = context.getString(R.string.str_OpenIA)
                val recommendations = obtenerRecomendaciones(apiKey, prompt)
                val codigos = procesarRespuesta(recommendations)
                if (codigos.isNotBlank()) {
                    showDialog(codigos)
                } else {
                    Log.e("API Error", "No se obtuvieron recomendaciones")
                    AlertDialog.Builder(context)
                        .setTitle("Recomendaciones")
                        .setMessage("No se obtuvieron recomendaciones")
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("RecommendationsDialog", "Error: ${e.message}", e)
                AlertDialog.Builder(context)
                    .setTitle("Recomendaciones")
                    .setMessage("No se pudieron obtener recomendaciones")
                    .setPositiveButton("Aceptar", null)
                    .show()
            } finally {
                progressDialog.dismiss()
                onFinished?.invoke()
            }
        }
    }
}
