package pl.yasinvolved.blockprotect.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.yasinvolved.blockprotect.Blockprotect;
import pl.yasinvolved.blockprotect.async.InspectorManager;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.networking.S2CInspectModePacket;
import pl.yasinvolved.blockprotect.selection.PlayerSelection;
import pl.yasinvolved.blockprotect.selection.SelectionManager;
import pl.yasinvolved.blockprotect.storage.dbentities.ClaimEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProtectCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("bp")
                    .then(buildWandCommand())
                    .then(buildInfoCommand())
                    .then(buildCreateCommand())
                    .then(buildDeleteCommand())
                    .then(buildInspectCommand())
                    .then(buildAddCoOwnerCommand())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildWandCommand() {
        return Commands.literal("wand")
                .executes(ProtectCommands::handleWand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildInfoCommand() {
        return Commands.literal("info")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ProtectCommands::handleInfo)
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCreateCommand() {
        return Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ProtectCommands::handleCreate));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildDeleteCommand() {
        return Commands.literal("delete")
                .then(Commands.argument("name", StringArgumentType.word())
                    .suggests(ClaimSuggestions.PLAYER_CLAIMS)
                    .executes(ProtectCommands::handleDelete)
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildInspectCommand() {
        return Commands.literal("inspect")
                .executes(ProtectCommands::handleInspect);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAddCoOwnerCommand() {
        return Commands.literal("coowner")
                .then(Commands.argument("claim", StringArgumentType.word())
                        .suggests(ClaimSuggestions.PLAYER_CLAIMS)
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ClaimSuggestions.ALL_PLAYERS)
                                .executes(ProtectCommands::handleAddCoOwner)
                        )
                );
    }

    private static int handleWand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.getInventory().add(new ItemStack(Blockprotect.PROTECT_WAND));
        player.sendSystemMessage(Component.literal("§aGiven 1x Protect Wand."));
        return 1;
    }

    private static int handleInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String claimName = StringArgumentType.getString(ctx, "name");

        var claim = ClaimManager.getClaimByName(player.getUUID(), claimName);
        if (claim.isEmpty())
        {
            source.sendSystemMessage(Component.literal(
                    String.format("§c[BlockProtect] Claim %s not found.")
            ));
            return 0;
        }

        String ownerName = source.getPlayer().getScoreboardName();
        source.sendSystemMessage(Component.literal("§6--- Claim Info ---"));
        source.sendSystemMessage(Component.literal("ID: " + claim.get().getId()));
        source.sendSystemMessage(Component.literal("Name: " + claimName));
        source.sendSystemMessage(Component.literal("Owner ID: " + claim.get().getOwnerId()));
        source.sendSystemMessage(Component.literal("Owner Name: " + ownerName));
        source.sendSystemMessage(Component.literal("Dimension: " + claim.get().getDimension()));

        String coOwnersList = claim.get().getCoOwners().stream()
                .map(uuid -> {
                    ServerPlayer p = source.getServer().getPlayerList().getPlayer(uuid);
                    return p != null ? player.getScoreboardName() : uuid.toString();
                })
                .collect(Collectors.joining(", "));
        source.sendSystemMessage(Component.literal("Co-owners: " + coOwnersList));

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

    private static int handleAddCoOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String claimName = StringArgumentType.getString(ctx, "claim");
        String coOwnerName = StringArgumentType.getString(ctx, "player");

        CommandSourceStack source = ctx.getSource();
        ServerPlayer invoker = source.getPlayerOrException();

        var claim = ClaimManager.getClaimByName(invoker.getUUID(), claimName);
        if (claim.isEmpty())
        {
            invoker.sendSystemMessage(Component.literal(
                    String.format("§c[BlockProtect] Claim %s doesn't exist.", claimName)
            ));
            return 0;
        }

        ServerPlayer coOwner = source.getServer().getPlayerList().getPlayerByName(coOwnerName);
        if (coOwner == null)
        {
            invoker.sendSystemMessage(Component.literal(
                    String.format("§c[BlockProtect] Player %s is not currently active.", coOwnerName)
            ));
            return 0;
        }

        claim.get().addCoOwner(coOwner.getUUID());
        invoker.sendSystemMessage(Component.literal(
                String.format("§a[BlockProtect] Successfully added %s to %s as a co-owner.", coOwnerName, claimName)
        ));

        return 1;
    }
}
