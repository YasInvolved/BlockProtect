package pl.yasinvolved.blockprotect;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import pl.yasinvolved.blockprotect.async.LogQueueManager;
import pl.yasinvolved.blockprotect.claim.ClaimManager;
import pl.yasinvolved.blockprotect.commands.ProtectCommands;
import pl.yasinvolved.blockprotect.items.WandItem;
import pl.yasinvolved.blockprotect.storage.DatabaseManager;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;

import java.nio.file.Path;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Blockprotect.MODID)
public class Blockprotect {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "blockprotect";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static DatabaseManager DATABASE;
    public static LogQueueManager LOG_QUEUE;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredHolder<Item, WandItem> PROTECT_WAND = ITEMS.register("protect_wand",
            () -> new WandItem(new Item.Properties()));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Blockprotect(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommand);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Initializing BlockProtect.");
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Initializing database and loading protected areas.");

        Path dbPath = FMLPaths.CONFIGDIR.get().resolve(MODID).resolve("blockprotect_data.db");
        DATABASE = new DatabaseManager(dbPath);
        LOG_QUEUE = new LogQueueManager(DATABASE, 5);

        List<ClaimEntity> savedClaims = DATABASE.loadAllClaims();
        ClaimManager.loadAll(savedClaims);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Flushing the queue and saving protected areas.");
        LOG_QUEUE.shutdown();
        DATABASE.close();
    }

    private void onRegisterCommand(RegisterCommandsEvent event) {
        ProtectCommands.register(event.getDispatcher());
    }
}
