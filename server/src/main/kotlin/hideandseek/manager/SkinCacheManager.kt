package kr.jmsoup.hideandseek.manager

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object SkinCacheManager {
    private val chunkCache = ConcurrentHashMap<UUID, MutableMap<Int, ByteArray>>()

    fun saveChunk(uuid: UUID, chunkIndex: Int, packet: ByteArray) {
        val map = chunkCache.computeIfAbsent(uuid) { ConcurrentHashMap() }
        map[chunkIndex] = packet
    }

    fun removeSkin(uuid: UUID) {
        chunkCache.remove(uuid)
    }

    fun getAllCachedChunks(): Collection<ByteArray> {
        return chunkCache.values.flatMap { it.values }
    }

    fun clearAllSkins() {
        chunkCache.clear()
    }
}