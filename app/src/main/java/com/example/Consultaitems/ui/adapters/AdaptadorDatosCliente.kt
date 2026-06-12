package com.example.Consultaitems.ui.adapters

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorDatosDelCliente(
    private val datos: MutableList<clienteD>,
    private val itemClickListener: OnItemClickListener,
    private val imageClickListener: OnImageClickListener
) : RecyclerView.Adapter<AdaptadorDatosDelCliente.ViewHolder>() {

    private var onItemLongClickListener: MiAdapterDetalle.OnItemLongClickListener? = null
    private val selectedPositions = mutableSetOf<Int>()
    private val itemStateArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.frmdetallecliente, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)

        val isSelected = selectedPositions.contains(position)
        holder.itemView.isSelected = isSelected

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isSelected

        holder.checkboxActClienteCL.setOnCheckedChangeListener(null)
        holder.checkboxActClienteCL.isChecked = item.isSelectedCli

        holder.itemView.setOnClickListener {
            selectedPositions.clear()
            selectedPositions.add(holder.bindingAdapterPosition)
            notifyDataSetChanged()
            itemClickListener.onItemClick(item)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val adapterPosition = holder.bindingAdapterPosition

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return@setOnCheckedChangeListener
            }

            itemStateArray.put(adapterPosition, isChecked)
            item.isSelected = isChecked

            if (isChecked) {
                itemClickListener.onCheckBoxClick(item.Codigo)
            } else {
                itemClickListener.onCheckBoxUncheck(item.Codigo)
            }
        }

        holder.checkboxActClienteCL.setOnCheckedChangeListener { _, isChecked ->
            item.isSelectedCli = isChecked
        }

        holder.itemView.setOnLongClickListener {
            val adapterPosition = holder.bindingAdapterPosition

            if (adapterPosition != RecyclerView.NO_POSITION) {
                onItemLongClickListener?.onItemLongClick(adapterPosition)
            }

            true
        }
    }

    override fun getItemCount(): Int {
        return datos.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtNumeroCl: TextView = view.findViewById(R.id.txtNumeroCL)
        val txtClienteCL: TextView = view.findViewById(R.id.txtClienteCL)
        val txtDireccionCL: TextView = view.findViewById(R.id.txtDireccionCL)
        val txtTelefonoCL: TextView = view.findViewById(R.id.txtTelefonoCL)

        val webCL: ImageView = view.findViewById(R.id.WebImageViewCL)
        val btnAdicionalCL: ImageView = view.findViewById(R.id.btnDatosAdicionalesCL)

        val checkBox: CheckBox = view.findViewById(R.id.checkboxClienteCL)
        val checkboxActClienteCL: CheckBox = view.findViewById(R.id.checkboxActClienteCL)

        fun bind(item: clienteD) {
            txtNumeroCl.text = item.Numero
            txtClienteCL.text = item.Cliente
            txtDireccionCL.text = item.Direccion
            txtTelefonoCL.text = item.Telefono

            webCL.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onImageClick(item.Cliente, item.Codigo)
                }
            }

            btnAdicionalCL.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    imageClickListener.onAdicionalClick(item.Codigo)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: clienteD)
        fun onCheckBoxClick(codigo: String)
        fun onCheckBoxUncheck(codigo: String)
    }

    interface OnImageClickListener {
        fun onImageClick(cliente: String, Codigo: String)
        fun onAdicionalClick(Codigo: String)
    }

    fun clearItems() {
        datos.clear()
        selectedPositions.clear()
        itemStateArray.clear()
        notifyDataSetChanged()
    }
}

data class clienteD(
    val Numero: String,
    val Cliente: String,
    val Direccion: String,
    val Telefono: String,
    val Codigo: String,
    var isSelected: Boolean = false,
    var isSelectedCli: Boolean = false
)