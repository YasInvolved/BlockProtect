package pl.yasinvolved.blockprotect.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.client.ClientClaimData;

import java.util.List;

public record S2CSyncClaimsPacket(List<ClientClaimData> claims) implements CustomPacketPayload {
    public static final Type<S2CSyncClaimsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockprotect.MODID, "sync_claims"));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncClaimsPacket> STREAM_CODEC = StreamCodec.composite(
            ClientClaimData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            S2CSyncClaimsPacket::claims,
            S2CSyncClaimsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
