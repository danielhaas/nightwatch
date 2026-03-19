package com.nightwatch.emergency

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple embedded HTTP mock server for development/testing.
 * Responds to POST /api/emergency with a mock success response.
 */
class EmergencyMockServer(private val port: Int = 8080) {

    private var serverSocket: ServerSocket? = null
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null

    fun start() {
        if (running.get()) return
        running.set(true)

        thread = Thread {
            try {
                serverSocket = ServerSocket(port)
                while (running.get()) {
                    try {
                        val socket = serverSocket?.accept() ?: break
                        Thread {
                            handleConnection(socket)
                        }.start()
                    } catch (e: Exception) {
                        if (!running.get()) break
                    }
                }
            } catch (e: Exception) {
                // Port already in use or other error
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        running.set(false)
        try {
            serverSocket?.close()
        } catch (e: Exception) { /* ignore */ }
        thread?.interrupt()
    }

    private fun handleConnection(socket: java.net.Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            // Read request line
            val requestLine = reader.readLine() ?: return

            // Read headers
            var contentLength = 0
            var line = reader.readLine()
            while (line != null && line.isNotEmpty()) {
                if (line.lowercase().startsWith("content-length:")) {
                    contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                }
                line = reader.readLine()
            }

            // Read body
            val body = if (contentLength > 0) {
                val chars = CharArray(contentLength)
                reader.read(chars, 0, contentLength)
                String(chars)
            } else ""

            // Generate response
            val responseJson = if (requestLine.contains("POST") && requestLine.contains("/api/emergency")) {
                JSONObject().apply {
                    put("status", "received")
                    put("emergency_id", "EM-${UUID.randomUUID().toString().take(5).uppercase()}")
                    put("message", "Notruf empfangen. Hilfe ist unterwegs.")
                }
            } else {
                JSONObject().apply {
                    put("error", "not_found")
                    put("message", "Endpoint not found")
                }
            }

            val responseBody = responseJson.toString()
            val statusCode = if (requestLine.contains("/api/emergency")) 200 else 404
            val statusText = if (statusCode == 200) "OK" else "Not Found"

            writer.print("HTTP/1.1 $statusCode $statusText\r\n")
            writer.print("Content-Type: application/json\r\n")
            writer.print("Content-Length: ${responseBody.length}\r\n")
            writer.print("\r\n")
            writer.print(responseBody)
            writer.flush()

            socket.close()
        } catch (e: Exception) {
            try { socket.close() } catch (ex: Exception) { /* ignore */ }
        }
    }
}
