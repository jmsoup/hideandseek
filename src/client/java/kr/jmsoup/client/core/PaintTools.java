package kr.jmsoup.client.core;

import com.mojang.blaze3d.platform.NativeImage;
import kr.jmsoup.client.util.ColorUtils;
import kr.jmsoup.client.util.SkinLayoutInfo;

import java.util.Arrays;

public class PaintTools {
    private static final int MAX_PIXELS = PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE;

    private static final int[][] strokeMap = new int[PaintCanvas.CANVAS_SIZE][PaintCanvas.CANVAS_SIZE];
    private static final int[] stackX = new int[MAX_PIXELS];
    private static final int[] stackY = new int[MAX_PIXELS];

    private static int currentStrokeId = 0;

    public static void startNewStroke() {
        currentStrokeId++;
        if (currentStrokeId == 0) {
            resetStrokeMap();
            currentStrokeId = 1;
        }
    }

    public static void resetStrokeMap() {
        currentStrokeId = 0;
        for (int i = 0; i < PaintCanvas.CANVAS_SIZE; i++) {
            Arrays.fill(strokeMap[i], 0);
        }
    }

    public static void paintPixel(PaintCanvas canvas, int u, int v, int colorAbgr, int brushSize) {
        NativeImage img = canvas.getImage();
        if (img == null) return;

        int halfBrush = brushSize / 2;
        int radiusSq = halfBrush * halfBrush;

        for (int dx = -halfBrush; dx <= halfBrush; dx++) {
            for (int dy = -halfBrush; dy <= halfBrush; dy++) {
                if (dx * dx + dy * dy <= radiusSq) {
                    int nx = u + dx;
                    int ny = v + dy;

                    if (nx >= 0 && nx < PaintCanvas.CANVAS_SIZE && ny >= 0 && ny < PaintCanvas.CANVAS_SIZE) {
                        if (!SkinLayoutInfo.isOuterLayer(nx, ny)) {
                            if (strokeMap[nx][ny] == currentStrokeId) continue;
                            
                            strokeMap[nx][ny] = currentStrokeId;
                            int oldColor = img.getPixel(nx, ny);
                            img.setPixelABGR(nx, ny, ColorUtils.blendColor(colorAbgr, oldColor));
                        }
                    }
                }
            }
        }
    }

    public static void fillPixel(PaintCanvas canvas, int startU, int startV, int colorAbgr) {
        NativeImage img = canvas.getImage();
        if (img == null) return;

        int targetColor = img.getPixel(startU, startV);
        if (targetColor == colorAbgr) return;

        int top = -1;
        top++; stackX[top] = startU; stackY[top] = startV;

        boolean[][] visited = new boolean[PaintCanvas.CANVAS_SIZE][PaintCanvas.CANVAS_SIZE];

        while (top >= 0) {
            int x = stackX[top];
            int y = stackY[top];
            top--;

            if (x < 0 || x >= PaintCanvas.CANVAS_SIZE || y < 0 || y >= PaintCanvas.CANVAS_SIZE) continue;
            if (visited[x][y]) continue;
            if (SkinLayoutInfo.isOuterLayer(x, y)) continue;

            if (img.getPixel(x, y) == targetColor) {
                visited[x][y] = true;
                
                int oldColor = img.getPixel(x, y);
                img.setPixelABGR(x, y, ColorUtils.blendColor(colorAbgr, oldColor));

                top++; stackX[top] = x + 1; stackY[top] = y;
                top++; stackX[top] = x - 1; stackY[top] = y;
                top++; stackX[top] = x; stackY[top] = y + 1;
                top++; stackX[top] = x; stackY[top] = y - 1;
            }
        }
    }
}