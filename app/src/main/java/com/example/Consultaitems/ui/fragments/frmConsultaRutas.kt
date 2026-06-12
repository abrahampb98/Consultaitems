package com.example.Consultaitems.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.adapters.AdaptadorConsultaRutas
import com.example.Consultaitems.ui.adapters.AdaptadorItemxCliente
import com.example.Consultaitems.ui.adapters.items
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.ConsultaRutas
import com.example.Consultaitems.utils.cls.Item
import com.example.Consultaitems.utils.cls.Semana

class frmConsultaRuta : Fragment(), AdaptadorConsultaRutas.OnItemClickListener {
    private lateinit var txtFechaInicial: TextView
    private lateinit var txtFechaFinal: TextView
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var adaptadorCliente: AdaptadorConsultaRutas
    private lateinit var spinnerSemana: Spinner
    private lateinit var spinnerMes: Spinner
    private lateinit var spinnerAnio: Spinner
    private val datosList = mutableListOf<ConsultaRutas>()
    lateinit var recyclerViewRV: RecyclerView
    private  var anio = listOf<Item>()
    private  var meses = listOf<Item>()
    private  var semanas = listOf<Semana>()
    private var anioAct: String = ""
    private var mesAct: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.frm_consulta_rutas, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        txtFechaInicial = view.findViewById(R.id.txtFechaInicialRV)
        txtFechaFinal = view.findViewById(R.id.txtFechaFinalRV)
        adaptadorCliente = AdaptadorConsultaRutas(datosList, this)
        recyclerViewRV = view.findViewById(R.id.recyclerViewRV)
        spinnerMes = view.findViewById(R.id.spinnerMes)
        spinnerSemana = view.findViewById(R.id.spinnerSemana)
        spinnerAnio = view.findViewById(R.id.spinnerAnio)

        fnCargarAnio()



        spinnerAnio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                 anioAct = anio[position].descripcion
                fnCargarMeses(anioAct)

            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerMes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mesAct = meses[position].codigo
                fnCargarSemanas(anioAct, mesAct)

            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        return view

    }


    private fun fnCargarAnio(){
        anio = llenarControles.fnObtenerAnios()
        val adapterAnios = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            anio.map { it.descripcion }  // mostrar solo nombre en el spinner
        )
        adapterAnios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAnio.adapter = adapterAnios
    }


    private fun fnCargarMeses(anios:String){
        meses = llenarControles.fnObtenerMeses(anios)
        val adapterMeses = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            meses.map { it.descripcion }  // mostrar solo nombre en el spinner
        )
        adapterMeses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMes.adapter = adapterMeses
    }

    private fun fnCargarSemanas(anios: String, mes: String) {
        val semanas = llenarControles.fnObtenerSemanas(mes, anios)

        // Aquí, solo mostramos los nombres de las semanas en el spinner
        val adapterSemanas = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            semanas.map { it.semanaNombre }  // mostrar solo nombre en el spinner
        )
        adapterSemanas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemana.adapter = adapterSemanas

        // Si necesitas hacer algo con las fechas, puedes acceder a ellas aquí
        spinnerSemana.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Cuando se selecciona una semana, puedes acceder a las fechas
                val semanaSeleccionada = semanas[position]
                val fechaInicio = semanaSeleccionada.fechaInicio
                val fechaFin = semanaSeleccionada.fechaFin

                txtFechaInicial.setText(fechaInicio)
                txtFechaFinal.setText(fechaFin)

                fnLlenarAdaptador(fechaInicio, fechaFin)

                // Usa estas fechas para realizar alguna acción, como filtrar otros datos
               // Log.d("Semana Seleccionada", "Fecha inicio: $fechaInicio, Fecha fin: $fechaFin")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fnLlenarAdaptador(fechaInic:String, FechaFin:String){

        datosList.clear()
        adaptadorCliente.clearItems()

        val resultados = llenarControles.fnObtenerRutasPorFechas(fechaInic, FechaFin)
        for (dato in resultados) {
           datosList.add(dato)
        }
        // Configura el RecyclerView y asigna el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRV.layoutManager = layoutManager
        recyclerViewRV.adapter = adaptadorCliente
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(item: ConsultaRutas) {

    }

    override fun observacion(codigo: String, cliente: String, observacion: String) {

    }


}