package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorDoc(
    private val datos: MutableList<Documento>,
    private val itemClickListener: OnItemClickListener,
    private val imageClickListener: OnImageClickListener
) : RecyclerView.Adapter<AdaptadorDoc.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frmdetalledocumento, parent, false)
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

    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtBodegaDC: TextView = view.findViewById(R.id.txtBodegaDC)
        val txtInternoDC: TextView = view.findViewById(R.id.txtInternoDC)
        val txtSriDC: TextView = view.findViewById(R.id.txtSriDC)
        val txtFechaDC: TextView = view.findViewById(R.id.txtFechaDC)
        val txtGuiarDC: TextView = view.findViewById(R.id.txtGuiarDC)
        val txtGuiatDC: TextView = view.findViewById(R.id.txtGuiatDC)
        val txtTotalDC: TextView = view.findViewById(R.id.txtTotalDC)
        val WebImageViewDC: ImageView = view.findViewById(R.id.WebImageViewDC)
        val WebImageViewDF: ImageView = view.findViewById(R.id.WebImageViewDF)

        fun bind(item: Documento) {
            txtBodegaDC.text = item.Bodega
            txtInternoDC.text = item.Interno
            txtSriDC.text = item.Sri
            txtFechaDC.text = item.Fecha
            txtGuiarDC.text = item.GuiaR
            txtGuiatDC.text = item.GuiaT
            txtTotalDC.text = item.Total


            WebImageViewDF.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onFirstImageClick(item.Interno, item.Sri) // Asumiendo que "data" es accesible
                }
            }

            // Listener para la segunda imagen
            WebImageViewDC.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onImageClick(item.Serie, item.ClaveA, item.Fecha) // Asumiendo que "data" es accesible
                }
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(item: Documento)
    }

    interface OnImageClickListener {

        fun onFirstImageClick(Interno: String, Sri: String)

        fun onImageClick(serie: String, clave: String, fecha: String)
    }


    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

}


data class Documento(
    val Bodega: String,
    val Interno: String,
    val Sri: String,
    val Fecha: String,
    val GuiaR: String,
    val GuiaT: String,
    val Total: String,
    val Serie: String,
    val ClaveA: String
)

data class ItemSpinner(val codigo: String, val descripcion: String) {
    override fun toString(): String {
        return descripcion
    }
}

