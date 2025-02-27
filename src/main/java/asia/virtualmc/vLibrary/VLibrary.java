package asia.virtualmc.vLibrary;

import asia.virtualmc.vLibrary.configs.ConfigManager;
import asia.virtualmc.vLibrary.core.CoreManager;
import asia.virtualmc.vLibrary.guis.GUIManager;
import asia.virtualmc.vLibrary.items.ItemManager;
import asia.virtualmc.vLibrary.storage.DatabaseLib;
import asia.virtualmc.vLibrary.storage.StorageManagerLib;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VLibrary extends JavaPlugin {
    private StorageManagerLib storageManagerLib;
    private DatabaseLib databaseLib;
    private ItemManager itemManager;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private CoreManager coreManager;

    private static Economy econ = null;

    @Override
    public void onEnable() {
        this.storageManagerLib = new StorageManagerLib(this);
        this.itemManager = new ItemManager(this);
        this.databaseLib = DatabaseLib.getInstance(this);
        this.configManager = new ConfigManager(this);
        this.coreManager = new CoreManager(this);
        this.guiManager = new GUIManager(this);

        PacketEvents.getAPI().init();
        CommandAPI.onEnable();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConsoleMessageUtil.pluginPrint("vLibrary has been enabled!");
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .verboseOutput(false)
                .silentLogs(true)
        );

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onDisable() {
        databaseLib.closeConnection();
        CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin not found!");
            return false;
        }
        getLogger().info("Vault was found, attempting to get economy registration...");

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider was registered with Vault!");
            return false;
        }
        econ = rsp.getProvider();
        getLogger().info("Successfully hooked into the economy: " + econ.getName());
        return true;
    }

    public StorageManagerLib getStorageManager() {
        return storageManagerLib;
    }

    public GUIManager getGuiManager() { return guiManager; }

    public CoreManager getCoreManager() { return coreManager; }

    public ItemManager getItemManager() { return itemManager; }

    public ConfigManager getConfigManager() { return configManager; }

    public Economy getEconomy() {
        return econ;
    }
}
