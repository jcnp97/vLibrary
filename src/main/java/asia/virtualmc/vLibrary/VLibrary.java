package asia.virtualmc.vLibrary;

import asia.virtualmc.vLibrary.configs.ConfigManager;
import asia.virtualmc.vLibrary.core.CoreManager;
import asia.virtualmc.vLibrary.guis.GUIManager;
import asia.virtualmc.vLibrary.items.ItemManager;
import asia.virtualmc.vLibrary.storage.LibraryData;
import asia.virtualmc.vLibrary.storage.StorageManagerLib;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VLibrary extends JavaPlugin {
    private StorageManagerLib storageManagerLib;
    private ItemManager itemManager;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private CoreManager coreManager;

    private static Economy econ = null;
    private static Permission perm = null;

    @Override
    public void onEnable() {
        if (!setupVault() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.storageManagerLib = new StorageManagerLib(this);
        this.itemManager = new ItemManager(this);
        this.configManager = new ConfigManager(this);
        this.coreManager = new CoreManager(this);
        this.guiManager = new GUIManager(this);

        PacketEvents.getAPI().init();
        CommandAPI.onEnable();

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
        storageManagerLib.getLibraryData().closeConnection();
        CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin not found!");
            return false;
        }
        getLogger().info("Vault was found, attempting to get economy registration...");
        getLogger().info("Vault was found, attempting to get permission registration...");

        RegisteredServiceProvider<Economy> rspEconomy = getServer().getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> rspPermission = getServer().getServicesManager().getRegistration(Permission.class);
        if (rspEconomy == null) {
            getLogger().warning("No economy provider was registered with Vault!");
            return false;
        }

        if (rspPermission == null) {
            getLogger().warning("No permission provider was registered with Vault!");
            return false;
        }

        econ = rspEconomy.getProvider();
        perm = rspPermission.getProvider();
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

    public Permission getPermission() { return perm; }
}
