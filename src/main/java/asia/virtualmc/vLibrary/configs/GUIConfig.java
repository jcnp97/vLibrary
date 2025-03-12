package asia.virtualmc.vLibrary.configs;

import asia.virtualmc.vLibrary.VLibrary;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GUIConfig {
    private final VLibrary vlib;
    // GUI Variables
    public static int INVISIBLE_ITEM;
    public static String SALVAGE_TITLE;
    public static String COMPONENTS_TITLE;
    public static String TRAITS_TITLE;
    public static String TRAITS_UPGRADE_TITLE;
    public static String RANK_TITLE;
    public static String SELL_TITLE;
    public static String CONFIRM_TITLE;
    public static String COLLECTION_TITLE;
    public static String COLLECTION_TITLE_NEXT;
    public static String COLLECTION_TITLE_PREV;
    public static String CRAFTING_TITLE;
    public static String TALENT_TREE_TITLE;
    // Plugin Exclusives
    public static String RESTORATION_TITLE;
    public static String ARTEFACT_INTERACT_TITLE;
    public static String DELIVERIES_TITLE;
    public static String DELIVERIES_CONFIRM;
    public static String DELIVERIES_NO_CONFIRM;


    public GUIConfig(@NotNull ConfigManager configManager) {
        this.vlib = configManager.getVlib();
        readGUISettings();
    }

    public void readGUISettings() {
        File dropsFile = new File(vlib.getDataFolder(), "gui-settings.yml");
        if (!dropsFile.exists()) {
            try {
                vlib.saveResource("gui-settings.yml", false);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration gui = YamlConfiguration.loadConfiguration(dropsFile);
        try {
            INVISIBLE_ITEM = gui.getInt("guiSettings.invisible-model-data", 1);
            SALVAGE_TITLE = gui.getString("guiSettings.salvage-gui-title", "Salvage GUI");
            COMPONENTS_TITLE = gui.getString("guiSettings.comp-gui-title", "Components GUI");
            TRAITS_TITLE = gui.getString("guiSettings.trait-gui-title", "Traits GUI");
            TRAITS_UPGRADE_TITLE = gui.getString("guiSettings.trait-up-gui-title", "Traits Upgrade");
            RANK_TITLE = gui.getString("guiSettings.rank-gui-title", "Rank GUI");
            SELL_TITLE = gui.getString("guiSettings.sell-gui-title", "Sell GUI");
            CONFIRM_TITLE = gui.getString("guiSettings.confirm-gui-title", "Are you sure?");
            COLLECTION_TITLE_NEXT = gui.getString("guiSettings.collection-next-title", "Collection Log");
            COLLECTION_TITLE_PREV = gui.getString("guiSettings.collection-prev-title", "Collection Log");
            COLLECTION_TITLE = gui.getString("guiSettings.collection-title", "Collection Log");
            CRAFTING_TITLE = gui.getString("guiSettings.crafting-station-title", "Crafting Station");
            TALENT_TREE_TITLE = gui.getString("guiSettings.talent-tree-title", "Talent Tree");

            // ARCHAEOLOGY EXCLUSIVES
            RESTORATION_TITLE = gui.getString("guiSettings.artefact-restore-gui-title", "Restore Artefacts GUI");
            ARTEFACT_INTERACT_TITLE = gui.getString("guiSettings.artefact-interact-title", "Item Interaction");

            // FISHING EXCLUSIVES
            DELIVERIES_TITLE = gui.getString("guiSettings.deliveries-title", "Deliveries GUI");
            DELIVERIES_CONFIRM = gui.getString("guiSettings.deliveries-confirm", "Deliveries GUI");
            DELIVERIES_NO_CONFIRM = gui.getString("guiSettings.deliveries-no-confirm", "Deliveries GUI");
        } catch (Exception e) {
            vlib.getLogger().severe("[vLibrary] Couldn't load gui-settings.yml");
            e.printStackTrace();
        }
    }
}
