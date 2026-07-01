package kr.jmsoup.client.gui.widget;

import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ColorPaletteWidget {
    private static final Identifier COLOR_WHEEL = Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, "textures/gui/color_wheel.png");

    private static final int MIN_W = 150, MIN_H = 96;
    private static final int MAX_W = 200, MAX_H = 140;
    public static int x = 20, y = 20, width = 150, height = 96;

    public float red = 1f, green = 1f, blue = 1f, alpha = 1f;
    private float hue = 0f, saturation = 0f, brightness = 1f;

    private int wheelX, wheelY, wheelSize;
    private int rightAreaX, rightAreaWidth;
    private int previewY, previewHeight;
    private int sliderStartY, sliderSpacing;

    private enum State { NONE, DRAGGING_PANEL, RESIZING_PANEL, DRAGGING_WHEEL, SLIDER }
    private State currentState = State.NONE;
    private int draggingSlider = -1;
    private double dragOffsetX, dragOffsetY;

    private final EditBox hexInput;
    private boolean isSyncingHex = false;

    public ColorPaletteWidget(Font font) {
        this.hexInput = new EditBox(font, 0, 0, 45, 10, net.minecraft.network.chat.Component.literal("Hex"));
        this.hexInput.setMaxLength(6);
        this.hexInput.setResponder(this::onHexInputChanged);
        
        clampPosition();
        updateLayout();
        syncHexInput();
    }

    public void clampPosition() {
        Minecraft client = Minecraft.getInstance();
        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();

        width = Mth.clamp(width, MIN_W, MAX_W);
        height = Mth.clamp(height, MIN_H, MAX_H);
        x = Mth.clamp(x, 0, screenW - width);
        y = Mth.clamp(y, 0, Math.max(0, screenH - height));
    }

    public void updateLayout() {
        int padding = 4;
        int titleHeight = 10;

        wheelSize = height - titleHeight - (padding * 2);
        wheelX = x + padding;
        wheelY = y + titleHeight + padding;

        rightAreaX = wheelX + wheelSize + padding + 2;
        rightAreaWidth = width - (wheelSize + padding * 3) - 2;

        previewY = wheelY + 4;
        previewHeight = 10;

        sliderStartY = previewY + previewHeight + 3;
        int remainingHeight = (height - 6) - sliderStartY;
        sliderSpacing = Math.max(12, remainingHeight / 5);

        hexInput.setX(rightAreaX + rightAreaWidth - 42);
        hexInput.setY(previewY);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Font font) {
        graphics.fill(x, y, x + width, y + height, 0xE015191D);
        graphics.fill(x, y, x + width, y + 12, 0xFF0D1013);
        
        graphics.fill(x - 1, y - 1, x + width + 1, y, 0xFF3A3D40);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFF3A3D40);
        graphics.fill(x - 1, y, x, y + height, 0xFF3A3D40);
        graphics.fill(x + width, y, x + width + 1, y + height, 0xFF3A3D40);

        graphics.text(font, "색칠", x + 3, y + 2, 0xFFAAAAAA, false);

        graphics.blit(RenderPipelines.GUI_TEXTURED, COLOR_WHEEL, wheelX, wheelY, 0, 0, wheelSize, wheelSize, wheelSize, wheelSize);

        double cx = wheelX + wheelSize / 2.0;
        double cy = wheelY + wheelSize / 2.0;
        double currentRadius = (wheelSize / 2.0) * saturation;
        double angle = hue * Math.PI * 2.0;
        int indX = (int) Math.round(cx + Math.cos(angle) * currentRadius);
        int indY = (int) Math.round(cy + Math.sin(angle) * currentRadius);

        graphics.fill(indX - 2, indY - 2, indX + 2, indY + 2, 0xFF000000);
        graphics.fill(indX - 1, indY - 1, indX + 1, indY + 1, 0xFFFFFFFF);

        int currentColor = getArgbColor();
        int previewW = Math.max(8, rightAreaWidth - 46);
        graphics.fill(rightAreaX, previewY, rightAreaX + previewW, previewY + previewHeight, currentColor);

        drawSlider(graphics, font, "V", 0, brightness, rightAreaX, sliderStartY, 0xFFAAAAAA, rightAreaWidth);
        drawSlider(graphics, font, "R", 1, red, rightAreaX, sliderStartY + sliderSpacing, 0xFFFF4444, rightAreaWidth);
        drawSlider(graphics, font, "G", 2, green, rightAreaX, sliderStartY + sliderSpacing * 2, 0xFF44FF44, rightAreaWidth);
        drawSlider(graphics, font, "B", 3, blue, rightAreaX, sliderStartY + sliderSpacing * 3, 0xFF4444FF, rightAreaWidth);
        drawSlider(graphics, font, "A", 4, alpha, rightAreaX, sliderStartY + sliderSpacing * 4, 0xFFFFFFFF, rightAreaWidth);

        int rx = x + width - 3;
        int ry = y + height - 3;
        graphics.fill(rx, ry, rx + 1, ry + 1, 0x80888888);
        
        hexInput.extractRenderState(graphics, mouseX, mouseY, 0);
    }

    private void drawSlider(GuiGraphicsExtractor graphics, Font font, String label, int index, float value, int dx, int dy, int color, int w) {
        graphics.text(font, label, dx, dy, 0xFFCCCCCC, false);
        int sliderX = dx + 8;
        int sWidth = w - 8;

        int handleW = Math.max(2, sWidth / 15);
        int handleH = Math.max(4, (int)(sliderSpacing * 0.75));

        int trackY = dy + (handleH / 2);
        graphics.fill(sliderX, trackY, sliderX + sWidth, trackY + 1, 0xFF111111);

        int handleX = sliderX + (int)(value * (sWidth - handleW));
        graphics.fill(handleX, dy, handleX + handleW, dy + handleH, color);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (hexInput.mouseClicked(event, doubleClick)) {
            hexInput.setFocused(true);
            return true;
        } else {
            hexInput.setFocused(false);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        if (mouseX >= x + width - 6 && mouseX <= x + width && mouseY >= y + height - 6 && mouseY <= y + height) {
            currentState = State.RESIZING_PANEL;
            return true;
        }

        if (isHovering(mouseX, mouseY)) {
            if (mouseY <= y + 10) {
                currentState = State.DRAGGING_PANEL;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            }
            if (mouseX >= wheelX && mouseX <= wheelX + wheelSize && mouseY >= wheelY && mouseY <= wheelY + wheelSize) {
                currentState = State.DRAGGING_WHEEL;
                updateWheel(mouseX, mouseY);
                return true;
            }
            for (int i = 0; i < 5; i++) {
                int sY = sliderStartY + (i * sliderSpacing);
                if (mouseX >= rightAreaX && mouseX <= rightAreaX + rightAreaWidth && mouseY >= sY && mouseY <= sY + sliderSpacing) {
                    currentState = State.SLIDER;
                    draggingSlider = i;
                    updateSlider(mouseX, i);
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public void mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (currentState == State.RESIZING_PANEL) {
            width = Mth.clamp((int) (mouseX - x), MIN_W, MAX_W);
            height = Mth.clamp((int) (mouseY - y), MIN_H, MAX_H);
            clampPosition();
            updateLayout();
        } else if (currentState == State.DRAGGING_PANEL) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
            clampPosition();
            updateLayout();
        } else if (currentState == State.DRAGGING_WHEEL) {
            updateWheel(mouseX, mouseY);
        } else if (currentState == State.SLIDER && draggingSlider != -1) {
            updateSlider(mouseX, draggingSlider);
        }
    }

    public void mouseReleased() {
        currentState = State.NONE;
        draggingSlider = -1;
    }

    public boolean keyPressed(KeyEvent event) {
        if (hexInput.isFocused()) {
            return hexInput.keyPressed(event);
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (hexInput.isFocused()) {
            return hexInput.charTyped(event);
        }
        return false;
    }

    public boolean isInteracting() {
        return currentState != State.NONE;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void updateWheel(double mouseX, double mouseY) {
        double cx = wheelX + wheelSize / 2.0;
        double cy = wheelY + wheelSize / 2.0;
        double dx = mouseX - cx, dy = mouseY - cy;
        double maxRadius = wheelSize / 2.0;
        double dist = Math.sqrt(dx * dx + dy * dy);

        saturation = (float) Mth.clamp(dist / maxRadius, 0.0, 1.0);
        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += Math.PI * 2.0;
        hue = (float) (angle / (Math.PI * 2.0));
        
        if (brightness <= 0.05f) brightness = 1.0f;
        updateRGBfromHSV();
    }

    private void updateSlider(double mouseX, int index) {
        int sWidth = rightAreaWidth - 8;
        int handleW = Math.max(2, sWidth / 15);
        float val = (float) Mth.clamp((mouseX - (rightAreaX + 8 + handleW / 2.0f)) / (sWidth - handleW), 0.0, 1.0);
        if (index == 0) { brightness = val; updateRGBfromHSV(); }
        else if (index == 1) { red = val; updateHSVfromRGB(); }
        else if (index == 2) { green = val; updateHSVfromRGB(); }
        else if (index == 3) { blue = val; updateHSVfromRGB(); }
        else if (index == 4) { alpha = val; }
    }

    private void onHexInputChanged(String hex) {
        if (isSyncingHex) return;
        if (hex.matches("^[0-9a-fA-F]*$") && hex.length() == 6) {
            try {
                int rgb = Integer.parseInt(hex, 16);
                red = ((rgb >> 16) & 0xFF) / 255.0f;
                green = ((rgb >> 8) & 0xFF) / 255.0f;
                blue = (rgb & 0xFF) / 255.0f;
                updateHSVfromRGB();
            } catch (NumberFormatException ignored) {}
        }
    }

    private void syncHexInput() {
        isSyncingHex = true;
        hexInput.setValue(String.format("%02X%02X%02X", (int)(red*255), (int)(green*255), (int)(blue*255)));
        isSyncingHex = false;
    }

    private void updateHSVfromRGB() {
        float[] hsv = new float[3];
        java.awt.Color.RGBtoHSB((int)(red*255), (int)(green*255), (int)(blue*255), hsv);
        hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2];
        syncHexInput();
    }

    private void updateRGBfromHSV() {
        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        red = ((rgb >> 16) & 0xFF) / 255.0f;
        green = ((rgb >> 8) & 0xFF) / 255.0f;
        blue = (rgb & 0xFF) / 255.0f;
        syncHexInput();
    }

    public void setRGB(float r, float g, float b) {
        this.red = r; this.green = g; this.blue = b;
        updateHSVfromRGB();
    }

    public EditBox getHexInput() { return hexInput; }
    
    public int getAbgrColor() {
        return ((int)(alpha * 255) << 24) | ((int)(blue * 255) << 16) | ((int)(green * 255) << 8) | ((int)(red * 255));
    }

    public int getArgbColor() {
        return ((int)(alpha * 255) << 24) | ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | ((int)(blue * 255));
    }
}