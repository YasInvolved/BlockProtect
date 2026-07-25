package pl.yasinvolved.blockprotect.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.storage.dbentities.ClaimEntity;

import java.util.ArrayList;
import java.util.List;

public class ClaimSuggestions {
    public static final SuggestionProvider<CommandSourceStack> PLAYER_CLAIMS = (ctx, builder) -> {
        CommandSourceStack source = ctx.getSource();

        boolean isAdmin = source.hasPermission(2);
        if (source.isPlayer())
        {
            String playerId = source.getPlayer().getUUID().toString();
            var claimNames = ClaimManager.getClaimsOfPlayer(playerId).stream().map(ClaimEntity::getName).toList();
            return SharedSuggestionProvider.suggest(claimNames, builder);
        }

        return SharedSuggestionProvider.suggest(new ArrayList<String>(), builder);
    };

    public static final SuggestionProvider<CommandSourceStack> ALL_PLAYERS = (ctx, builder) -> {
        CommandSourceStack source = ctx.getSource();

        if (source.isPlayer() && (source.getPlayer() instanceof ServerPlayer player)) {
            String senderName = player.getName().getString();
            var names = source.getOnlinePlayerNames().stream()
                    .filter(name -> !name.equalsIgnoreCase(senderName));
            return SharedSuggestionProvider.suggest(names, builder);
        }

        return SharedSuggestionProvider.suggest(new ArrayList<String>(), builder);
    };
}
