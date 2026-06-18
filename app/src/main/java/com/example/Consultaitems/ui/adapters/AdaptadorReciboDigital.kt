package com.example.Consultaitems.ui.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.fragments.frmReciboDigital
import java.util.Locale

class AdaptadorRecibo(
    private val activity: frmReciboDigital,
    val datos: MutableList<Recibo>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (Recibo, Int) -> Unit  // Modificado para aceptar la posición
    ): RecyclerView.Adapter<AdaptadorRecibo.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalle_recibo_digital, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)

        holder.itemView.isSelected = selectedPositions.contains(position)

        holder.itemView.setOnClickListener(DoubleClickListener(
            onDoubleClick = { _ ->
                val realPosition = holder.bindingAdapterPosition

                if (realPosition != RecyclerView.NO_POSITION) {
                    val itemActual = datos[realPosition]

                    seleccionarFila(realPosition)

                    // Esto asegura que la fila seleccionada y los datos cargados sean los mismos
                    itemClickListener.onItemClick(itemActual, realPosition)

                    // Aquí haces la edición
                    doubleClickListener(itemActual, realPosition)
                }
            },
            onSingleClick = { _ ->
                val realPosition = holder.bindingAdapterPosition

                if (realPosition != RecyclerView.NO_POSITION) {
                    val itemActual = datos[realPosition]

                    seleccionarFila(realPosition)

                    itemClickListener.onItemClick(itemActual, realPosition)
                }
            }
        ))
    }

    override fun getItemCount() : Int{
        return datos.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtTransaccionDE: TextView = view.findViewById(R.id.txtTransaccionDE)
        val txtBancoDE: TextView = view.findViewById(R.id.txtBancoDE)
        val txtDocDE: TextView = view.findViewById(R.id.txtDocDE)
        val txtCtaDE: TextView = view.findViewById(R.id.txtCtaDE)
        val txtFechaDE: TextView = view.findViewById(R.id.txtFechaDE)
        val txtValorDE: TextView = view.findViewById(R.id.txtValorDE)
        val txtConceptoDE: TextView = view.findViewById(R.id.txtConceptoDE)
        val txtObservacionDE: TextView = view.findViewById(R.id.txtObservacionDE)
        val eliminarImageView: ImageView = itemView.findViewById(R.id.eliminarDE)

        fun bind(item: Recibo) {
            txtTransaccionDE.text = item.Transaccion
            txtBancoDE.text = item.Banco
            txtDocDE.text = item.Doc
            txtCtaDE.text = item.Cuenta
            txtFechaDE.text = item.Fecha
            txtValorDE.text = item.Valor
            txtConceptoDE.text = item.Concepto
            txtObservacionDE.text = item.Observacion
        }
        init {

            // Agregar un click listener a la imagen de eliminar
            eliminarImageView.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    fnEliminarItem(position)
                    activity.fnCalcularTotales()
                }
            }
        }

    }

    private fun seleccionarFila(position: Int) {
        val posicionAnterior = selectedPositions.firstOrNull()

        selectedPositions.clear()
        selectedPositions.add(position)

        if (posicionAnterior != null && posicionAnterior != position) {
            notifyItemChanged(posicionAnterior)
        }

        notifyItemChanged(position)
    }

    private fun fnEliminarItem(position: Int) {
        datos.removeAt(position)
        selectedPositions.remove(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, datos.size)
    }


    interface OnItemClickListener {
        fun onItemClick(item: Recibo, position: Int)
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

    fun fnSumatoriaValor(): Double {
        return datos.sumOf { it.Valor.toDoubleOrNull() ?: 0.0 }
    }

    fun updateData(newData: List<Recibo>) {
        //Log.d("RecyclerViewAdapter", "Updating data with ${newData.size} items.")
        datos.clear()
        datos.addAll(newData)
        notifyDataSetChanged()
        //checkEmptyState()
    }


}


data class Recibo(
    val Transaccion: String,
    val Banco: String,
    val Doc: String,
    val Cuenta: String,
    val Fecha: String,
    val Valor: String,
    val Concepto: String,
    val Observacion: String,
    val ba_codigo: Int,
    val tr_codigo: Int,
    val bc_codigo: Int
)



class DoubleClickListener(
    private val doubleClickTimeLimit: Long = 300L, // Ajusta según sea necesario
    private val onDoubleClick: (View) -> Unit,
    private val onSingleClick: (View) -> Unit
) : View.OnClickListener {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var isDoubleClick = false

    override fun onClick(v: View) {
        if (isDoubleClick) {
            isDoubleClick = false
            handler.removeCallbacksAndMessages(null)
            onDoubleClick(v) // Llamar al evento de doble clic
        } else {
            isDoubleClick = true
            handler.postDelayed({
                if (isDoubleClick) {
                    isDoubleClick = false
                    onSingleClick(v) // Llamar al evento de clic simple si no hubo segundo clic
                }
            }, doubleClickTimeLimit)
        }
    }
}