package kr.jmsoup.client.core;

import kr.jmsoup.client.gui.PaintScreen;
import net.minecraft.client.Minecraft;

public class PaintManager {
    public static boolean isPaintingMode = false;
    public static boolean showNameplate = true;

    public static void setPaintingMode(boolean paintingMode) {
        isPaintingMode = paintingMode;

        if (paintingMode) {
            PaintCanvas.getInstance().resetAllData();
        } else {
            Minecraft client = Minecraft.getInstance();
            if (client.screen instanceof PaintScreen) {
                client.setScreen(null);
            }
        }
    }

    public static void setShowNameplate(boolean nameplateMode) {
        showNameplate = nameplateMode;
    }
}