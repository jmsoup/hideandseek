package kr.jmsoup.client.core;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.util.Stack;

public class PaintHistory {
    private static final Stack<NativeImage> undoStack = new Stack<>();
    private static final Stack<NativeImage> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 30;

    public static void saveState(PaintCanvas canvas) {
        if (canvas.getImage() == null) return;

        NativeImage snapshot = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long bytesCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), snapshot.getPointer(), bytesCount);

        undoStack.push(snapshot);

        if (undoStack.size() > MAX_HISTORY) {
            NativeImage oldest = undoStack.remove(0);
            oldest.close(); // 메모리 해제
        }

        for (NativeImage img : redoStack) img.close();
        redoStack.clear();
    }

    public static boolean undo(PaintCanvas canvas) {
        if (undoStack.isEmpty() || canvas.getImage() == null) return false;

        NativeImage current = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long bytesCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), current.getPointer(), bytesCount);
        redoStack.push(current);

        NativeImage previous = undoStack.pop();
        MemoryUtil.memCopy(previous.getPointer(), canvas.getImage().getPointer(), bytesCount);
        previous.close();

        canvas.upload();
        return true;
    }

    public static boolean redo(PaintCanvas canvas) {
        if (redoStack.isEmpty() || canvas.getImage() == null) return false;

        NativeImage current = new NativeImage(NativeImage.Format.RGBA, PaintCanvas.CANVAS_SIZE, PaintCanvas.CANVAS_SIZE, false);
        long bytesCount = (long) PaintCanvas.CANVAS_SIZE * PaintCanvas.CANVAS_SIZE * 4L;
        MemoryUtil.memCopy(canvas.getImage().getPointer(), current.getPointer(), bytesCount);
        undoStack.push(current);

        NativeImage next = redoStack.pop();
        MemoryUtil.memCopy(next.getPointer(), canvas.getImage().getPointer(), bytesCount);
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