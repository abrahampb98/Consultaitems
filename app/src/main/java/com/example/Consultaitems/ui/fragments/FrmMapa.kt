package com.example.Consultaitems.ui.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var btnActualizar: Button
    private lateinit var spnDias: Spinner

    private var diaActualSistema: String = ""
    private var diaSeleccionadoUsuario: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_mapa, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        llenarControles = ClsLLenarControles(requireContext())

        spnDias = view.findViewById(R.id.spinnerDiaRV)
        btnActualizar = view.findViewById(R.id.btnActualizarRV)

        // Llenar el spinner con los días
        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnDias.adapter = adapter

        // Definir día actual del sistema
        diaActualSistema = obtenerDiaActual()
        diaSeleccionadoUsuario = diaActualSistema

        // Seleccionar automáticamente el día actual en el spinner
        val index = dias.indexOfFirst { it.equals(diaActualSistema, ignoreCase = true) }
        if (index != -1) spnDias.setSelection(index)

        // Botón actualizar
        btnActualizar.setOnClickListener {
            diaSeleccionadoUsuario = spnDias.selectedItem.toString() // Siempre usar lo seleccionado
            mapView.getMapAsync(this) // recargar el mapa
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            googleMap = map

            fnConfigurarInfoWindowAdapter()

            googleMap.uiSettings.isZoomControlsEnabled = true

            val dia = diaSeleccionadoUsuario.ifEmpty { diaActualSistema }
            Log.e("Mapa", "$dia")

            val puntosClientes = try {
                llenarControles.fnOtenerRutasPorDia(requireContext(), dia)
                            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al obtener puntos: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                return
            }

            googleMap.clear()

            if (puntosClientes.isEmpty()) {
                Toast.makeText(requireContext(), "No hay ubicaciones para $dia", Toast.LENGTH_SHORT).show()
                return
            }

            val boundsBuilder = LatLngBounds.Builder()

            for (cliente in puntosClientes) {
                val latLng = LatLng(cliente.latitud, cliente.longitud)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("#${cliente.linea} - ${cliente.nombre}") // Título simple
                        .snippet(
                            "Teléfono: ${cliente.fono}\n" +
                                    "Dirección: ${cliente.direccion}\n"
                        )
                )
                boundsBuilder.include(latLng)
            }

            val bounds = boundsBuilder.build()
            val padding = 100
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al cargar puntos: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerDiaActual(): String {
        val formato = SimpleDateFormat("EEEE", Locale("es", "EC"))
        return formato.format(Date()).replaceFirstChar { it.uppercase() }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun fnConfigurarInfoWindowAdapter() {
        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                val context = requireContext()

                val layout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 20, 20, 20)
                }

                val tvTitulo = TextView(context).apply {
                    text = marker.title
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.BLACK)
                    textSize = 16f
                }

                val lines = marker.snippet?.split("\n") ?: listOf()

                val tvTelefono = TextView(context).apply {
                    text = lines.getOrNull(0) ?: ""
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.DKGRAY)
                }

                val tvDireccion = TextView(context).apply {
                    text = lines.getOrNull(1) ?: ""
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.DKGRAY)
                }

                layout.addView(tvTitulo)
                layout.addView(tvTelefono)
                layout.addView(tvDireccion)

                return layout
            }
        })
    }

}
