package com.example.Consultaitems.ui.activities

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.Consultaitems.BuildConfig
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.ui.fragments.frmPrincipalF
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.ConsultaWorker
import com.example.Consultaitems.utils.cls.Item
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class frmLogin : AppCompatActivity() {

    object Configuracion {
        var strIp: String = ""
    }

    object CadenaHolder {
        var cadena: String = ""
        var ep_codigo: Int = 0
    }

    private lateinit var dbHelper: SqLiteOpenHelper
    private lateinit var database: SQLiteDatabase
    private lateinit var spinner: Spinner
    private lateinit var llenarControles: ClsLLenarControles

    private var userCode: Int = 0
    private var usuario: String = ""
    private var password: String = ""
    private var isUserLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frmlogin)

        if (!areNotificationsEnabled()) {
            solicitarActivacionDeNotificaciones()
        }

        fnWorkManager(this)

        val currentTitle = supportActionBar?.title.toString()
        supportActionBar?.title = "$currentTitle (${BuildConfig.VERSION_NAME})"

        llenarControles = ClsLLenarControles(this)

        dbHelper = SqLiteOpenHelper(this)
        database = dbHelper.writableDatabase
        //spinner = findViewById(R.id.spinnerOpciones)

        insertDataManually()
        //fnConexion()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            usuario = findViewById<EditText>(R.id.txtUsuario).text.toString().trim()
            password = findViewById<EditText>(R.id.txtContrasena).text.toString().trim()

            if (validateLogin(usuario, password)) {
                isUserLoggedIn = true

                userCode = getUserCode(usuario)
                CadenaHolder.ep_codigo = userCode

                //fnObtenerIpSeleccionada()

                val intent = Intent(this@frmLogin, frmPrincipalF::class.java)
                startActivity(intent)

                findViewById<EditText>(R.id.txtUsuario)?.text?.clear()
                findViewById<EditText>(R.id.txtContrasena)?.text?.clear()
            } else {
                showToast("Usuario o contraseña incorrectos")
            }
        }

        val btnMostrarContrasena = findViewById<ImageButton>(R.id.btnMostrarContrasena)
        val txtContrasena = findViewById<EditText>(R.id.txtContrasena)

        btnMostrarContrasena.setOnClickListener {
            if (txtContrasena.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                txtContrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarContrasena.setImageResource(R.mipmap.ic_invisible)
            } else {
                txtContrasena.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarContrasena.setImageResource(R.mipmap.ic_visible)
            }

            txtContrasena.setSelection(txtContrasena.text.length)
        }
    }

    private fun fnWorkManager(context: Context) {
        val consultaWorkRequest = PeriodicWorkRequestBuilder<ConsultaWorker>(
            15,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ConsultaWorkerTag",
            ExistingPeriodicWorkPolicy.KEEP,
            consultaWorkRequest
        )
    }

    private fun fnConexion() {
        var conexion = listOf(
            Item("1", "Modo Internet"),
            Item("2", "Red Local")
        )

        val epCodigo = llenarControles.fnObtenerVendedor()
        val tipo = llenarControles.fnObtenerTipoVendedorLogin(epCodigo.toInt()).toInt()

        conexion = if (tipo == 2) {
            conexion.sortedByDescending { it.descripcion }
        } else {
            conexion.sortedBy { it.descripcion }
        }

        val adapter = object : ArrayAdapter<Item>(
            this,
            android.R.layout.simple_spinner_item,
            conexion
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.textSize = 20f
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.textSize = 20f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position) as Item
                selectedItem.codigo
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fnObtenerIpSeleccionada() {
        val selectedItemPosition = spinner.selectedItemPosition
        val selectedItem = spinner.getItemAtPosition(selectedItemPosition) as Item

        Configuracion.strIp = when (selectedItem.codigo.toInt()) {
            1 -> getString(R.string.str_ipPublica)
            2 -> getString(R.string.str_ipLocal)
            else -> "Desconocido"
        }
    }

    private fun validateLogin(usuario: String, password: String): Boolean {
        val database = dbHelper.readableDatabase
        val selection = "vn_login = ? AND vn_password = ?"
        val selectionArgs = arrayOf(usuario, password)

        val cursor = database.query(
            "ve_ws_usuario",
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val isValid = cursor.count > 0

        cursor.close()
        database.close()

        return isValid
    }

    private fun getUserCode(usuario: String): Int {
        dbHelper.readableDatabase.use { database ->
            val columns = arrayOf("vn_codigo")
            val selection = "vn_login = ?"
            val selectionArgs = arrayOf(usuario)

            database.query(
                "ve_ws_usuario",
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            ).use { cursor ->
                return if (cursor.moveToFirst()) {
                    cursor.getInt(cursor.getColumnIndexOrThrow("vn_codigo"))
                } else {
                    0
                }
            }
        }
    }

    private fun insertDataManually() {
        if (!isTableEmpty()) return

        val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
        val db = dbHelper.writableDatabase

        val usuarios = listOf(
            Triple(33, "crodriguez", "0911801934"),
            Triple(63, "rdiaz", "0910358928"),
            Triple(8720, "jpalma", "0913608832"),
            Triple(68341, "lgalvez", "0919663906"),
            Triple(59037, "hcruz", "0915895494"),
            Triple(7833, "wecheverria", "0702080789"),
            Triple(68526, "bavila", "0925837718"),
            Triple(17111, "acalderon", "0930477013"),
            Triple(29763, "bquimis", "0930649843"),
            Triple(76835, "jcampozano", "0922355755"),
            Triple(72599, "pyepez", "0923507909"),
            Triple(67, "mvillacres", "0919748699"),
            Triple(48303, "kgarcia", "0950094813"),
            Triple(76836, "jlino", "0931938377"),
            Triple(81982, "aengracia", "0930561717"),
            Triple(81736, "landrade", "0932283278"),
            Triple(70986, "rguerrero", "0918664947"),
            Triple(56669, "jdiaz", "0922963905")
        )

        val vendedoresAlmacen = listOf(17111, 29763, 76835, 72599)
        val vendedoresVmr = listOf(67, 48303, 76836, 81982, 81736)

        db.beginTransaction()

        try {
            usuarios.forEach { (codigo, login, password) ->
                val values = ContentValues().apply {
                    put("vn_codigo", codigo)
                    put("vn_login", login)
                    put("vn_password", password)
                    put("vn_fechaing", currentDate)
                }

                db.insert("ve_ws_usuario", null, values)
            }

            vendedoresAlmacen.forEach { vnCodigo ->
                val values = ContentValues().apply {
                    put("vn_codigo", vnCodigo)
                    put("gv_tipo", 2)
                    put("bo_codigo", 2)
                    put("bo_descripcion", "Almacen")
                }

                db.insert("ve_ws_vendedor", null, values)
            }

            vendedoresVmr.forEach { vnCodigo ->
                val values = ContentValues().apply {
                    put("vn_codigo", vnCodigo)
                    put("gv_tipo", 2)
                    put("bo_codigo", 51)
                    put("bo_descripcion", "Almacén VMR")
                }

                db.insert("ve_ws_vendedor", null, values)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun isTableEmpty(): Boolean {
        val database = dbHelper.readableDatabase
        val cursor = database.rawQuery("SELECT COUNT(*) FROM ve_ws_usuario", null)

        cursor.moveToFirst()
        val count = cursor.getInt(0)

        cursor.close()
        database.close()

        return count == 0
    }

    private fun showToast(message: String) {
        Toast.makeText(this@frmLogin, message, Toast.LENGTH_SHORT).show()
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            Settings.Secure.getInt(contentResolver, "notification_enabled", 1) == 1
        }
    }

    private fun solicitarActivacionDeNotificaciones() {
        AlertDialog.Builder(this)
            .setTitle("Activar notificaciones")
            .setMessage("Para recibir notificaciones sobre recibos y pedidos pendientes, activa las notificaciones en la configuración.")
            .setPositiveButton("Activar") { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)

                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
