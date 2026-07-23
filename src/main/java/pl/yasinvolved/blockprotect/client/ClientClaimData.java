package pl.yasinvolved.blockprotect.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientClaimData(String name, BlockPos min, BlockPos max) {
    public static final StreamCodec<FriendlyByteBuf, ClientClaimData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientClaimData::name,
            BlockPos.STREAM_CODEC, ClientClaimData::min,
            BlockPos.STREAM_CODEC, ClientClaimData::max,
            ClientClaimData::new
    );
}
