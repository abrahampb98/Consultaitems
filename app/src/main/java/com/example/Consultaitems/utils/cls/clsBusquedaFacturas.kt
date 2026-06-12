package com.example.Consultaitems.utils.cls

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.adapters.AdaptadorFacturas
import com.example.Consultaitems.ui.adapters.Facturas
import java.math.BigDecimal

class consultaItems(private val cl_codigo: String) : DialogFragment(), AdaptadorFacturas.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorFacturas
    private lateinit var spnCriteriosF: Spinner
    private lateinit var bntAgregarF: Button
    private lateinit var llenarControles: ClsLLenarControles
    private val listReferencia = mutableListOf<Facturas>()
    private var listener: OnItemSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_consulta_facturas, container, false)

        // Inicializa las vistas
        recyclerView = view.findViewById(R.id.recyclerviewItems)
        spnCriteriosF = view.findViewById(R.id.spnCriteriosF)
        bntAgregarF = view.findViewById(R.id.bntAgregarF)

        // Inicializa el adaptador y las utilidades
        adapter = AdaptadorFacturas(listReferencia, object : AdaptadorFacturas.OnItemClickListener {
            override fun onItemClick(item: Facturas) {
                // Maneja el clic en el item
            }
        })
        llenarControles = ClsLLenarControles(this.requireContext())

        val criterios = listOf("Facturas", "Criterios")
        val adapterCriterios = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, criterios)
        adapterCriterios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCriteriosF.adapter = adapterCriterios

        realizarBusqueda()

        // Configura el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        spnCriteriosF.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> realizarBusqueda() // Facturas seleccionadas
                    1 -> llenarCriterios() // Criterios seleccionados
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bntAgregarF.setOnClickListener {
            val (facturas, saldos) = obtenerSeleccionados()
            listener?.onItemsSelected(facturas, saldos)
            dismiss() // Cerrar el DialogFragment
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? OnItemSelectedListener
        if (listener == null) {
            // Log.e("consultaItems", "Target fragment does not implement OnItemSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun llenarCriterios() {
        listReferencia.clear()
        val resultados = llenarControles.fnBuscarFacturasCriterios()
        listReferencia.addAll(resultados)
        adapter.notifyDataSetChanged()
    }

    private fun realizarBusqueda() {
        listReferencia.clear()
        val resultados = llenarControles.fnBuscarFacturas(cl_codigo)
        listReferencia.addAll(resultados)
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(item: Facturas) {}

    interface OnItemSelectedListener {
        fun onItemsSelected(facturas: List<String>, saldos: List<BigDecimal>)
    }

    private fun obtenerSeleccionados(): Pair<List<String>, List<BigDecimal>> {
        val criterioSeleccionado = spnCriteriosF.selectedItem.toString() // Obtiene el criterio actual

        val facturas = listReferencia.filter { it.isSelected }
            .map { it.Factura }

        val saldos: List<BigDecimal> = if (criterioSeleccionado == "Facturas") {
            // Solo obtener saldos si el criterio seleccionado es "Facturas"
            listReferencia.filter { it.isSelected }
                .map {
                    try {
                        it.Saldo?.trim()?.takeIf { it.isNotEmpty() }?.toBigDecimal() ?: BigDecimal.ZERO
                    } catch (e: NumberFormatException) {
                        println("Error al convertir '${it.Saldo}' a BigDecimal")
                        BigDecimal.ZERO
                    }
                }
        } else {
            // Si el criterio es "Criterios", la lista de saldos debe estar vacía
            emptyList()
        }

        return Pair(facturas, saldos)
    }


}
