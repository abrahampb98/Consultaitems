package com.example.Consultaitems.utils.cls

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorProforma
import com.example.Consultaitems.ui.adapters.Proformas
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class consultaProforma(
    private val bodega: String
) : DialogFragment(), AdaptadorProforma.OnItemClickListener {

    interface OnItemSelectedListener {
        fun onItemsSelected(codigo: Proformas)
    }

    lateinit var solicitudSoap: SolicitudSoap
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorProforma
    private lateinit var etCodigoP: EditText
    private lateinit var bntBuscarP: ImageView
    private lateinit var txtFechaIncP: TextView
    private lateinit var txtFechaFinP: TextView

    private val listReferencia: MutableList<Proformas> = mutableListOf()
    private var listener: OnItemSelectedListener? = null
    var XmlDatos: String = ""
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo
    private var isDatePickerShown: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_criterios_proforma, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewProformas)
        etCodigoP = view.findViewById(R.id.etCodigoP)
        bntBuscarP = view.findViewById(R.id.ivBuscarP)
        txtFechaIncP = view.findViewById(R.id.txtFechaIncP)
        txtFechaFinP = view.findViewById(R.id.txtFechaFinP)

        adapter = AdaptadorProforma(
            listReferencia,
            this
        ) { item: Proformas, _: Int ->
            listener?.onItemsSelected(item)
            dismiss()
        }

        llenarControles = ClsLLenarControles(requireContext())

        txtFechaIncP.text = fnFecha()
        txtFechaFinP.text = fnFecha()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        bntBuscarP.setOnClickListener { fnConsultarItems() }
        txtFechaIncP.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaIncP)
        }
        txtFechaFinP.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaFinP)
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

    override fun onItemClick(item: Proformas, position: Int) {
        // La selección real se maneja en el callback del adaptador.
    }

    private fun fnFecha(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, day: Int ->
                targetView.text = formatDate(year, month, day)
                isDatePickerShown = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.setOnDismissListener { isDatePickerShown = false }
        datePicker.setOnCancelListener { isDatePickerShown = false }
        datePicker.show()
        isDatePickerShown = true
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    fun fnConsultarItems() {
        val context = requireContext()

        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
            return
        }

        solicitudSoap = SolicitudSoap(context)

        val id = getString(R.string.str_FaProformadinamico).toInt()
        val codigo = etCodigoP.text.toString().trim()
        val cadena = "'$codigo','','','${txtFechaIncP.text}','${txtFechaFinP.text}','2','$bodega'"

        val progressDialog = showProgressDialog()

        clsObtenerDatos(
            context = context,
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                progressDialog.dismiss()
                if (!isAdded) return@clsObtenerDatos
                XmlDatos = xml
                val lista = fnParseXml(XmlDatos)
                adapter.setItems(lista)

            },
            onError = { ex ->
                progressDialog.dismiss()
                if (!isAdded) return@clsObtenerDatos
                XmlDatos = ""
                adapter.clearItems()
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
        return ProgressDialog(requireContext()).apply {
            setMessage("Cargando Datos...")
            setCancelable(false)
            show()
        }
    }


    fun fnParseXml(xmlString: String): List<Proformas> {
        val result = mutableListOf<Proformas>()
        if (xmlString.isBlank()) return result

        try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = true
            }

            val parser = factory.newPullParser().apply {
                setInput(StringReader(xmlString))
            }

            var event = parser.eventType
            var insideTable = false

            var numero = ""
            var vendedor = ""
            var clCodigo = ""
            var cliente = ""
            var fecha = ""
            var total = ""

            fun resetRowVars() {
                numero = ""
                vendedor = ""
                clCodigo = ""
                cliente = ""
                fecha = ""
                total = ""
            }

            fun addRowIfValid() {
                Log.d(
                    "consultaProforma",
                    "Fila XML -> numero=$numero vendedor=$vendedor clCodigo=$clCodigo cliente=$cliente fecha=$fecha total=$total"
                )

                val numeroInt = parseNumero(numero)

                if (numeroInt == null) {
                    Log.d("consultaProforma", "Fila ignorada, número inválido: $numero")
                    return
                }

                result.add(
                    Proformas(
                        numero = numeroInt,
                        vendedor = vendedor.trim(),
                        cliente = cliente.ifBlank { clCodigo }.trim(),
                        fecha = formatFecha(fecha.trim()),
                        total = normalizeDecimal(total.trim())
                    )
                )
            }

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val tagOriginal = parser.name.orEmpty()
                        val tag = normalizeTagName(tagOriginal)

                        if (tag == "table") {
                            insideTable = true
                            resetRowVars()
                        } else if (insideTable) {
                            when (tag) {
                                "pr_codigo",
                                "codigo",
                                "numero",
                                "proforma" -> {
                                    numero = parser.nextText().orEmpty().trim()
                                }

                                "vendedor",
                                "vn_nombre",
                                "ep_nombre",
                                "usuario",
                                "pr_usuarioing" -> {
                                    vendedor = parser.nextText().orEmpty().trim()
                                }

                                "cl_codigo" -> {
                                    clCodigo = parser.nextText().orEmpty().trim()
                                }

                                "cliente",
                                "cl_nombre",
                                "pr_nombre",
                                "nombre" -> {
                                    cliente = parser.nextText().orEmpty().trim()
                                }

                                "fecha",
                                "pr_fechaing",
                                "pr_fechatrn",
                                "pr_fecha",
                                "fechatrn" -> {
                                    fecha = parser.nextText().orEmpty().trim()
                                }

                                "total",
                                "pr_valortotal",
                                "valortotal",
                                "valor_total" -> {
                                    total = parser.nextText().orEmpty().trim()
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tag = normalizeTagName(parser.name.orEmpty())

                        if (tag == "table") {
                            addRowIfValid()
                            insideTable = false
                        }
                    }
                }

                event = parser.next()
            }
        } catch (e: Exception) {
            Log.e("consultaProforma", "Error parseando XML: ${e.message}", e)
        }

        return result
    }

    private fun normalizeTagName(value: String): String {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)
    }

    private fun parseNumero(value: String): Int? {
        val clean = value
            .trim()
            .replace(",", ".")
            .replace(" ", "")

        return clean.toIntOrNull()
            ?: clean.toDoubleOrNull()?.toInt()
    }

    private fun normalizeDecimal(value: String): String {
        if (value.isBlank()) return "0.00"

        val clean = value
            .trim()
            .replace(",", ".")

        val number = clean.toDoubleOrNull() ?: return clean
        return String.format(Locale.US, "%.2f", number)
    }

    private fun formatFecha(raw: String): String {
        if (raw.isBlank()) return ""

        val outFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val inFormats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy"
        )

        for (format in inFormats) {
            val parsed = runCatching {
                SimpleDateFormat(format, Locale.getDefault()).parse(raw)
            }.getOrNull()

            if (parsed != null) {
                return outFormat.format(parsed)
            }
        }

        return raw
    }
}
