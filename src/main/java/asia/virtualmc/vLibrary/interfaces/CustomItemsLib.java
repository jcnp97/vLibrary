package asia.virtualmc.vLibrary.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CustomItemsLib {
    void createItems();
    void giveItem(Player player, String itemName, int amount);
    void reloadConfig();
    List<String> getItemNames();
}
