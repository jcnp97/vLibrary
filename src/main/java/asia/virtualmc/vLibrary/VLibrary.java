package asia.virtualmc.vLibrary;

import org.bukkit.plugin.java.JavaPlugin;

public final class VLibrary extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("vLibrary has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
