package kr.jmsoup.mixin.player;

import com.mojang.authlib.GameProfile;
import kr.jmsoup.HideAndSeekClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow public ClientInput input;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void updateRpgRotation(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (!client.options.getCameraType().isFirstPerson() && this.input != null) {
            if (client.options.keyAttack.isDown()) {
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();

                float pitch = HideAndSeekClient.cameraPitch;
                if (Math.abs(pitch) < 10f) {
                    pitch = 0f;
                }
                this.setXRot(pitch);
                this.setYRot(HideAndSeekClient.cameraYaw);

                this.yHeadRot = HideAndSeekClient.cameraYaw;
                this.yHeadRotO = this.yHeadRot;
                this.yBodyRot = HideAndSeekClient.cameraYaw;
                this.yBodyRotO = this.yBodyRot;
            }

            Input keys = this.input.keyPresses;

            if (keys.forward() || keys.backward() || keys.left() || keys.right()) {

                float yawOffset;

                if (keys.forward()) {
                    yawOffset = keys.left() ? -45f : (keys.right() ? 45f : 0f);
                } else if (keys.backward()){
                    yawOffset = keys.left() ? -135f : (keys.right() ? 135f : 180f);
                } else{
                    yawOffset = keys.left() ? -90f : (keys.right() ? 90f : 0f);
                }

                this.yRotO = this.getYRot();
                this.setYRot(HideAndSeekClient.cameraYaw + yawOffset);
            }
        }
    }
}