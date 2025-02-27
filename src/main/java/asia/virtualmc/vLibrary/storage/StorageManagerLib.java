package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class StorageManagerLib {
    private final VLibrary vlib;
    private final DatabaseLib databaseLib;
    private final PlayerDataLib playerDataLib;
    private final DoubleDataLib doubleDataLib;
    private final IntegerDataLib integerDataLib;

    public StorageManagerLib(@NotNull VLibrary vlib) {
        this.vlib = vlib;
        this.databaseLib = DatabaseLib.getInstance(vlib);
        this.playerDataLib = new PlayerDataLib(this);
        this.doubleDataLib = new DoubleDataLib(this);
        this.integerDataLib = new IntegerDataLib(this);
    }

    public VLibrary getMain() {
        return vlib;
    }
    public DatabaseLib getDatabaseLib() { return databaseLib; }
    public PlayerDataLib getPlayerDataLib() {
        return playerDataLib;
    }
    public DoubleDataLib getDoubleDataLib() {
        return doubleDataLib;
    }
    public IntegerDataLib getIntegerDataLib() { return integerDataLib; }
}
