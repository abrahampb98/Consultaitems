package com.example.Consultaitems.utils.cls

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.Consultaitems.ui.adapters.AdaptadorConsultaFactura
import com.example.Consultaitems.ui.adapters.ConsultaFacturas
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

class consultaFactura(
    private val bodega: String
) : DialogFragment(), AdaptadorConsultaFactura.OnItemClickListener {

    interface OnItemSelectedListener {
        fun onItemsSelected(facturas: ConsultaFacturas)
    }

    var XmlDatos: String = ""
    lateinit var solicitudSoap: SolicitudSoap
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo

    private lateinit var recyclerView: RecyclerView
    private lateinit var etCodigoP: EditText
    private lateinit var bntBuscarP: ImageView
    private lateinit var txtFechaIncP: TextView
    private lateinit var txtFechaFinP: TextView
    private lateinit var adapter: AdaptadorConsultaFactura
    private var isDatePickerShown = false
    private val listReferencia = mutableListOf<ConsultaFacturas>()
    private var listener: OnItemSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_criterios_factura, container, false)
        recyclerView = view.findViewById(R.id.recyclerviewProformas)
        etCodigoP = view.findViewById(R.id.etCodigoP)
        bntBuscarP = view.findViewById(R.id.ivBuscarP)
        txtFechaIncP = view.findViewById(R.id.txtFechaIncP)
        txtFechaFinP = view.findViewById(R.id.txtFechaFinP)

        solicitudSoap = SolicitudSoap(requireContext())
        adapter = AdaptadorConsultaFactura(listReferencia, this) { facturas: ConsultaFacturas, _: Int ->
            listener?.onItemsSelected(facturas)
            dismiss()
        }

        txtFechaIncP.text = fnFecha()
        txtFechaFinP.text = fnFecha()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        bntBuscarP.setOnClickListener { fnConsultarItems() }
        txtFechaIncP.setOnClickListener { if (!isDatePickerShown) showDatePickerDialog(txtFechaIncP) }
        txtFechaFinP.setOnClickListener { if (!isDatePickerShown) showDatePickerDialog(txtFechaFinP) }

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

    private fun fnFecha(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                targetView.text = formatDate(selectedYear, selectedMonth, selectedDay)
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
        if (isNetworkAvailable(requireContext())) {
            fnFacturas()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION")
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    fun fnFacturas() {
        val codigo = etCodigoP.text.toString()
        val id = getString(R.string.str_FaFacturaDinamico).toInt()
        val cadena = "'$codigo','','','${txtFechaIncP.text}','${txtFechaFinP.text}','2','$bodega',''"
        val progressDialog = showProgressDialog()

        clsObtenerDatos(
            requireContext(),
            solicitudSoap,
            id,
            cadena,
            onSuccess = { xml ->
                val datos = fnParseXml(xml)
                listReferencia.clear()
                listReferencia.addAll(datos)
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
            },
            onError = { ex ->
                Log.e("consultaFactura", "Falló: ${ex.message}", ex)
                progressDialog.dismiss()
            }
        ).execute()
    }

    fun fnParseXml(xmlString: String): List<ConsultaFacturas> {
        if (xmlString.isBlank()) return emptyList()
        return runCatching {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(xmlString)))
            doc.documentElement.normalize()

            val rows = collectRows(
                doc.documentElement,
                setOf("Table", "Table1", "Factura", "Facturas", "Row", "ConsultaFacturas")
            )

            rows.mapNotNull { row ->
                val factura = value(row, "fa_coddocumento", "fa_documento", "factura", "Factura", "documento").toIntOrNull()
                    ?: return@mapNotNull null
                ConsultaFacturas(
                    factura = factura,
                    sri = value(row, "sri", "SRI", "fa_sri", "fa_numero", "fa_numeroSRI", "numeroSri"),
                    fecha = formatFecha(value(row, "fecha", "Fecha", "fa_fecha", "fa_fechafactura", "fa_fechaFactura")),
                    cliente = value(row, "cliente", "Cliente", "cl_nombre", "nombreCliente"),
                    total = value(row, "total", "Total", "fa_total", "fa_valorTotal", "fa_valortotfact"),
                    vendedor = value(row, "vendedor", "Vendedor", "vn_nombre", "nombreVendedor"),
                    ep_codigo = value(row, "ep_codigo", "vn_codigo", "vendedorCodigo").toIntOrNull() ?: 0
                )
            }
        }.getOrElse { ex ->
            Log.e("consultaFactura", "Error parseando XML: ${ex.message}", ex)
            emptyList()
        }
    }

    private fun collectRows(root: Element, names: Set<String>): List<Element> {
        val result = mutableListOf<Element>()
        val all = root.getElementsByTagName("*")
        for (i in 0 until all.length) {
            val e = all.item(i) as? Element ?: continue
            if (e.tagName in names && hasElementChildren(e)) result.add(e)
        }
        return if (result.isNotEmpty()) result else listOf(root)
    }

    private fun hasElementChildren(element: Element): Boolean {
        val children = element.childNodes
        for (i in 0 until children.length) if (children.item(i) is Element) return true
        return false
    }

    private fun value(element: Element, vararg names: String): String {
        val wanted = names.map { it.lowercase(Locale.getDefault()) }.toSet()
        val children = element.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i) as? Element ?: continue
            if (child.tagName.lowercase(Locale.getDefault()) in wanted) {
                return child.textContent?.trim().orEmpty()
            }
        }
        for (name in names) {
            val nodes = element.getElementsByTagName(name)
            if (nodes.length > 0) return nodes.item(0)?.textContent?.trim().orEmpty()
        }
        return ""
    }

    private fun formatFecha(raw: String): String {
        if (raw.isBlank()) return ""
        val out = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy"
        )
        for (f in formats) {
            val parsed = runCatching { SimpleDateFormat(f, Locale.getDefault()).parse(raw) }.getOrNull()
            if (parsed != null) return out.format(parsed)
        }
        return raw
    }

    override fun onItemClick(item: ConsultaFacturas, position: Int) {
        // Selección simple manejada por el adapter. Doble click retorna el item al fragment padre.
    }
}
