package kr.jmsoup.mixin.camera;

import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow private Entity entity;
	@Shadow private float eyeHeightOld;
	@Shadow private float eyeHeight;
	@Shadow private boolean detached;

	@Shadow protected abstract void setRotation(float yRot, float xRot);
	@Shadow protected abstract void setPosition(double x, double y, double z);
	@Shadow protected abstract void move(float forwards, float up, float right);

	@Inject(method = "alignWithEntity", at = @At("TAIL"))
	private void applyIndependentCamera(float partialTicks, CallbackInfo ci) {
		if (this.detached && this.entity != null) {
			float currentEyeHeight = Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight);

			this.setPosition(
					Mth.lerp(partialTicks, this.entity.xo, this.entity.getX()),
					Mth.lerp(partialTicks, this.entity.yo, this.entity.getY()) + (currentEyeHeight * 0.5f),
					Mth.lerp(partialTicks, this.entity.zo, this.entity.getZ())
			);

			this.setRotation(HideAndSeekClient.cameraYaw, HideAndSeekClient.cameraPitch);
			this.move(-HideAndSeekClient.cameraZoom, 0f, 0f);
		}
	}
}