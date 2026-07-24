package pl.yasinvolved.blockprotect.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;

import java.util.ArrayList;

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
}
