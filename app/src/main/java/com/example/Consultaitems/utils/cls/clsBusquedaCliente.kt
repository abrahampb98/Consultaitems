package com.example.Consultaitems.utils.cls

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class consultaCliente : DialogFragment(), AdaptadorClientes.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorClientes
    private lateinit var spnCriteriosC: Spinner
    private lateinit var bntAgregarC: Button
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtCriterioC: EditText
    private lateinit var criterios: List<Criterio>

    private val listReferencia = mutableListOf<AdaptadorClientes.Clientes>()
    private var listener: OnItemSelectedListener? = null

    lateinit var solicitudSoap: SolicitudSoap
    var XmlDatos: String = ""
    var criterio: Int = 0

    interface OnItemSelectedListener {
        fun onItemsSelected(clientes: AdaptadorClientes.Clientes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_consulta_clientes, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewItemsC)
        spnCriteriosC = view.findViewById(R.id.spnCriteriosC)
        bntAgregarC = view.findViewById(R.id.bntAgregarC)
        txtCriterioC = view.findViewById(R.id.txtCriterioC)

        adapter = AdaptadorClientes(
            datos = listReferencia,
            itemClickListener = this,
            doubleClickListener = { item, _ ->
                listener?.onItemsSelected(item)
                dismiss()
            }
        )

        solicitudSoap = SolicitudSoap(requireContext())
        llenarControles = ClsLLenarControles(requireContext())

        llenarSpinnerCriterios()
        spnCriteriosC.setSelection(2)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        spnCriteriosC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val codigo = criterios.getOrNull(position)?.codigo

                if (codigo == 1 || codigo == 2) {
                    txtCriterioC.inputType = InputType.TYPE_CLASS_NUMBER
                } else {
                    txtCriterioC.inputType = InputType.TYPE_CLASS_TEXT
                }

                txtCriterioC.text.clear()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bntAgregarC.setOnClickListener {
            fnConsultarItems()
        }

        txtCriterioC.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            ) {
                fnConsultarItems()
                true
            } else {
                false
            }
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
        val window: Window? = dialog?.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun llenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(2, "Código"),
            Criterio(1, "Identificación"),
            Criterio(4, "Apellido"),
            Criterio(3, "Nombre"),
            Criterio(5, "Razón Social")
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
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                "Verifique su conexión a internet",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (txtCriterioC.text.toString().isBlank()) {
            return
        }

        fnConsultaCliente()
    }

    fun fnConsultaCliente() {
        val id = getString(R.string.str_Clientes).toInt()

        criterio = criterios
            .getOrNull(spnCriteriosC.selectedItemPosition)
            ?.codigo ?: return

        val buscar = txtCriterioC.text.toString()
        val cadena = " $criterio,'$buscar',2"

        val progressDialog = showProgressDialog()

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                progressDialog.dismiss()

                val clientes = fnParseXml(xml)

                listReferencia.clear()
                listReferencia.addAll(clientes)
                adapter.notifyDataSetChanged()
            },
            onError = { ex ->
                progressDialog.dismiss()
                Log.e("consultaCliente", "Falló: ${ex.message}", ex)

                Toast.makeText(
                    requireContext(),
                    "Error consultando clientes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ).execute()
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

    fun fnParseXml(xmlString: String): List<AdaptadorClientes.Clientes> {
        val resultado = mutableListOf<AdaptadorClientes.Clientes>()

        if (xmlString.isBlank()) return resultado

        try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = false
            }

            val parser = factory.newPullParser().apply {
                setInput(StringReader(xmlString))
            }

            val recordTags = setOf("Table", "cliente")

            var eventType = parser.eventType
            var currentField = ""
            var insideRecord = false

            var codigo = ""
            var nombre = ""
            var ccNivelPrecio = ""
            var enIdentificacion = ""
            var direccion = ""
            var fono = ""
            var ciudad = ""
            var cupototal = ""
            var cupodisponible = ""
            var cupoutilizado = ""
            var clientepublico = ""
            var pagaiva = ""
            var enTipoId = ""
            var enRuc = ""
            var enGenero = ""
            var enTipoPersona = ""
            var enApellido1 = ""
            var enApellido2 = ""
            var enNombre1 = ""
            var enNombre2 = ""
            var razonComercial = ""
            var enRazonSocial = ""
            var enCorreo = ""
            var ppCodigo = ""
            var pzDescripcion = ""
            var cupoCliente = ""
            var restringido = ""
            var pzCantidadPago = ""
            var clOrden = ""
            var dqInterno = ""
            var prDescripcion = ""
            var dcPorcentaje = ""
            var faCodDocumento = ""
            var clLopdpUsuarioIng = ""

            fun resetRowVars() {
                codigo = ""
                nombre = ""
                ccNivelPrecio = ""
                enIdentificacion = ""
                direccion = ""
                fono = ""
                ciudad = ""
                cupototal = ""
                cupodisponible = ""
                cupoutilizado = ""
                clientepublico = ""
                pagaiva = ""
                enTipoId = ""
                enRuc = ""
                enGenero = ""
                enTipoPersona = ""
                enApellido1 = ""
                enApellido2 = ""
                enNombre1 = ""
                enNombre2 = ""
                razonComercial = ""
                enRazonSocial = ""
                enCorreo = ""
                ppCodigo = ""
                pzDescripcion = ""
                cupoCliente = ""
                restringido = ""
                pzCantidadPago = ""
                clOrden = ""
                dqInterno = ""
                prDescripcion = ""
                dcPorcentaje = ""
                faCodDocumento = ""
                clLopdpUsuarioIng = ""
            }

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (tag in recordTags) {
                            insideRecord = true
                            resetRowVars()
                        } else if (insideRecord) {
                            currentField = tag
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (insideRecord && currentField.isNotBlank()) {
                            val text = parser.text?.trim().orEmpty()

                            if (text.isNotBlank()) {
                                when (currentField) {
                                    "Código", "codigo", "cl_codigo" -> codigo = text
                                    "Nombre", "nombre" -> nombre = text
                                    "cc_nivelprecio" -> ccNivelPrecio = text
                                    "en_identificacion" -> enIdentificacion = text
                                    "direccion", "en_direccion" -> direccion = text
                                    "celular" -> fono = text
                                    "ciudad", "ci_descripcion" -> ciudad = text
                                    "cupototal" -> cupototal = text
                                    "cupodisponible" -> cupodisponible = text
                                    "cupoutilizado" -> cupoutilizado = text
                                    "clientepublico" -> clientepublico = text
                                    "pagaiva" -> pagaiva = text
                                    "en_tipoid" -> enTipoId = text
                                    "en_ruc" -> enRuc = text
                                    "en_genero" -> enGenero = text
                                    "en_tipopersona" -> enTipoPersona = text
                                    "en_apellido1" -> enApellido1 = text
                                    "en_apellido2" -> enApellido2 = text
                                    "en_nombre1" -> enNombre1 = text
                                    "en_nombre2" -> enNombre2 = text
                                    "RazonComercial", "razonComercial" -> razonComercial = text
                                    "en_razonsocial" -> enRazonSocial = text
                                    "en_correo" -> enCorreo = text
                                    "pp_codigo" -> ppCodigo = text
                                    "pz_descripcion" -> pzDescripcion = text
                                    "cupoCliente" -> cupoCliente = text
                                    "restringido" -> restringido = text
                                    "pz_cantidadpago" -> pzCantidadPago = text
                                    "cl_orden" -> clOrden = text
                                    "dq_interno", "sin_nombre_columna" -> dqInterno = text
                                    "pr_descripcion" -> prDescripcion = text
                                    "dc_porcentaje" -> dcPorcentaje = text
                                    "fa_coddocumento" -> faCodDocumento = text
                                    "cl_lopdpusuarioing" -> clLopdpUsuarioIng = text
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (tag in recordTags && insideRecord) {
                            resultado.add(
                                AdaptadorClientes.Clientes(
                                    codigo = codigo,
                                    nombre = nombre,
                                    cc_nivelprecio = ccNivelPrecio,
                                    en_identificacion = enIdentificacion,
                                    direccion = direccion,
                                    fono = fono,
                                    ciudad = ciudad,
                                    cupototal = cupototal,
                                    cupodisponible = cupodisponible,
                                    cupoutilizado = cupoutilizado,
                                    clientepublico = clientepublico,
                                    pagaiva = pagaiva,
                                    en_tipoid = enTipoId,
                                    en_ruc = enRuc,
                                    en_genero = enGenero,
                                    en_tipopersona = enTipoPersona,
                                    en_apellido1 = enApellido1,
                                    en_apellido2 = enApellido2,
                                    en_nombre1 = enNombre1,
                                    en_nombre2 = enNombre2,
                                    razonComercial = razonComercial,
                                    en_razonsocial = enRazonSocial,
                                    en_correo = enCorreo,
                                    pp_codigo = ppCodigo,
                                    pz_descripcion = pzDescripcion,
                                    cupoCliente = cupoCliente,
                                    restringido = restringido,
                                    pz_cantidadpago = pzCantidadPago,
                                    cl_orden = clOrden,
                                    dq_interno = dqInterno,
                                    pr_descripcion = prDescripcion,
                                    dc_porcentaje = dcPorcentaje,
                                    fa_coddocumento = faCodDocumento,
                                    cl_lopdpusuarioing = clLopdpUsuarioIng
                                )
                            )

                            insideRecord = false
                            currentField = ""
                        } else if (insideRecord) {
                            currentField = ""
                        }
                    }
                }

                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("consultaCliente", "Error parseando XML: ${e.message}", e)
        }

        return resultado
    }

    override fun onItemClick(item: AdaptadorClientes.Clientes, position: Int) {
        // Selección simple: no cierra el diálogo.
        // Doble click: envía el cliente y cierra.
    }
}

data class Criterio(
    val codigo: Int,
    val descripcion: String
)