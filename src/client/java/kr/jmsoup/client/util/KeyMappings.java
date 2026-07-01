package kr.jmsoup.client.util;

import kr.jmsoup.HideAndSeekClient;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse(HideAndSeekClient.MOD_ID));
    public static KeyMapping OPEN_PAINT;
    public static KeyMapping BRUSH_INCREASE;
    public static KeyMapping BRUSH_DECREASE;
    public static KeyMapping UNDO;
    public static KeyMapping EYE_DROPPER;

    public static void register() {
        OPEN_PAINT = KeyMappingHelper.registerKeyMapping(new KeyMapping("페인트 모드", GLFW.GLFW_KEY_B, CATEGORY));

        BRUSH_INCREASE = KeyMappingHelper.registerKeyMapping(new KeyMapping("브러쉬 크기 증가", GLFW.GLFW_KEY_RIGHT_BRACKET, CATEGORY));

        BRUSH_DECREASE = KeyMappingHelper.registerKeyMapping(new KeyMapping("브러쉬 크기 감소", GLFW.GLFW_KEY_LEFT_BRACKET, CATEGORY));

        UNDO = KeyMappingHelper.registerKeyMapping(new KeyMapping("실행 취소", GLFW.GLFW_KEY_Z, CATEGORY));

        EYE_DROPPER = KeyMappingHelper.registerKeyMapping(new KeyMapping("스포이드", GLFW.GLFW_KEY_LEFT_ALT, CATEGORY));
    }
}