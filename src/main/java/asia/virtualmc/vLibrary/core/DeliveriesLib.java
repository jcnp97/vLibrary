package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.configs.GUIConfig;
import asia.virtualmc.vLibrary.guis.GUILib;
import asia.virtualmc.vLibrary.items.ItemsLib;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.*;

public class DeliveriesLib {
    private static final Random random = new Random();

    public static List<ItemStack> getVanillaItems(int min, int max) {
        int[] range = validateRange(min, max);

        int count = random.nextInt(range[0], range[1] + 1);
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Material material = getRandomMaterial();
            int quantity = random.nextInt(1, 256);
            ItemStack item = new ItemStack(material);

            while (quantity > 0) {
                int amount = Math.min(64, quantity);
                ItemStack stack = item.clone();
                stack.setAmount(amount);
                items.add(stack);
                quantity -= amount;
            }
        }

        return items;
    }

    public static int[] validateRange(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        } else if (min == max) {
            min -= 1;
        }

        return new int[]{min, max};
    }

    public static Material getRandomMaterial() {
        Material[] materials = {Material.COD, Material.PUFFERFISH, Material.TROPICAL_FISH};
        return materials[random.nextInt(materials.length)];
    }

    public static List<Integer> getRarityIDs(int level) {
        List<Integer> rarityIDs = new ArrayList<>();
        rarityIDs.add(1);
        if (level >= 10) rarityIDs.add(2);
        if (level >= 20) rarityIDs.add(3);
        if (level >= 30) rarityIDs.add(4);
        if (level >= 40) rarityIDs.add(5);
        if (level >= 50) rarityIDs.add(6);
        if (level >= 60) rarityIDs.add(7);

        return rarityIDs;
    }

    public static List<Integer> getRegionIDs(int level) {
        List<Integer> regionIDs = new ArrayList<>();
        regionIDs.add(1);
        regionIDs.add(2);
        if (level >= 10) regionIDs.add(3);
        if (level >= 20) regionIDs.add(4);
        if (level >= 30) regionIDs.add(5);
        if (level >= 40) regionIDs.add(6);
        if (level >= 50) regionIDs.add(7);
        if (level >= 60) regionIDs.add(8);

        return regionIDs;
    }

    public static int getMaxTasks(int level) {
        int max = 6;
        if (level >= 10) max += 1;
        if (level >= 20) max += 1;
        if (level >= 30) max += 1;
        if (level >= 40) max += 1;
        if (level >= 50) max += 1;
        if (level >= 60) max += 1;

        return max;
    }

    public static int getRandomID(List<Integer> numbers) {
        return numbers.get(random.nextInt(numbers.size()));
    }

    public static List<String> getDeliveryLore(List<ItemStack> items, long duration) {
        List<String> lore = new ArrayList<>();
        lore.add("§8§m                                        ");

        for (ItemStack item : items) {
            lore.add("§7• " + ItemsLib.getDisplayName(item) + " §fx" + ItemsLib.getItemStackAmount(item));
        }
        lore.add("§8§m                                        ");
        lore.add("§7Expires in: §c" + DigitUtils.formatDuration(duration));

        return lore;
    }

    public static List<String> getDeliveryLoreVanilla(List<ItemStack> items, long duration) {
        List<String> lore = new ArrayList<>();
        Map<ItemStack, Integer> vanillaItems = new HashMap<>();

        for (ItemStack item : items) {
            ItemStack clone = item.clone();
            clone.setAmount(1);
            vanillaItems.merge(clone, item.getAmount(), Integer::sum);
        }

        lore.add("§8§m                                        ");

        for (Map.Entry<ItemStack, Integer> entry : vanillaItems.entrySet()) {
            lore.add("§7• " + ItemsLib.getDisplayName(entry.getKey()) + " §fx" + entry.getValue());
        }
        lore.add("§8§m                                        ");
        lore.add("§7Expires in: §c" + DigitUtils.formatDuration(duration));

        return lore;
    }

    public static ItemStack createTaskButton(int taskID, long expiration, List<ItemStack> items, boolean completed) {
        long currentTime = Instant.now().getEpochSecond();

        if (completed) {
            List<String> lore = List.of(
                    "§7Refreshes in: §e" + DigitUtils.formatDuration(expiration - currentTime)
            );

            return GUILib.createLegacyButton(Material.BARRIER,
                    "§aDelivery Completed!", 1, lore);
        }

        String displayName = "§eDelivery #§a" + taskID;
        List<String> lore;
        if (taskID <= 3) {
            lore = getDeliveryLoreVanilla(items, expiration - currentTime);
        } else {
            lore = getDeliveryLore(items, expiration - currentTime);
        }
        return GUILib.createLegacyButton(Material.FLOW_BANNER_PATTERN,
                displayName, 1, lore);
    }

    public static Set<Integer> getAllIDs(List<ItemStack> items, NamespacedKey ITEM_KEY) {
        Set<Integer> itemIDs = new HashSet<>();

        for (ItemStack item : items) {
            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            itemIDs.add(itemID);
        }

        return itemIDs;
    }

    // Requires Snapshot inventory from InventoryUtils
    public static Map<Integer, Integer> getInventoryItems(NamespacedKey ITEM_KEY, Map<Integer, ItemStack> snapshot) {
        Map<Integer, Integer> inventoryItems = new HashMap<>();

        for (ItemStack item : snapshot.values()) {
            if (item == null) continue;

            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            if (itemID > 0) {
                inventoryItems.merge(itemID, item.getAmount(), Integer::sum);
            }

        }

        return inventoryItems;
    }

    // Map<itemID, amount> inventory & List<ItemStack> items (required items)
    public static boolean hasRequiredItems(NamespacedKey ITEM_KEY, Map<Integer, Integer> inventory, List<ItemStack> items) {
        Map<Integer, Integer> copy = new HashMap<>(inventory);

        for (ItemStack item : items) {
            if (item == null) continue;

            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            if (itemID > 0) {
                if (copy.get(itemID) >= item.getAmount()) {
                    copy.merge(itemID, -item.getAmount(), Integer::sum);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public static Pair<OutlinePane, Boolean> getItemDisplay(NamespacedKey ITEM_KEY, Map<Integer, Integer> inventory, List<ItemStack> items) {
        OutlinePane pane = new OutlinePane(0, 0, 9, 4);
        Map<Integer, Integer> copy = new HashMap<>(inventory);
        boolean canConfirm = true;

        for (ItemStack item : items) {
            if (item == null) continue;

            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            if (itemID > 0) {
                if (copy.get(itemID) != null && copy.get(itemID) >= item.getAmount()) {
                    pane.addItem(new GuiItem(item.clone()));
                    copy.merge(itemID, -item.getAmount(), Integer::sum);
                } else {
                    ItemStack newItem = cloneItem(Material.BARRIER, item);
                    pane.addItem(new GuiItem(newItem));
                    canConfirm = false;
                }
            }
        }

        return Pair.of(pane, canConfirm);
    }

    public static Map<Integer, Integer> convertItemStackToMap(List<ItemStack> items, NamespacedKey ITEM_KEY) {
        Map<Integer, Integer> requiredItems = new HashMap<>();

        for (ItemStack item : items) {
            if (item == null) continue;

            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            if (itemID > 0) {
                requiredItems.merge(itemID, item.getAmount(), Integer::sum);
            }
        }

        return requiredItems;
    }

    // For Vanilla Items
    public static Set<Material> getMaterials(List<ItemStack> items) {
        Set<Material> materials = new HashSet<>();

        for (ItemStack item : items) {
            materials.add(item.getType());
        }

        return materials;
    }

    // Requires snapshot inventory from InventoryUtils
    public static Map<Material, Integer> getInventoryItems(Map<Integer, ItemStack> snapshot) {
        Map<Material, Integer> inventoryItems = new HashMap<>();

        for (ItemStack item : snapshot.values()) {
            if (item == null) continue;

            inventoryItems.merge(item.getType(), item.getAmount(), Integer::sum);
        }

        return inventoryItems;
    }

    // Map<Material, amount> inventory & List<Material> items (required items)
    public static boolean hasRequiredItems(Map<Material, Integer> inventory, List<ItemStack> items) {
        Map<Material, Integer> copy = new HashMap<>(inventory);

        for (ItemStack item : items) {
            if (item == null) continue;

            if (copy.get(item.getType()) >= item.getAmount()) {
                copy.merge(item.getType(), -item.getAmount(), Integer::sum);
            } else {
                return false;
            }
        }

        return true;
    }

    public static Pair<OutlinePane, Boolean> getItemDisplay(Map<Material, Integer> inventory, List<ItemStack> items) {
        OutlinePane pane = new OutlinePane(0, 0, 9, 4);
        Map<Material, Integer> copy = new HashMap<>(inventory);
        boolean canConfirm = true;

        for (ItemStack item : items) {
            if (item == null) continue;

            if (copy.get(item.getType()) != null && copy.get(item.getType()) >= item.getAmount()) {
                pane.addItem(new GuiItem(item.clone()));
                copy.merge(item.getType(), -item.getAmount(), Integer::sum);
            } else {
                ItemStack newItem = cloneItem(Material.BARRIER, item);
                pane.addItem(new GuiItem(newItem));
                canConfirm = false;
            }
        }

        return Pair.of(pane, canConfirm);
    }

    // Miscellaneous Methods
    public static ItemStack cloneItem(Material material, ItemStack item) {
        ItemStack newItem = new ItemStack(material, item.getAmount());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return newItem;

            ItemMeta newMeta = newItem.getItemMeta();
            if (newMeta == null) return newItem;

            newMeta.setDisplayName(meta.getDisplayName());
            newMeta.setLore(meta.getLore());
            newMeta.setCustomModelData(meta.getCustomModelData());

            newItem.setItemMeta(newMeta);
        }

        return newItem;
    }

    public static StaticPane getDeliverButton(boolean canConfirm) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 4);

        for (int x = 1; x <= 3; x++) {
            GuiItem guiItem;
            if (canConfirm) {
                ItemStack button = GUILib.createConfirmButton();
                guiItem = new GuiItem(button);
            } else {
                ItemStack button = GUILib.createButton(
                        Material.PAPER, "§cNo enough items!", GUIConfig.INVISIBLE_ITEM);
                guiItem = new GuiItem(button);
            }
            staticPane.addItem(guiItem, x, 3);
        }

        return  staticPane;
    }

    public static StaticPane getCloseButton() {
        StaticPane staticPane = new StaticPane(0, 0, 9, 4);

        for (int x = 5; x <= 7; x++) {
            ItemStack closeButton = GUILib.createCancelButton();
            staticPane.addItem(new GuiItem(closeButton), x, 3);
        }

        return staticPane;
    }

    public static ChestGui getGUIDisplay(boolean canConfirm) {
        ChestGui gui;
        if (canConfirm) {
            gui = new ChestGui(4, GUIConfig.DELIVERIES_CONFIRM);
        } else {
            gui = new ChestGui(4, GUIConfig.DELIVERIES_NO_CONFIRM);
        }

        gui.setOnGlobalClick(event -> event.setCancelled(true));

        return gui;
    }
}
