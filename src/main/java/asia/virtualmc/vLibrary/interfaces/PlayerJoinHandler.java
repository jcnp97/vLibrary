package asia.virtualmc.vLibrary.interfaces;

import org.bukkit.event.player.PlayerQuitEvent;

public interface PlayerJoinHandler {
    void onPlayerJoinHandler(org.bukkit.event.player.PlayerJoinEvent event);
    void onPlayerQuitHandler(PlayerQuitEvent event);
}
