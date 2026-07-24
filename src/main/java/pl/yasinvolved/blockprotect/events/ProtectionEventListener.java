package pl.yasinvolved.blockprotect.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;
import pl.yasinvolved.blockprotect.storage.entities.LogEntity;

import java.util.Optional;

@EventBusSubscriber(modid = Blockprotect.MODID)
public class ProtectionEventListener {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) return;

        if (event.getPlayer() instanceof ServerPlayer player) {
            String dimension = player.level().dimension().location().toString();
            Optional<ClaimEntity> claim = ClaimManager.getClaimAt(dimension, event.getPos());
            if (claim.isPresent()) {
                LogEntity log = new LogEntity(
                        claim.get(),
                        player.getUUID().toString(),
                        player.getName().toString(),
                        "BLOCK_BREAK",
                        dimension,
                        event.getPos().getX(),
                        event.getPos().getY(),
                        event.getPos().getZ(),
                        event.getState().toString(),
                        System.currentTimeMillis()
                );

                Blockprotect.LOG_QUEUE.offer(log);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            String dimension = player.level().dimension().location().toString();
            Optional<ClaimEntity> claim = ClaimManager.getClaimAt(dimension, event.getPos());
            if (claim.isPresent()) {
                LogEntity log = new LogEntity(
                        claim.get(),
                        player.getUUID().toString(),
                        player.getName().toString(),
                        "BLOCK_PLACE",
                        dimension,
                        event.getPos().getX(),
                        event.getPos().getY(),
                        event.getPos().getZ(),
                        event.getState().toString(),
                        System.currentTimeMillis()
                );

                Blockprotect.LOG_QUEUE.offer(log);
            }
        }
    }
}
