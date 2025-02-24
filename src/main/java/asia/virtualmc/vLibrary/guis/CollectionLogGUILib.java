//package asia.virtualmc.vLibrary.guis;
//
//import asia.virtualmc.vLibrary.VLibrary;
//import asia.virtualmc.vLibrary.configs.GUIConfig;
//import com.github.stefvanschie.inventoryframework.gui.GuiItem;
//import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
//import com.github.stefvanschie.inventoryframework.pane.StaticPane;
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//public class CollectionLogGUILib {
//    private final VLibrary plugin;
//    private static final int ITEMS_PER_PAGE = 45;
//    private static final int GUI_ROWS = 6;
//    private static final int GUI_COLUMNS = 9;
//    private static final int CONTENT_ROWS = 5;
//
//    private final WeakHashMap<UUID, ChestGui> activeGUIs = new WeakHashMap<>();
//
//    public CollectionLogGUILib(@NotNull VLibrary vlib) {
//        this.plugin = vlib;
//    }
//
//    public void openCollectionLog(@NotNull Player player,
//                                  @NotNull Map<Integer, ItemStack> collectionsMap,
//                                  @NotNull Map<Integer, Integer> playerCollections,
//                                  int totalCollections,
//                                  int pageNumber) {
//
//        int totalPages = Math.max(1, (totalCollections + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
//        pageNumber = Math.min(Math.max(1, pageNumber), totalPages);
//
//        UUID uuid = player.getUniqueId();
//        ChestGui gui = new ChestGui(GUI_ROWS, getGUIDesign(pageNumber, totalPages));
//        gui.setOnGlobalClick(event -> event.setCancelled(true));
//
//        StaticPane staticPane = new StaticPane(0, 0, GUI_COLUMNS, GUI_ROWS);
//
//        // Add collection items
//        int startIndex = (pageNumber - 1) * ITEMS_PER_PAGE;
//        for (int row = 0; row < CONTENT_ROWS; row++) {
//            for (int col = 0; col < GUI_COLUMNS; col++) {
//                int itemID = startIndex + (row * GUI_COLUMNS) + col + 1;
//                if (itemID <= totalCollections) {
//                    Integer amount = playerCollections.getOrDefault(itemID, 0);
//                    ItemStack collectionItem = amount > 0 ? createCollectionItem(itemID, amount, collectionsMap)
//                            : createCollectionItemNew(itemID, collectionsMap);
//                    staticPane.addItem(new GuiItem(collectionItem), col, row);
//                }
//            }
//        }
//
//        addNavigationButtons(staticPane, pageNumber, totalPages, player, collectionsMap, playerCollections, totalCollections);
//
//        // Close button
//        for (int x = 3; x <= 5; x++) {
//            staticPane.addItem(new GuiItem(createCloseButton(), event -> event.getWhoClicked().closeInventory()), x, 5);
//        }
//
//        gui.addPane(staticPane);
//        gui.show(player);
//
//        // Store the GUI reference to prevent memory leaks
//        activeGUIs.put(uuid, gui);
//    }
//
//    private void addNavigationButtons(StaticPane pane, int currentPage, int totalPages, Player player,
//                                      Map<Integer, ItemStack> collectionsMap, Map<Integer, Integer> playerCollections, int totalCollections) {
//        if (currentPage < totalPages) {
//            pane.addItem(new GuiItem(createNextButton(), event -> {
//                event.getWhoClicked().closeInventory();
//                Bukkit.getScheduler().runTaskLater(plugin, () ->
//                        openCollectionLog(player, collectionsMap, playerCollections, totalCollections, currentPage + 1), 1L);
//            }), 6, 5);
//        }
//
//        if (currentPage > 1) {
//            pane.addItem(new GuiItem(createPreviousButton(), event -> {
//                event.getWhoClicked().closeInventory();
//                Bukkit.getScheduler().runTaskLater(plugin, () ->
//                        openCollectionLog(player, collectionsMap, playerCollections, totalCollections, currentPage - 1), 1L);
//            }), 2, 5);
//        }
//    }
//
//    private String getGUIDesign(int pageNumber, int totalPages) {
//        if (pageNumber == 1) return GUIConfig.COLLECTION_TITLE_NEXT;
//        if (pageNumber == totalPages) return GUIConfig.COLLECTION_TITLE_PREV;
//        return GUIConfig.COLLECTION_TITLE;
//    }
//
//    private ItemStack createCollectionItem(int collectionID, int amount, Map<Integer, ItemStack> collectionsMap) {
//        ItemStack item = new ItemStack(Material.FLINT);
//        ItemMeta meta = item.getItemMeta();
//        if (meta == null) return item;
//
//        meta.setDisplayName("§6" + getDisplayName(collectionID, collectionsMap));
//        meta.setCustomModelData(99999 + collectionID);
//        List<String> lore = new ArrayList<>(collectionsMap.get(collectionID).getLore());
//        lore.add("§7Acquired: §e" + amount);
//        meta.setLore(lore);
//        item.setItemMeta(meta);
//
//        return item;
//    }
//
//    private ItemStack createCollectionItemNew(int collectionID, Map<Integer, ItemStack> collectionsMap) {
//        ItemStack item = new ItemStack(Material.PAPER);
//        ItemMeta meta = item.getItemMeta();
//        if (meta == null) return item;
//
//        meta.setDisplayName("§6" + getDisplayName(collectionID, collectionsMap));
//        meta.setCustomModelData(100020);
//        meta.setLore(new ArrayList<>(collectionsMap.get(collectionID).getLore()));
//        item.setItemMeta(meta);
//
//        return item;
//    }
//
//    private ItemStack createNextButton() {
//        return createButton("§aNext Page", GUIConfig.INVISIBLE_ITEM);
//    }
//
//    private ItemStack createPreviousButton() {
//        return createButton("§aPrevious Page", GUIConfig.INVISIBLE_ITEM);
//    }
//
//    private ItemStack createCloseButton() {
//        return createButton("§cClose", GUIConfig.INVISIBLE_ITEM);
//    }
//
//    private ItemStack createButton(String displayName, int modelData) {
//        ItemStack item = new ItemStack(Material.PAPER);
//        ItemMeta meta = item.getItemMeta();
//        if (meta != null) {
//            meta.setDisplayName(displayName);
//            meta.setCustomModelData(modelData);
//            item.setItemMeta(meta);
//        }
//        return item;
//    }
//
//    private String getDisplayName(int collectionID, Map<Integer, ItemStack> collectionsMap) {
//        return Optional.ofNullable(collectionsMap.get(collectionID))
//                .map(ItemStack::getItemMeta)
//                .map(ItemMeta::getDisplayName)
//                .orElse("Unknown Item");
//    }
//}
