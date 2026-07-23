package pl.yasinvolved.blockprotect.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.selection.SelectionManager;

@EventBusSubscriber(modid = Blockprotect.MODID)
public class WandSelectionListener {
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.getMainHandItem().is(Blockprotect.PROTECT_WAND)) {
            BlockPos pos = event.getPos();
            SelectionManager.setPos1(player.getUUID(), pos);

            player.sendSystemMessage(Component.literal(
                String.format("§a[BlockProtect] First position set to [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())
            ));

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (player.getMainHandItem().is(Blockprotect.PROTECT_WAND)) {
            BlockPos pos = event.getPos();
            SelectionManager.setPos2(player.getUUID(), pos);

            player.sendSystemMessage(Component.literal(
                String.format("§a[BlockProtect] Second position set to [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())
            ));

            event.setCanceled(true);
        }
    }
}
