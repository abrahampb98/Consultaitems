package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.Llamadas

class AdaptadorLlamada(
    private val datos: MutableList<Llamadas>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (Llamadas, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorLlamada.ViewHolder>() {

    private var selectedPos = -1

    interface OnItemClickListener {
        fun onItemClick(item: Llamadas, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCodigoLl: TextView = view.findViewById(R.id.txtCodigoLl)
        val txtFechaLl: TextView = view.findViewById(R.id.txtFechaLl)
        val txtEmpleadoLl: TextView = view.findViewById(R.id.txtEmpleadoLl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_llamadas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]
        holder.txtCodigoLl.text = item.codigo.toString()
        holder.txtFechaLl.text = item.fecha
        holder.txtEmpleadoLl.text = item.empleado
        holder.itemView.isSelected = position == selectedPos

        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        doubleClickListener(datos[pos], pos)
                    }
                },
                onSingleClick = {
                    val prev = selectedPos
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        selectedPos = pos
                        if (prev != -1) notifyItemChanged(prev)
                        notifyItemChanged(pos)
                        itemClickListener.onItemClick(datos[pos], pos)
                    }
                }
            )
        )
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevos: List<Llamadas>) {
        datos.clear()
        datos.addAll(nuevos)
        selectedPos = -1
        notifyDataSetChanged()
    }

    fun clearItems() {
        datos.clear()
        selectedPos = -1
        notifyDataSetChanged()
    }

    private class DoubleClickListener(
        private val onDoubleClick: () -> Unit,
        private val onSingleClick: () -> Unit,
        private val doubleClickTimeout: Long = 250L
    ) : View.OnClickListener {
        private var lastClickTime = 0L

        override fun onClick(v: View?) {
            val now = SystemClock.uptimeMillis()
            if (now - lastClickTime < doubleClickTimeout) {
                lastClickTime = 0L
                onDoubleClick()
            } else {
                lastClickTime = now
                v?.postDelayed({
                    if (SystemClock.uptimeMillis() - lastClickTime >= doubleClickTimeout) {
                        onSingleClick()
                    }
                }, doubleClickTimeout)
            }
        }
    }
}
