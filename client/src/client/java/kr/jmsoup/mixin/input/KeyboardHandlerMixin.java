package kr.jmsoup.mixin.input;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void disableNarratorShortcut(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (event.key() == GLFW.GLFW_KEY_B && event.hasControlDown()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void blockHitboxToggle(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.keyDebugShowHitboxes.matches(event)) {
            cir.setReturnValue(true);
        }
    }
}