package pl.yasinvolved.blockprotect.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.blockprotect.Blockprotect;

public record S2CInspectModePacket(boolean enabled) implements CustomPacketPayload {
    public static final Type<S2CInspectModePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockprotect.MODID, "inspect_mode_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CInspectModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            S2CInspectModePacket::enabled,
            S2CInspectModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
