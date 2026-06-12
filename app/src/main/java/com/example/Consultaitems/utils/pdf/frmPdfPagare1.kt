package com.example.Consultaitems.utils.pdf

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.Consultaitems.ui.fragments.frmReporteRecibo
import com.example.Consultaitems.utils.cls.clsConvertirDolares
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class frmPdfPagare1 {

    companion object {
        var Cliente:String =""
        var Total:Double = 0.00
        var Descripcion:String=""
        var Meses: Int = 0
        var Final: Double = 0.00
        var Letras: String =""
        var Ruc: String =""
        var fileName: String = ""
        var Dias: Int = 0
        var Vendedor: Int = 0
        fun generatePdf(context: Context, nombreCliente: String, total: Double, descripcion: String, rucCliente: String, vn_codigo: Int): File {
            Cliente = nombreCliente
            Total = total
            Descripcion = descripcion
            Vendedor = vn_codigo
            Meses = calcularMeses(Descripcion)
            Final = String.format("%.2f", Total / Meses).toDouble()
            val parteDecimal = BigDecimal(Final.toString()).subtract(BigDecimal(Final.toInt())).multiply(BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
            Letras = clsConvertirDolares.convertirNumeroALetras(Final - parteDecimal / 100.0) + " CON $parteDecimal"
            Ruc = rucCliente
            val sdf = SimpleDateFormat("MMMM dd 'del' yyyy", Locale("es", "ES"))
            val cal = Calendar.getInstance()


            //Genera pdf segun la cantidad de meses
            for (i in 1..Meses) {
                val fechaActual = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
                val horaActual = SimpleDateFormat("HHmmss").format(Date())
                fileName = "10$i"+"_"+"$fechaActual"+"_"+"$Cliente.pdf"
                val filePath =
                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName"
                val document = Document()
                try {
                    val outputStream = FileOutputStream(filePath)
                    PdfWriter.getInstance(document, outputStream)
                    document.open()

                    //manejo de fechas dependiendo el numero de pagare
                    when (i) {
                        1 -> { // Si es 1, no agregamos días
                            Dias = 30// La fecha actual no necesita modificaciones
                        }
                        2 -> {
                            Dias = 60 // Asignamos 30 días a la variable `dias`
                        }
                        3 -> {
                            Dias = 90 // Asignamos 60 días a la variable `dias`
                        }
                        else -> {
                        }
                    }
                    val FechaActual = sdf.format(cal.time)



                    //Titulo
                    val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
                    val chunkTitle = Chunk("PAGARÉ A LA ORDEN \n", fontTitle)
                    val paragraphTitle = Paragraph(chunkTitle).apply {
                        alignment =
                            Element.ALIGN_CENTER // Establece la alineación al centro del párrafo
                    }
                    // Subtitulo
                    val fontSubTitle1 = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
                    val chunkSubTitle1 = Chunk("$i/$Meses", fontSubTitle1)
                    val paragraphSubTitle1 = Paragraph(chunkSubTitle1).apply {
                        alignment =
                            Element.ALIGN_LEFT // Establece la alineación al centro del párrafo
                    }

                    // Subtitulo
                    val fontSubTitle = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
                    val chunkSubTitle = Chunk("Por US $Final\n\n", fontSubTitle)
                    val paragraphSubTitle = Paragraph(chunkSubTitle).apply {
                        alignment =
                            Element.ALIGN_RIGHT // Establece la alineación al centro del párrafo
                    }

                    val fontContent = Font(Font.FontFamily.HELVETICA, 10f)
                    val chunkContent = Chunk(
                        "Debo y pagaré a $Dias días, en la ciudad de Guayaquil o donde se me reconvenga, a la " +
                                "orden de la Compañía  COTZUL S.A., la suma" +
                                "  $Letras/100 DOLARES DE LOS ESTADOS UNIDOS DE NORTEAMÉRICA  ( $$Final )   cantidad recibida por mí de dicha compañía.\n\n" +
                                "La cantidad indicada me obligo a devolverla incondicionalmente,  en dólares de los " +
                                "Estados Unidos de Norteamérica, más la tasa activa efectiva máxima aprobada por el " +
                                "Banco Central del Ecuador para el segmento de crédito de consumo que rija al momento " +
                                "del pago y que se calculará desde la presente fecha; siendo de mi cuenta todos los  " +
                                "impuestos y tasas que cause este pagaré así como los gastos judiciales y " +
                                "extrajudiciales, inclusive honorarios profesionales, que ocasione el cobro en caso de mora, " +
                                "siendo suficiente prueba para establecer tales gastos la mera aseveración del acreedor.\n\n" +
                                "Al fiel cumplimiento de lo estipulado en este Pagaré a la Orden,  me obligo con todos " +
                                "mis bienes presentes y futuros; y faculto al acreedor para disponer de valores, cheques y " +
                                "documentos al cobro como pago parcial o total de este Pagaré, así como cargar o " +
                                "acreditar en mi cuenta los saldos que quedaren hasta la completa cancelación del   " +
                                "mismo y sus intereses.\n\n" +
                                "El pago no podrá hacerse por partes, ni aun por mis herederos.- SIN PROTESTO.- Eximo al " +
                                "acreedor de presentación para el pago y de aviso por falta del mismo.\n\n" +
                                "Renuncio al domicilio y quedo sometido a los jueces y tribunales de esta ciudad o a los " +
                                "que elija el acreedor y renuncio al derecho de interponer recurso de apelación, y aún el " +
                                "de hecho,de la sentencias que se expidieren en el juicio o juicios que en relación al " +
                                "presente documento hubieren lugar.\n\n" +
                                "Para los efectos pertinentes, expreso mi conformidad con lo estipulado en este Pagaré a " +
                                "la Orden.\n\n" +
                                "Guayaquil, $FechaActual\n\n\n" +
                                "_______________________________\n" +
                                "$Cliente\n" +
                                "C.I/R.U.C. #$Ruc \n\n" +
                                "Me/Nos constituyo/imos en fiador/es solidario/s, llano/s pagador/es del/os señor/es ," +
                                "$Cliente Por las obligaciones que ha/n contraído en este Pagaré; haciendo de deuda " +
                                "ajena, deuda propia, renunciando a los beneficios de orden y de excusión de bienes del deudor" +
                                " principal, el de división y cualquier ley que pueda favorecernos, así como la apelación y el " +
                                "recurso de hecho. Quedo sometido a los jueces y tribunales de esta ciudad, o de los que elija " +
                                "el acreedor. Sin protesto.\n\n" +
                                "Guayaquil, $FechaActual\n\n\n" +
                                "______________________                                        _____________________\n" +
                                "Nombre de Garante                                                     Nombre de Garante\n\n" +
                                "C.I.                                                                                C.I.\n\n" +
                                "Lugar y fecha de Suscripcion: Guayaquil, $FechaActual\n\n\n\n" +
                                "Firma deudor                                                                        Firma Garante\n\n\n"
                                + "F)_________________________                                         F)_________________________\n\n" +
                                "$Cliente\n" +
                                "C.I/R.U.C. #$Ruc \n\n" +
                                "En Santiago de Guayaquil, el ______________________ ante mi , " +
                                "_____________________ Notario Público  __________ del Cantón, Guayaquil,República del Ecuador , " +
                                "de conformidad con la facultad que me concede el numeral nueve, del articulo dieciocho de la ley" +
                                " Notarial en actual vigencia comparece.\n\n__________________________________________________, " +
                                "portador de la cedula de identidad numero ______________ , con el objeto de reconocer su firma y " +
                                "rubrica respectivamente con la cual suscribe el presente instrumento , al efecto juramentando " +
                                "legalidad y advertido que fue el compareciente por mi el Notario, de las penas de perjurio, luego " +
                                "de la cual pasa a la vista la firma y rubrica, declara y asevera que es suya propia la misma que " +
                                "usa en todos los actos públicos y privados y como tal  la reconoce, con lo cual termina la presente" +
                                " diligencia .- Para constancia suscribe el compareciente en unidad de acto conmigo de todo lo cual " +
                                "doy fe.\n\n\n\n" +
                                "______________________                                        _____________________\n" +
                                "Nombre de Garante                                                     Nombre de Garante\n\n" +
                                "$Cliente\n" +
                                "C.I/R.U.C. #$Ruc \n\n\n" +
                                "                                                                   ______________________\n" +
                                "                                                                   Nombre Notario\n" +
                                "                                                                   C.I.", fontContent
                    )

                    val paragraphContent = Paragraph(chunkContent).apply {
                        alignment = Element.ALIGN_JUSTIFIED // Justificar el texto
                    }

                    document.add(paragraphTitle)
                    document.add(Paragraph(""))
                    document.add(paragraphSubTitle1)
                    document.add(Paragraph(""))
                    document.add(paragraphSubTitle)
                    document.add(Paragraph(""))
                    document.add(paragraphContent)

                    document.close()
                    // Mostrar el Toast indicando que el PDF se ha generado correctamente
                } catch (e: IOException) {
                    // En caso de error al generar el PDF, mostrar un mensaje de error
                }
            }

            return  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        }
        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        private fun calcularMeses(politicaPago: String): Int {
            return when {
                politicaPago.contains("30-60-90") -> 3
                politicaPago.contains("30-60") -> 2
                politicaPago.contains("30") -> 1
                else -> 1 // Por defecto, si no se especifica ninguna política, se divide en un mes
            }
        }

         fun pagareManual(dias: String, num: String){
          var calculo =  dias.toInt() / num.toInt()


        }
    }




}
