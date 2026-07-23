package pl.yasinvolved.blockprotect.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WandItem extends Item {
    public WandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Left-Click a block to set §aPos 1§7."));
        tooltipComponents.add(Component.literal("§7Right-Click a block to set §aPos 2§7."));
        tooltipComponents.add(Component.literal("§eUse §f/claim create <name> §eto claim the area."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
