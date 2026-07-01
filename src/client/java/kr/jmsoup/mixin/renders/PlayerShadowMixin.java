package kr.jmsoup.mixin.renders;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class PlayerShadowMixin {
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void removePlayerShadow(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof AbstractClientPlayer) {
            EntityRenderState state = cir.getReturnValue();

            if (state != null) {
                state.shadowRadius = 0f;

                try {
                    state.shadowPieces.clear();
                } catch (UnsupportedOperationException ignored) {
                }
            }
        }
    }
}