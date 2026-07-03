package kr.jmsoup.client.core;

import com.mojang.blaze3d.platform.NativeImage;
import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

public class PaintCanvas {
    public static final int CANVAS_SIZE = 1024;
    public static final int HD_SCALE = CANVAS_SIZE / 64;
    public static final Identifier CANVAS_ID = Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, "hd_canvas");

    private static PaintCanvas instance;
    public static boolean isPaintingMode = false;
    public static boolean showNameplate = true;

    private NativeImage canvasImage;
    private DynamicTexture dynamicTexture;

    public static void setPaintingMode(boolean paintingMode) {
        isPaintingMode = paintingMode;
        getInstance().resetAllData();
    }

    public static void setShowNameplate(boolean nameplateMode) {
        showNameplate = nameplateMode;
    }

    public void init() {
        if (dynamicTexture == null) {
            canvasImage = new NativeImage(CANVAS_SIZE, CANVAS_SIZE, false);
            resetAllData();

            dynamicTexture = new DynamicTexture(() -> "paint_canvas", canvasImage);
            ClientAsset.ResourceTexture dummy = new ClientAsset.ResourceTexture(CANVAS_ID);
            Minecraft.getInstance().getTextureManager().register(dummy.texturePath(), dynamicTexture);
        }
    }

    public void resetAllData() {
        if (canvasImage != null) {
            canvasImage.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE, 0xFFFFFFFF);
            canvasImage.fillRect(32 * HD_SCALE, 0, 32 * HD_SCALE, 16 * HD_SCALE, 0x00000000);
            canvasImage.fillRect(16 * HD_SCALE, 32 * HD_SCALE, 24 * HD_SCALE, 16 * HD_SCALE, 0x00000000);
            canvasImage.fillRect(40 * HD_SCALE, 32 * HD_SCALE, 16 * HD_SCALE, 16 * HD_SCALE, 0x00000000);
            canvasImage.fillRect(48 * HD_SCALE, 48 * HD_SCALE, 16 * HD_SCALE, 16 * HD_SCALE, 0x00000000);
            canvasImage.fillRect(0, 32 * HD_SCALE, 16 * HD_SCALE, 16 * HD_SCALE, 0x00000000);
            canvasImage.fillRect(0, 48 * HD_SCALE, 16 * HD_SCALE, 16 * HD_SCALE, 0x00000000);

            if (dynamicTexture != null) dynamicTexture.upload();
        }
        PaintTools.resetStrokeMap();
        PaintHistory.clearHistory();
    }

    public NativeImage getImage() { return canvasImage; }
    public void upload() { if (dynamicTexture != null) dynamicTexture.upload(); }

    public static PaintCanvas getInstance() {
        if (instance == null) {
            instance = new PaintCanvas();
        }
        return instance;
    }
}