package kr.jmsoup.mixin.camera;

import kr.jmsoup.HideAndSeekClient;
import kr.jmsoup.client.gui.PaintScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void applyCameraZoomOnScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (client.player != null && (client.screen == null || client.screen instanceof PaintScreen) && !client.options.getCameraType().isFirstPerson()) {
            HideAndSeekClient.cameraZoom = Mth.clamp(HideAndSeekClient.cameraZoom - (float) vertical * 0.5f, 0.5f, 6f);
            ci.cancel(); 
        }
    }
}