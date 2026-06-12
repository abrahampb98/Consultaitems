package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R


class MiAdaptadorRef(private val datos: MutableList<datos>,
                     private val itemClickListener: OnItemClickListener,
                     private val imageClickListener: OnImageClickListener, // Agregar referencia al listener de la imagen
) : RecyclerView.Adapter<MiAdaptadorRef.ViewHolder>() {

    private var onItemLongClickListener: MiAdapterDetalle.OnItemLongClickListener? = null
    private var selectedPositions =
        mutableSetOf<Int>() // Conjunto para mantener las posiciones de los elementos seleccionados
    private var spinner: Int = 0



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.frmitems, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)
        //println("Elemento en la posición $position seleccionado: ${selectedPositions.contains(position)}")

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


    override fun getItemCount(): Int {
        return datos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtReferencia: TextView = itemView.findViewById(R.id.txtReferencia)
        private val txtDescrip: TextView = itemView.findViewById(R.id.txtDescrip)
        private val txtStock: TextView = itemView.findViewById(R.id.txtStock)
        private val txtPrecioSub: TextView = itemView.findViewById(R.id.txtPrecioSub)
        private val txtPrecioCont: TextView = itemView.findViewById(R.id.txtPrecioCont)
        private val txtPrecioCred: TextView = itemView.findViewById(R.id.txtPrecioCred)
        private val txtLSub: TextView = itemView.findViewById(R.id.txtLSub)
        private val txtLCont: TextView = itemView.findViewById(R.id.txtLCont)
        private val txtLoteCred: TextView = itemView.findViewById(R.id.txtLoteCred)
        private val stockImageView: ImageView = itemView.findViewById(R.id.StockImageView)
        private val webPV: ImageView = itemView.findViewById(R.id.WebImageViewPV)
        private val historialPV: ImageView = itemView.findViewById(R.id.HisotricoImageViewPV)

        fun bind(item: datos) {

            val textColor = if (item.cd_codigo != "0" && spinner != 1) {
                ContextCompat.getColor(itemView.context, R.color.red) // Color amarillo
            } else {
                ContextCompat.getColor(itemView.context, R.color.android_gray) // Color normal
            }

            // Aplicar el color a los textos
            txtReferencia.setTextColor(textColor)
            txtStock.setTextColor(textColor)
            txtPrecioSub.setTextColor(textColor)
            txtPrecioCont.setTextColor(textColor)
            txtPrecioCred.setTextColor(textColor)
            txtDescrip.setTextColor(textColor)
            txtLSub.setTextColor(textColor)
            txtLCont.setTextColor(textColor)
            txtLoteCred.setTextColor(textColor)


            txtReferencia.text = item.referencia
            txtStock.text = item.stock
            txtPrecioSub.text = item.precioSub
            txtPrecioCont.text = item.precioCont
            txtPrecioCred.text = item.precioCred
            txtDescrip.text = item.descripcion
            txtLSub.text = calculateAndFormat(item.precioSub.toDouble(), item.costoProm.toDouble())
            txtLCont.text = calculateAndFormat(item.precioCont.toDouble(), item.costoProm.toDouble())
            txtLoteCred.text = calculateAndFormat(item.precioCred.toDouble(), item.costoProm.toDouble())


            // Configura el OnClickListener para la ImageView
            stockImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onFirstImageClick(item.referencia, item.cd_codigo) // Asumiendo que "data" es accesible
                }
            }

            // Listener para la segunda imagen
            webPV.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onSecondImageClick(item.codigo) // Asumiendo que "data" es accesible
                }
            }

            historialPV.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onThirdImageClick(item.codigo) // Asumiendo que "data" es accesible
                }
            }



            // Configura el OnClickListener para toda la vista del item
            itemView.setOnClickListener {
                itemClickListener.onItemClick(item)
            }
        }
    }

    fun calculateAndFormat(value: Double, costProm: Double): String {
        if (costProm == 0.0) return "0.00" // Retorna "0.00" si costProm es 0 para evitar división por cero
        val result = value / costProm
        return String.format("%.2f", result) // Formatea el resultado a dos decimales
    }



    interface OnImageClickListener {
        fun onFirstImageClick(referencia: String, codigo: String)
        fun onSecondImageClick(codigo: String)

        fun onThirdImageClick(codigo: String)
    }


    // Método para obtener los elementos seleccionados
    fun getSelectedItems(): List<datos> {
        val selectedList = mutableListOf<datos>()
        for (position in selectedPositions) {
            if (position != RecyclerView.NO_POSITION && position < datos.size) {
                selectedList.add(datos[position])
            }
        }
       // println("Elementos seleccionados: $selectedList")
        return selectedList
    }

    interface OnItemClickListener {
        fun onItemClick(item: datos)
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

    fun fnObtenerSpinner(posicion: Int){
        spinner = posicion
    }
}


data class datos(
    val referencia: String,
    val stock: String,
    val precioSub: String,
    val precioCont: String,
    val precioCred: String,
    val codigo: String,
    val descripcion: String,
    val unidadCE: String,
    val costoProm: String,
    val combo: String,
    val cd_codigo: String,
    val it_regalo: String,
    val pv_precio7: String =""
)
