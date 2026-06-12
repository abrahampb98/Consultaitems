package com.example.Consultaitems.utils.cls

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.fragments.frmPedidoVendedor
import java.util.Locale

// ===== Modelos UI =====
data class ComboGroupUI(
    val cbCodigo: String,
    val comboNombre: String,
    val fuente: String,
    val itemsTotal: Int,
    val itemsEnPedido: Int,
    val qtyHistCombo: Double,
    val items: List<ComboItemUI>,
    val lote: Double,
    val stock: Int // <- no se usa en cabecera (queda 0.0)
)

data class ComboItemUI(
    val itCodigo: String,
    val referencia: String,
    val descripcion: String,
    val enPedido: Boolean,
    val qtyHistItem: Int,
    val lote: Double,
    val stock: Int
)

class CombosDialogFragment : DialogFragment() {

    // >>> Igual que tu PedidosDialogFragment:
    var onComboSelected: ((cbCodigo: String, cbNombre: String) -> Unit)? = null
    var onCerrar: (() -> Unit)? = null

    private lateinit var llenarControles: ClsLLenarControles

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val peCodDocumento = requireArguments().getString(ARG_PEDIDO) ?: ""
        val clCodigo       = requireArguments().getString(ARG_CLIENTE) ?: ""

        llenarControles = ClsLLenarControles(requireContext())

        // --- Buscador general (arriba del listado)
        val etBuscar = EditText(requireContext()).apply {
            hint = "Buscar referencia..."
            setSingleLine(true)
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // --- Recycler raíz
        val rv = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        // --- Contenedor vertical
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(etBuscar)
            addView(rv)
            setPadding(0, 8, 0, 0)
        }

        val rowsOriginal: List<ComboItemRow> = llenarControles.fnCombosPedidoMasHistorico(
            peCodDocumento = peCodDocumento,
            clCodigo = clCodigo,
            incluirLinea = true
        )

        return if (rowsOriginal.isEmpty()) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Sugerencias de combos")
                .setMessage("No hay combos para mostrar.")
                .setPositiveButton("Cerrar", null) // lo controlamos en onShow
                .create()

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            dialog.setOnShowListener {
                val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                btn.setOnClickListener {
                    onCerrar?.invoke()
                    dialog.dismiss()
                }
            }

            dialog
        } else {
            // Construcción inicial
            var grupos = buildComboGroups(rowsOriginal)

            val adapter = CombosCardAdapter(grupos, object : CombosCardAdapter.OnComboClickListener {
                override fun onComboClick(combo: ComboGroupUI) {
                    onComboSelected?.invoke(combo.cbCodigo, combo.comboNombre)
                    dismiss()
                }
            })
            rv.adapter = adapter

            // --- Filtro general
            fun norm(s: String) = s.lowercase(Locale.ROOT)

            fun aplicaFiltro(q: String) {
                if (q.isBlank()) {
                    grupos = buildComboGroups(rowsOriginal)
                } else {
                    val qn = norm(q)
                    val combosQueCoinciden = rowsOriginal
                        .groupBy { it.cbCodigo }
                        .filter { (_, items) ->
                            items.any { r -> norm(r.itReferencia).contains(qn) }
                        }
                        .values
                        .flatten()
                    grupos = buildComboGroups(combosQueCoinciden)
                }
                adapter.updateData(grupos)
            }

            etBuscar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    aplicaFiltro(s?.toString().orEmpty())
                }
            })

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Sugerencias de combos")
                .setView(container)
                .setNegativeButton("Cerrar", null) // lo controlamos en onShow
                .create()

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            // (Opcional extra) bloquear explícitamente el botón atrás:
            // dialog.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }

            dialog.setOnShowListener {
                val btnCerrar = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                btnCerrar.setOnClickListener {
                    onCerrar?.invoke()   // 🔹 aquí llamamos tu callback
                    dialog.dismiss() // SOLO aquí se cierra
                }
            }
            dialog
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)       // no se cierra con botón atrás
        dialog?.setCanceledOnTouchOutside(false) // no se cierra tocando fuera
    }

      private fun buildComboGroups(rows: List<ComboItemRow>): List<ComboGroupUI> {
        // Clave SOLO con campos de CABECERA (sin stock ni lote por ítem)
        data class Key(
            val cb: String,
            val nom: String,
            val fuente: String,
            val tot: Int,
            val enPed: Int,
            val qty: Double
        )

        val map = linkedMapOf<Key, MutableList<ComboItemUI>>()

        rows.forEach { r ->
            val k = Key(
                cb    = r.cbCodigo,
                nom   = r.comboNombre,
                fuente= r.fuente,
                tot   = r.itemsTotal,
                enPed = r.itemsEnPedido,
                qty   = r.qtyHistCombo
            )
            val list = map.getOrPut(k) { mutableListOf() }
            list += ComboItemUI(
                itCodigo    = r.itCodigo,
                referencia  = r.itReferencia,
                descripcion = r.itDescripcion,
                enPedido    = r.enPedido,
                qtyHistItem = r.qtyHistItem,
                lote        = r.lote,   // detalle
                stock       = r.stock   // detalle
            )
        }

        return map.map { (k, items) ->
            val loteCab  = items.firstOrNull()?.lote ?: 0.0 // si quieres mostrarlo en cabecera
            ComboGroupUI(
                cbCodigo      = k.cb,
                comboNombre   = k.nom,
                fuente        = k.fuente,
                itemsTotal    = k.tot,
                itemsEnPedido = k.enPed,
                qtyHistCombo  = k.qty,
                items         = items,
                lote          = loteCab,
                stock         = 0 // no usamos stock en cabecera
            )
        }
    }

    // ===== Adapters =====

    private class ComboLinesAdapter(
        private val items: List<ComboItemUI>,
        private val onRowClick: () -> Unit
    ) : RecyclerView.Adapter<ComboLinesAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvCheck: TextView = v.findViewById(R.id.tvCheck)
            val tvRef: TextView   = v.findViewById(R.id.tvRef)
            val tvDesc: TextView  = v.findViewById(R.id.tvDesc)
            val tvQty: TextView   = v.findViewById(R.id.tvQty)
            val rowRoot: View     = v.findViewById(R.id.rowRoot) // root del item
            val tvStock: TextView = v.findViewById(R.id.tvStock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.frm_detalle_combo, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = items[position]
            h.tvCheck.text = if (item.enPedido) "✓" else "✗"
            h.tvCheck.setTextColor(
                if (item.enPedido) Color.parseColor("#2E7D32")
                else Color.parseColor("#B71C1C")
            )
            h.tvRef.text  = item.referencia
            h.tvDesc.text = item.descripcion
            h.tvQty.text  = item.qtyHistItem.toString()
            h.tvStock.text = item.stock.toString()

            // ← click en cualquier fila del detalle dispara selección del combo
            h.itemView.setOnClickListener { onRowClick() }
            h.rowRoot.setOnClickListener { onRowClick() }
        }

        override fun getItemCount() = items.size
    }

    private class CombosCardAdapter(
        private var grupos: List<ComboGroupUI>,
        private val listener: OnComboClickListener
    ) : RecyclerView.Adapter<CombosCardAdapter.VH>() {

        interface OnComboClickListener {
            fun onComboClick(combo: ComboGroupUI)
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cardRoot: View? = v.findViewById(R.id.cardRoot)
            val tvComboCab: TextView = v.findViewById(R.id.tvComboCab)
            val tvFuente: TextView   = v.findViewById(R.id.tvFuente)
            val tvCobertura: TextView= v.findViewById(R.id.tvCobertura)
            val tvHistQty: TextView  = v.findViewById(R.id.tvHistQty)
            val rvComboItems: RecyclerView = v.findViewById(R.id.rvComboItems)
            val tvLote: TextView = v.findViewById(R.id.tvLote)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.frm_combos, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val g = grupos[position]
            h.tvComboCab.text  = "${g.cbCodigo} - ${g.comboNombre}"
            h.tvFuente.text    = g.fuente
            h.tvCobertura.text = "${g.itemsEnPedido}/${g.itemsTotal}"
            h.tvHistQty.text   = g.qtyHistCombo.toInt().toString()
            h.tvLote.text      = String.format("%.2f", g.lote)

            // Callback a usar por cada fila del detalle
            val rowClick = { listener.onComboClick(g) }

            h.rvComboItems.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ComboLinesAdapter(g.items, onRowClick = rowClick)
                isNestedScrollingEnabled = false
                isFocusable = false
                isClickable = false
            }

            // Click en la cabecera/card
            h.itemView.setOnClickListener { listener.onComboClick(g) }
            h.cardRoot?.setOnClickListener { listener.onComboClick(g) }
        }

        override fun getItemCount() = grupos.size

        // --- permite refrescar la data al filtrar
        fun updateData(nuevos: List<ComboGroupUI>) {
            this.grupos = nuevos
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val ARG_PEDIDO  = "arg_pedido"
        private const val ARG_CLIENTE = "arg_cliente"

        fun newInstance(peCodDocumento: String, clCodigo: String) = CombosDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PEDIDO, peCodDocumento)
                putString(ARG_CLIENTE, clCodigo)
            }
        }
    }
}
