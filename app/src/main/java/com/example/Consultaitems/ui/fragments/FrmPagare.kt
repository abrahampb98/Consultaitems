package com.example.Consultaitems.ui.fragments

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.utils.pdf.frmPdfPagare
import com.example.Consultaitems.utils.pdf.frmPdfPagare1
import com.example.Consultaitems.utils.pdf.frmPdfPagare2
import com.example.Consultaitems.ui.adapters.DataModel
import com.example.Consultaitems.ui.adapters.FacturaListAdapter
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class frmPagare : Fragment() {
    lateinit var spinnerNombreCliente: Spinner
    lateinit var txtPolitica: TextView
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var clientesList: ArrayList<String>
    lateinit var Reciclerview: RecyclerView
    var posicion: Int = 0
    lateinit var txtTotal: TextView
    lateinit var txtCliente: String
    private var datePickerDialog: DatePickerDialog? = null
    var rucCliente: String=""
    var Pagare: Int = 1
    lateinit var spinnerPagare: Spinner
    private lateinit var editText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.frmpagare1, container, false)

        // Inicializar variables
        spinnerNombreCliente = view.findViewById(R.id.spinnerNombreCliente)
        txtPolitica = view.findViewById(R.id.txtPolitica)
        dbHelper = SqLiteOpenHelper(requireContext())
        clientesList = ArrayList()
        Reciclerview = view.findViewById(R.id.recyclerView)
        val botonBusqueda = view.findViewById<Button>(R.id.botonBusqueda)
        txtTotal = view.findViewById(R.id.txtTotal)
        llenarSpinnerPagare(view)
        val radioButton1 = view.findViewById<RadioButton>(R.id.radioButton1)
        val radioButton2 = view.findViewById<RadioButton>(R.id.radioButton2)
        val txtDias = view.findViewById<TextView>(R.id.txtDias)
        spinnerPagare = view.findViewById<Spinner>(R.id.spinnerPagare)
        val txtMaximo = view.findViewById<TextView>(R.id.txtMaximo)
        val txtAbono = view.findViewById<TextView>(R.id.txtAbono)
        val generatePdfButton: Button = view.findViewById(R.id.botonImprimir)
        val textViewFechaInicial: TextView = view.findViewById(R.id.textViewFechaInicial)
        val textViewFechaFinal: TextView = view.findViewById(R.id.textViewFechaFinal)
        val txtBusqueda: TextView = view.findViewById(R.id.txtBusqueda)
        val txtPorcentaje: TextView = view.findViewById(R.id.txtPorcentaje)
        val txtPorcentaje2: TextView = view.findViewById(R.id.txtPorcentaje2)

        //habilita el radiobutton 1 por defecto
        radioButton1.isChecked = true
        txtMaximo.isEnabled = false
        txtAbono.isEnabled = false
        txtPorcentaje2.isEnabled = false

        // Obtener los nombres de los clientes de la tabla ve_ws_clienteAsignadoVendedor
        obtenerNombresClientes()

        // Crear un adaptador para el Spinner con la lista de nombres de los clientes
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, clientesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNombreCliente.adapter = adapter

        // Mapa para almacenar la posición de los nombres de los clientes originales
        val mapaPosicionesClientes = mutableMapOf<String, Int>()

        // Inicializa el mapa
        for ((index, cliente) in clientesList.withIndex()) {
            mapaPosicionesClientes[cliente] = index
        }

        // Agregar un TextWatcher al EditText para filtrar los nombres del cliente mientras escribes
        txtBusqueda.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Filtra la lista de clientes basándote en el texto ingresado
                val textoBusqueda = s.toString().toLowerCase(Locale.getDefault())
                val listaFiltrada = clientesList.filter {
                    it.toLowerCase(Locale.getDefault()).contains(textoBusqueda)
                }

                // Actualiza el adaptador del Spinner con la lista filtrada
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listaFiltrada
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNombreCliente.adapter = adapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Agregar un listener al Spinner para detectar la selección del cliente
        spinnerNombreCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Recupera el nombre del cliente seleccionado
                val nombreClienteSeleccionado = parent.getItemAtPosition(position).toString()

                // Recupera la posición original del cliente seleccionado
                val posicionOriginal = mapaPosicionesClientes[nombreClienteSeleccionado] ?: -1

                // Aquí puedes utilizar la posición original como lo hacías antes
                txtCliente = nombreClienteSeleccionado
                // Obtener la descripción del cliente seleccionado
                val descripcionCliente = obtenerDescripcionCliente(posicionOriginal)

                rucCliente = obtenerRucCliente(posicionOriginal)
                // Mostrar la descripción del cliente en el TextView
                txtPolitica.text = descripcionCliente
                textViewFechaInicial.text = ""
                textViewFechaFinal.text = ""
                textViewFechaInicial.hint = "Fecha inicial"
                textViewFechaFinal.hint = "Fecha final"
                posicion = posicionOriginal
                Reciclerview.adapter = null
                txtTotal.text = "$0.00"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No es necesario implementar este método si no se va a realizar ninguna acción
            }
        }

        botonBusqueda.setOnClickListener {

            // Obtener las fechas ingresadas
            val fechaInicial = textViewFechaInicial.text.toString()
            val fechaFinal = textViewFechaFinal.text.toString()

            // Verificar si se ingresaron las fechas
            if (fechaInicial.isNotEmpty() && fechaFinal.isNotEmpty()) {
                // Obtener el código del cliente seleccionado
                val codigoCliente = obtenerCodigoCliente(posicion)
                // Llamar a la función para mostrar las facturas del cliente seleccionado
                mostrarFacturasDelClienteSeleccionado(codigoCliente, fechaInicial, fechaFinal)
            } else {
                // Mostrar mensajes Toast indicando que se deben ingresar las fechas
                Toast.makeText(
                    requireContext(),
                    "Debe ingresar la fecha inicial y la fecha final",
                    Toast.LENGTH_SHORT
                ).show()
            }
            view.findViewById<EditText>(R.id.txtDias)?.text?.clear()
            view.findViewById<EditText>(R.id.txtMaximo)?.text?.clear()
            view.findViewById<EditText>(R.id.txtAbono)?.text?.clear()
            view.findViewById<Spinner>(R.id.spinnerPagare).setSelection(0)
            view.findViewById<EditText>(R.id.txtPorcentaje)?.text?.clear()
            view.findViewById<EditText>(R.id.txtPorcentaje2)?.text?.clear()
            txtTotal.text = "$0.00"
        }

        //generar pdf al imrpimir
        generatePdfButton.setOnClickListener {
            if (txtTotal.text.toString() != "$0.00") {

                //Genera el pagare 1 manualmente
                if (Pagare == 1) {
                    if (txtDias.text.toString() != "") {
                        val vn_codigo = obtenerCodigoVendedor()
                        val total = try {
                            NumberFormat.getCurrencyInstance(Locale.US)
                                .parse(txtTotal.text.toString())?.toDouble() ?: 0.0
                        } catch (e: ParseException) {
                            0.0
                        }
                        val indiceSeleccionado = spinnerPagare.selectedItemPosition
                        val itemSeleccionado =
                            spinnerPagare.getItemAtPosition(indiceSeleccionado).toString()
                        frmPdfPagare.pagareManual(txtDias.text.toString(), itemSeleccionado)
                        val file = frmPdfPagare.generatePdf(
                            requireContext(),
                            txtCliente,
                            total,
                            txtPolitica.text.toString(),
                            rucCliente,
                            vn_codigo
                        )
                        fnOpenPdf(file)
                    } else {
                        //Genera el pagare por defecto
                        val total = try {
                            NumberFormat.getCurrencyInstance(Locale.US)
                                .parse(txtTotal.text.toString())?.toDouble() ?: 0.0
                        } catch (e: ParseException) {
                            0.0
                        }
                        val vn_codigo = obtenerCodigoVendedor()
                        val file = frmPdfPagare1.generatePdf(
                            requireContext(),
                            txtCliente,
                            total,
                            txtPolitica.text.toString(),
                            rucCliente,
                            vn_codigo
                        )
                        fnOpenPdf(file)
                    }


                    //Genera el pagare 2
                } else if (Pagare == 2) {
                    if (txtMaximo.text.toString() != "" && txtAbono.text.toString() != "") {
                        val total = try {
                            NumberFormat.getCurrencyInstance(Locale.US)
                                .parse(txtTotal.text.toString())?.toDouble() ?: 0.0
                        } catch (e: ParseException) {
                            0.0
                        }
                        val vn_codigo = obtenerCodigoVendedor()
                        val file = frmPdfPagare2.generatePdf(
                            requireContext(),
                            txtCliente,
                            total,
                            rucCliente,
                            vn_codigo,
                            txtMaximo.text.toString(),
                            txtAbono.text.toString()
                        )
                        fnOpenPdf(file)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Debe completar los campos de Dias y Abono",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    view.findViewById<EditText>(R.id.txtDias)?.text?.clear()
                    view.findViewById<Spinner>(R.id.spinnerPagare).setSelection(0)
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Debe seleccionar alguna factura",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Agregar clics a los TextView para mostrar el DatePicker
        textViewFechaInicial.setOnClickListener {
            showDatePickerDialog(textViewFechaInicial)
        }

        textViewFechaFinal.setOnClickListener {
            showDatePickerDialog(textViewFechaFinal)
        }

        // Agrega un listener al radioButton1
        radioButton1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

                // Desmarca el radioButton2
                radioButton2.isChecked = false
                txtMaximo.isEnabled = false
                txtAbono.isEnabled = false
                txtPorcentaje2.isEnabled = false
                //Habilita los elementos
                txtDias.isEnabled = true
                spinnerPagare.isEnabled = true
                txtPorcentaje.isEnabled = true
                view.findViewById<EditText>(R.id.txtMaximo)?.text?.clear()
                view.findViewById<EditText>(R.id.txtAbono)?.text?.clear()
                view.findViewById<EditText>(R.id.txtPorcentaje2)?.text?.clear()
                Pagare = 1
            }
        }

        // Agrega un listener al radioButton2
        radioButton2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Desmarca el radioButton1
                radioButton1.isChecked = false
                // Deshabilita todos los elementos dentro del LinearLayout1
                txtDias.isEnabled = false
                spinnerPagare.isEnabled = false
                //Habilita los elementos
                txtPorcentaje2.isEnabled = true
                txtMaximo.isEnabled = true
                txtAbono.isEnabled = true
                txtPorcentaje.isEnabled = false
                view.findViewById<EditText>(R.id.txtDias)?.text?.clear()
                view.findViewById<Spinner>(R.id.spinnerPagare).setSelection(0)
                view.findViewById<EditText>(R.id.txtPorcentaje)?.text?.clear()
                Pagare = 2
            }
        }


        return view
    }


    fun llenarSpinnerPagare(view: View) {
        val spinnerPagare = view.findViewById<Spinner>(R.id.spinnerPagare)
        val pagareValues = arrayOf("1", "2", "3", "4", "5")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pagareValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPagare.adapter = adapter
    }

    private fun showDatePickerDialog(textView: TextView) {
        if (datePickerDialog == null || !datePickerDialog!!.isShowing) {
            datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
                    textView.text = sdf.format(selectedDate.time)
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog!!.show()
        }
    }

    private fun obtenerNombresClientes() {
        // Obtener una instancia de la base de datos
        val database = dbHelper.readableDatabase
        // Realizar la consulta SQL para obtener los nombres de los clientes
        val cursor = database.rawQuery("SELECT cl_nombre FROM ve_ws_clienteAsignadoVendedor ORDER BY cl_nombre ASC", null)
        // Verificar si el cursor contiene la columna "cl_nombre"
        val nombreClienteIndex = cursor.getColumnIndex("cl_nombre")
        // Iterar sobre el cursor para obtener los nombres de los clientes y agregarlos a la lista
        while (cursor.moveToNext()) {
            // Verificar si la columna existe antes de intentar obtener su valor
            if (nombreClienteIndex != -1) {
                val nombreCliente = cursor.getString(nombreClienteIndex)
                clientesList.add(nombreCliente)
            }
        }
        // Cerrar el cursor y la base de datos
        cursor.close()
        database.close()
    }

    private fun obtenerDescripcionCliente(position: Int): String {
        // Obtener una instancia de la base de datos
        val database = dbHelper.readableDatabase
        // Obtener el nombre del cliente seleccionado
        val nombreCliente = clientesList[position]
        // Realizar la consulta SQL para obtener la descripción del cliente seleccionado
        val cursor = database.rawQuery("SELECT pz_descripcion FROM ve_ws_clienteAsignadoVendedor WHERE cl_nombre=?", arrayOf(nombreCliente))
        // Obtener el índice de la columna "pz_descripcion"
        val descripcionIndex = cursor.getColumnIndex("pz_descripcion")
        // Inicializar la descripción del cliente
        var descripcionCliente = ""
        // Verificar si el cursor contiene la columna "pz_descripcion"
        if (descripcionIndex != -1 && cursor.moveToFirst()) {
            // Obtener la descripción del cliente si la columna existe y el cursor no está vacío
            descripcionCliente = cursor.getString(descripcionIndex)
        }
        // Cerrar el cursor y la base de datos
        cursor.close()
        database.close()
        return descripcionCliente
    }

    private fun obtenerRucCliente(position: Int): String {
        // Obtener una instancia de la base de datos
        val database = dbHelper.readableDatabase
        // Obtener el nombre del cliente seleccionado
        val nombreCliente = clientesList[position]
        // Realizar la consulta SQL para obtener la descripción del cliente seleccionado
        val cursor = database.rawQuery("SELECT en_identificacion FROM ve_ws_clienteAsignadoVendedor WHERE cl_nombre=?", arrayOf(nombreCliente))
        // Obtener el índice de la columna "pz_descripcion"
        val RucIndex = cursor.getColumnIndex("en_identificacion")
        // Inicializar la descripción del cliente
        var RucCliente = ""
        // Verificar si el cursor contiene la columna "pz_descripcion"
        if (RucIndex != -1 && cursor.moveToFirst()) {
            // Obtener la descripción del cliente si la columna existe y el cursor no está vacío
            RucCliente = cursor.getString(RucIndex)
        }
        // Cerrar el cursor y la base de datos
        cursor.close()
        database.close()
        return RucCliente
    }


    private fun obtenerCodigoCliente(position: Int): Int {
        // Obtener una instancia de la base de datos
        val database = dbHelper.readableDatabase
        // Obtener el nombre del cliente seleccionado
        val nombreCliente = clientesList[position]
        // Realizar la consulta SQL para obtener el código del cliente seleccionado
        val cursor = database.rawQuery("SELECT cl_codigo FROM ve_ws_clienteAsignadoVendedor WHERE cl_nombre=?", arrayOf(nombreCliente))
        // Obtener el índice de la columna "cl_codigo"
        val codigoIndex = cursor.getColumnIndex("cl_codigo")
        // Inicializar el código del cliente
        var codigoCliente = 0
        // Verificar si el cursor contiene la columna "cl_codigo"
        if (codigoIndex != -1 && cursor.moveToFirst()) {
            // Obtener el código del cliente si la columna existe y el cursor no está vacío
            codigoCliente = cursor.getInt(codigoIndex)
        }
        // Cerrar el cursor y la base de datos
        cursor.close()
        database.close()
        return codigoCliente
    }

    private fun obtenerCodigoVendedor(): Int {
        // Obtener una instancia de la base de datos
        val database = dbHelper.readableDatabase
        // Realizar la consulta SQL para obtener el código del vendedor
        val cursor = database.rawQuery("SELECT vn_codigo FROM ve_ws_vendedor", null)
        // Obtener el índice de la columna "vn_codigo"
        val codigoIndex = cursor.getColumnIndex("vn_codigo")
        // Inicializar el código del vendedor
        var codigoVendedor = 0
        // Verificar si el cursor contiene la columna "vn_codigo"
        if (codigoIndex != -1 && cursor.moveToFirst()) {
            // Obtener el código del vendedor si la columna existe y el cursor no está vacío
            codigoVendedor = cursor.getInt(codigoIndex)
        }
        // Cerrar el cursor y la base de datos
        cursor.close()
        database.close()
        return codigoVendedor
    }



    private fun mostrarFacturasDelClienteSeleccionado(clienteSeleccionado: Int, fechaInicial: String, fechaFinal: String) {
        val database = dbHelper.readableDatabase
        val facturasList = ArrayList<DataModel>() // Cambio a lista de DataModel


        // Realizar la consulta SQL para obtener las facturas del cliente seleccionado dentro del rango de fechas
        val query = "SELECT * FROM cc_ws_clienteFacturaVendedor " +
                "WHERE cl_codigo = $clienteSeleccionado " +
                "AND  fa_fechafactura BETWEEN '$fechaInicial' AND '$fechaFinal'"



        val cursor = database.rawQuery(query, null)

        val columnaSriIndex = cursor.getColumnIndex("fa_sri")
        val columnaDescripcionIndex = cursor.getColumnIndex("bo_descripcion")
        val columnaFechaFacturaIndex = cursor.getColumnIndex("fa_fechafactura")
        val columnaValorTotalFacturaIndex = cursor.getColumnIndex("fa_valortotfact")

        while (cursor.moveToNext()) {
            val sri = cursor.getString(columnaSriIndex)
            val descripcion = cursor.getString(columnaDescripcionIndex)
            val fechaFacturaString = cursor.getString(columnaFechaFacturaIndex)
            val valorTotalFactura = cursor.getString(columnaValorTotalFacturaIndex)

            // Formatear la fecha de la factura al formato corto (dd-mm-yyyy)
            val fechaFactura = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaFacturaString)
            val fechaFormateada = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(fechaFactura)

            val factura = DataModel(sri, descripcion, fechaFormateada, "\$$valorTotalFactura")

            facturasList.add(factura)
        }

        cursor.close()
        database.close()

        if (facturasList.isNotEmpty()) {
            // Configurar el RecyclerView con un LayoutManager
            val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerView)
            val layoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = layoutManager

            // Crear un adaptador para el RecyclerView
            val adapter = FacturaListAdapter(requireContext(), facturasList, requireView().findViewById(
                R.id.txtTotal
            ),requireView().findViewById(R.id.txtPorcentaje),requireView().findViewById(R.id.txtPorcentaje2) )

            // Establecer el adaptador en el RecyclerView
            recyclerView.adapter = adapter
        }
        else {
            // Mostrar un mensaje indicando que no se encontraron facturas
            Toast.makeText(requireContext(), "No se encontraron facturas para este cliente en el rango de fechas especificado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fnOpenPdf(file: File) {
        if (!file.exists()) {
            Toast.makeText(requireContext(), "El archivo no existe", Toast.LENGTH_LONG).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No hay una aplicación para abrir PDFs", Toast.LENGTH_LONG).show()
        }
    }

}
