package com.example.Consultaitems.ui.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.HistorialResumen

class AdaptadorClienteItems(
    private val datos: MutableList<HistorialResumen>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorClienteItems.ViewHolder>() {

    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalle_cliente_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)
        println("Elemento en la posición $position seleccionado: ${selectedPositions.contains(position)}")

        // Determinar si el elemento actual está seleccionado
        val isSelected = selectedPositions.contains(position)

        // Actualiza el estado de selección de la vista
        holder.itemView.isSelected = isSelected

        // Configura el OnClickListener para manejar los clics en los elementos
        holder.itemView.setOnClickListener {
            // Eliminar todas las selecciones anteriores
            selectedPositions.clear()
            // Marcar el elemento actual como seleccionado
            selectedPositions.add(position)
            // Notificar al adaptador de los cambios en la selección
            notifyDataSetChanged()
            itemClickListener.onItemClick(item) // Llama al método onItemClick del itemClickListener
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(position)
            true
        }
    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //private val llRow: LinearLayout = itemView.findViewById(R.id.llRow)

        fun bind(item: HistorialResumen) {
            val llRow: LinearLayout = itemView.findViewById(R.id.llRow)

            // 💥 Detecta si esta fila es la TOTAL
            val esTotal = item.nombre.equals("TOTAL", ignoreCase = true)

            // Llama a generarDetalle con el flag esTotal
            generarDetalle(llRow, item.nombre, item.cantidades, esTotal)
        }

        fun generarDetalle(layout: LinearLayout, cliente: String, cantidades: List<Int>, esTotal: Boolean = false) {
            layout.removeAllViews()

            // 💥 Columna Cliente
            val txtCliente = TextView(itemView.context).apply {
                text = cliente
                textSize = 14f
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setPadding(8, 8, 8, 8)
                setTextColor(Color.BLACK)
                if (esTotal) {
                    setTypeface(null, Typeface.BOLD) // ✅ Negrita solo para TOTAL
                } else {
                    setTypeface(null, Typeface.NORMAL) // 📝 Normal para las demás filas
                }
                background = ContextCompat.getDrawable(itemView.context, R.drawable.dotted_border)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2.8f
                )
            }
            layout.addView(txtCliente)

            // 💥 Columnas de años
            cantidades.forEach { cantidad ->
                val txtCantidad = TextView(itemView.context).apply {
                    text = cantidad.toString()
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setTextColor(Color.BLACK)
                    if (esTotal) {
                        setTypeface(null, Typeface.BOLD) // ✅ Negrita solo para TOTAL

                    } else {
                        setTypeface(null, Typeface.NORMAL) // 📝 Normal para las demás filas
                    }
                    background = ContextCompat.getDrawable(itemView.context, R.drawable.dotted_border)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT, // 💥 Igual alto para todas las celdas
                        0.5f
                    )
                }
                layout.addView(txtCantidad)
            }
        }


    }


    interface OnItemClickListener {
        fun onItemClick(item: HistorialResumen)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }


}


