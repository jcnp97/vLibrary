package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import org.jetbrains.annotations.NotNull;

public class StorageManagerLib {
    private final VLibrary vlib;
    private final DatabaseLib databaseLib;
    private final PlayerDataLib playerDataLib;
    private final OtherDataLib statisticsLib;

    public StorageManagerLib(@NotNull VLibrary vlib) {
        this.vlib = vlib;
        this.databaseLib = DatabaseLib.getInstance(vlib);
        this.playerDataLib = new PlayerDataLib(this);
        this.statisticsLib = new OtherDataLib(this);
    }

    public VLibrary getMain() {
        return vlib;
    }
    //public EffectsUtil getEffectsUtil() { return getMain().getEffectsUtil(); }
    public DatabaseLib getDatabaseLib() { return databaseLib; }
    public PlayerDataLib getPlayerDataLib() {
        return playerDataLib;
    }
    public OtherDataLib getOtherDataLib() {
        return statisticsLib;
    }
}
