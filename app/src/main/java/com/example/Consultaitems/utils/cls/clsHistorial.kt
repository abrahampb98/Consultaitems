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
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.adapters.AdaptadorFacturas
import com.example.Consultaitems.ui.adapters.AdaptadorVentasAnuales
import com.example.Consultaitems.ui.adapters.Facturas
import java.math.BigDecimal
import java.util.Calendar

class hisotrial(private val cl_codigo: String, private val it_codigo: String) : DialogFragment() {


    private lateinit var llenarControles: ClsLLenarControles

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_historial, container, false)
        val txtTitulo = view.findViewById<TextView>(R.id.txtTitulo)
        llenarControles = ClsLLenarControles(requireContext())

        // Simulación de datos desde DB
        val listaVentas = llenarControles.fnObtenerVentasAnuales(cl_codigo, it_codigo)

        if (listaVentas.isNotEmpty()) {
            txtTitulo.text = "${listaVentas[0].referencia}"
        } else {
            txtTitulo.text = "(Sin ventas)"
        }


        val recyclerAnios = view.findViewById<RecyclerView>(R.id.recyclerAnios)
        recyclerAnios.layoutManager = GridLayoutManager(context, 2) // 2 columnas
        recyclerAnios.adapter = AdaptadorVentasAnuales(listaVentas)


        return view
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(), // Máximo 85% del ancho pantalla
            ViewGroup.LayoutParams.WRAP_CONTENT                   // Alto se adapta al contenido
        )
    }


}
