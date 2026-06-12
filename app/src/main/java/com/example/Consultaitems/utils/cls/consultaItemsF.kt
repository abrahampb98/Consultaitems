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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorItemsFacturas
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

class consultaItemsF(
    vlpReferencia: String,
    vlpBodega: String
) : DialogFragment(), AdaptadorItemsFacturas.OnItemClickListener {

    interface OnItemSelectedListener {
        fun onItemsSelected(items: AdaptadorItemsFacturas.itemsFactura)
    }

    var XmlDatos: String = ""
    lateinit var solicitudSoap: SolicitudSoap
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo

    private lateinit var adapter: AdaptadorItemsFacturas
    private lateinit var bntBuscarF: Button
    private lateinit var etDescripcion: EditText
    private lateinit var etMarca: EditText
    private lateinit var etCodigo: EditText
    private lateinit var etReferencia: EditText
    private lateinit var recyclerView: RecyclerView
    private var listener: OnItemSelectedListener? = null

    private val listaItmes = mutableListOf<AdaptadorItemsFacturas.itemsFactura>()
    private var vlsReferencia: String = vlpReferencia
    private var bodega: String = vlpBodega

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_items_factura, container, false)
        recyclerView = view.findViewById(R.id.recyclerviewItemsA)
        bntBuscarF = view.findViewById(R.id.bntBuscarGV)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        etMarca = view.findViewById(R.id.etMarca)
        etCodigo = view.findViewById(R.id.etCodigo)
        etReferencia = view.findViewById(R.id.etReferencia)

        adapter = AdaptadorItemsFacturas(listaItmes, this) { item: AdaptadorItemsFacturas.itemsFactura, _: Int ->
            listener?.onItemsSelected(item)
            dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        etReferencia.setText(vlsReferencia)
        bntBuscarF.setOnClickListener { fnConsultarItems() }

        fnConsultarItems()
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

    override fun onItemClick(item: AdaptadorItemsFacturas.itemsFactura, position: Int) {
        // Selección simple manejada por el adapter. Doble click retorna el item al fragment padre.
    }

    fun fnConsultarItems() {
        if (isNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
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

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = listOf(
                    sq(etDescripcion.text.toString()),
                    sq(etMarca.text.toString()),
                    sq(etCodigo.text.toString()),
                    sq(etReferencia.text.toString()),
                    "'2'",
                    "'$bodega'"
                ).joinToString(",")

                solicitudSoap.initializeVariables(getString(R.string.str_ConsultaMultiple).toInt(), cadena)
                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.use { stream ->
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                }
                if (!result.isNullOrBlank()) XmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("consultaItemsF", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            val datos = fnParseXml(XmlDatos)
            listaItmes.clear()
            listaItmes.addAll(datos)
            adapter.notifyDataSetChanged()
        }

        private fun sq(s: String): String {
            return "'${s.trim().replace("'", "''")}'"
        }
    }

    fun fnParseXml(xmlString: String): List<AdaptadorItemsFacturas.itemsFactura> {
        if (xmlString.isBlank()) return emptyList()
        return runCatching {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(xmlString)))
            doc.documentElement.normalize()

            val rows = collectRows(
                doc.documentElement,
                setOf("Table", "Table1", "Item", "Items", "Row", "ConsultaMultiple")
            )

            rows.mapNotNull { row ->
                val codigo = value(row, "it_codigo", "codigo", "Codigo")
                val referencia = value(row, "it_referencia", "referencia", "Referencia")
                if (codigo.isBlank() && referencia.isBlank()) return@mapNotNull null

                AdaptadorItemsFacturas.itemsFactura(
                    codigo = codigo,
                    referencia = referencia,
                    descripcion = value(row, "it_descripcion", "descripcion", "Descripcion"),
                    titulo = value(row, "it_titulo", "titulo", "Titulo"),
                    stock = value(row, "stock", "Stock", "total", "Total", "existencia").toDoubleSafe(),
                    marca = value(row, "it_marca", "marca", "Marca"),
                    codigoB = value(row, "bo_codigo", "codigoB", "CodigoB", "bodega"),
                    peso = value(row, "peso", "Peso", "um_pesoCE", "um_pesoce").toDoubleSafe(),
                    costoProm = value(row, "costoProm", "costoprom", "it_costoprom", "it_costopromedio").toDoubleSafe()
                )
            }
        }.getOrElse { ex ->
            Log.e("consultaItemsF", "Error parseando XML: ${ex.message}", ex)
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

    private fun String.toDoubleSafe(): Double {
        return replace(",", ".").toDoubleOrNull() ?: 0.0
    }
}
