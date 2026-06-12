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

class AdapterReporteCriterio2(
    private var columnas: List<Columna>,
    private var data: List<Map<String, String>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FechaViewHolder(val txt: TextView) : RecyclerView.ViewHolder(txt)
    class DataViewHolder(val fila: LinearLayout) : RecyclerView.ViewHolder(fila)
    class TotalViewHolder(val txt: TextView) : RecyclerView.ViewHolder(txt)

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 2
        return if (data.getOrNull(position - 1)?.get(consultaLlamada.ConstantesReporte.TIPO_FILA) == consultaLlamada.ConstantesReporte.TIPO_FECHA) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> FechaViewHolder(TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(30))
                setPadding(dp(12), dp(4), dp(12), dp(4))
                setTypeface(null, Typeface.BOLD)
                setBackgroundColor(Color.parseColor("#E0E0E0"))
                setTextColor(Color.BLACK)
                textSize = 14f
            })
            2 -> TotalViewHolder(TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(30))
                setPadding(dp(12), dp(4), dp(12), dp(4))
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                textSize = 13f
            })
            else -> DataViewHolder(LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.TOP
                setPadding((4), (6), (4), (6))
            })
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TotalViewHolder) {
            val total = data.count { it[consultaLlamada.ConstantesReporte.TIPO_FILA] == consultaLlamada.ConstantesReporte.TIPO_DATA }
            holder.txt.text = "Total: $total"
            return
        }

        val row = data.getOrNull(position - 1).orEmpty()
        when (holder) {
            is FechaViewHolder -> holder.txt.text = row["gc_fechaing"].orEmpty()
            is DataViewHolder -> {
                holder.fila.removeAllViews()
                for (col in columnas) {
                    holder.fila.addView(TextView(holder.fila.context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, col.peso)
                        text = row[col.campo].orEmpty()
                        maxLines = 3
                        ellipsize = TextUtils.TruncateAt.END
                        setTextColor(Color.BLACK)
                        textSize = 12f
                        gravity = Gravity.START or Gravity.TOP
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

    fun actualizarDatos(nuevasColumnas: List<Columna>, nuevosDatos: List<Map<String, String>>) {
        columnas = nuevasColumnas
        data = nuevosDatos
        notifyDataSetChanged()
    }

    private fun TextView.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
