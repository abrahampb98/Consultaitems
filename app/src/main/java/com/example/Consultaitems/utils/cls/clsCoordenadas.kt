package com.example.Consultaitems.utils.cls

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class RegistrarCoordenadas(private val context: Context, private val activity: Activity) {
    private lateinit var llenarControles: ClsLLenarControles
    private val REQUEST_LOCATION_PERMISSION = 100

    fun fnRegistrarCoordenadas(codigo: String, onComplete: (Boolean) -> Unit) {
        llenarControles = ClsLLenarControles(context)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Verificar permisos antes de proceder
        if (!tienePermisoUbicacion()) {
            Log.e("RegistrarCoordenadas", "Permiso de ubicación no otorgado")
            onComplete(false) // Notificar que no se pudo completar por falta de permisos
            return
        }

        try {
            // Obtener la última ubicación conocida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Datos de la ubicación
                    val latitud = location.latitude
                    val longitud = location.longitude
                    val timestamp = System.currentTimeMillis().toString() // Marca de tiempo actual
                    val estado = "A" // Estado por defecto
                    val check = 1

                    // Insertar en la base de datos
                    llenarControles.fnActualizarCoordenada(latitud, longitud, timestamp, estado, codigo, check)
                    //Log.d("RegistrarCoordenadas", "Coordenada registrada correctamente: $latitud, $longitud")
                    onComplete(true) // Notificar éxito
                } else {
                    //Log.e("RegistrarCoordenadas", "No se pudo obtener la ubicación")
                    onComplete(false) // Notificar fallo
                }
            }.addOnFailureListener { exception ->
                //Log.e("RegistrarCoordenadas", "Error al obtener la ubicación: ${exception.message}")
                onComplete(false) // Notificar fallo
            }
        } catch (e: SecurityException) {
           // Log.e("RegistrarCoordenadas", "Excepción de seguridad: ${e.message}")
            onComplete(false) // Notificar fallo
        }
    }

    private fun tienePermisoUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun solicitarPermisoUbicacion() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Mostrar explicación al usuario antes de solicitar el permiso
            AlertDialog.Builder(context)
                .setTitle("Permiso de Ubicación Requerido")
                .setMessage("Se necesita acceso a la ubicación para registrar coordenadas.")
                .setPositiveButton("Aceptar") { _, _ ->
                    solicitarPermisoDirecto()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            // Solicitar permiso directamente
            solicitarPermisoDirecto()
        }
    }

    private fun solicitarPermisoDirecto() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, codigo: String, onComplete: (Boolean) -> Unit) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("RegistrarCoordenadas", "Permiso de ubicación otorgado")
                // Llama nuevamente a la función para registrar coordenadas
                fnRegistrarCoordenadas(codigo, onComplete)
            } else {
                Log.e("RegistrarCoordenadas", "Permiso de ubicación denegado")
                onComplete(false) // Notificar fallo
            }
        }
    }
}
