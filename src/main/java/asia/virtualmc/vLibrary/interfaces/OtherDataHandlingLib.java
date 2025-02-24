package asia.virtualmc.vLibrary.interfaces;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface OtherDataHandlingLib {
    void loadPlayerData(UUID uuid);
    void updatePlayerData(UUID uuid);
    void updateAllData();
    void unloadData(UUID uuid);
}
