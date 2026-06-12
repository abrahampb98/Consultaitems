package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorItemsFacturas(
    private val lista: MutableList<itemsFactura>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (itemsFactura, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorItemsFacturas.ViewHolder>() {

    private var selectedPos = -1

    interface OnItemClickListener {
        fun onItemClick(item: itemsFactura, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_items_factura, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvCodigo.text = item.codigo
        holder.tvReferencia.text = item.referencia
        holder.tvDescripcion.text = item.descripcion
        holder.tvStock.text = item.stock.toString()
        holder.tvMarca.text = item.marca
        holder.tvPeso.text = item.peso.toString()
        holder.itemView.isSelected = position == selectedPos

        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) doubleClickListener(lista[pos], pos)
                },
                onSingleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val prev = selectedPos
                        selectedPos = pos
                        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                        notifyItemChanged(pos)
                        itemClickListener.onItemClick(lista[pos], pos)
                    }
                }
            )
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvReferencia: TextView = view.findViewById(R.id.tvReferencia)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvMarca: TextView = view.findViewById(R.id.tvMarca)
        val tvPeso: TextView = view.findViewById(R.id.tvPeso)
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

    data class itemsFactura(
        val codigo: String,
        val referencia: String,
        val descripcion: String,
        val titulo: String,
        val stock: Double,
        val marca: String,
        val codigoB: String,
        val peso: Double,
        val costoProm: Double
    )
}
