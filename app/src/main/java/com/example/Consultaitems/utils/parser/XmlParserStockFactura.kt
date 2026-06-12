package com.example.Consultaitems.utils.parser

data class StockItem(val codigo: String, val existencia: Int)

class XmlParserStock {
    companion object {
        fun buildAlertTextFromResponse(response: String): String {
            val items = extractStockItems(response)
            return if (items.isEmpty()) {
                "null"
            } else {
                items.joinToString("\n") { "• ${it.codigo} — Stock: ${it.existencia}" }
            }
        }

        private fun extractStockItems(text: String): List<StockItem> {
            val trimmed = text.trim()
            return if (trimmed.startsWith("<")) {
                parseXmlStock(trimmed)
            } else {
                parseTsvStock(trimmed)
            }
        }

        private fun parseTsvStock(text: String): List<StockItem> {
            val lines = text.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()

            if (lines.isEmpty()) return emptyList()
            val startIndex = if (lines.first().contains("it_codigo", ignoreCase = true)) 1 else 0

            val out = mutableListOf<StockItem>()
            for (i in startIndex until lines.size) {
                val line = lines[i]
                val parts = if (line.contains('\t')) line.split('\t')
                else line.split(Regex("\\s{2,}|\\s"))
                if (parts.size >= 2) {
                    val codigo = parts[0].trim()
                    val ex = parts[1].trim().toIntOrNull() ?: 0
                    if (codigo.isNotEmpty()) out += StockItem(codigo, ex)
                }
            }
            return out
        }

        private fun parseXmlStock(xml: String): List<StockItem> {
            val out = mutableListOf<StockItem>()
            val parser = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser().apply {
                setInput(java.io.StringReader(xml))
                setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            }

            var codigo: String? = null
            var existencia: Int? = null

            var event = parser.eventType
            while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                when (event) {
                    org.xmlpull.v1.XmlPullParser.START_TAG -> {
                        val name = parser.name.substringAfter(':').lowercase()
                        when (name) {
                            "table" -> {
                                codigo = null; existencia = null
                            }

                            "it_codigo" -> codigo = parser.nextText()?.trim()
                            "it_existencia" -> existencia = parser.nextText()?.trim()?.toIntOrNull()
                        }
                    }

                    org.xmlpull.v1.XmlPullParser.END_TAG -> {
                        val name = parser.name.substringAfter(':').lowercase()
                        if (name == "table") {
                            if (!codigo.isNullOrEmpty()) {
                                out += StockItem(codigo!!, existencia ?: 0)
                            }
                            codigo = null
                            existencia = null
                        }
                    }
                }
                event = parser.next()
            }

            // por si el XML no usa <Table> y quedó una fila pendiente:
            if (!codigo.isNullOrEmpty()) out += StockItem(codigo!!, existencia ?: 0)

            return out
        }
    }
}
