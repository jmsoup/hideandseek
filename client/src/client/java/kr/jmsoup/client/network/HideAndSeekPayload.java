package kr.jmsoup.client.network;

import kr.jmsoup.HideAndSeekClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HideAndSeekPayload(String action, byte[] dataBytes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HideAndSeekPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(HideAndSeekClient.MOD_ID, "generic"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HideAndSeekPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, HideAndSeekPayload::action,
            ByteBufCodecs.byteArray(1048576), HideAndSeekPayload::dataBytes,
            HideAndSeekPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}