package com.example.Consultaitems.utils.cls

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.Consultaitems.R

class ConsultaWorker(appContext: Context, workerParams: WorkerParameters) :

    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        //Log.e("ConsultaWorker", "Worker ejecutado")
        val totalRecibos = fnVerificarRecibos()
        val totalPedidos = fnVerificarPedidos()

        // Solo mostrar la notificación si hay recibos pendientes
        if (totalRecibos > 0) {
            val mensaje = "Tiene $totalRecibos recibo(s) pendiente(s) por enviar."
            mostrarNotificacion("Gestion Vendedor - Recibos", mensaje,1)
        }

        if (totalPedidos > 0) {
            val mensaje = "Tiene $totalPedidos pedido(s) pendiente(s) por enviar."
            mostrarNotificacion("Gestion Vendedor - Pedidos", mensaje,2)
        }



        // Indica que el trabajo se ejecutó correctamente
        return Result.success()
    }

    private fun fnVerificarRecibos(): Int {
        var llenarControles: ClsLLenarControles
        llenarControles = ClsLLenarControles(applicationContext)

        // Llama a la función contarRecibosPendientes para obtener el número de recibos pendientes
        val recibos = llenarControles.fnVerificarRecibosPendientes()

        // Retorna el número total de recibos pendientes
        return recibos
    }

    private fun fnVerificarPedidos(): Int {
        var llenarControles: ClsLLenarControles
        llenarControles = ClsLLenarControles(applicationContext)

        val pedidos = llenarControles.fnVerificarPedidosPendientes()

        // Retorna el número total
        return pedidos
    }


    private fun mostrarNotificacion(titulo: String, contenido: String, notificationId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIconBitmap = BitmapFactory.decodeResource(applicationContext.resources,
            R.mipmap.ic_logo_bebes_round
        )


        // Crear el canal de notificación si es Android 8.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal de Notificaciones"
            val descriptionText = "Notificaciones para la app de gestión"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TU_CANAL_ID", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación
        val notification = NotificationCompat.Builder(applicationContext, "TU_CANAL_ID")
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSmallIcon(R.mipmap.ic_logo_bebes_round)  // Icono blanco y simple para la barra de notificaciones
            .setLargeIcon(largeIconBitmap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // La notificación desaparecerá al hacer clic en ella
            .build()

        // Mostrar la notificación con un ID único
        notificationManager.notify(notificationId, notification)
    }


}
