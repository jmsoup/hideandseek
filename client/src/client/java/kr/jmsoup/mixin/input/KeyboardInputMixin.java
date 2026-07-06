package kr.jmsoup.mixin.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    public void forceRpgForwardVector(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        KeyboardInput input = (KeyboardInput) (Object) this;

        if (client.player != null && !client.options.getCameraType().isFirstPerson()) {
            if (input.keyPresses.forward() || input.keyPresses.backward() || input.keyPresses.left() || input.keyPresses.right()) {
                ((ClientInputAccessor) input).setMoveVector(new Vec2(0f, 1f));
            }
        }
    }
}