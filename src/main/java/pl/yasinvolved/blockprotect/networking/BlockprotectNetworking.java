package pl.yasinvolved.blockprotect.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.client.ClientClaimCache;
import pl.yasinvolved.blockprotect.client.ClientInspectState;

@EventBusSubscriber(modid = Blockprotect.MODID)
public class BlockprotectNetworking {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                S2CInspectModePacket.TYPE,
                S2CInspectModePacket.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> ClientInspectState.setInspecting(payload.enabled()));
                }
        );

        registrar.playToClient(
                S2CSyncClaimsPacket.TYPE,
                S2CSyncClaimsPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    ClientClaimCache.setActiveClaims(payload.claims());
                }
        );
    }
}
