package kr.jmsoup.mixin.player;

import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void separateCameraTurn(double yRot, double xRot, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (this.equals(client.player) && !client.options.getCameraType().isFirstPerson()) {

            HideAndSeekClient.cameraYaw += (float) yRot * 0.15f;
            HideAndSeekClient.cameraPitch = Mth.clamp(HideAndSeekClient.cameraPitch + (float) xRot * 0.1f, -90f, 90f);

            ci.cancel();
        }
    }
}