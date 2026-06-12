package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorClientes(
    private val datos: MutableList<Clientes>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (Clientes, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorClientes.ViewHolder>() {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onItemClick(item: Clientes, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.frm_detalle_clientes, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.txtCodigo.text = item.codigo
        holder.txtNombre.text = item.nombre
        holder.txtRazonC.text = item.razonComercial

        holder.itemView.isSelected = position == selectedPos

        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition

                    if (pos != RecyclerView.NO_POSITION && pos < datos.size) {
                        doubleClickListener(datos[pos], pos)
                    }
                },
                onSingleClick = {
                    val pos = holder.bindingAdapterPosition

                    if (pos != RecyclerView.NO_POSITION && pos < datos.size) {
                        val prev = selectedPos

                        selectedPos = pos

                        if (prev != RecyclerView.NO_POSITION) {
                            notifyItemChanged(prev)
                        }

                        notifyItemChanged(pos)

                        itemClickListener.onItemClick(datos[pos], pos)
                    }
                }
            )
        )
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevos: List<Clientes>) {
        datos.clear()
        datos.addAll(nuevos)
        selectedPos = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun clearItems() {
        datos.clear()
        selectedPos = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCodigo: TextView = view.findViewById(R.id.txtCodigoC)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreC)
        val txtRazonC: TextView = view.findViewById(R.id.txtRazonC)
    }

    private class DoubleClickListener(
        private val onDoubleClick: () -> Unit,
        private val onSingleClick: () -> Unit,
        private val doubleClickTimeout: Long = 250L
    ) : View.OnClickListener {

        private var lastClickTime: Long = 0L
        private var singleClickRunnable: Runnable? = null

        override fun onClick(v: View?) {
            val now = SystemClock.uptimeMillis()

            if (now - lastClickTime < doubleClickTimeout) {
                singleClickRunnable?.let { runnable ->
                    v?.removeCallbacks(runnable)
                }

                singleClickRunnable = null
                lastClickTime = 0L

                onDoubleClick()
            } else {
                lastClickTime = now

                val runnable = Runnable {
                    if (SystemClock.uptimeMillis() - lastClickTime >= doubleClickTimeout) {
                        lastClickTime = 0L
                        onSingleClick()
                    }
                }

                singleClickRunnable = runnable
                v?.postDelayed(runnable, doubleClickTimeout)
            }
        }
    }

    data class Clientes(
        val codigo: String,
        val nombre: String,
        val cc_nivelprecio: String,
        val en_identificacion: String,
        val direccion: String,
        val fono: String,
        val ciudad: String,
        val cupototal: String,
        val cupodisponible: String,
        val cupoutilizado: String,
        val clientepublico: String,
        val pagaiva: String,
        val en_tipoid: String,
        val en_ruc: String,
        val en_genero: String,
        val en_tipopersona: String,
        val en_apellido1: String,
        val en_apellido2: String,
        val en_nombre1: String,
        val en_nombre2: String,
        val razonComercial: String,
        val en_razonsocial: String,
        val en_correo: String,
        val pp_codigo: String,
        val pz_descripcion: String,
        val cupoCliente: String,
        val restringido: String,
        val pz_cantidadpago: String,
        val cl_orden: String,
        val dq_interno: String,
        val pr_descripcion: String,
        val dc_porcentaje: String,
        val fa_coddocumento: String,
        val cl_lopdpusuarioing: String
    )
}