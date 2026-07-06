package kr.jmsoup.mixin.renders;

import kr.jmsoup.client.core.PaintCanvas;
import kr.jmsoup.client.core.PaintManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void viewOwnLabel(LivingEntity entity, double distanceSquared, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(entity instanceof Player && PaintManager.showNameplate);
    }
}