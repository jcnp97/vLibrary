package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.configs.GUIConfig;
import asia.virtualmc.vLibrary.items.ItemsLib;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalvageGUILib {

    public static class Component {
        public List<ItemStack> items;
        public int amount;

        public Component(List<ItemStack> items, int amount) {
            this.items = items;
            this.amount = amount;
        }
    }

    public static ItemStack createSalvageButton(int[] components) {
        String displayName = "<gold>Confirm Salvage";
        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray><st>                                        </st>");

        for (int i = 0; i < components.length; i++) {
            if (components[i] > 0) {
                lore.add("<gray>• " + getComponentName(i + 1) + " × " + components[i]);
            }
        }

        lore.add("<dark_gray><st>                                        </st>");

        return GUILib.createModernButton(Material.PAPER,
                displayName, GUIConfig.INVISIBLE_ITEM, lore);
    }

    public static int[] getComponentList(Map<Integer, Component> salvageableItems) {
        int[] components = new int[7];

        for (int rarityID : salvageableItems.keySet()) {
            components[rarityID - 1] = salvageableItems.get(rarityID).amount;
        }

        return components;
    }

    // GUI Display
    public static OutlinePane createItemPane(Map<Integer, Component> salvageableItems) {
        OutlinePane itemPane = new OutlinePane(0, 0, 9, 4);

        for (Component component : salvageableItems.values()) {
            if (component.amount > 0) {
                for (ItemStack item : component.items) {
                    itemPane.addItem(new GuiItem(item));
                }
            }

        }

        return itemPane;
    }

    public static int getRarityID(NamespacedKey key, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static Map<Integer, Component> getSalvageableItems(Player player, NamespacedKey RARITY_KEY) {
        Map<Integer, Component> salvageableItems = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            int rarityID = getRarityID(RARITY_KEY, item);
            if (rarityID == 0) continue;

            int amount = item.getAmount();

            salvageableItems.merge(rarityID, new Component(new ArrayList<>(List.of(item)), amount),
                    (existing, incoming) -> {
                        existing.items.add(item);
                        existing.amount += amount;
                        return existing;
                    });
        }

        return salvageableItems;
    }

    public static boolean compareComponents(int[] initialComponents, int[] finalComponents) {
        for (int i = 0; i < 7; i++) {
            if (initialComponents[i] != finalComponents[i]) return false;
        }

        return true;
    }

    public static void salvageEffects(Player player, int[] finalComponents) {
        EffectsUtil.playSound(player, "minecraft:block.anvil.use", Sound.Source.PLAYER, 1.0f, 1.0f);

        for (int i = 0; i < finalComponents.length; i++) {
            if (finalComponents[i] > 0) {
                EffectsUtil.sendPlayerMessage(player,
                        "<yellow>You have received " + getComponentName(i + 1) + " <yellow>x" +
                        finalComponents[i] + "<yellow>.");
            }
        }
    }

    public static String getComponentName(int rarityID) {
        switch (rarityID) {
            default -> { return ""; }
            case 1 -> { return "<green>Common Components"; }
            case 2 -> { return "<gray>Uncommon Components"; }
            case 3 -> { return "<aqua>Rare Components"; }
            case 4 -> { return "<gold>Unique Components"; }
            case 5 -> { return "<light_purple>Special Components"; }
            case 6 -> { return "<dark_purple>Mythical Components"; }
            case 7 -> { return "<dark_red>Exotic Components"; }
        }
    }

    public static Material getComponentMaterial(int rarityID) {
        switch (rarityID) {
            default -> { return Material.BARRIER; }
            case 1 -> { return Material.LIME_WOOL; }
            case 2 -> { return Material.LIGHT_GRAY_WOOL; }
            case 3 -> { return Material.LIGHT_BLUE_WOOL; }
            case 4 -> { return Material.YELLOW_WOOL; }
            case 5 -> { return Material.MAGENTA_WOOL; }
            case 6 -> { return Material.PURPLE_WOOL; }
            case 7 -> { return Material.RED_WOOL; }
        }
    }
}
