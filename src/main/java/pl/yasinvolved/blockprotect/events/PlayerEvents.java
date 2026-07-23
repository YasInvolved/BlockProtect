package pl.yasinvolved.blockprotect.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.async.InspectorManager;

@EventBusSubscriber(modid = Blockprotect.MODID)
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getServer() != null && !FMLLoader.isProduction()) {
            var server = event.getEntity().getServer();
            var player = event.getEntity();

            if (!server.getPlayerList().isOp(player.getGameProfile())) {
                server.getPlayerList().op(player.getGameProfile());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getServer() != null && (event.getEntity() instanceof ServerPlayer player)) {
            InspectorManager.removeInspector(player.getUUID());
        }
    }
}
