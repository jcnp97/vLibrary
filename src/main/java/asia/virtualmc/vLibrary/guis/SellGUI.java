//package asia.virtualmc.vLibrary.guis;
//
//import asia.virtualmc.vLibrary.configs.GUIConfig;
//import com.github.stefvanschie.inventoryframework.gui.GuiItem;
//import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
//import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
//import com.github.stefvanschie.inventoryframework.pane.StaticPane;
//import net.kyori.adventure.sound.Sound;
//import org.bukkit.Material;
//import org.bukkit.NamespacedKey;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.persistence.PersistentDataContainer;
//import org.bukkit.persistence.PersistentDataType;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//public class SellGUI {
//
//    public static void openSellGUI(@NotNull Player player,
//                                   double sellMultiplier,
//                                   double[] dropPrices,
//                                   NamespacedKey ITEM_KEY) {
//
//        double initialValue = calculateInventoryValue(player);
//
//        ChestGui gui = new ChestGui(5, GUIConfig.SELL_TITLE);
//        gui.setOnGlobalClick(event -> event.setCancelled(true));
//
//        StaticPane staticPane = new StaticPane(0, 0, 9, 5);
//
//        // Add sell buttons
//        for (int x = 1; x <= 3; x++) {
//            ItemStack sellButton = createSellButton(initialValue);
//            GuiItem guiItem = new GuiItem(sellButton, event -> processSellAction(player, initialValue));
//            staticPane.addItem(guiItem, x, 4);
//        }
//
//        // Add close buttons
//        for (int x = 5; x <= 7; x++) {
//            ItemStack closeButton = GUILib.createCancelButton();
//            staticPane.addItem(new GuiItem(closeButton, event -> event.getWhoClicked().closeInventory()), x, 4);
//        }
//
//        OutlinePane itemPane = createItemDisplay(player);
//
//        gui.addPane(staticPane);
//        gui.addPane(itemPane);
//        gui.show(player);
//    }
//
//    private static ItemStack createSellButton(double totalValue) {
//        String displayName = "§aSell Items";
//        List<String> lore = List.of(
//                "§7Current total: §2$" + totalValue
//        );
//
//        return GUILib.createDetailedButton(Material.PAPER,
//                displayName, GUIConfig.INVISIBLE_ITEM, lore);
//    }
//
//    private static OutlinePane createItemDisplay(Player player) {
//        OutlinePane itemPane = new OutlinePane(0, 0, 9, 4);
//        Arrays.stream(player.getInventory().getContents())
//                .filter(SellGUI::isSellable)
//                .map(ItemStack::clone)
//                .forEach(itemCopy -> itemPane.addItem(new GuiItem(itemCopy)));
//        return itemPane;
//    }
//
//    private void processSellAction(Player player, double initialValue) {
//        double currentValue = calculateInventoryValue(player);
//
//        if (currentValue != initialValue) {
//            player.sendMessage("§cError: Inventory has changed. Please reopen the GUI.");
//            player.closeInventory();
//            return;
//        }
//
//        try {
//            if (currentValue > 0) {
//                UUID uuid = player.getUniqueId();
//                double taxesPaid = taxDeduction(player, currentValue);
//                sellItems(player, currentValue);
//                statistics.addStatistics(uuid, 20, (int) currentValue);
//                statistics.addStatistics(uuid, 21, (int) taxesPaid);
//                effectsUtil.sendPlayerMessage(uuid, configManager.pluginPrefix + "<#00FFA2>You have sold your items for <gold>$" + currentValue);
//                effectsUtil.sendPlayerMessage(uuid, configManager.pluginPrefix + "<gold>$" + taxesPaid + " <red>has been deducted from your balance.");
//                effectsUtil.playSound(player, "minecraft:cozyvanilla.sell_confirmed", Sound.Source.PLAYER, 1.0f, 1.0f);
//            } else {
//                player.sendMessage("§cNo sellable items found in your inventory!");
//            }
//        } catch (Exception e) {
//            player.sendMessage("§cAn error occurred while processing your sale. Please try again.");
//            plugin.getLogger().severe("Error processing sale for " + player.getName() + ": " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        player.closeInventory();
//    }
//
//
//
//    private int getRarityID(@NotNull ItemStack item) {
//
//    }
//
////    private Integer getCustomId(ItemStack item) {
////        if (item == null || !item.hasItemMeta()) return null;
////        ItemMeta meta = item.getItemMeta();
////        PersistentDataContainer pdc = meta.getPersistentDataContainer();
////        Integer customID = pdc.get(SELL_PRICE, PersistentDataType.INTEGER);
////        return (customID != null && customID >= 1 && customID <= 7) ? customID : null;
////    }
//
//
//    private String getItemRarity(ItemStack item) {
//        Integer customID = getCustomId(item);
//        if (customID == null) return "null";
//
//        return switch (customID) {
//            case 1 -> "Common";
//            case 2 -> "Uncommon";
//            case 3 -> "Rare";
//            case 4 -> "Unique";
//            case 5 -> "Special";
//            case 6 -> "Mythical";
//            case 7 -> "Exotic";
//            default -> "null";
//        };
//    }
//
//    private double calculateInventoryValue(Player player) {
//        UUID uuid = player.getUniqueId();
//        double sum = Arrays.stream(player.getInventory().getContents())
//                .filter(this::isSellable)
//                .mapToDouble(item -> getItemValue(item, uuid) * item.getAmount())
//                .sum();
//
//        return Math.round(sum * 100.0) / 100.0;
//    }
//
//    private void sellItems(Player player, double totalValue) {
//        ItemStack[] contents = player.getInventory().getContents();
//        for (int i = 0; i < contents.length; i++) {
//            ItemStack stack = contents[i];
//            if (isSellable(stack)) {
//                sellLog.logTransaction(player.getName(), getItemRarity(stack), stack.getAmount());
//                player.getInventory().setItem(i, null);
//            }
//        }
//        addEconomy(player, totalValue);
//    }
//
//    //
//    private Map<ItemStack, Double> getSellableItems(Player player, NamespacedKey ITEM_KEY) {
//        UUID uuid = player.getUniqueId();
//        Map<ItemStack, Double> sellableItems = new HashMap<>();
//
//        for (ItemStack item : player.getInventory().getContents()) {
//            if (isSellable(item, ITEM_KEY)) {
//                double value = getItemValue(item, uuid) * item.getAmount();
//                sellableItems.put(item, value);
//            }
//        }
//        return sellableItems;
//    }
//
//    private static boolean isSellable(ItemStack item, NamespacedKey ITEM_KEY) {
//        if (item == null || !item.hasItemMeta()) return false;
//        ItemMeta meta = item.getItemMeta();
//        PersistentDataContainer pdc = meta.getPersistentDataContainer();
//        return pdc.has(ITEM_KEY, PersistentDataType.INTEGER);
//    }
//
//    private double getItemValue(ItemStack item, UUID uuid) {
//        int rarityID = ()
//        Integer customID = getCustomId(item);
//        if (customID == null) return 0;
//
//        return switch (customID) {
//            case 1 -> configManager.dropBasePrice[0] * getDropsData(uuid);
//            case 2 -> configManager.dropBasePrice[1] * getDropsData(uuid);
//            case 3 -> configManager.dropBasePrice[2] * getDropsData(uuid);
//            case 4 -> configManager.dropBasePrice[3] * getDropsData(uuid);
//            case 5 -> configManager.dropBasePrice[4] * getDropsData(uuid);
//            case 6 -> configManager.dropBasePrice[5] * getDropsData(uuid);
//            case 7 -> configManager.dropBasePrice[6] * getDropsData(uuid);
//            default -> 0.0;
//        };
//    }
//}
