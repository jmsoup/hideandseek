package kr.jmsoup.client.network;

import com.mojang.blaze3d.platform.NativeImage;
import kr.jmsoup.client.core.PaintCanvas;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ClientSkinSender {
    public static void broadcastSkin() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        NativeImage img = PaintCanvas.getInstance().getImage();
        if (img == null || img.isClosed()) return;

        int[] abgrPixels = img.getPixelsABGR();
        int width = img.getWidth();
        int height = img.getHeight();

        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int i = 0; i < abgrPixels.length; i++) {
                    int p = abgrPixels[i];
                    int a = (p >> 24) & 0xFF;
                    int b = (p >> 16) & 0xFF;
                    int g = (p >> 8) & 0xFF;
                    int r = p & 0xFF;
                    abgrPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
                }
                bufferedImage.setRGB(0, 0, width, height, abgrPixels, 0, width);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                byte[] pngBytes = baos.toByteArray();

                int CHUNK_SIZE = 30000;
                int totalChunks = (int) Math.ceil((double) pngBytes.length / CHUNK_SIZE);

                client.execute(() -> {
                    for (int i = 0; i < totalChunks; i++) {
                        int start = i * CHUNK_SIZE;
                        int length = Math.min(CHUNK_SIZE, pngBytes.length - start);

                        ByteBuffer chunkBuf = ByteBuffer.allocate(24 + length);
                        chunkBuf.putLong(client.player.getUUID().getMostSignificantBits());
                        chunkBuf.putLong(client.player.getUUID().getLeastSignificantBits());
                        chunkBuf.putInt(totalChunks); // 조각 총 갯수
                        chunkBuf.putInt(i); // 조각 인덱스
                        chunkBuf.put(pngBytes, start, length); // 이미지 데이터

                        byte[] chunkPayload = chunkBuf.array();

                        ModNetworking.sendToServer(ModNetworking.UPDATE_SKIN, chunkPayload);

                        CustomSkinManager.receiveSkinData(chunkPayload);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}