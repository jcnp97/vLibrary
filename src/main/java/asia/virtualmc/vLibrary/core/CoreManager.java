package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class CoreManager {
    private final VLibrary plugin;
    private final EconomyLib economyLib;
    private final PermissionLib permissionLib;
    private final YAMLGenerator yamlGenerator;
    private final ModelGenerator modelGenerator;

    public CoreManager(@NotNull VLibrary plugin) {
        this.plugin = plugin;

        // Initialize components in a controlled order
        this.economyLib = new EconomyLib(this);
        this.permissionLib = new PermissionLib(this);
        this.yamlGenerator = new YAMLGenerator(this);
        this.modelGenerator = new ModelGenerator(this);
    }

    /**
     * Gets the main plugin instance
     *
     * @return The VLibrary plugin instance
     */
    public @NotNull VLibrary getVLibrary() {
        return plugin;
    }

    /**
     * Gets the economy library instance
     *
     * @return The EconomyLib instance
     */
    public @NotNull EconomyLib getEconomyLib() {
        return economyLib;
    }

    /**
     * Gets the permission library instance
     *
     * @return The PermissionLib instance
     */
    public @NotNull PermissionLib getPermissionLib() {
        return permissionLib;
    }

    /**
     * Gets the YAML generator instance
     *
     * @return The YAMLGenerator instance
     */
    public @NotNull YAMLGenerator getYAMLGenerator() {
        return yamlGenerator;
    }

    /**
     * Gets the model generator instance
     *
     * @return The ModelGenerator instance
     */
    public @NotNull ModelGenerator getModelGenerator() {
        return modelGenerator;
    }
}