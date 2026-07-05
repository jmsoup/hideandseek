package kr.jmsoup;

import kr.jmsoup.client.core.PaintCanvas;
import kr.jmsoup.client.core.PaintManager;
import kr.jmsoup.client.gui.PaintScreen;
import kr.jmsoup.client.network.ModNetworking;
import kr.jmsoup.client.util.KeyMappings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HideAndSeekClient implements ClientModInitializer {
	public static final String MOD_ID = "hideandseek";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static float cameraYaw = 0.0f;
	public static float cameraPitch = 0.0f;
	public static float cameraZoom = 5.0f;

	private static boolean wasThirdPerson = false;

	@Override
	public void onInitializeClient() {
		LOGGER.info("HideAndSeek Mod is initializing..");
		KeyMappings.register();
		ModNetworking.register();

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
            while (client.options.keyTogglePerspective.consumeClick()) {
				client.options.setCameraType(
						client.options.getCameraType().isFirstPerson() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON
				);
            }
        });

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			boolean isThirdPerson = !client.options.getCameraType().isFirstPerson();

			if (isThirdPerson != wasThirdPerson) {
				if (isThirdPerson) {
					cameraYaw = client.player.getYRot();
					cameraPitch = client.player.getXRot();
				} else {
					client.player.setYRot(cameraYaw);
					client.player.setXRot(cameraPitch);
					client.player.yRotO = cameraYaw;
					client.player.xRotO = cameraPitch;
				}
				wasThirdPerson = isThirdPerson;
			}

			while (KeyMappings.OPEN_PAINT.consumeClick() && PaintManager.isPaintingMode) {
				if (client.screen == null && !client.options.getCameraType().isFirstPerson()) {
					client.setScreen(new PaintScreen(client.player, PaintCanvas.getInstance()));
				}
			}
		});
	}
}