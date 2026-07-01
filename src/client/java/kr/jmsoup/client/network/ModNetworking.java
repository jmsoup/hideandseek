package kr.jmsoup.client.network;

import kr.jmsoup.client.core.PaintCanvas;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModNetworking {
    public static final String UPDATE_SKIN = "update_skin";
    public static final String SET_MODE = "set_mode";
    public static final String SHOW_NAME = "show_name";

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(HideAndSeekPayload.TYPE, HideAndSeekPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HideAndSeekPayload.TYPE, HideAndSeekPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(HideAndSeekPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                switch (payload.action()) {
                    case UPDATE_SKIN:
                        CustomSkinManager.receiveSkinData(payload.dataBytes());
                        break;

                    case SET_MODE:
                        PaintCanvas.setPaintingMode(payload.dataBytes()[0] == 1);
                        CustomSkinManager.clearAllSkins();
                        break;

                    case SHOW_NAME:
                        PaintCanvas.setShowNameplate(payload.dataBytes()[0] == 1);
                        break;

                    default:
                        System.out.println("알 수 없는 패킷 타입: " + payload.action());
                        break;
                }
            });
        });
    }

    public static void sendToServer(String action, byte[] data) {
        ClientPlayNetworking.send(new HideAndSeekPayload(action, data));
    }
}