package com.example.Consultaitems.utils.parser

import android.content.Context
import com.example.Consultaitems.ui.adapters.AdaptadorActividad

class XmlGestionProyecto(private val context: Context) {

    private fun esc(valor: String?): String {
        return valor.orEmpty()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun fnObtenerXmlActividades(
        opcion: Int,
        usuario: String,
        ep_codigo: String,
        listaActividades: List<AdaptadorActividad.Actividades>
    ): String {
        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"").append(listaActividades.firstOrNull()?.emCodigo ?: 0).append("\" ")
        xml.append("c1=\"").append(ep_codigo).append("\" ")
        xml.append("c2=\"").append(usuario).append("\" ")
        xml.append("c3=\"2\" ")
        xml.append(">")

        listaActividades.forEach { item ->
            xml.append("<detalle ")
            xml.append("d0=\"").append(item.dpCodigo).append("\" ")
            xml.append("d1=\"").append(esc(item.gpTarea)).append("\" ")
            xml.append("d2=\"").append(esc(item.gpObservacion)).append("\" ")
            xml.append("d3=\"").append(esc(item.gpRecurso)).append("\" ")
            xml.append("d4=\"").append(item.gpDuracion).append("\" ")
            xml.append("d5=\"").append(item.gpAvance).append("\" ")
            xml.append("d6=\"").append(esc(item.gpFechaInicial)).append("\" ")
            xml.append("d7=\"").append(esc(item.gpFechaFinal)).append("\" ")
            xml.append("d8=\"").append(item.proceso).append("\" ")
            xml.append("d9=\"").append(item.gpCodigo).append("\" ")
            xml.append("d10=\"").append(esc(item.gpTareaP)).append("\" ")
            xml.append("d11=\"").append(esc(item.gpFechaTrn)).append("\" ")
            xml.append("d12=\"").append(esc(item.gpObservacion)).append("\" ")
            xml.append("d13=\"").append(item.ppCodigo).append("\" ")
            xml.append("></detalle>")
        }

        xml.append("</c>")
        return "'$xml',$opcion"
    }
}
