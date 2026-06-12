package com.example.Consultaitems.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.drawerlayout.widget.DrawerLayout
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.example.Consultaitems.utils.parser.XMLParser
import com.example.Consultaitems.BuildConfig
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.ui.fragments.frmClientes
import com.example.Consultaitems.ui.fragments.frmDocumento
import com.example.Consultaitems.ui.fragments.frmItemxCliente
import com.example.Consultaitems.ui.fragments.frmPagare
import com.example.Consultaitems.ui.fragments.frmPedidoVendedor
import com.example.Consultaitems.ui.fragments.frmPreciosyStock
import com.example.Consultaitems.ui.fragments.frmReciboDigital
import com.example.Consultaitems.ui.fragments.frmStatusPedido
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


private const val URL_PDF = "https://app.cotzul.com/sitenet/movil/01-gestionvendedor.pdf"

class FrmPrincipalActivity : AppCompatActivity()  {

    //varibles globales
    lateinit var btnSync: Button
    lateinit var btnVerifica: Button
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var txtResultado: TextView
    lateinit var txtDatos:TextView
    lateinit var lblUsuario: TextView
    val usuario = frmLogin.CadenaHolder.ep_codigo
    private var nombreUsuario: String = ""
    private var backPressedTime: Long = 0
    private lateinit var llenarControles: ClsLLenarControles


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frmprincipal)

        val txtVersion = findViewById<TextView>(R.id.txtVersion)
        val versionName = BuildConfig.VERSION_NAME
        txtVersion.text = "V $versionName"



        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        //val navView: NavigationView = findViewById(R.id.nav_view)


        /*navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_pagare1 -> {
                    val intent = Intent(this@FrmPrincipalActivity, frmPagare::class.java)
                    startActivity(intent)
                }
                R.id.nav_manual -> {
                    val downloadTask = DownloadPdfTask()
                    downloadTask.execute(URL_PDF)
                }


                R.id.nav_sync -> {
                    sincronizarDatos()
                    fnActualizarUsuario()
                }
                R.id.nav_verifica -> {
                    if (isVerificationAllowed()) {
                        val database = dbHelper.readableDatabase
                        val success = checkIfDataInsertedSuccessfully(database)
                        if (success) {
                            showToast("Los datos se actualizaron correctamente")
                        } else {
                            showToast("Error: La actualización falló")
                        }
                    } else {
                        showToast("Espere a que se complete la sincronización antes de verificar nuevamente")
                    }

                }
                R.id.nav_logout -> {
                    // Construir el diálogo de confirmación
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Cerrar sesión")
                    builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
                    builder.setPositiveButton("Sí") { _, _ ->
                        // Si el usuario hace clic en Sí, cierra la sesión y redirige a la pantalla de inicio de sesión
                        // Agrega aquí la lógica para cerrar la sesión del usuario

                        val intent = Intent(this@FrmPrincipalActivity, frmLogin::class.java)
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
                R.id.nav_pedido ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmPedidoVendedor::class.java)
                    startActivity(intent)

                }

                R.id.nav_precios ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmPreciosyStock::class.java)
                    startActivity(intent)

                }

                R.id.nav_item ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmItemxCliente::class.java)
                    startActivity(intent)
                }


                R.id.nav_cliente ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmClientes::class.java)
                    startActivity(intent)
                }

                R.id.nav_status ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmStatusPedido::class.java)
                    startActivity(intent)
                }

                R.id.nav_documento ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmDocumento::class.java)
                    startActivity(intent)
                }

                R.id.nav_recibo ->{
                    val intent = Intent(this@FrmPrincipalActivity, frmReciboDigital::class.java)
                    startActivity(intent)
                }


            }
            // Cerrar el menú lateral después de manejar el clic en un elemento
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }*/

        initializeVariables()

        dbHelper = SqLiteOpenHelper(this)
        solicitudSoap = SolicitudSoap(this)
        llenarControles = ClsLLenarControles(this)


        obtenerNombreUsuario(usuario)
        lblUsuario.text = "Bienvenido, $nombreUsuario"

    }


    override fun onResume() {
        super.onResume()
        SessionManager.onActivityResumed(this)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        SessionManager.onUserInteraction()
    }


    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000) {
            backPressedTime = currentTime
            Toast.makeText(this, "Presione nuevamente para salir", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
            finishAffinity()
        }
    }


    private fun fnActualizarUsuario(){
        obtenerNombreUsuario(usuario)
        lblUsuario.text = "Bienvenido, $nombreUsuario"
    }

    private fun sincronizarDatos() {
        if (isNetworkAvailable(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Está seguro de que desea sincronizar los datos?")
                .setPositiveButton("Sí") { dialog, _ ->
                    // Aquí se ejecuta la tarea asíncrona si el usuario hace clic en "Sí"
                    dialog.dismiss()
                    val progressDialog = showProgressDialog()
                    MiAsyncTask(progressDialog).execute()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    // Aquí se cancela la acción si el usuario hace clic en "Cancelar"
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




    private fun initializeVariables() {
        //btnSync = findViewById(R.id.btnSync)
       // btnVerifica = findViewById(R.id.btnVerifica)
        //txtResultado = findViewById(R.id.txtResultado)
        //txtDatos = findViewById(R.id.txtDatos)
        lblUsuario = findViewById(R.id.lblUsuario)


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
                return null // Puedes modificar este valor de retorno dependiendo de cómo desees manejar el error
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrBlank()) {
                    //txtResultado.text = result
                    showToast("Los datos se sincronizaron correctamente")
                   // txtDatos.text = datosInsertados
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
                //txtError.text = e.message
            } finally {
               // val navSyncItem = findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.nav_sync)
                //navSyncItem.isEnabled = true

                //btnSync.isEnabled = true
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

    private fun obtenerNombreUsuario(userCode: Int) {
        dbHelper.readableDatabase.use { database ->
            val selection = "vn_codigo = ?"
            val selectionArgs = arrayOf(userCode.toString())

            database.query("ve_ws_vendedor", arrayOf("vn_nombre"), selection, selectionArgs, null, null, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("vn_nombre"))
                }
            }
        }
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
            Toast.makeText(this@FrmPrincipalActivity, message, Toast.LENGTH_SHORT).show()
        }



        fun disableSSLVerification() {
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
