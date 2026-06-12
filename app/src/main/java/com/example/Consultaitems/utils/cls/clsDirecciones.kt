package com.example.Consultaitems.utils.cls

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DirectionsTask(
    private val context: Context,
    private val origin: LatLng,
    private val destination: LatLng,
    private val googleMap: GoogleMap,
    private val apiKey: String,
    private val onRouteReady: (String, String) -> Unit // Callback para tiempo y distancia
) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String? {
        val urlStr =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"
        val url = URL(urlStr)

        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            try {
                val jsonResponse = JSONObject(result)
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    if (legs.length() > 0) {
                        val leg = legs.getJSONObject(0)
                        val duration = leg.getJSONObject("duration").getString("text") // Tiempo
                        val distance = leg.getJSONObject("distance").getString("text") // Distancia

                        // Dibuja la ruta en el mapa
                        val steps = leg.getJSONArray("steps")
                        val polylineOptions = PolylineOptions().width(10f).color(Color.BLUE)
                        for (i in 0 until steps.length()) {
                            val points =
                                steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                            polylineOptions.addAll(PolyUtil.decode(points))
                        }
                        googleMap.addPolyline(polylineOptions)

                        // Llamar al callback con tiempo y distancia
                        onRouteReady(duration, distance)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
