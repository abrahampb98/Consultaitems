package com.example.Consultaitems.ui.dialogs

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.adapters.PreciosyStock
import java.util.Locale

class DetallePrecioStockDialog(
    private val context: Context,
    private val onVerStock: (referencia: String) -> Unit,
    private val onVerImagen: (codigo: String) -> Unit
) {

    fun mostrar(item: PreciosyStock) {
        val contenedor = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
            setBackgroundResource(R.drawable.background)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        agregarFilaDoble(
            contenedor,
            "Marca", item.marca,
            "Referencia", item.referencia
        )

        agregarFilaDoble(
            contenedor,
            "SKU", item.sku,
            "Código", item.codigo
        )

        agregarFila(
            contenedor,
            "Descripción",
            item.descripcion
        )

        agregarFilaDoble(
            contenedor,
            "Tipo producto", item.titulo,
            "Stock total", item.total
        )

        agregarFilaDoble(
            contenedor,
            "Precio sub.", item.sub,
            "Contado", item.contado
        )

        agregarFilaDoble(
            contenedor,
            "Crédito", item.credito,
            "Público", item.publico
        )

        agregarFilaDoble(
            contenedor,
            "Público + IVA", item.final,
            "Peso", item.peso
        )

        agregarFilaDoble(
            contenedor,
            "L-Sub", calcularYFormatear(item.sub, item.costProm),
            "L-Cont", calcularYFormatear(item.contado, item.costProm)
        )

        agregarFilaDoble(
            contenedor,
            "L-Cred", calcularYFormatear(item.credito, item.costProm),
            "L-Pub", calcularYFormatear(item.publico, item.costProm)
        )

        val scrollView = ScrollView(context).apply {
            setBackgroundResource(R.drawable.background)
            addView(contenedor)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(item.referencia.ifBlank { "Detalle del producto" })
            .setView(scrollView)
            .setPositiveButton("Ver stock") { _, _ ->
                onVerStock(item.referencia)
            }
            .setNegativeButton("Ver imagen") { _, _ ->
                onVerImagen(item.codigo)
            }
            .setNeutralButton("Cerrar", null)
            .create()

        dialog.show()

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(R.drawable.background)
            window.setGravity(Gravity.CENTER)

            val params = WindowManager.LayoutParams().apply {
                copyFrom(window.attributes)
                width = (context.resources.displayMetrics.widthPixels * 0.92).toInt()
                height = WindowManager.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER
                windowAnimations = 0
            }

            window.attributes = params
        }
    }

    private fun agregarFilaDoble(
        contenedor: LinearLayout,
        titulo1: String,
        valor1: String,
        titulo2: String,
        valor2: String
    ) {
        val fila = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val columna1 = crearColumna(titulo1, valor1).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = dp(6)
            }
        }

        val columna2 = crearColumna(titulo2, valor2).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(6)
            }
        }

        fila.addView(columna1)
        fila.addView(columna2)
        contenedor.addView(fila)
    }

    private fun agregarFila(
        contenedor: LinearLayout,
        titulo: String,
        valor: String
    ) {
        val columna = crearColumna(titulo, valor).apply {
            setPadding(0, dp(4), 0, dp(8))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        contenedor.addView(columna)
    }

    private fun crearColumna(
        titulo: String,
        valor: String
    ): LinearLayout {
        val columna = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.edittext_background)
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }

        val txtTitulo = TextView(context).apply {
            text = titulo
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
        }

        val txtValor = TextView(context).apply {
            text = valor.ifBlank { "-" }
            textSize = 14f
            maxLines = if (titulo == "Descripción") 4 else 2
        }

        columna.addView(txtTitulo)
        columna.addView(txtValor)

        return columna
    }

    private fun calcularYFormatear(
        value: String,
        costProm: String
    ): String {
        val valueDouble = limpiarNumero(value)
        val costPromDouble = limpiarNumero(costProm)

        if (costPromDouble == 0.0) return "0.00"

        val resultado = valueDouble / costPromDouble
        return String.format(Locale.US, "%.2f", resultado)
    }

    private fun limpiarNumero(valor: String): Double {
        return valor
            .replace("$", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    private fun dp(valor: Int): Int {
        return (valor * context.resources.displayMetrics.density).toInt()
    }
}