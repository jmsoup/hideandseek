package kr.jmsoup.client.util;

public class ColorUtils {
    public static int blendColor(int srcAbgr, int dstAbgr) {
        int srcA = (srcAbgr >> 24) & 0xFF;
        if (srcA == 255) return srcAbgr;
        if (srcA == 0) return dstAbgr;

        int dstA = (dstAbgr >> 24) & 0xFF;
        int srcB = (srcAbgr >> 16) & 0xFF;
        int srcG = (srcAbgr >> 8) & 0xFF;
        int srcR = srcAbgr & 0xFF;

        int dstB = (dstAbgr >> 16) & 0xFF;
        int dstG = (dstAbgr >> 8) & 0xFF;
        int dstR = dstAbgr & 0xFF;

        float alphaSrc = srcA / 255.0f;
        float alphaDst = dstA / 255.0f;
        float outA = alphaSrc + alphaDst * (1.0f - alphaSrc);

        if (outA == 0.0f) return 0;

        int rOut = (int) (((srcR * alphaSrc) + (dstR * alphaDst * (1.0f - alphaSrc))) / outA);
        int gOut = (int) (((srcG * alphaSrc) + (dstG * alphaDst * (1.0f - alphaSrc))) / outA);
        int bOut = (int) (((srcB * alphaSrc) + (dstB * alphaDst * (1.0f - alphaSrc))) / outA);
        int aOutInt = (int) (outA * 255.0f);

        return (aOutInt << 24) | (bOut << 16) | (gOut << 8) | rOut;
    }
}