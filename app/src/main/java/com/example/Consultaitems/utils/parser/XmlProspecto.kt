package com.example.Consultaitems.utils.parser

import android.content.Context
import com.example.Consultaitems.ui.adapters.AdaptadorProspectos

/**
 * Reconstruido desde el APK con JADX.
 * Fuente original detectada: xmlProspecto.kt
 */
class XmlProspecto(private val context: Context) {

    private fun esc(valor: String?): String {
        return valor
            ?.replace("&", "&amp;")
            ?.replace("<", "&lt;")
            ?.replace(">", "&gt;")
            ?.replace("\"", "&quot;")
            ?.replace("'", "&apos;")
            ?: ""
    }

    fun fnObtenerXmlProspecto(
        cliente: AdaptadorProspectos.Prospectos,
        usuario: String,
        tipoId: String,
        identificacion: String,
        tipoPersona: String,
        genero: String,
        epCodigo: String,
        co_codigo: Int,
        opcion: Int
    ): String {
        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"${esc(usuario)}\" ")
        xml.append("c1=\"$tipoId\" ")
        xml.append("c2=\"${esc(identificacion)}\" ")
        xml.append("c3=\"${esc(cliente.gt_codigo)}\" ")
        xml.append(">")
        xml.append("<detalle ")
        xml.append("d0=\"${esc(cliente.Codigo)}\" ")
        xml.append("d1=\"${esc(cliente.Nombre1)}\" ")
        xml.append("d2=\"${esc(cliente.Nombre2)}\" ")
        xml.append("d3=\"${esc(cliente.Apellido1)}\" ")
        xml.append("d4=\"${esc(cliente.Apellido2)}\" ")
        xml.append("d5=\"${esc(cliente.NombreComercial)}\" ")
        xml.append("d6=\"${esc(cliente.RazonSocial)}\" ")
        xml.append("d7=\"${esc(cliente.Direccion)}\" ")
        xml.append("d8=\"${esc(cliente.Fono)}\" ")
        xml.append("d9=\"$co_codigo\" ")
        xml.append("d10=\"${esc(cliente.CodCiudad)}\" ")
        xml.append("d11=\"${esc(cliente.Correo)}\" ")
        xml.append("d12=\"$epCodigo\" ")
        xml.append("d13=\"$tipoPersona\" ")
        xml.append("d14=\"$genero\" ")
        xml.append("d15=\"0\" ")
        xml.append("d16=\"${esc(cliente.gc_codigopostal)}\" ")
        xml.append("d17=\"${esc(cliente.gc_sector)}\" ")
        xml.append("d18=\"${esc(cliente.gc_puntoreferencia)}\" ")
        xml.append("d19=\"${esc(cliente.gc_googlemap)}\" ")
        xml.append("></detalle>")
        xml.append("</c>")
        return "'${xml}',${cliente.Codigo},'$usuario','$identificacion',$opcion"
    }

    fun fnObtenerXmLLamada(
        fecha: String,
        usuario: String,
        linea: String,
        observacion: String,
        catalogo: String,
        epCodigo: String,
        cliente: Int,
        redsocial: Int
    ): String {
        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"${esc(fecha)}\" ")
        xml.append("c1=\"${esc(usuario)}\" ")
        xml.append("c2=\"0\" ")
        xml.append(">")
        xml.append("<detalle ")
        xml.append("d0=\"0\" ")
        xml.append("d1=\"${esc(epCodigo)}\" ")
        xml.append("d2=\"$cliente\" ")
        xml.append("d3=\"${esc(linea)}\" ")
        xml.append("d4=\"${esc(observacion)}\" ")
        xml.append("d5=\"0\" ")
        xml.append("d6=\"0\" ")
        xml.append("d7=\"0\" ")
        xml.append("d8=\"0\" ")
        xml.append("d9=\"0\" ")
        xml.append("d10=\"0\" ")
        xml.append("d11=\"0\" ")
        xml.append("d12=\"0\" ")
        xml.append("d13=\"1\" ")
        xml.append("d14=\"$redsocial\" ")
        xml.append("d15=\"${esc(catalogo)}\" ")
        xml.append("></detalle>")
        xml.append("</c>")
        return "'${xml}','1',$epCodigo,'',0,'',$usuario,I"
    }

    fun fnObtenerXmPublico(
        cliente: AdaptadorProspectos.Prospectos,
        usuario: String,
        epCodigo: String,
        cl_codigo: Int,
        identificacion: String,
        gc_codigo: Int
    ): String {
        val nombreCompleto = listOf(
            cliente.Apellido1,
            cliente.Apellido2,
            cliente.Nombre1,
            cliente.Nombre2
        ).filter { it.isNotBlank() }.joinToString(" ")

        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"0\" ")
        xml.append("c1=\"${esc(epCodigo)}\" ")
        xml.append("c2=\"${esc(usuario)}\" ")
        xml.append(">")
        xml.append("<detalle ")
        xml.append("d0=\"$cl_codigo\" ")
        xml.append("d1=\"$nombreCompleto\" ")
        xml.append("d2=\"$identificacion\" ")
        xml.append("d3=\"0\" ")
        xml.append("d4=\"0\" ")
        xml.append("d5=\"0\" ")
        xml.append("d6=\"$gc_codigo\" ")
        xml.append("></detalle>")
        xml.append("</c>")
        return "'${xml}','4',$epCodigo,'',0,'',$usuario,I"
    }

    fun fnObtenerGestionVentaPublico(
        clientes: List<ClienteProspecto>,
        documento: String,
        usuario: String,
        epCodigo: String,
        opcion: String
    ): String {
        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"$documento\" ")
        xml.append("c1=\"${esc(epCodigo)}\" ")
        xml.append("c2=\"${esc(usuario)}\" ")
        xml.append(">")

        clientes.forEach { cliente ->
            xml.append("<detalle ")
            xml.append("d0=\"1774\" ")
            xml.append("d1=\"${cliente.nombre}\" ")
            xml.append("d2=\"9999999999999\" ")
            xml.append("d3=\"${cliente.proceso}\" ")
            xml.append("d4=\"${cliente.secuencia}\" ")
            xml.append("d5=\"${cliente.gv_interno}\" ")
            xml.append("d6=\"${cliente.codigoP}\" ")
            xml.append("></detalle>")
        }

        xml.append("</c>")
        return "'${xml}','4',$epCodigo,0,$documento,'',$usuario,$opcion"
    }

    fun fnObtenerXmGestionVenta(
        opcion: Int,
        fecha: String,
        usuario: String,
        documento: String,
        epCodigo: String,
        listaCliente: List<ClienteProspecto>,
        opcionMenu: String
    ): String {
        val xml = StringBuilder()
        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        xml.append("<c ")
        xml.append("c0=\"${esc(fecha)}\" ")
        xml.append("c1=\"${esc(usuario)}\" ")
        xml.append("c2=\"$documento\" ")
        xml.append(">")

        listaCliente.forEach { item ->
            xml.append("<detalle ")
            xml.append("d0=\"${item.proceso}\" ")
            xml.append("d1=\"$epCodigo\" ")
            xml.append("d2=\"${item.cl_codigo}\" ")
            xml.append("d3=\"${esc(item.linea)}\" ")
            xml.append("d4=\"${esc(item.observacion)}\" ")
            xml.append("d5=\"51\" ")
            xml.append("d6=\"0\" ")
            xml.append("d7=\"0\" ")
            xml.append("d8=\"0\" ")
            xml.append("d9=\"0\" ")
            xml.append("d10=\"0\" ")
            xml.append("d11=\"0\" ")
            xml.append("d12=\"0\" ")
            xml.append("d13=\"${item.secuencia}\" ")
            xml.append("d14=\"${item.redSocial}\" ")
            xml.append("d15=\"${esc(item.catalogo)}\" ")
            xml.append("></detalle>")
        }

        xml.append("</c>")
        return "'${xml}','$opcion',$epCodigo,'',$documento,'',$usuario,$opcionMenu"
    }
}
data class ClienteProspecto(
    val codigoP: Int,
    val cl_codigo: Int,
    val secuencia: Int,
    val bodega: Int,
    val identificacion: String,
    val nombre: String,
    val linea: String,
    val observacion: String,
    val redSocial: Int,
    val catalogo: String,
    val gv_interno: Int,
    val proceso: Int,
    val fecha: String
)
