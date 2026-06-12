package com.example.Consultaitems.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.Consultaitems.BuildConfig
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.parser.XMLParser
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.parser.XmlSincronizacion
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val URL_MANUAL = "https://app.cotzul.com/sitenet/movil/01-gestionvendedor.pdf"
class frmPrincipalF : AppCompatActivity() {
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap
    val usuario = frmLogin.CadenaHolder.ep_codigo
    private val frmPagare: frmPagare by lazy { frmPagare() }
    private val fragmentPrecios: frmPreciosyStock by lazy { frmPreciosyStock() }
    private val frmDefault: frmDefaultFragment by lazy { frmDefaultFragment() }
    private val frmItemxCliente: frmItemxCliente by lazy { frmItemxCliente() }
    private val frmStatusPedido: frmStatusPedido by lazy { frmStatusPedido() }
    private val frmClientes: frmClientes by lazy { frmClientes() }
    private val frmDocumento: frmDocumento by lazy { frmDocumento() }
    private val frmReciboDigital: frmReciboDigital by lazy { frmReciboDigital() }
    private val frmPedidoVendedor: frmPedidoVendedor by lazy { frmPedidoVendedor() }
    private val frmRuta: FrmRuta by lazy { FrmRuta() }
    private val frmMapa: MapsFragment by lazy { MapsFragment() }
    private val frmEstadistica: frmEstadistica by lazy { frmEstadistica() }
    private val frmClienteItems: frmClienteItems by lazy { frmClienteItems() }
    private val frmProforma: frmProformaA by lazy { frmProformaA() }
    private val frmConsultaR: frmConsultaRuta by lazy { frmConsultaRuta() }
    private val frmOcr: OCRFragment by lazy {OCRFragment()}
    private val frmProspecto: ProspectoFragment by lazy { ProspectoFragment() }
    private val frmLlamada: LlamadaFragment by lazy { LlamadaFragment() }
    private val frmActividad: ActividadesFragment by lazy { ActividadesFragment() }
    private val frmAuditar: FrmAuditarInventario by lazy { FrmAuditarInventario() }
    private val frmPreciosyStockVertical : frmPreciosyStockVertical by lazy { frmPreciosyStockVertical() }


    private var activeFragment: Fragment? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var llenarControles: ClsLLenarControles
    var vendedor: String = ""
    var userType = 1 //getUserType()
    var gv_tipo: String = "1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        llenarControles = ClsLLenarControles(this)

        gv_tipo = llenarControles.fnObtenerTipoVendedorLogin(usuario)


        val layoutRes = when (gv_tipo.toInt()) {
            1 -> R.layout.frm_principal_fragment
            2 -> R.layout.frm_fragment_2
            else -> R.layout.frm_fragment_3
        }
        setContentView(layoutRes)

        // setContentView(R.layout.frm_principal_fragment)

        //obtener el nombre y la version
        val currentTitle = supportActionBar?.title.toString()
        supportActionBar?.title = "$currentTitle (${BuildConfig.VERSION_NAME})"

        llenarControles = ClsLLenarControles(this)

        val login = llenarControles.fnLLenarVendedor()
        if (login != null) {
            vendedor = login.login
        }

        // Restaurar el fragmento activo si existe un estado guardado
        if (savedInstanceState != null) {
            activeFragment = supportFragmentManager.getFragment(savedInstanceState, "activeFragment")
        } else {
            // Agregar solo el fragmento por defecto si no hay un estado guardado
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, frmDefault, "frmDefault")
                .commit()
            activeFragment = frmDefault
        }



        val appName = getString(R.string.app_name) // Obtén el nombre de la aplicación

        if (gv_tipo.toInt()==1){
            bottomNavigationView = findViewById(R.id.bottom_navigation)
            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_pedido -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showFragment(frmPedidoVendedor, "Pedido")
                    }

                    R.id.nav_precios -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                        showFragment(fragmentPrecios, "Precios")
                    }

                    R.id.nav_status -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        showFragment(frmStatusPedido, "Status")
                    }

                    R.id.nav_recibo -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showFragment(frmReciboDigital, "Recibo")
                    }

                    R.id.nav_mas_opciones -> {

                        showPopupMenuMayorista(findViewById(R.id.nav_mas_opciones))
                    }
                }
                true
            }
        }else {
            bottomNavigationView = findViewById(R.id.bottom_navigation2)
            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_Proforma -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showFragment(frmProforma, "Pedido")
                    }

                    R.id.nav_precios -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                        showFragment(frmPreciosyStockVertical, "Precios")
                    }

                    R.id.nav_item -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showFragment(frmItemxCliente, "Item")
                    }

                    R.id.nav_cliente -> {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showFragment(frmClientes, "Clientes")
                    }

                    R.id.nav_mas_opciones2 -> {

                        showPopupMenuAlmacen(findViewById(R.id.nav_mas_opciones2))
                    }
                }
                true
            }
        }
    }


    // Función para mostrar el PopupMenu con opciones adicionales
    private fun showPopupMenuMayorista(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.frm_submenu_frargment, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout ->{
                    // Construir el diálogo de confirmación
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Cerrar sesión")
                    builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
                    builder.setPositiveButton("Sí") { _, _ ->
                        // Si el usuario hace clic en Sí, cierra la sesión y redirige a la pantalla de inicio de sesión
                        // Agrega aquí la lógica para cerrar la sesión del usuario

                        val intent = Intent(this@frmPrincipalF, frmLogin::class.java)
                        startActivity(intent)
                        finish()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        // Si el usuario hace clic en No, cierra el diálogo sin hacer nada
                    }

                    // Mostrar el diálogo
                    val dialog = builder.create()
                    dialog.show()
                }

                R.id.nav_sync -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Sincronizar"
                    dbHelper = SqLiteOpenHelper(this)
                    solicitudSoap = SolicitudSoap(this)
                    sincronizarDatos()
                }
                R.id.nav_pagare1 -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Pagare"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmPagare, "Pagare")
                }
                R.id.nav_cliente -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Clientes"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmClientes, "Clientes")
                }
                R.id.nav_documento -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Documento"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmDocumento, "Documento")
                }
                R.id.nav_item -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Item"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmItemxCliente, "Item")
                }

                R.id.nav_estadistica -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Estadistica"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmEstadistica, "Estadistica")
                }

                /* */
                R.id.nav_ruta -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Ruta"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmRuta, "Ruta")
                }

                R.id.nav_mapa -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Ruta"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmMapa, "Ruta")
                }

                R.id.nav_manual -> {
                    val downloadTask = DownloadPdfTask()
                    downloadTask.execute(URL_MANUAL)
                }

                R.id.nav_respaldo -> {
                    fnBackupDatabaseToDownloads(this,vendedor)
                }

                R.id.nav_clienteitem ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Cliente Item"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmClienteItems, "Cliente Item")
                }

                R.id.nav_consultaR ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Consulta Ruta"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmConsultaR, "Cliente Item")
                }

                R.id.nav_sincRapida ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Sincronizar"
                    dbHelper = SqLiteOpenHelper(this)
                    solicitudSoap = SolicitudSoap(this)
                    fnSincronizacionRapida()
                }

                R.id.nav_registro -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Registro"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmOcr, "Registro Cliente")
                }
                R.id.nav_Proforma -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones).title = "Proforma"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmProforma, "Pedido")
                }

                R.id.nav_qr -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(FrmScanQr(), "Qr")

                }

            }
            true
        }
        if (frmLogin.CadenaHolder.ep_codigo != 70986 /*|| frmLogin.CadenaHolder.ep_codigo != 3752*/ ) {
            popup.menu.findItem(R.id.nav_Proforma).isVisible = false
            popup.menu.findItem(R.id.nav_qr).isVisible = false

        }else{
            popup.menu.findItem(R.id.nav_ruta).isVisible = false
            popup.menu.findItem(R.id.nav_mapa).isVisible = false
            popup.menu.findItem(R.id.nav_consultaR).isVisible = false
            popup.menu.findItem(R.id.nav_estadistica).isVisible = false
            popup.menu.findItem(R.id.nav_pagare1).isVisible = false
        }
        popup.show()
    }


    private fun showPopupMenuAlmacen(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.frm_submenu_frargment2, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout2 ->{
                    // Construir el diálogo de confirmación
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Cerrar sesión")
                    builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
                    builder.setPositiveButton("Sí") { _, _ ->
                        // Si el usuario hace clic en Sí, cierra la sesión y redirige a la pantalla de inicio de sesión
                        // Agrega aquí la lógica para cerrar la sesión del usuario

                        val intent = Intent(this@frmPrincipalF, frmLogin::class.java)
                        startActivity(intent)
                        finish()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        // Si el usuario hace clic en No, cierra el diálogo sin hacer nada
                    }

                    // Mostrar el diálogo
                    val dialog = builder.create()
                    dialog.show()
                }

                R.id.nav_sync2 -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Sincronizar"
                    dbHelper = SqLiteOpenHelper(this)
                    solicitudSoap = SolicitudSoap(this)
                    sincronizarDatos()
                }

                R.id.nav_clienteitem2 ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Cliente Item"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmClienteItems, "Cliente Item")

                }

                R.id.nav_documento2 -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Documento"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmDocumento, "Documento")
                }

                R.id.nav_manual2 -> {
                    val downloadTask = DownloadPdfTask()
                    downloadTask.execute(URL_MANUAL)
                }

                R.id.nav_respaldo2 -> {
                    fnBackupDatabaseToDownloads(this,vendedor)
                }

                R.id.nav_registro -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Registro"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmOcr, "Registro Cliente")
                }

                R.id.nav_qr -> {
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Escanear Qr"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(FrmScanQr(), "Qr")

                }

                R.id.nav_inventario ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Escanear Inventario"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(FrmInventario(), "inventario")
                }

                R.id.nav_prospecto ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Prospecto"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmProspecto, "prospecto")
                }

                R.id.nav_llamada ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Llamada"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmLlamada, "llamada")
                }

                R.id.nav_actividad ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Actividad"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmActividad, "actividad")
                }

                R.id.nav_auditar ->{
                    bottomNavigationView.menu.findItem(R.id.nav_mas_opciones2).title = "Auditar"
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    showFragment(frmAuditar, "Auditar")
                }

            }
            true
        }
        popup.show()
    }

    // Mostrar el fragmento seleccionado y ocultar el anterior
    private fun showFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // Ocultar el fragmento activo actual antes de mostrar el nuevo
        activeFragment?.let {
            transaction.hide(it)
        }

        // Si el fragmento ya está añadido, solo mostrarlo; de lo contrario, añadirlo
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragment_container, fragment, tag)
        }

        transaction.commitAllowingStateLoss()

        // Actualizar el fragmento activo
        activeFragment = fragment
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Guardar el fragmento activo
        activeFragment?.let {
            supportFragmentManager.putFragment(outState, "activeFragment", it)
        }
    }


    override fun onResume() {
        super.onResume()
        if (gv_tipo == "1"){
            SessionManager.onActivityResumed(this)
        }

    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (gv_tipo == "1") {
            SessionManager.onUserInteraction()
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirmar salida")
            setMessage("¿Estás seguro de que quieres salir de la aplicación?")
            setPositiveButton("Sí") { dialog, which ->
                super@frmPrincipalF.onBackPressed()
                finish()
            }
            setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            setCancelable(true)
            show()
        }
    }

    private fun sincronizarDatos() {
        if (isNetworkAvailable(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Está seguro de que desea sincronizar los datos?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    val progressDialog = showProgressDialog()
                    MiAsyncTask(progressDialog).execute()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            showToast("Verifique su conexión a internet")
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Sincronizando datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""
        override fun onPreExecute() {
            super.onPreExecute()
            // Desactivar el botón de sincronización
        }

        override fun doInBackground(vararg voids: Void): String? {
            try {
                database = dbHelper.writableDatabase
                val cadena = getString(R.string.str_cadena, usuario)
                solicitudSoap.initializeVariables(getString(R.string.str_id).toInt(), cadena)
                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }
                if (!result.isNullOrBlank()) {
                    XMLParser.parseAndInsertData(result, database) { insertedData ->
                        datosInsertados = insertedData
                    }
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrBlank()) {
                    showToast("Los datos se sincronizaron correctamente")
                    val success = checkIfDataInsertedSuccessfully(database)
                    if (success) {
                        //showToast("Datos insertados Correctamente")
                    } else {
                        throw Exception("Error: Data insertion failed")
                    }
                } else {
                    showToast("Error en la sincronizacion ")
                    showToast("verfique su internet")
                }
            } catch (e: Exception) {
            } finally {
                // val navSyncItem = findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.nav_sync)
                //navSyncItem.isEnabled = true
                database.close()
                progressDialog.dismiss()
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkIfDataInsertedSuccessfully(database: SQLiteDatabase): Boolean {
        var success = false
        database.use { db ->
            val vendedorCount = db.rawQuery("SELECT COUNT(*) FROM ve_ws_vendedor", null)
            val clienteAsignadoVendedorCount = db.rawQuery("SELECT COUNT(*) FROM ve_ws_clienteAsignadoVendedor", null)
            val usuarioCount = db.rawQuery("SELECT COUNT(*) FROM ve_ws_usuario", null)

            try {
                if (vendedorCount.moveToFirst() && clienteAsignadoVendedorCount.moveToFirst() && usuarioCount.moveToFirst()) {
                    success = vendedorCount.getInt(0) > 0 && clienteAsignadoVendedorCount.getInt(0) > 0 && usuarioCount.getInt(0) > 0
                }
            } finally {
                vendedorCount.close()
                clienteAsignadoVendedorCount.close()
                usuarioCount.close()
            }
        }
        return success
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnectedOrConnecting ?: false
        }
    }

    private fun isVerificationAllowed(): Boolean {
        // Verifica si la tarea de sincronización asíncrona está en curso
        return true
    }


    inner class DownloadPdfTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg urls: String?): Boolean {
            val url = urls[0]
            if (url != null) {
                disableSSLVerification()
                try {
                    val connection = URL(url).openConnection()
                    val inputStream = connection.getInputStream()
                    val outputDirectory = File("/storage/emulated/0/Download/") // Ruta a la carpeta de Descargas
                    outputDirectory.mkdirs() // Asegurarse de que la carpeta exista
                    val fechaActual = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
                    val outputFile = File(outputDirectory, "01-appandroid_$fechaActual.pdf") // Archivo de destino en la carpeta de Descargas
                    val outputStream = FileOutputStream(outputFile)

                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    return true // Descarga exitosa
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return false // Descarga fallida
        }

        override fun onPostExecute(result: Boolean) {
            val message = if (result) {
                "Archivo descargado exitosamente"
            } else {
                "¡Error al descargar el archivo!"
            }
            Toast.makeText(this@frmPrincipalF, message, Toast.LENGTH_SHORT).show()
        }

        fun disableSSLVerification() {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fnBackupDatabaseToDownloads(context: Context, vendedor: String) {
        try {
            val dbName = "db_vendedor.db" // Nombre real de tu base de datos
            val dbFile = context.getDatabasePath(dbName)

            // Formatear fecha y hora actual
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

            // Ruta a la carpeta Descargas
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Nombre del archivo con fecha y vendedor
            val backupFileName = "${currentDateTime}_${vendedor}_$dbName"
            val backupFile = File(downloadsDir, backupFileName)

            // Copiar el archivo
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                    output.flush()
                }
            }

            Toast.makeText(context, "Respaldo creado en: ${backupFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al crear respaldo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun fnSincronizacionRapida() {
        if (isNetworkAvailable(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Está seguro de que desea sincronizar los datos?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    val progressDialog = showProgressDialog()
                    MiAsyncTaskSincronizacionRapida(progressDialog).execute()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            showToast("Verifique su conexión a internet")
        }
    }


    private inner class MiAsyncTaskSincronizacionRapida(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""
        override fun onPreExecute() {
            super.onPreExecute()
            // Desactivar el botón de sincronización
        }

        override fun doInBackground(vararg voids: Void): String? {
            try {
                database = dbHelper.writableDatabase
                val cadena = "2,${frmLogin.CadenaHolder.ep_codigo} ,6"
                solicitudSoap.initializeVariables(getString(R.string.str_id).toInt(), cadena)
                var pedido: String = ""

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }
                if (!result.isNullOrBlank()) {
                    database.execSQL("DELETE FROM ve_ws_clienteAsignadoVendedor")
                    database.execSQL("DELETE FROM iv_ws_itemComboCab")
                    database.execSQL("DELETE FROM iv_ws_itemComboDet")

                    pedido = XmlSincronizacion.parseMultiTable(result,database, this@frmPrincipalF)

                }
                return pedido
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrBlank()) {
                    showToast("Los datos se sincronizaron correctamente")
                    val success = checkIfDataInsertedSuccessfully(database)
                    if (success) {
                        //showToast("Datos insertados Correctamente")
                    } else {
                        throw Exception("Error: Data insertion failed")
                    }
                } else {
                    showToast("Error en la sincronizacion ")
                    showToast("verfique su internet")
                }
            } catch (e: Exception) {
            } finally {
                // val navSyncItem = findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.nav_sync)
                //navSyncItem.isEnabled = true
                database.close()
                progressDialog.dismiss()
            }
        }
    }
}
