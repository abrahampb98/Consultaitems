package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorPrecios(
    private val datos: MutableList<PreciosyStock>,
    private val itemClickListener: OnItemClickListener,
    private val imageClickListener: OnImageClickListener
) : RecyclerView.Adapter<AdaptadorPrecios.ViewHolder>() {

    private var onItemLongClickListener: MiAdapterDetalle.OnItemLongClickListener? = null
    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frmdetallepreciosystock, parent, false)
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
        val txtMarcaPS: TextView = view.findViewById(R.id.txtMarcaPS)
        val txtRefPS: TextView = view.findViewById(R.id.txtRefPS)
        val txtSkuPS: TextView = view.findViewById(R.id.txtSkuPS)
        val txtTotalPS: TextView = view.findViewById(R.id.txtTotalPS)
        val txtContadoPS: TextView = view.findViewById(R.id.txtContadoPS)
        val txtCreditoPS: TextView = view.findViewById(R.id.txtCreditoPS)
        val txtPublicoPS: TextView = view.findViewById(R.id.txtPublicoPS)
        val txtFinalPS: TextView = view.findViewById(R.id.txtFinalPS)
        val txtPesoPS: TextView = view.findViewById(R.id.txtPesoPS)
        val stockPS: ImageView = view.findViewById(R.id.stockImageViewPS)
        val webPS: ImageView = view.findViewById(R.id.WebImageViewPS)
        val txtLsubPS: TextView = view.findViewById(R.id.txtLsubPS)
        val txtLcontPS: TextView = view.findViewById(R.id.txtLcontPS)
        val txtLcredPS: TextView = view.findViewById(R.id.txtLcredPS)
        val txtLpubPS: TextView = view.findViewById(R.id.txtLpubPS)


        fun bind(item: PreciosyStock) {
            txtMarcaPS.text = item.sub
            txtRefPS.text = item.referencia
            txtSkuPS.text = item.sku
            txtTotalPS.text = item.total
            txtContadoPS.text = item.contado
            txtCreditoPS.text = item.credito
            txtPublicoPS.text = item.publico
            txtFinalPS.text = item.final
            txtPesoPS.text = item.peso
            txtLsubPS.text = calculateAndFormat(  item.sub.toDoubleOrNull() ?: 0.0, item.costProm.toDouble())
            txtLcontPS.text = calculateAndFormat(item.contado.toDouble(), item.costProm.toDouble())
            txtLcredPS.text = calculateAndFormat(item.credito.toDouble(), item.costProm.toDouble())
            txtLpubPS.text = calculateAndFormat(item.publico.toDouble(), item.costProm.toDouble())


            // Configura el OnClickListener para la ImageView
            stockPS.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onFirstImageClick(item.referencia) // Asumiendo que "data" es accesible
                }
            }

            // Listener para la segunda imagen
            webPS.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onSecondImageClick(item.codigo) // Asumiendo que "data" es accesible
                }
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
        if (costProm == 0.0) return "0.00" // Retorna "0.00" si costProm es 0 para evitar división por cero
        val result = value / costProm
        return String.format("%.2f", result) // Formatea el resultado a dos decimales
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

}


data class PreciosyStock(
    val marca: String,
    val referencia: String,
    val sku: String,
    val descripcion: String,
    val total: String,
    val contado: String,
    val credito: String,
    val publico: String,
    val final: String,
    val peso: String,
    val titulo:String,
    val codigo: String,
    val sub: String,
    val costProm: String
)
