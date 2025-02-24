package asia.virtualmc.vLibrary.interfaces;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface ItemInteractHandler {
    void onItemInteractHandler(PlayerInteractEvent event);
    void onBlockPlaceHandler(BlockPlaceEvent event);
}
