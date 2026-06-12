package com.example.Consultaitems.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.ClsLLenarControles

class AdaptadorRutaNueva(
    private val datos: MutableList<RutaNueva>,
    private val itemClickListener: OnItemClickListener,
    private val imageClickListener:OnImageClickListenerN
) : RecyclerView.Adapter<AdaptadorRutaNueva.ViewHolder>() {

    private var selectedPositions =
        mutableSetOf<Int>()
    private val selectedItems = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.frm_ruta_nueva, parent, false)
        return ViewHolder(view)
    }

    private var lastSelectedPosition: Int = -1 // Guardar la última selección

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
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
    }


    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSecRN: TextView = view.findViewById(R.id.tvSecRN)
        val tvCiudadRN: TextView = view.findViewById(R.id.tvCiudadRN)
        val chkVisitaRN: CheckBox = view.findViewById(R.id.chkVisitaRN)
        val chkCobroRN: CheckBox = view.findViewById(R.id.chkCobroRN)
        val chkVentaRN: CheckBox = view.findViewById(R.id.chkVentaRN)
        val chkGTRN: CheckBox = view.findViewById(R.id.chkGTRN)
        val tvClienteNombreRN: TextView = view.findViewById(R.id.tvClienteNombreRN)
        val tvDiaRN: TextView = view.findViewById(R.id.tvDiaRN)
        val btnRN: Button = view.findViewById(R.id.bntObservacionRN)
        val eliminarImageViewRN: ImageView = itemView.findViewById(R.id.eliminarImageViewRN)

        var codigo: String = ""

        fun bind(item: RutaNueva) {
            tvClienteNombreRN.text = item.cliente
            tvSecRN.text = item.sec
            tvCiudadRN.text = item.Ciudad
            tvDiaRN.text = item.dia
            codigo = item.codigo

            // **Eliminar cualquier listener anterior**
            chkVentaRN.setOnCheckedChangeListener(null)
            chkCobroRN.setOnCheckedChangeListener(null)
            chkVisitaRN.setOnCheckedChangeListener(null)
            chkGTRN.setOnCheckedChangeListener(null)

            // **Configurar el estado del CheckBox**
            chkVentaRN.isChecked = item.isSelectedVenta
            chkCobroRN.isChecked = item.isSelectedCobro
            chkVisitaRN.isChecked = item.isSelectedVisita
            chkGTRN.isChecked = item.isSelectedTelefono

            // **Reactivar Listeners**
            chkVentaRN.setOnCheckedChangeListener { _, isChecked ->
                item.isSelectedVenta = isChecked
                itemClickListener.onCheckBoxClickVentaN(item.codigo, if (isChecked) 1 else 0, item.estado)
            }

            chkCobroRN.setOnCheckedChangeListener { _, isChecked ->
                item.isSelectedCobro = isChecked
                itemClickListener.onCheckBoxClickCobroN(item.codigo, if (isChecked) 1 else 0, item.estado)
            }

            chkVisitaRN.setOnCheckedChangeListener { _, isChecked ->
                item.isSelectedVisita = isChecked
                itemClickListener.onCheckBoxClickVisitaN(item.codigo, if (isChecked) 1 else 0, item.estado)
            }

            chkGTRN.setOnCheckedChangeListener { _, isChecked ->
                item.isSelectedVisita = isChecked
                itemClickListener.onCheckBoxClickGTN(item.codigo, if (isChecked) 1 else 0, item.estado)
            }

            btnRN.setOnClickListener {
                itemClickListener.observacionN(item.codigo, item.cliente, item.observacion)
            }
        }

        init {

            // Agregar un click listener a la imagen de eliminar
            eliminarImageViewRN.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    eliminarItem(itemView.context, position, codigo)
                    notifyDataSetChanged()

                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: RutaNueva)
        fun onCheckBoxClickVisitaN(codigo: String, check: Int, estado: String)
        fun onCheckBoxClickCobroN(codigo: String, check: Int, estado: String)
        fun onCheckBoxClickVentaN(codigo: String, check: Int, estado: String)
        fun onCheckBoxClickGTN(codigo: String, check: Int, estado: String)
        fun observacionN(codigo: String, cliente: String, observacion: String)
    }

    interface OnImageClickListenerN {
        fun onDeleteItem(codigo: String)
    }

    fun eliminarItem(context: Context, position: Int, codigo: String) {
        if (position != RecyclerView.NO_POSITION) {

            // Eliminación estándar de un solo ítem
            datos.removeAt(position)
            notifyItemRemoved(position)

            imageClickListener.onDeleteItem(codigo)

        }
    }

    fun clearItems() {
        datos.clear()
        selectedItems.clear()
        notifyDataSetChanged()
    }

    data class RutaNueva(
        val codigo: String,
        val sec: String,
        val cliente: String,
        val Ciudad: String,
        var isSelectedVisita: Boolean = false,
        var isSelectedVenta: Boolean = false,
        var isSelectedCobro: Boolean = false,
        var isSelectedTelefono: Boolean = false,
        var dia: String,
        val estado: String,
        var observacion: String
    )
}
