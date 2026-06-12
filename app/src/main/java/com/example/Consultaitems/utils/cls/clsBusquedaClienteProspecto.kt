package com.example.Consultaitems.utils.cls

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorProspectos
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader


class consultaClienteProspecto : DialogFragment(), AdaptadorProspectos.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorProspectos
    private lateinit var spnCriteriosC: Spinner
    private lateinit var bntAgregarC: Button
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtCriterioC: EditText

    private val listReferencia = mutableListOf<AdaptadorProspectos.Prospectos>()
    private var listener: OnItemSelectedListener? = null
    lateinit var solicitudSoap: SolicitudSoap

    var XmlDatos: String = ""
    var criterio: Int = 0
    private lateinit var criterios: List<Criterio>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_consulta_prospectos, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewItemsC)
        spnCriteriosC = view.findViewById(R.id.spnCriteriosC)
        bntAgregarC = view.findViewById(R.id.bntAgregarC)
        txtCriterioC = view.findViewById(R.id.txtCriterioC)

        adapter = AdaptadorProspectos(
            datos = listReferencia,
            itemClickListener = this,
            doubleClickListener = { item, _ ->
                listener?.onItemsSelected(item)
                dismiss()
            }
        )

        llenarControles = ClsLLenarControles(requireContext())
        llenarSpinnerCriterios()

        if (criterios.size > 2) spnCriteriosC.setSelection(2)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        spnCriteriosC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bntAgregarC.setOnClickListener {
            fnConsultarItems()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? OnItemSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    interface OnItemSelectedListener {
        fun onItemsSelected(clientes: AdaptadorProspectos.Prospectos)
    }

    private fun llenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(1, "Codigo"),
            Criterio(2, "Nombre"),
            Criterio(3, "Apellido"),
            Criterio(4, "Razón Comercial"),
            Criterio(5, "Razón Social"),
            Criterio(6, "Telefono")
        )

        val adapterCriterios = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            criterios.map { it.descripcion }
        )
        adapterCriterios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCriteriosC.adapter = adapterCriterios
    }

    fun fnConsultarItems() {
        if (isNetworkAvailable(requireContext())) {
            if (txtCriterioC.text.toString().isNotEmpty()) {
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = showProgressDialog()
                MiAsyncTask(progressDialog).execute()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Verifique su conexión a internet",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            try {
                criterio = criterios.getOrNull(spnCriteriosC.selectedItemPosition)?.codigo!!
                val buscar = txtCriterioC.text.toString().trim()
                val cadena = " ${criterio},'${buscar}'"

                solicitudSoap.initializeVariables(getString(R.string.str_Prospectos).toInt(), cadena)

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlDatos = result
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            val clientes = try {
                fnParseXml(XmlDatos)
            } catch (e: Exception) {
                Log.e("Error", "Error parseando prospectos: ${e.message}", e)
                emptyList()
            }

            listReferencia.clear()
            listReferencia.addAll(clientes)
            adapter.notifyDataSetChanged()
        }
    }

    fun fnParseXml(xmlString: String): List<AdaptadorProspectos.Prospectos> {
        val resultado = mutableListOf<AdaptadorProspectos.Prospectos>()
        if (xmlString.isBlank()) return resultado

        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
        val parser = factory.newPullParser().apply { setInput(StringReader(xmlString)) }

        val recordTags = setOf("Table", "Table1", "cliente", "Cliente", "prospecto", "Prospecto")

        var eventType = parser.eventType
        var currentField = ""
        var insideRecord = false

        var tipo = ""
        var codigo = ""
        var nombre1 = ""
        var nombre2 = ""
        var apellido1 = ""
        var apellido2 = ""
        var nombreComercial = ""
        var razonSocial = ""
        var direccion = ""
        var fono = ""
        var codCiudad = ""
        var ciudad = ""
        var provincia = ""
        var correo = ""
        var ocCodigo = ""
        var ocDescripcion = ""
        var gtCodigo = ""
        var gcCodigoPostal = ""
        var gcSector = ""
        var gcPuntoReferencia = ""
        var gcGoogleMap = ""

        fun resetRowVars() {
            tipo = ""
            codigo = ""
            nombre1 = ""
            nombre2 = ""
            apellido1 = ""
            apellido2 = ""
            nombreComercial = ""
            razonSocial = ""
            direccion = ""
            fono = ""
            codCiudad = ""
            ciudad = ""
            provincia = ""
            correo = ""
            ocCodigo = ""
            ocDescripcion = ""
            gtCodigo = ""
            gcCodigoPostal = ""
            gcSector = ""
            gcPuntoReferencia = ""
            gcGoogleMap = ""
        }

        fun assignField(rawField: String, rawValue: String) {
            val value = rawValue.trim()
            if (value.isEmpty()) return

            when (rawField.trim()) {
                "Tipo", "tipo", "_tipo", "TipoF", "ttTipo" -> tipo = value
                "Codigo", "Código", "codigo", "cl_codigo", "gc_codigo" -> codigo = value
                "Nombre1", "nombre1", "en_nombre1" -> nombre1 = value
                "Nombre2", "nombre2", "en_nombre2" -> nombre2 = value
                "Apellido1", "apellido1", "en_apellido1" -> apellido1 = value
                "Apellido2", "apellido2", "en_apellido2" -> apellido2 = value
                "NombreComercial", "nombreComercial", "RazonComercial", "razonComercial", "cl_nombre" -> nombreComercial = value
                "RazonSocial", "razonSocial", "en_razonsocial" -> razonSocial = value
                "Direccion", "direccion", "gc_direccion" -> direccion = value
                "Fono", "fono", "gc_fono", "telefono", "Telefono" -> fono = value
                "CodCiudad", "codCiudad", "ci_codigo" -> codCiudad = value
                "Ciudad", "ciudad", "ci_descripcion" -> ciudad = value
                "Provincia", "provincia", "dp_descripcion" -> provincia = value
                "Correo", "correo", "en_correo", "gc_email" -> correo = value
                "oc_codigo" -> ocCodigo = value
                "oc_descripcion" -> ocDescripcion = value
                "gt_codigo" -> gtCodigo = value
                "gc_codigopostal" -> gcCodigoPostal = value
                "gc_sector" -> gcSector = value
                "gc_puntoreferencia" -> gcPuntoReferencia = value
                "gc_googlemap" -> gcGoogleMap = value
            }
        }

        fun readAttributes() {
            for (i in 0 until parser.attributeCount) {
                val name = parser.getAttributeName(i) ?: continue
                val value = parser.getAttributeValue(i) ?: continue
                assignField(name, value)
            }
        }

        fun addCurrentRecord() {
            if (
                tipo.isBlank() && codigo.isBlank() && nombre1.isBlank() && nombre2.isBlank() &&
                apellido1.isBlank() && apellido2.isBlank() && nombreComercial.isBlank() &&
                razonSocial.isBlank()
            ) {
                return
            }

            resultado.add(
                AdaptadorProspectos.Prospectos(
                    Tipo = tipo,
                    Codigo = codigo,
                    Nombre1 = nombre1,
                    Nombre2 = nombre2,
                    Apellido1 = apellido1,
                    Apellido2 = apellido2,
                    NombreComercial = nombreComercial,
                    RazonSocial = razonSocial,
                    Direccion = direccion,
                    Fono = fono,
                    CodCiudad = codCiudad,
                    Ciudad = ciudad,
                    Provincia = provincia,
                    Correo = correo,
                    oc_codigo = ocCodigo,
                    oc_descripcion = ocDescripcion,
                    gt_codigo = gtCodigo,
                    gc_codigopostal = gcCodigoPostal,
                    gc_sector = gcSector,
                    gc_puntoreferencia = gcPuntoReferencia,
                    gc_googlemap = gcGoogleMap
                )
            )
        }

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name ?: ""
                    if (tag in recordTags) {
                        insideRecord = true
                        resetRowVars()
                        readAttributes()
                    } else if (insideRecord) {
                        currentField = tag
                        readAttributes()
                    }
                }

                XmlPullParser.TEXT -> {
                    if (insideRecord && currentField.isNotEmpty()) {
                        assignField(currentField, parser.text.orEmpty())
                    }
                }

                XmlPullParser.END_TAG -> {
                    val tag = parser.name ?: ""
                    if (tag in recordTags && insideRecord) {
                        addCurrentRecord()
                        insideRecord = false
                        currentField = ""
                    } else if (insideRecord) {
                        currentField = ""
                    }
                }
            }
            eventType = parser.next()
        }

        return resultado
    }

    override fun onItemClick(item: AdaptadorProspectos.Prospectos, position: Int) {
        // Selección simple: el adapter ya marca la fila. Doble click devuelve el registro.
    }
}

