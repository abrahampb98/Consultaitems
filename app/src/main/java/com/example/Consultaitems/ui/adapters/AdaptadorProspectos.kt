package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorProspectos(
    private val datos: MutableList<Prospectos>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (Prospectos, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorProspectos.ViewHolder>() {

    private var selectedPos: Int = -1

    interface OnItemClickListener {
        fun onItemClick(item: Prospectos, position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTipoP: TextView = itemView.findViewById(R.id.txtTipoP)
        val txtNombre1P: TextView = itemView.findViewById(R.id.txtNombre1P)
        val txtNombre2P: TextView = itemView.findViewById(R.id.txtNombre2P)
        val txtApellido1P: TextView = itemView.findViewById(R.id.txtApellido1P)
        val txtApellido2P: TextView = itemView.findViewById(R.id.txtApellido2P)
        val txtRazonSocialP: TextView = itemView.findViewById(R.id.txtRazonSocialP)
        val txtFonoP: TextView = itemView.findViewById(R.id.txtFonoP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_prospecto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.txtNombre1P.text = item.Nombre1
        holder.txtNombre2P.text = item.Nombre2
        holder.txtApellido1P.text = item.Apellido1
        holder.txtApellido2P.text = item.Apellido2
        holder.txtRazonSocialP.text = item.RazonSocial
        holder.txtFonoP.text = item.Fono

        holder.txtTipoP.text = when (item.Tipo) {
            "PROSPECTO" -> "PRO"
            "COTZUL" -> "COT"
            else -> item.Tipo
        }

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
                    if (pos == RecyclerView.NO_POSITION) return@DoubleClickListener

                    selectedPos = pos
                    if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                    notifyItemChanged(pos)
                    itemClickListener.onItemClick(datos[pos], pos)
                }
            )
        )
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevos: List<Prospectos>) {
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
        private var lastClickTime: Long = 0L

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

    data class Prospectos(
        val Tipo: String,
        val Codigo: String,
        val Nombre1: String,
        val Nombre2: String,
        val Apellido1: String,
        val Apellido2: String,
        val NombreComercial: String,
        val RazonSocial: String,
        val Direccion: String,
        val Fono: String,
        val CodCiudad: String,
        val Ciudad: String,
        val Provincia: String,
        val Correo: String,
        val oc_codigo: String,
        val oc_descripcion: String,
        val gt_codigo: String,
        val gc_codigopostal: String,
        val gc_sector: String,
        val gc_puntoreferencia: String,
        val gc_googlemap: String
    )
}
