package com.example.Consultaitems.ui.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.utils.cls.Columna
import com.example.Consultaitems.utils.cls.consultaLlamada

class AdapterReporteCriterio5(
    private var columnasCliente: List<Columna>,
    private var data: List<Map<String, Any>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val columnasDetalle = listOf(
        Columna("Fecha", 1.3f, "gv_fechatrn"),
        Columna("Observación", 3.2f, "gv_observacion"),
        Columna("Línea Ofrecida", 2.0f, "gv_lineaofrecida")
    )

    class ClienteViewHolder(val fila: LinearLayout) : RecyclerView.ViewHolder(fila)
    class DetalleViewHolder(val fila: LinearLayout) : RecyclerView.ViewHolder(fila)
    class FechaCreacionViewHolder(val txt: TextView) : RecyclerView.ViewHolder(txt)
    class TotalViewHolder(val txt: TextView) : RecyclerView.ViewHolder(txt)

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 3
        return when (data.getOrNull(position - 1)?.get(consultaLlamada.ConstantesReporte.TIPO_FILA)) {
            TIPO_CLIENTE -> 0
            TIPO_DETALLE -> 1
            TIPO_FECHA_CREACION -> 2
            else -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            2 -> FechaCreacionViewHolder(headerText(parent, 13f))
            3 -> TotalViewHolder(headerText(parent, 13f).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL })
            0 -> ClienteViewHolder(row(parent).apply { setBackgroundColor(Color.parseColor("#E0E0E0")) })
            else -> DetalleViewHolder(row(parent))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TotalViewHolder) {
            holder.txt.text = "Total: ${data.count { it[consultaLlamada.ConstantesReporte.TIPO_FILA] == TIPO_DETALLE }}"
            return
        }

        val map = data.getOrNull(position - 1).orEmpty()
        when (holder) {
            is FechaCreacionViewHolder -> holder.txt.text = "FECHA CREACION CLIENTES : ${map["gc_fechaing"]?.toString().orEmpty()}"
            is ClienteViewHolder -> bindRow(holder.fila, columnasCliente, map, bold = true)
            is DetalleViewHolder -> bindRow(holder.fila, columnasDetalle, map, bold = false)
        }
    }

    override fun getItemCount(): Int = data.size + 1

    fun actualizarDatos(nuevasColumnas: List<Columna>, nuevosDatos: List<Map<String, Any>>) {
        columnasCliente = nuevasColumnas
        data = nuevosDatos
        notifyDataSetChanged()
    }

    private fun bindRow(fila: LinearLayout, columnas: List<Columna>, map: Map<String, Any>, bold: Boolean) {
        fila.removeAllViews()
        for (col in columnas) {
            fila.addView(TextView(fila.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, col.peso)
                text = map[col.campo]?.toString().orEmpty()
                maxLines = if (bold) 2 else 4
                ellipsize = TextUtils.TruncateAt.END
                if (bold) setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
                textSize = if (bold) 13f else 12f
                gravity = Gravity.START or Gravity.TOP
            })
        }
    }

    private fun row(parent: ViewGroup): LinearLayout = LinearLayout(parent.context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        gravity = Gravity.TOP
        setPadding(dp(4), dp(6), dp(4), dp(6))
    }

    private fun headerText(parent: ViewGroup, size: Float): TextView = TextView(parent.context).apply {
        layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32))
        setPadding(dp(12), dp(6), dp(12), dp(6))
        setTypeface(null, Typeface.BOLD)
        setBackgroundColor(Color.parseColor("#E0E0E0"))
        setTextColor(Color.BLACK)
        textSize = size
    }

    private fun TextView.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun LinearLayout.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
