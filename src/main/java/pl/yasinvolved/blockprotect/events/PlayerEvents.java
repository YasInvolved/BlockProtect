package pl.yasinvolved.blockprotect.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.async.InspectorManager;
import pl.yasinvolved.blockprotect.storage.entities.LogEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Blockprotect.MODID)
public class PlayerEvents {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (InspectorManager.isInspecting(player.getUUID())) {
            event.setCanceled(true);

            BlockPos pos = event.getPos();
            String dimension = event.getLevel().dimension().location().toString();
            CompletableFuture.runAsync(() -> {
                try {
                    List<LogEntity> logs = Blockprotect.DATABASE.queryLogsAt(dimension, pos.getX(), pos.getY(), pos.getZ(), 10);

                    if (logs.isEmpty()) {
                        player.sendSystemMessage(Component.literal("§7No history recorded for this block position."));
                        return;
                    }

                    player.sendSystemMessage(Component.literal("§6--- Block History ---"));
                    for (LogEntity log : logs) {
                        String timeStr = DATE_FORMAT.format(new Date(log.getTimestamp()));
                        player.sendSystemMessage(Component.literal(
                                String.format("§7[%s] §f%s §7- §a%s §7(%s)",
                                        timeStr,
                                        log.getPlayerId().substring(0, 8) + "...",
                                        log.getAction(),
                                        log.getBlockState()
                                )
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
