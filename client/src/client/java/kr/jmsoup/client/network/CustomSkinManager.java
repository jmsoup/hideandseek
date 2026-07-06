package kr.jmsoup.client.network;

import com.mojang.blaze3d.platform.NativeImage;
import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomSkinManager {
    private static final Map<UUID, Identifier> networkSkins = new HashMap<>();
    private static final Map<UUID, ChunkAssembler> assemblers = new HashMap<>();

    private static class ChunkAssembler {
        int totalChunks;
        byte[][] chunks;
        int receivedCount = 0;

        ChunkAssembler(int total) {
            this.totalChunks = total;
            this.chunks = new byte[total][];
        }

        boolean addChunk(int index, byte[] data) {
            if (chunks[index] == null) {
                chunks[index] = data;
                receivedCount++;
            }
            return receivedCount == totalChunks;
        }

        byte[] assemble() {
            int totalLength = 0;
            for (byte[] c : chunks) totalLength += c.length;
            ByteBuffer buf = ByteBuffer.allocate(totalLength);
            for (byte[] c : chunks) buf.put(c);
            return buf.array();
        }
    }

    public static void receiveSkinData(byte[] payloadData) {
        ByteBuffer buffer = ByteBuffer.wrap(payloadData);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);

        int totalChunks = buffer.getInt();
        int chunkIndex = buffer.getInt();

        byte[] chunkData = new byte[buffer.remaining()];
        buffer.get(chunkData);

        ChunkAssembler assembler = assemblers.computeIfAbsent(uuid, k -> new ChunkAssembler(totalChunks));

        if (assembler.addChunk(chunkIndex, chunkData)) {
            byte[] fullPngBytes = assembler.assemble();
            assemblers.remove(uuid);

            Minecraft.getInstance().execute(() -> {
                try {
                    ByteBuffer imgBuffer = MemoryUtil.memAlloc(fullPngBytes.length);
                    imgBuffer.put(fullPngBytes).flip();

                    NativeImage img = NativeImage.read(imgBuffer);
                    MemoryUtil.memFree(imgBuffer);

                    String longPath = "textures/skin_" + uuid + ".png";
                    Identifier registerId = Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, longPath);
                    Identifier returnId = Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, "skin_" + uuid);

                    DynamicTexture dynamicTexture = new DynamicTexture(() -> longPath, img);
                    Minecraft.getInstance().getTextureManager().register(registerId, dynamicTexture);

                    networkSkins.put(uuid, returnId);

                } catch (Exception e) {
                    HideAndSeekClient.LOGGER.error("텍스쳐 에러", e);
                }
            });
        }
    }

    public static Identifier getCustomSkin(UUID uuid) {
        return networkSkins.get(uuid);
    }

    public static void clearAllSkins() {
        networkSkins.clear();
        assemblers.clear();
    }
}