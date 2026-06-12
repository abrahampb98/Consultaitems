package com.example.Consultaitems.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.Consultaitems.BuildConfig
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.utils.cls.ClsLLenarControles

class frmDefaultFragment : Fragment() {
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var lblUsuario: TextView
    val usuario = frmLogin.CadenaHolder.ep_codigo
    private var nombreUsuario: String = ""
    private lateinit var llenarControles: ClsLLenarControles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.frmprincipal, container, false)

        // Inicializar variables
        dbHelper = SqLiteOpenHelper(requireContext())
        initializeVariables(view)

        val txtVersion = view.findViewById<TextView>(R.id.txtVersion)
        val versionName = BuildConfig.VERSION_NAME
        txtVersion.text = "V $versionName"

        return view
    }

    private fun initializeVariables(view: View) {
        lblUsuario = view.findViewById(R.id.lblUsuario)  // Usar la vista inflada para obtener el TextView
        llenarControles = ClsLLenarControles(requireContext())

        // Obtener el nombre del usuario antes de asignarlo al lblUsuario
        obtenerNombreUsuario(usuario)
        lblUsuario.text = "Bienvenido, $nombreUsuario"
    }

    private fun obtenerNombreUsuario(userCode: Int) {
        dbHelper.readableDatabase.use { database ->
            val selection = "vn_codigo = ?"
            val selectionArgs = arrayOf(userCode.toString())

            database.query(
                "ve_ws_vendedor",
                arrayOf("vn_nombre"),
                selection,
                selectionArgs,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("vn_nombre"))?:""
                }
            }
        }
    }
}
