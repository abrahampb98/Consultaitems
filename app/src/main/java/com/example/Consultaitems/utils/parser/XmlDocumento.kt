package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class XmlDoc {
    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseCabFactura(xmlData: String, database: SQLiteDatabase, cadena: String, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            // Variables para almacenar los valores de las etiquetas de la primera tabla
            var bodega: String? = null
            var factura: String? = null
            var sri: String? = null
            var fecha: String? = null
            var gremision: String? = null
            var gtransporte: String? = null
            var valortotal: String? = null
            var de_serie: String? = null
            var de_claveacceso: String? = null

            // Variables para almacenar los valores de las etiquetas de la segunda tabla
            var fa_coddocumento: String? = null
            var fa_valordescuento: String? = null
            var fa_rentab: String? = null
            var fa_descripcion: String? = null
            var it_codigo: String? = null
            var it_referencia: String? = null
            var gt_porcdescuento: String? = null
            var gt_cant_req: String? = null
            var gt_costo_prom: String? = null
            var gt_preciovta: String? = null

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                // Primera tabla
                                "bodega" -> {
                                    parser.next()
                                    bodega = parser.text
                                }
                                "factura" -> {
                                    parser.next()
                                    factura = parser.text
                                }
                                "sri" -> {
                                    parser.next()
                                    sri = parser.text
                                }
                                "fecha" -> {
                                    parser.next()
                                    fecha = parser.text
                                }
                                "gremision" -> {
                                    parser.next()
                                    gremision = parser.text
                                }
                                "gtransporte" -> {
                                    parser.next()
                                    gtransporte = parser.text
                                }
                                "valortotal" -> {
                                    parser.next()
                                    valortotal = parser.text
                                }
                                "de_serie1" -> {
                                    parser.next()
                                    de_serie = parser.text
                                }
                                "de_claveacceso1" -> {
                                    parser.next()
                                    de_claveacceso = parser.text
                                }
                                // Segunda tabla
                                "fa_coddocumento" -> {
                                    parser.next()
                                    fa_coddocumento = parser.text
                                }
                                "fa_porcentdesc" -> {
                                    parser.next()
                                    fa_valordescuento = parser.text
                                }
                                "fa_rentab" -> {
                                    parser.next()
                                    fa_rentab = parser.text
                                }
                                "fa_descripcion" -> {
                                    parser.next()
                                    fa_descripcion = parser.text
                                }
                                "it_codigo" -> {
                                    parser.next()
                                    it_codigo = parser.text
                                }
                                "it_referencia" -> {
                                    parser.next()
                                    it_referencia = parser.text
                                }
                                "gt_porcdescuento" -> {
                                    parser.next()
                                    gt_porcdescuento = parser.text
                                }
                                "gt_cant_req" -> {
                                    parser.next()
                                    gt_cant_req = parser.text
                                }
                                "gt_costo_prom" -> {
                                    parser.next()
                                    gt_costo_prom = parser.text
                                }
                                "gt_preciovta" -> {
                                    parser.next()
                                    gt_preciovta = parser.text
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                // Insertar valores en la base de datos si todos los campos de la primera tabla están presentes
                                if (bodega != null && factura != null && sri != null && fecha != null &&
                                    gremision != null && gtransporte != null && valortotal != null ) {

                                    val values1 = ContentValues().apply {
                                        put("bodega", bodega)
                                        put("factura", factura)
                                        put("sri", sri)
                                        put("fecha", fecha)
                                        put("gremision", gremision)
                                        put("gtransporte", gtransporte)
                                        put("valortotal", valortotal)
                                        put("de_serie", de_serie)
                                        put("de_claveacceso", de_claveacceso)
                                    }
                                    val newRowId1 = database.insert("fa_ws_cabfactura", null, values1)
                                    if (newRowId1 == -1L) {
                                        Log.e("CabFactura", "Error inserting into fa_ws_cabfactura")
                                    } else {
                                        Log.d("CabFactura", "Inserted into fa_ws_cabfactura with row id: $newRowId1")
                                    }
                                } else {
                                    Log.e("CabFactura", "Missing required fields in the XML data for fa_ws_cabfactura")
                                }

                                // Restablecer las variables para la siguiente fila
                                bodega = null
                                factura = null
                                sri = null
                                fecha = null
                                gremision = null
                                gtransporte = null
                                valortotal = null
                                de_serie = null
                                de_claveacceso = null
                            } else if (parser.name == "Table1") {
                                // Insertar valores en la base de datos si todos los campos de la segunda tabla están presentes
                                if (fa_coddocumento != null ) {

                                    val values2 = ContentValues().apply {
                                        put("fa_coddocumento", fa_coddocumento)
                                        put("fa_valordescuento", fa_valordescuento)
                                        put("fa_rentab", fa_rentab)
                                        put("fa_descripcion", fa_descripcion)
                                        put("it_codigo", it_codigo)
                                        put("it_referencia", it_referencia)
                                        put("gt_porcdescuento", gt_porcdescuento)
                                        put("gt_cant_req", gt_cant_req)
                                        put("gt_costo_prom", gt_costo_prom)
                                        put("gt_preciovta", gt_preciovta)
                                    }
                                    val newRowId2 = database.insert("fa_ws_detfactura", null, values2)
                                    if (newRowId2 == -1L) {
                                        Log.e("DetFactura", "Error inserting into fa_ws_detfactura")
                                    } else {
                                        Log.d("DetFactura", "Inserted into fa_ws_detfactura with row id: $newRowId2")
                                    }
                                } else {
                                    Log.e("DetFactura", "Missing required fields in the XML data for fa_ws_detfactura")
                                }

                                // Restablecer las variables para la siguiente fila
                                fa_coddocumento = null
                                fa_valordescuento = null
                                fa_rentab = null
                                fa_descripcion = null
                                it_codigo = null
                                it_referencia = null
                                gt_porcdescuento = null
                                gt_cant_req = null
                                gt_costo_prom = null
                                gt_preciovta = null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CabFacturaXMLParser", "Error parsing XML: ${e.message}")
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Error")
                builder.setMessage("Se produjo un error al procesar el XML: ${e.message}")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.setCancelable(false)
                builder.show()
            }

            return "Parseo completado"
        }
    }
}
