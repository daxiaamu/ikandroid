package com.ikan.app.cast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

data class DlnaDevice(val name: String, val location: String, val controlUrl: String)

class DlnaController {
    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun discover(timeoutMs: Long = 3_500): List<DlnaDevice> = withContext(Dispatchers.IO) {
        val search = (
            "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n\r\n"
            ).toByteArray(StandardCharsets.UTF_8)
        val locations = linkedSetOf<String>()
        DatagramSocket().use { socket ->
            socket.soTimeout = 350
            val target = InetAddress.getByName("239.255.255.250")
            repeat(2) { socket.send(DatagramPacket(search, search.size, target, 1900)) }
            val deadline = System.currentTimeMillis() + timeoutMs
            val buffer = ByteArray(8_192)
            while (System.currentTimeMillis() < deadline) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val response = String(packet.data, 0, packet.length, StandardCharsets.UTF_8)
                    response.lineSequence().firstOrNull { it.startsWith("location:", true) }
                        ?.substringAfter(':')?.trim()?.let(locations::add)
                } catch (_: java.net.SocketTimeoutException) {
                    // Keep listening until the overall deadline.
                }
            }
        }
        locations.mapNotNull { readDevice(it) }.distinctBy { it.controlUrl }
    }

    suspend fun play(device: DlnaDevice, mediaUrl: String, title: String) = withContext(Dispatchers.IO) {
        soap(
            device.controlUrl,
            "SetAVTransportURI",
            "<InstanceID>0</InstanceID>" +
                "<CurrentURI>${escape(mediaUrl)}</CurrentURI>" +
                "<CurrentURIMetaData>${escape(didl(title, mediaUrl))}</CurrentURIMetaData>",
        )
        soap(device.controlUrl, "Play", "<InstanceID>0</InstanceID><Speed>1</Speed>")
    }

    private fun readDevice(location: String): DlnaDevice? = runCatching {
        val xml = request(Request.Builder().url(location).get().build())
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray()))
        val name = document.getElementsByTagNameNS("*", "friendlyName").item(0)?.textContent?.trim()
            .orEmpty().ifBlank { URI(location).host }
        val services = document.getElementsByTagNameNS("*", "service")
        var control = ""
        for (index in 0 until services.length) {
            val service = services.item(index) as? Element ?: continue
            val type = service.getElementsByTagNameNS("*", "serviceType").item(0)?.textContent.orEmpty()
            if (type.contains("AVTransport")) {
                control = service.getElementsByTagNameNS("*", "controlURL").item(0)?.textContent.orEmpty()
                break
            }
        }
        if (control.isBlank()) null else DlnaDevice(name, location, URI(location).resolve(control).toString())
    }.getOrNull()

    private fun soap(url: String, action: String, arguments: String) {
        val body = """<?xml version="1.0" encoding="utf-8"?>
            <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
              <s:Body><u:$action xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">$arguments</u:$action></s:Body>
            </s:Envelope>""".trimIndent()
        val request = Request.Builder().url(url)
            .header("SOAPACTION", "\"urn:schemas-upnp-org:service:AVTransport:1#$action\"")
            .post(body.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            .build()
        request(request)
    }

    private fun request(request: Request): String = http.newCall(request).execute().use { response ->
        if (!response.isSuccessful) error("DLNA 设备返回 ${response.code}")
        response.body.string()
    }

    private fun didl(title: String, url: String) = """<DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/"><item id="0" parentID="0" restricted="1"><dc:title>${escape(title)}</dc:title><upnp:class>object.item.videoItem</upnp:class><res protocolInfo="http-get:*:application/vnd.apple.mpegurl:*">${escape(url)}</res></item></DIDL-Lite>"""
    private fun escape(value: String) = value.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}
