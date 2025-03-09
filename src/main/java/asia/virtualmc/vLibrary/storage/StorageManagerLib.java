package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class StorageManagerLib {
    private final VLibrary vlib;
    private final LibraryData libraryData;
    private final PlayerDataLib playerDataLib;
    private final StatisticsDataLib statisticsDataLib;
    private final IntegerDataLib integerDataLib;
    private final LocalStorageLib localStorageLib;

    public StorageManagerLib(@NotNull VLibrary vlib) {
        this.vlib = vlib;
        this.libraryData = new LibraryData(vlib);
        this.playerDataLib = new PlayerDataLib(this);
        this.statisticsDataLib = new StatisticsDataLib(this);
        this.integerDataLib = new IntegerDataLib(this);
        this.localStorageLib = new LocalStorageLib(this);
    }

    public VLibrary getMain() {
        return vlib;
    }
    public LibraryData getLibraryData() { return libraryData; }
    public PlayerDataLib getPlayerDataLib() {
        return playerDataLib;
    }
    public StatisticsDataLib getStatisticsDataLib() { return statisticsDataLib; }
    public IntegerDataLib getIntegerDataLib() { return integerDataLib; }
    public LocalStorageLib getLocalStorageLib() { return localStorageLib; }
}
