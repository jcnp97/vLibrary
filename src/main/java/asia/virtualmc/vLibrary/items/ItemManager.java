package asia.virtualmc.vLibrary.items;

import asia.virtualmc.vLibrary.VLibrary;
import org.jetbrains.annotations.NotNull;

public class ItemManager {
    private final VLibrary vlib;

    public ItemManager(@NotNull VLibrary vlib) {
        this.vlib = vlib;
    }

    public VLibrary getMain() {
        return vlib;
    }
}
