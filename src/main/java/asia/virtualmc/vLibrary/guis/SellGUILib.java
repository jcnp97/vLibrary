package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.configs.GUIConfig;
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
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SellGUILib {
    private static Map<String, List<Integer>> pricesCache = new ConcurrentHashMap<>();
    private static Map<String, List<Double>> qualityCache = new ConcurrentHashMap<>();

    public static ItemStack createSellButton(double totalValue) {
        String displayName = "§aSell Items";
        List<String> lore = List.of(
                "§7Current total: §2$" + totalValue
        );

        return GUILib.createLegacyButton(Material.PAPER,
                displayName, GUIConfig.INVISIBLE_ITEM, lore);
    }

    public static double calculateInventoryValue(Map<ItemStack, Double> sellableItems) {
        double sum = sellableItems.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.round(sum * 100.0) / 100.0;
    }

    public static OutlinePane createItemPane(Map<ItemStack, Double> sellableItems) {
        OutlinePane itemPane = new OutlinePane(0, 0, 9, 4);

        for (ItemStack item : sellableItems.keySet()) {
            itemPane.addItem(new GuiItem(item.clone()));
        }

        return itemPane;
    }

    public static boolean isSellable(NamespacedKey key, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(key, PersistentDataType.INTEGER);
    }

    public static Map<ItemStack, Double> getSellableItems(String name, Player player, Plugin plugin) {
        Map<ItemStack, Double> sellableItems = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (isSellable(new NamespacedKey(plugin, "rarity_id"), item)) {
                double value = getItemValue(plugin, name, item) * item.getAmount();
                sellableItems.put(item, value);
            }
        }

        return sellableItems;
    }

    private static double getItemValue(Plugin plugin, String name, ItemStack item) {
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        int rarityID = pdc.getOrDefault(new NamespacedKey(plugin, "rarity_id"), PersistentDataType.INTEGER, 0);
        int qualityID = pdc.getOrDefault(new NamespacedKey(plugin, "quality_id"), PersistentDataType.INTEGER, 0);

        if (qualityID > 0) {
            double weight = pdc.getOrDefault(new NamespacedKey(plugin, "weight"), PersistentDataType.DOUBLE, 0.0);
            return getPriceFromCache(name, rarityID - 1) + getBonusPrice(name, weight, qualityID);
        }

        return getPriceFromCache(name, rarityID - 1);
    }



    private static int getPriceFromCache(String pluginName, int index) {
        List<Integer> prices = pricesCache.get(pluginName);

        if (prices == null) {
            return 0;
        }

        if (index < 0 || index >= prices.size()) {
            return 0;
        }

        return prices.get(index);
    }

    private static double getQualityFromCache(String pluginName, int index) {
        List<Double> quality = qualityCache.get(pluginName);

        if (quality == null) {
            return 0;
        }

        if (index < 0 || index >= quality.size()) {
            return 0;
        }

        return quality.get(index);
    }

    private static double getBonusPrice(String pluginName, double weight, int qualityID) {
        return (weight * getPriceFromCache(pluginName, qualityID - 1));
    }

    public static void addPricesToCache(String pluginName, List<Integer> prices) {
        pricesCache.computeIfAbsent(pluginName, key -> new CopyOnWriteArrayList<>()).addAll(prices);
    }

    public static void addQualityToCache(String pluginName, List<Double> quality) {
        qualityCache.computeIfAbsent(pluginName, key -> new CopyOnWriteArrayList<>()).addAll(quality);
    }

    public static void sellEffects(Player player, double finalValue) {
        EffectsUtil.sendPlayerMessage(player, "<#00FFA2>You have sold your items for <gold>$" + finalValue);
        EffectsUtil.playSound(player, "minecraft:cozyvanilla.sell_confirmed", Sound.Source.PLAYER, 1.0f, 1.0f);
    }
}
