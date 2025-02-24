package asia.virtualmc.vLibrary.interfaces;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface DataHandlingLib {
    void loadPlayerData(UUID uuid);
    void updatePlayerData(UUID uuid);
    void updateAllData();
    void unloadData(UUID uuid);
    void updateEXP(Player player, EnumsLib.UpdateType type, double value, boolean useBXP);
    void updateLevel(Player player, EnumsLib.UpdateType type, int value);
    void updateXPM(Player player, EnumsLib.UpdateType type, double value);
    void updateBXP(Player player, EnumsLib.UpdateType type, double value);
    void updateTraitPoints(Player player, EnumsLib.UpdateType type, int value);
    void updateTalentPoints(Player player, EnumsLib.UpdateType type, int value);
}
