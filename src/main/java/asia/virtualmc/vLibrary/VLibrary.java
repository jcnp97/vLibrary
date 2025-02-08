package asia.virtualmc.vLibrary;

import asia.virtualmc.vLibrary.storage.DatabaseLib;
import asia.virtualmc.vLibrary.storage.PlayerDataLib;
import asia.virtualmc.vLibrary.storage.OtherDataLib;
import asia.virtualmc.vLibrary.storage.StorageManagerLib;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class VLibrary extends JavaPlugin {
    private StorageManagerLib storageManagerLib;
    private DatabaseLib databaseLib;
    private EffectsUtil effectsUtil;

    @Override
    public void onEnable() {
        this.storageManagerLib = new StorageManagerLib(this);
        this.effectsUtil = new EffectsUtil(this);
        this.databaseLib = DatabaseLib.getInstance(this);

        ConsoleMessageUtil.pluginPrint("vLibrary has been enabled!");
    }

    @Override
    public void onDisable() {
        databaseLib.closeConnection();
    }

    public PlayerDataLib getPlayerDataLib() {
        return storageManagerLib.getPlayerData();
    }

    public OtherDataLib getStatisticsLib() { return storageManagerLib.getStatisticsLib(); }

    public EffectsUtil getEffectsUtil() { return effectsUtil; }
}
