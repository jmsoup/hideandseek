package kr.jmsoup.client.core;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayDeque;

public class PaintHistory {
    private static final ArrayDeque<NativeImage> undoStack = new ArrayDeque<>();
    private static final ArrayDeque<NativeImage> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 30;

    public static void saveState(PaintCanvas canvas) {
        if (canvas.getImage() == null) return;

        NativeImage snapshot = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long bytesCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), snapshot.getPointer(), bytesCount);

        undoStack.addFirst(snapshot);

        if (undoStack.size() > MAX_HISTORY) {
            NativeImage oldest = undoStack.pollLast();
            oldest.close();
        }

        for (NativeImage img : redoStack) img.close();
        redoStack.clear();
    }

    public static boolean undo(PaintCanvas canvas) {
        if (undoStack.isEmpty() || canvas.getImage() == null) return false;

        NativeImage current = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long byteCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), current.getPointer(), byteCount);
        redoStack.push(current);

        NativeImage previous = undoStack.pollFirst();
        MemoryUtil.memCopy(previous.getPointer(), canvas.getImage().getPointer(), byteCount);
        previous.close();

        canvas.upload();
        return true;
    }

    public static boolean redo(PaintCanvas canvas) {
        if (redoStack.isEmpty() || canvas.getImage() == null) return false;

        NativeImage current = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long byteCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), current.getPointer(), byteCount);
        undoStack.push(current);

        NativeImage next = redoStack.pollFirst();
        MemoryUtil.memCopy(next.getPointer(), canvas.getImage().getPointer(), byteCount);
        next.close();

        canvas.upload();
        return true;
    }

    public static void clearHistory() {
        for (NativeImage img : undoStack) img.close();
        for (NativeImage img : redoStack) img.close();
        undoStack.clear();
        redoStack.clear();
    }
}