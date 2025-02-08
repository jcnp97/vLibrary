package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.utils.EffectsUtil;

public class StorageManagerLib {
    private final VLibrary plugin;
    private final DatabaseLib databaseLib;
    private final PlayerDataLib playerDataLib;
    private final OtherDataLib statisticsLib;

    public StorageManagerLib(VLibrary plugin) {
        this.plugin = plugin;
        this.databaseLib = DatabaseLib.getInstance(plugin);
        this.playerDataLib = new PlayerDataLib(this);
        this.statisticsLib = new OtherDataLib(this);
    }

    public VLibrary getMain() {
        return plugin;
    }
    public EffectsUtil getEffectsUtil() { return getMain().getEffectsUtil(); }
    public DatabaseLib getDatabaseLib() { return databaseLib; }
    public PlayerDataLib getPlayerData() {
        return playerDataLib;
    }
    public OtherDataLib getStatisticsLib() {
        return statisticsLib;
    }
}
