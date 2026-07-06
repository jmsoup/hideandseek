package kr.jmsoup.hideandseek.network

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object NetworkUtils {
    fun readString(buf: ByteBuffer): String {
        val length = readVarInt(buf)
        val bytes = ByteArray(length)
        buf.get(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun readVarInt(buf: ByteBuffer): Int {
        var numRead = 0
        var result = 0
        var read: Byte
        do {
            read = buf.get()
            val value = (read.toInt() and 0b01111111)
            result = result or (value shl (7 * numRead))
            numRead++
            if (numRead > 5) {
                throw RuntimeException("VarInt가 너무 큽니다!")
            }
        } while ((read.toInt() and 0b10000000) != 0)
        return result
    }

    fun writeString(value: String, buffer: ByteBuffer) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(bytes.size, buffer)
        buffer.put(bytes)
    }

    fun writeVarInt(value: Int, buffer: ByteBuffer) {
        var v = value
        while (true) {
            if ((v and 0x7F.inv()) == 0) {
                buffer.put(v.toByte())
                return
            }
            buffer.put(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
    }

    fun createPacket(action: String, data: ByteArray): ByteArray {
        val actionBytes = action.toByteArray(StandardCharsets.UTF_8)
        val buffer = ByteBuffer.allocate(actionBytes.size + data.size + 10)

        writeString(action, buffer)
        writeVarInt(data.size, buffer)
        buffer.put(data)

        buffer.flip()
        val result = ByteArray(buffer.remaining())
        buffer.get(result)
        return result
    }
}