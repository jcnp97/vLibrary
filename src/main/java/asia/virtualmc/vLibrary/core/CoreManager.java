package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class CoreManager {
    private final VLibrary plugin;
    private final YAMLGenerator yamlGenerator;
    private final EconomyLib economyLib;
    private final ModelGenerator modelGenerator;

    public CoreManager(@NotNull VLibrary plugin) {
        this.plugin = plugin;
        this.economyLib = new EconomyLib(this);
        this.yamlGenerator = new YAMLGenerator(this);
        this.modelGenerator = new ModelGenerator(this);
    }

    public VLibrary getVLibrary() {
        return plugin;
    }

    public EconomyLib getEconomyLib() {
        return economyLib;
    }
}
