package asia.virtualmc.vLibrary.interfaces;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface DataHandlingLib {
    void updatePlayerData(UUID uuid); // Stores data from hashmap to database
    void updateAllData(); // Stores all data from hashmap to database
    void unloadData(UUID uuid); // Stores data to database before unloading from hashmap
    void updateEXP(Player player, EnumsLib.UpdateType type, double value);
    void updateLevel(Player player, EnumsLib.UpdateType type, int value);
    void updateXPM(Player player, EnumsLib.UpdateType type, double value);
    void updateBXP(Player player, EnumsLib.UpdateType type, double value);
    void updateTraitPoints(Player player, EnumsLib.UpdateType type, int value);
    void updateTalentPoints(Player player, EnumsLib.UpdateType type, int value);
    void updateNumericalRank(Player player, EnumsLib.UpdateType type, int value);
    void updateLuck(Player player, EnumsLib.UpdateType type, int value);
    void addWisdomTrait(Player player, int value);
    void addCharismaTrait(Player player, int value);
    void addKarmaTrait(Player player, int value);
    void addDexterityTrait(Player player, int value);
}
