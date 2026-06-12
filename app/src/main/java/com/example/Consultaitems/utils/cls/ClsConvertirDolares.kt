package com.example.Consultaitems.utils.cls
import java.math.BigDecimal

class clsConvertirDolares {
    companion object {
           fun convertirNumeroALetras(numero: Double): String {
            val parteEntera = numero.toInt()
            val parteDecimal = BigDecimal(numero.toString()).subtract(BigDecimal(parteEntera)).multiply(BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()

            if (parteEntera == 1000) {
                return "UN MIL"
            }

            val miles = parteEntera / 1000
            val centenasRestantes = parteEntera % 1000

            val milesEnLetras = if (miles == 1) "UN MIL " else if (miles > 0) "${convertirGrupoALetras(miles)} MIL " else ""
            val centenasEnLetras = convertirGrupoALetras(centenasRestantes)

            val decimalEnNumeros = if (parteDecimal > 0) "CON $parteDecimal" else ""

            return "$milesEnLetras$centenasEnLetras $decimalEnNumeros".trim()
        }

        fun convertirGrupoALetras(numero: Int): String {
            val centenas = arrayOf("", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos")
            val decenas = arrayOf("", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")
            val especiales = arrayOf("diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve")
            val unidades = arrayOf("", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")

            val centenasParte = numero / 100
            val decenasParte = (numero % 100) / 10
            val unidadesParte = numero % 10

            val centenasEnLetras = if (centenasParte > 0) "${centenas[centenasParte.toInt()]} " else ""
            val decenasEnLetras = if (decenasParte == 1) especiales[unidadesParte.toInt()] ?: "" else decenas[decenasParte.toInt()]
            val unidadesEnLetras = if (decenasParte != 1) unidades[unidadesParte.toInt()] else ""

            val espacioDecenas = if (decenasParte > 1 && unidadesParte > 0) " Y " else ""

            return "$centenasEnLetras$decenasEnLetras$espacioDecenas$unidadesEnLetras".toUpperCase()
        }

        fun main() {
            val numero = 2312.22
            val parteDecimal = BigDecimal(numero.toString()).subtract(BigDecimal(numero.toInt())).multiply(BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
            val letras = convertirNumeroALetras(numero - parteDecimal / 100.0) + " CON $parteDecimal" // Asignar el resultado a la variable 'letras'
            println(letras) // Imprimir el valor de la variable 'letras' si es necesario
        }
    }
}
