package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XMlParserEnte {

    companion object INSTANCE {

        fun parserEnte(
            xmlData: String,
            database: SQLiteDatabase?,
            ente: Int,
            context: Context
        ): String {
            return try {
                val valores = extraerValores(xmlData)
                val nuevoEnte = buscarEntero(
                    valores,
                    listOf(
                        "en_codigo", "ente", "codigo", "Codigo", "id", "Id",
                        "Resultado", "result", "return"
                    )
                ) ?: buscarPrimerEntero(xmlData)

                if (nuevoEnte != null && database != null && ente > 0 && nuevoEnte > 0 && nuevoEnte != ente) {
                    actualizarCodigoEnte(database, ente, nuevoEnte)
                }

                nuevoEnte?.toString() ?: limpiarRespuesta(xmlData)
            } catch (e: Exception) {
                Log.e("XMlParserEnte", "Error en parserEnte: ${e.message}", e)
                mostrarError(context, e)
                "ERROR: ${e.message}"
            }
        }

        fun parserCliente(
            xmlData: String,
            database: SQLiteDatabase?,
            cliente: Int,
            ente: Int,
            context: Context
        ): String {
            return try {
                val valores = extraerValores(xmlData)
                val codigoCliente = buscarEntero(
                    valores,
                    listOf(
                        "cl_codigo", "cliente", "codigo", "Codigo", "id", "Id",
                        "Resultado", "result", "return"
                    )
                ) ?: buscarPrimerEntero(xmlData) ?: cliente

                if (database != null && codigoCliente > 0) {
                    runCatching {
                        database.execSQL(
                            "UPDATE cc_ws_ente SET cl_codigo = ? WHERE en_codigo = ?",
                            arrayOf(codigoCliente, cliente)
                        )
                    }
                    if (ente > 0 && ente != cliente) {
                        runCatching {
                            database.execSQL(
                                "UPDATE cc_ws_ente SET cl_codigo = ? WHERE en_codigo = ?",
                                arrayOf(codigoCliente, ente)
                            )
                        }
                    }
                }

                codigoCliente.toString()
            } catch (e: Exception) {
                Log.e("XMlParserEnte", "Error en parserCliente: ${e.message}", e)
                mostrarError(context, e)
                "ERROR: ${e.message}"
            }
        }

        private fun actualizarCodigoEnte(database: SQLiteDatabase, anterior: Int, nuevo: Int) {
            val tablas = listOf(
                "cc_ws_ente" to "en_codigo",
                "cc_ws_conctacto" to "cl_codigo",
                "cc_ws_clienteCategoriaDetalle" to "cl_codigo"
            )

            for ((tabla, columna) in tablas) {
                runCatching {
                    database.execSQL(
                        "UPDATE $tabla SET $columna = ? WHERE $columna = ?",
                        arrayOf(nuevo, anterior)
                    )
                }
            }
        }

        private fun extraerValores(xmlData: String): Map<String, String> {
            val result = linkedMapOf<String, String>()
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var currentTag: String? = null
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        val tag = currentTag.orEmpty()
                        for (i in 0 until parser.attributeCount) {
                            val name = parser.getAttributeName(i).orEmpty()
                            val value = parser.getAttributeValue(i).orEmpty().trim()
                            if (name.isNotBlank()) result[name] = value
                            if (tag.isNotBlank() && name.isNotBlank()) result["$tag.$name"] = value
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        val tag = currentTag
                        if (!tag.isNullOrBlank() && text.isNotBlank()) {
                            result[tag] = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == currentTag) currentTag = null
                    }
                }
                eventType = parser.next()
            }
            return result
        }

        private fun buscarEntero(valores: Map<String, String>, claves: List<String>): Int? {
            for (clave in claves) {
                valores[clave]?.toIntOrNull()?.let { return it }
                valores.entries.firstOrNull { it.key.endsWith(".$clave", ignoreCase = true) }
                    ?.value
                    ?.toIntOrNull()
                    ?.let { return it }
            }
            return null
        }

        private fun buscarPrimerEntero(texto: String): Int? {
            val match = Regex("[-]?\\d+").find(texto) ?: return null
            return match.value.toIntOrNull()
        }

        private fun limpiarRespuesta(xmlData: String): String {
            return xmlData
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .ifBlank { "Proceso completado" }
        }

        private fun mostrarError(context: Context, e: Exception) {
            runCatching {
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Error al procesar XML: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
            }
        }
    }
}
