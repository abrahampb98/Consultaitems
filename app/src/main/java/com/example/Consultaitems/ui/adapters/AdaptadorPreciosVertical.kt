package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import java.util.Locale

class AdaptadorPreciosVertical(
    private val datos: MutableList<PreciosyStock>,
    private val itemClickListener: OnItemClickListener,
    private val imageClickListener: OnImageClickListener
) : RecyclerView.Adapter<AdaptadorPreciosVertical.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_precio_stock_vertical, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]
        holder.bind(item, position == selectedPosition)
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevosDatos: List<PreciosyStock>) {
        datos.clear()
        datos.addAll(nuevosDatos)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun clearItems() {
        datos.clear()
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtReferencia: TextView = view.findViewById(R.id.txtReferenciaVerticalPS)
        private val txtSku: TextView = view.findViewById(R.id.txtSkuVerticalPS)
        private val txtMarca: TextView = view.findViewById(R.id.txtMarcaVerticalPS)
        private val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcionVerticalPS)
        private val txtStock: TextView = view.findViewById(R.id.txtStockVerticalPS)
        private val txtContado: TextView = view.findViewById(R.id.txtContadoVerticalPS)
        private val txtPublicoIva: TextView = view.findViewById(R.id.txtPublicoIvaVerticalPS)

        private val btnOpciones: Button = view.findViewById(R.id.btnOpcionesVerticalPS)
        private val btnStock: Button = view.findViewById(R.id.btnStockVerticalPS)
        private val btnImagen: Button = view.findViewById(R.id.btnImagenVerticalPS)

        fun bind(item: PreciosyStock, isSelected: Boolean) {
            itemView.isSelected = isSelected

            txtReferencia.text = item.referencia.ifBlank { "Sin referencia" }
            txtSku.text = "SKU: ${item.sku.ifBlank { "-" }}"
            txtMarca.text = "Marca: ${item.marca.ifBlank { "-" }}"
            txtDescripcion.text = item.descripcion.ifBlank { "Sin descripción" }

            txtStock.text = item.total.ifBlank { "0" }
            txtContado.text = item.contado.ifBlank { "0.00" }
            txtPublicoIva.text = item.final.ifBlank { "0.00" }

            itemView.setOnClickListener {
                seleccionarItem()
                itemClickListener.onItemClick(item)
            }

            btnOpciones.setOnClickListener {
                seleccionarItem()
                itemClickListener.onItemClick(item)
            }

            btnStock.setOnClickListener {
                imageClickListener.onFirstImageClick(item.referencia)
            }

            btnImagen.setOnClickListener {
                imageClickListener.onSecondImageClick(item.codigo)
            }
        }

        private fun seleccionarItem() {
            val previousPosition = selectedPosition
            selectedPosition = adapterPosition

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition)
            }

            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(selectedPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: PreciosyStock)
    }

    interface OnImageClickListener {
        fun onFirstImageClick(referencia: String)
        fun onSecondImageClick(codigo: String)
    }

    fun calculateAndFormat(value: Double, costProm: Double): String {
        if (costProm == 0.0) return "0.00"

        val result = value / costProm
        return String.format(Locale.US, "%.2f", result)
    }
}