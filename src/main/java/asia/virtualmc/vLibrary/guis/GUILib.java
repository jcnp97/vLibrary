package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.configs.GUIConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUILib {

    public static ItemStack createButton(Material material, String displayName, int modelData) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setCustomModelData(modelData);
            button.setItemMeta(meta);
        }
        return button;
    }

    public static ItemStack createDetailedButton(Material material, String displayName, int modelData,
                                                 List<String> lore) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setCustomModelData(modelData);
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

    public static ItemStack createNextButton() {
        return createButton(Material.PAPER, "§aNext Page", GUIConfig.INVISIBLE_ITEM);
    }

    public static ItemStack createPreviousButton() {
        return createButton(Material.PAPER, "§aPrevious Page", GUIConfig.INVISIBLE_ITEM);
    }

    public static ItemStack createCloseButton() {
        return createButton(Material.PAPER, "§cClose", GUIConfig.INVISIBLE_ITEM);
    }

    public static ItemStack createCancelButton() {
        return createButton(Material.PAPER, "§cCancel", GUIConfig.INVISIBLE_ITEM);
    }

    public static ItemStack createConfirmButton() {
        return createButton(Material.PAPER, "§aConfirm", GUIConfig.INVISIBLE_ITEM);
    }

    public static String getGUIDesign(int pageNumber, int totalPages) {
        if (pageNumber == 1) {
            return GUIConfig.COLLECTION_TITLE_NEXT;
        } else if (pageNumber == totalPages) {
            return GUIConfig.COLLECTION_TITLE_PREV;
        } else {
            return GUIConfig.COLLECTION_TITLE;
        }
    }
}
