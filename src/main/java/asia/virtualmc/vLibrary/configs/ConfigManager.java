package asia.virtualmc.vLibrary.configs;

import asia.virtualmc.vLibrary.VLibrary;

public class ConfigManager {
    private final VLibrary vlib;
    private final EXPTableConfig expTableConfig;

    public ConfigManager(VLibrary vlib) {
        this.vlib = vlib;
        this.expTableConfig = new EXPTableConfig(this);
    }

    public VLibrary getVlib() {
        return vlib;
    }

    public EXPTableConfig getPlayerData() {
        return expTableConfig;
    }
}
