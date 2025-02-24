package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class GUIManager {
    private final VLibrary plugin;

    public GUIManager(@NotNull VLibrary plugin) {
        this.plugin = plugin;
    }

    public VLibrary getPlugin() {
        return plugin;
    }
}
