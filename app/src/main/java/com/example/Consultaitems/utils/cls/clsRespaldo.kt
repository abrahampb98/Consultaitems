package com.example.Consultaitems.utils.cls

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun backupDatabase(context: Context, fileName: String): Boolean {
    val dbPath = context.getDatabasePath("db_vendedor").absolutePath
    val dbFile = File(dbPath)

    // Verifica que el archivo de la base de datos existe
    if (!dbFile.exists()) {
        Log.e("BackupDatabase", "La base de datos no existe en la ruta: $dbPath")
        return false
    }

    val databases = context.databaseList()
    Log.d("DatabaseList", "Bases de datos: ${databases.joinToString()}")


    val backupDir = File(context.getExternalFilesDir(null), "RespaldoApp")

    // Crear el directorio de respaldo si no existe
    if (!backupDir.exists()) {
        backupDir.mkdirs()
    }

    val backupFile = File(backupDir, "$fileName.db")
    return try {
        val input = FileInputStream(dbFile)
        val output = FileOutputStream(backupFile)

        // Copiar el archivo byte por byte
        input.channel.use { inputChannel ->
            output.channel.use { outputChannel ->
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
            }
        }
        true // Respaldo exitoso
    } catch (e: IOException) {
        e.printStackTrace()
        false // Error en el respaldo
    }
}

