package pl.yasinvolved.blockprotect.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.async.InspectorManager;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.client.ClientClaimData;
import pl.yasinvolved.blockprotect.networking.S2CInspectModePacket;
import pl.yasinvolved.blockprotect.networking.S2CSyncClaimsPacket;
import pl.yasinvolved.blockprotect.selection.PlayerSelection;
import pl.yasinvolved.blockprotect.selection.SelectionManager;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProtectCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("bp")
                    .then(buildWandCommand())
                    .then(buildCreateCommand())
                    .then(buildDeleteCommand())
                    .then(buildInspectCommand())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildWandCommand() {
        return Commands.literal("wand")
                .executes(ProtectCommands::handleWand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCreateCommand() {
        return Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ProtectCommands::handleCreate));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildDeleteCommand() {
        return Commands.literal("delete")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ProtectCommands::handleDelete));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildInspectCommand() {
        return Commands.literal("inspect")
                .executes(ProtectCommands::handleInspect);
    }

    private static int handleWand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.getInventory().add(new ItemStack(Blockprotect.PROTECT_WAND));
        player.sendSystemMessage(Component.literal("§aGiven 1x Protect Wand."));
        return 1;
    }

    private static int handleCreate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String claimName = StringArgumentType.getString(ctx, "name");
        UUID playerId = player.getUUID();
        String dimension = player.level().dimension().location().toString();

        PlayerSelection sel = SelectionManager.getSelection(playerId);
        if (!sel.isComplete()) {
            player.sendSystemMessage(Component.literal("§cIncomplete selection! Select Pos 1 and Pos 2 with the wand."));
            return 0;
        }

        if (ClaimManager.intersects(dimension, sel.pos1(), sel.pos2())) {
            player.sendSystemMessage(Component.literal("§cCannot create claim: selection overlaps with an existing claim."));
            return 0;
        }

        ClaimEntity entity = ClaimEntity.fromSelection(claimName, playerId, dimension, sel);
        ClaimManager.addClaim(entity);
        Blockprotect.DATABASE.saveClaim(entity);
        ClaimManager.syncToClient(player);

        SelectionManager.clearSelection(playerId);
        player.sendSystemMessage(Component.literal(String.format("§aCreated claim '%s'!", claimName)));
        return 1;
    }

    private static int handleDelete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String claimName = StringArgumentType.getString(ctx, "name");

        Optional<ClaimEntity> claim = ClaimManager.getClaimByName(player.getUUID(), claimName);
        if (claim.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cClaim '" + claimName + "' not found."));
            return 0;
        }

        ClaimManager.removeClaim(claim.get().getId());
        Blockprotect.DATABASE.deleteClaim(claim.get());
        ClaimManager.syncToClient(player);

        player.sendSystemMessage(Component.literal("§eDeleted claim '" + claimName + "'."));
        return 1;
    }

    private static int handleInspect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        UUID uuid = player.getUUID();

        boolean enabled = InspectorManager.toggleInspector(uuid);

        PacketDistributor.sendToPlayer(player, new S2CInspectModePacket(enabled));
        if (enabled) {
            ClaimManager.syncToClient(player);
            player.sendSystemMessage(Component.literal("§a[BlockProtect] Inspector mode ENABLED."));
        } else {
            player.sendSystemMessage(Component.literal("§c[BlockProtect] Inspector mode DISABLED."));
        }

        return 1;
    }
}
