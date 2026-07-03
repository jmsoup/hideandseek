package kr.jmsoup.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import kr.jmsoup.HideAndSeekClient;
import kr.jmsoup.client.core.PaintCanvas;
import kr.jmsoup.client.core.PaintHistory;
import kr.jmsoup.client.core.PaintTools;
import kr.jmsoup.client.gui.widget.ColorPaletteWidget;
import kr.jmsoup.client.network.ClientSkinSender;
import kr.jmsoup.client.util.ColorUtils;
import kr.jmsoup.client.util.KeyMappings;
import kr.jmsoup.client.util.SkinRaycaster;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class PaintScreen extends Screen {
    private static final double[] COS_CACHE = new double[72];
    private static final double[] SIN_CACHE = new double[72];

    static {
        for (int i = 0; i < 72; i++) {
            double rad = Math.toRadians(i * 5);
            COS_CACHE[i] = Math.cos(rad);
            SIN_CACHE[i] = Math.sin(rad);
        }
    }

    public static int currentBrushSize = 16;

    private final AbstractClientPlayer player;
    private final PaintCanvas paintCanvas;

    private ColorPaletteWidget paletteWidget;
    private float partialTick = 0f;
    private boolean isEyeDropperDown = false;
    private boolean isStrokeSaved = false;
    private boolean isCanvasDirty = false;

    public PaintScreen(AbstractClientPlayer player, PaintCanvas canvas) {
        super(Component.literal("Paint Mode"));
        this.player = player;
        this.paintCanvas = canvas;
    }

    @Override
    protected void init() {
        super.init();

        if (this.paletteWidget == null) {
            this.paletteWidget = new ColorPaletteWidget(this.font);
        } else {
            this.paletteWidget.clampPosition();
            this.paletteWidget.updateLayout();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.partialTick = partialTick;

        paletteWidget.render(graphics, mouseX, mouseY, this.font);

        if (!isEyeDropperDown) {
            drawBrushCursor(graphics, mouseX, mouseY);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBrushCursor(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int radius = Math.max(0, currentBrushSize / 4);
        int drawX = (int) Math.round(mouseX);
        int drawY = (int) Math.round(mouseY);

        for (int i = 0; i < 72; i++) {
            int px = (int) (drawX + COS_CACHE[i] * radius);
            int py = (int) (drawY + SIN_CACHE[i] * radius);
            graphics.fill(px, py, px + 1, py + 1, 0xFFFFFFFF);
        }
    }

    private void pickColor(double mouseX, double mouseY) {
        SkinRaycaster.HitResult hit = SkinRaycaster.cast(this.player, mouseX, mouseY, this.partialTick);

        if (hit != null) {
            int hitU = (int) (hit.u * PaintCanvas.HD_SCALE);
            int hitV = (int) (hit.v * PaintCanvas.HD_SCALE);

            if (hitU >= 0 && hitU < PaintCanvas.CANVAS_SIZE && hitV >= 0 && hitV < PaintCanvas.CANVAS_SIZE) {
                NativeImage img = paintCanvas.getImage();
                if (img != null) {
                    int color = img.getPixel(hitU, hitV);
                    float[] rgba = ColorUtils.getRGBA(color);
                    paletteWidget.setRGBA(rgba[0], rgba[1], rgba[2], rgba[3]);
                    return;
                }
            }
        }

        pickColorFromScreen(mouseX, mouseY);
    }

    private void pickColorFromScreen(double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        int physW = client.getWindow().getScreenWidth();
        int physH = client.getWindow().getScreenHeight();
        int guiW = client.getWindow().getGuiScaledWidth();
        int guiH = client.getWindow().getGuiScaledHeight();

        int physX = (int) (mouseX * ((double) physW / guiW));
        int physY = (int) (physH - (mouseY * ((double) physH / guiH)));

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            GL11.glReadPixels(physX, physY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            float[] rgba = ColorUtils.getRGBA(buffer);
            paletteWidget.setRGBA(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        if (button == 1) return true;

        if (button == 0) {
            isStrokeSaved = false;
            double mouseX = event.x();
            double mouseY = event.y();

            if (isEyeDropperDown) {
                pickColor(mouseX, mouseY);
                return true;
            }

            if (paletteWidget.mouseClicked(event, doubleClick)) {
                return true;
            }

            SkinRaycaster.HitResult hit = SkinRaycaster.cast(this.player, mouseX, mouseY, this.partialTick);
            if (hit != null) {
                if (executePaintAction(mouseX, mouseY, event.hasControlDown())) {
                    isCanvasDirty = true;
                    paintCanvas.upload();
                }
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        int button = event.button();

        if (button == 1) {
            HideAndSeekClient.cameraYaw += (float) dx * 0.75f;
            HideAndSeekClient.cameraPitch = Mth.clamp(HideAndSeekClient.cameraPitch + (float) dy * 0.75f, -90.0F, 90.0F);
            return true;
        }

        if (button == 0) {

            double mouseX = event.x();
            double mouseY = event.y();

            if (isEyeDropperDown) {
                pickColor(mouseX, mouseY);
                return true;
            }

            if (paletteWidget.isInteracting()) {
                paletteWidget.mouseDragged(event, dx, dy);
                return true;
            }

            if(event.hasControlDown()){
                return true;
            }

            int steps = Math.max(1, (int) Math.ceil(Math.hypot(dx, dy) * 2.0));
            double startX = mouseX - dx;
            double startY = mouseY - dy;

            boolean changed = false;
            for (int i = 1; i <= steps; i++) {
                double t = (double) i / steps;
                if(executePaintAction(startX + dx * t, startY + dy * t, false)){
                    changed = true;
                }
            }

            if(changed){
                isCanvasDirty = true;
                paintCanvas.upload();
            }
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        paletteWidget.mouseReleased();
        if (event.button() == 0 && isCanvasDirty) {
            ClientSkinSender.broadcastSkin();
            isCanvasDirty = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event){
        if (paletteWidget.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (paletteWidget.keyPressed(event)) {
            return true;
        }

        int modifiers = event.modifiers();
        boolean isCtrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean isShift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (KeyMappings.EYE_DROPPER.matches(event)) {
            isEyeDropperDown = true;
            return true;
        }

        if (KeyMappings.UNDO.matches(event)) {
            if (isCtrl && isShift) {
                if (PaintHistory.redo(paintCanvas)) {
                    ClientSkinSender.broadcastSkin();
                }
                return true;
            } else if (isCtrl) {
                if (PaintHistory.undo(paintCanvas)) {
                    ClientSkinSender.broadcastSkin();
                }
                return true;
            }
        }

        if (KeyMappings.BRUSH_INCREASE.matches(event)) {
            currentBrushSize = Math.min(currentBrushSize + 4, 128); return true;
        } else if (KeyMappings.BRUSH_DECREASE.matches(event)) {
            currentBrushSize = Math.max(currentBrushSize - 4, 4); return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (KeyMappings.EYE_DROPPER.matches(event)) {
            isEyeDropperDown = false;
            return true;
        }
        return super.keyReleased(event);
    }

    private boolean executePaintAction(double mouseX, double mouseY, boolean isFillMode) {
        SkinRaycaster.HitResult hit = SkinRaycaster.cast(this.player, mouseX, mouseY, this.partialTick);

        if (hit != null) {
            int hitU = (int) (hit.u * PaintCanvas.HD_SCALE);
            int hitV = (int) (hit.v * PaintCanvas.HD_SCALE);

            if (hitU >= 0 && hitU < PaintCanvas.CANVAS_SIZE && hitV >= 0 && hitV < PaintCanvas.CANVAS_SIZE) {
                if (!isStrokeSaved) {
                    PaintHistory.saveState(paintCanvas);
                    PaintTools.startNewStroke();
                    isStrokeSaved = true;
                }

                int color = ColorUtils.getABGR(paletteWidget.red, paletteWidget.green, paletteWidget.blue, paletteWidget.alpha);

                if (isFillMode) {
                    PaintTools.fillPixel(paintCanvas, hitU, hitV, color);
                } else {
                    PaintTools.paintPixel(paintCanvas, hitU, hitV, color, currentBrushSize);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

    }

    @Override
    public boolean isPauseScreen() { return false; }
}