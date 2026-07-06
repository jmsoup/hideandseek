package kr.jmsoup.mixin.player;

import kr.jmsoup.HideAndSeekClient;
import kr.jmsoup.client.core.PaintCanvas;
import kr.jmsoup.client.core.PaintManager;
import kr.jmsoup.client.network.CustomSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void replaceSkinWithHdCanvas(CallbackInfoReturnable<PlayerSkin> cir) {
        Minecraft client = Minecraft.getInstance();
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        PlayerSkin original = cir.getReturnValue();

        boolean isLocalPlayer = client.player != null && player.getUUID().equals(client.player.getUUID());

        if (isLocalPlayer && PaintManager.isPaintingMode) {
            PaintCanvas canvas = PaintCanvas.getInstance();
            canvas.init();

            ClientAsset.Texture hdCanvasTexture = new ClientAsset.ResourceTexture(PaintCanvas.CANVAS_ID);
            PlayerSkin customSkin = new PlayerSkin(hdCanvasTexture, null, null, PlayerModelType.WIDE, original.secure());
            cir.setReturnValue(customSkin);
            return;
        }

        Identifier networkTexture = CustomSkinManager.getCustomSkin(player.getUUID());

        if (networkTexture != null) {
            ClientAsset.Texture customTex = new ClientAsset.ResourceTexture(networkTexture);
            PlayerSkin customSkin = new PlayerSkin(customTex, null, null, PlayerModelType.WIDE, original.secure());
            cir.setReturnValue(customSkin);
            return;
        }

        if (PaintManager.isPaintingMode) {
            Identifier staticBlankId = Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, "white_skin");
            ClientAsset.Texture blankCanvasTexture = new ClientAsset.ResourceTexture(staticBlankId);
            PlayerSkin blankSkin = new PlayerSkin(blankCanvasTexture, null, null, PlayerModelType.WIDE, original.secure());
            cir.setReturnValue(blankSkin);
            return;
        }
    }
}