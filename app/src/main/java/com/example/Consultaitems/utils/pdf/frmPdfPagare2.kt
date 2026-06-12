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

class frmPdfPagare2 {

    companion object {
        var Cliente:String =""
        var Total:Double = 0.00
        var Cuotas: Int = 0
        var Final: Double = 0.00
        var Letras: String =""
        var Ruc: String =""
        var fileName: String = ""
        var Dias: Int = 0
        var Vendedor: Int = 0
        var Maximo: Int = 0
        var Abono: Int = 0
        fun generatePdf(context: Context, nombreCliente: String, total: Double, rucCliente: String, vn_codigo: Int, maximo: String, abono: String): File {
            Cliente = nombreCliente
            Total = total
           Vendedor = vn_codigo
            Maximo = maximo.toInt()
            Abono = abono.toInt()
            Cuotas = Maximo / Abono
            Final = String.format("%.2f", Total / Cuotas).toDouble()
            val parteDecimal = BigDecimal(Final.toString()).subtract(BigDecimal(Final.toInt())).multiply(
                BigDecimal(100)
            ).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
            Letras = clsConvertirDolares.convertirNumeroALetras(Final - parteDecimal / 100.0) + " CON $parteDecimal"
            Ruc = rucCliente
            val sdf = SimpleDateFormat("MMMM dd 'del' yyyy", Locale("es", "ES"))
            val cal = Calendar.getInstance()
            val resultados = tabla()
            val textoResultado = resultados.joinToString("\n")

            //Genera pdf segun la cantidad de meses
            // Obtener la hora actual
            val fechaActual = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
            fileName = "201"+"_"+"$fechaActual"+"_"+"$Cliente.pdf"
                    val filePath =
                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName"
                val document = Document()
                try {
                    val outputStream = FileOutputStream(filePath)
                    PdfWriter.getInstance(document, outputStream)
                    document.open()
                    val FechaActual = sdf.format(cal.time)

                    //Titulo
                    val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
                    val chunkTitle = Chunk("PAGARÉ A LA ORDEN \n\n", fontTitle)
                    val paragraphTitle = Paragraph(chunkTitle).apply {
                        alignment =
                            Element.ALIGN_CENTER // Establece la alineación al centro del párrafo
                    }
                    // Subtitulo
                    val fontSubTitle = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
                    val fontContent = Font(Font.FontFamily.HELVETICA, 10f)
                    val chunkContent = Chunk(
                        "Debo(mos) y pagaré(mos) solidariamente en esta ciudad o en el lugar que se me(nos) reconvenga, " +
                                "a la orden de COTZUL S.A., la suma  de $Letras/100 DOLARES DE LOS ESTADOS UNIDOS DE NORTEAMÉRICA " +
                                " ( US $Total). Cantidad recibida por mí de dicha compañía.\n" +
                                "La cantidad indicada me obligo a devolverla incondicionalmente, en dólares de los Estados Unidos de " +
                                "Norteamérica, más la tasa de interés activa efectiva máxima aprobada por el Banco Central del Ecuador" +
                                " para el segmento de crédito de comercial ordinario que rija al momento del pago y que se calculará " +
                                "desde la presente fecha; siendo de mi cuenta todos los impuestos y tasas que cause este pagaré así " +
                                "como los gastos judiciales y extrajudiciales, inclusive honorarios profesionales, que ocasione el " +
                                "cobro en caso de mora, siendo suficiente prueba para establecer tales gastos la mera aseveración del " +
                                "acreedor.\n" +
                                "Estados Unidos de Norteamérica, más la tasa activa efectiva máxima aprobada por el " +
                                "Banco Central del Ecuador para el segmento de crédito de consumo que rija al momento " +
                                "del pago y que se calculará desde la presente fecha; siendo de mi cuenta todos los  " +
                                "impuestos y tasas que cause este pagaré así como los gastos judiciales y " +
                                "extrajudiciales, inclusive honorarios profesionales, que ocasione el cobro en caso de mora, " +
                                "siendo suficiente prueba para establecer tales gastos la mera aseveración del acreedor.\n\n" +
                                " Me(nos) obligo(amos) a pagar la suma recibida mediante las cuotas de capital  e interés, que" +
                                " vencen sucesivamente en los siguientes plazos  y que constan a continuación:\n\n" +
                                "$textoResultado\n\n " +
                                "En caso de mora en el pago de cualquiera de las cantidades señaladas pagaré (mos) la tasa máxima " +
                                "de mora vigente a la fecha del vencimiento respectivo, calculada de acuerdo a Io dispuesto en las " +
                                "leyes y regulaciones pertinentes, sobre el valor del capital vencido y no pagado. Además, en caso " +
                                "de mora de una o varias cuotas de capital e intereses, o de una fracción de cuota, queda facultado " +
                                "a COTZUL S.A  para declarar de plazo vencido este pagaré, y exigir el pago anticipado de todo el " +
                                "capital, intereses moratorios y gastos de cobranza.Me(nos) obligo(amos) también a pagar todos los " +
                                "gastos judiciales y extrajudiciales, y honorarios profesionales que ocasione su cobro. El pago no " +
                                "podrá hacerse por partes ni aún por mis(nuestros) herederos o sucesores.\n\n" +
                                "Al fiel cumplimiento de lo estipulado en este Pagaré a la Orden,  me obligo con todos mis bienes " +
                                "presentes y futuros; y faculto al acreedor para disponer de valores, cheques y documentos al cobro " +
                                "como pago parcial o total de este Pagaré, así como cargar o acreditar en mi cuenta los saldos que " +
                                "quedaren hasta la completa cancelación del mismo y sus intereses.\n\n" +
                                "Declaro (amos) que los fondos objeto de esta transacción y los que honrarán su obligación, no " +
                                "serán destinados a/ni provienen de ninguna actividad ilegal o delictiva; ni consentiré (emos) que " +
                                "se efectúen depósitos o transferencias a mi (nuestra) cuenta provenientes de estas actividades." +
                                "Expresamente autorizo(amos) a COTZUL S.A.., realizar las verificaciones y debida diligencia " +
                                "correspondientes, e informar de manera inmediata y documentada a la autoridad competente en casos " +
                                "de investigación o cuando se detectare transacciones inusuales e injustificadas, por lo que no " +
                                "ejercerá ningún reclamo o acción judicial. Conozco (cernos) que puedo (podemos) hacer abonos " +
                                "parciales o cancelar anticipadamente la totalidad del crédito sin penalidad, por este concepto.\n\n" +
                                "Para el caso de controversias hago (cernos) una renuncia general de domicilio y quedo (amos) " +
                                "sometido(s) a los jueces o tribunales del lugar donde me (nos) encuentre (mos), o a los de la Ciudad" +
                                " donde suscribo (mos) este pagaré a la orden, o a los de la Ciudad de Quito, y al proceso ejecutivo. " +
                                "Y renuncio al derecho de interponer recurso de apelación, y aún el de hecho, de la sentencias que se " +
                                "expidieren en el juicio o juicios que en relación al presente documento hubieren lugar.\n\n" +
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
                                "Lugar y fecha de Suscripción: Guayaquil, $FechaActual\n\n\n" +
                                "Firma deudor                                                                        Firma Garante\n\n\n"
                                + "F)_________________________                                         F)_________________________\n\n" +
                                "$Cliente\n" +
                                "C.I./R.U.C. #$Ruc \n\n" +
                                "En la ciudad de Guayaquil, el ______________________ ante mi , " +
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
                                "_______________________________\n" +
                                "$Cliente\n                                                         " +
                                "R.U.C./C.I#$Ruc                                                       C.I.#\n\n" +
                                "                                                                   ______________________\n" +
                                "                                                                   Nombre Notario\n" +
                                "                                                                   C.I.", fontContent
                    )

                    val paragraphContent = Paragraph(chunkContent).apply {
                        alignment = Element.ALIGN_JUSTIFIED // Justificar el texto
                    }

                    document.add(paragraphTitle)
                    document.add(Paragraph(""))
                    document.add(Paragraph(""))
                    document.add(paragraphContent)
                    document.close()

                    return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                } catch (e: Exception) {
                    if (document.isOpen) {
                        document.close()
                    }
                    throw e
                }

        }
        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        private fun tabla(): List<String> {
            val resultados = mutableListOf<String>() // Lista para almacenar los resultados

            for (i in 1..Cuotas) {
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("MMMM dd 'del' yyyy", Locale("es", "ES"))
                cal.add(Calendar.DAY_OF_YEAR, Abono * i)
                val FechaActual = sdf.format(cal.time)
                val abonoMultiplicado = "${Abono * i} días ($FechaActual)"

                val resultado = "${abonoMultiplicado}  $$Final"
                resultados.add(resultado) // Agregar el resultado a la lista
            }

            return resultados // Devolver la lista de resultados
        }


    }
}
