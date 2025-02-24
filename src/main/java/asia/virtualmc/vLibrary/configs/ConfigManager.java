package asia.virtualmc.vLibrary.configs;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {
    private final VLibrary vlib;
    private final EXPTableConfig expTableConfig;
    private final GUIConfig guiConfig;

    public ConfigManager(@NotNull VLibrary vlib) {
        this.vlib = vlib;
        this.expTableConfig = new EXPTableConfig(this);
        this.guiConfig = new GUIConfig(this);
    }

    public VLibrary getVlib() {
        return vlib;
    }

    public EXPTableConfig getExpTableConfig() {
        return expTableConfig;
    }
}
