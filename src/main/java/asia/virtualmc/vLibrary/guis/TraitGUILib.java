package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.utils.DigitUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class TraitGUILib {
    private static final Map<String, List<Traits>> traitsMap = new HashMap<>();
    public record Traits(String trait, int slot, ItemStack item) {}

    public static void storeTraitsFromFile(Plugin plugin, String FILE_PATH) {
        File file = new File(plugin.getDataFolder(), FILE_PATH);
        if (!file.exists()) {
            plugin.getLogger().warning("File " + FILE_PATH + " not found!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection traitSection = config.getConfigurationSection("traitList");

        if (traitSection == null) {
            plugin.getLogger().warning("traitList section not found in " + FILE_PATH);
            return;
        }

        for (String trait : traitSection.getKeys(false)) {
            ConfigurationSection traitConfig = traitSection.getConfigurationSection(trait);
            if (traitConfig == null) continue;

            String displayName = traitConfig.getString("name", trait);
            String materialName = traitConfig.getString("material", "EMERALD");
            int modelData = traitConfig.getInt("custom-model-data", 0);
            int slot = traitConfig.getInt("slot", 0);

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material: " + materialName + " for trait: " + trait);
                material = Material.EMERALD;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.displayName(MiniMessage.miniMessage().deserialize("<!i>"  + displayName));

                if (traitConfig.contains("custom-model-data")) {
                    meta.setCustomModelData(modelData);
                }

                List<String> rawLore = traitConfig.getStringList("lore");
                List<Component> lore = new ArrayList<>();

                if (!rawLore.isEmpty()) {
                    for (String loreLine : rawLore) {
                        lore.add(MiniMessage.miniMessage().deserialize(loreLine));
                    }
                }

                meta.lore(lore);
                item.setItemMeta(meta);
            }

            traitsMap.computeIfAbsent(plugin.getName(), k ->
                    new ArrayList<>()).add(new Traits(displayName, slot, item));
        }
    }

    public static StaticPane getTraitPane(String pluginName, int[] levels, double[] values) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 4);
        int x = 0;

        for (Traits trait : traitsMap.get(pluginName)) {
            ItemStack item = trait.item().clone();
            item.setAmount(Math.max(levels[x], 1));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore();
                if (lore != null) {
                    List<Component> newLore = new ArrayList<>();

                    for (Component component : lore) {
                        String text = MiniMessage.miniMessage().serialize(component);

                        // Look for value placeholders and replace them
                        for (int i = 0; i < values.length; i++) {
                            String placeholder = "{value_" + i + "}";
                            if (text.contains(placeholder)) {
                                String replacement = getReplacement(levels, values, i);
                                text = text.replace(placeholder, replacement);
                            }
                        }

                        Component newComponent = MiniMessage.miniMessage().deserialize("<!i>" + text);
                        newLore.add(newComponent);
                    }

                    meta.lore(newLore);
                }
                item.setItemMeta(meta);
            }

            staticPane.addItem(new GuiItem(item), trait.slot, 1);
            x++;
        }

        return staticPane;
    }

    private static String getReplacement(int[] level, double[] values, int i) {
        double value;
        if (i == 3 || i == 7 || i == 11 || i == 15) {
            return DigitUtils.formattedNoDecimals(values[i]);
        } else if (i <= 2) {
            value = level[0] * values[i];
        } else if (i <= 6) {
            value = level[1] * values[i];
        } else if (i <= 10) {
            value = level[2] * values[i];
        } else if (i <= 14) {
            value = level[3] * values[i];
        } else {
            value = values[i];
        }
        return String.format("%.1f", value);
    }

    public static ItemStack getItemTrait(String pluginName, int index, double[] values) {
        ItemStack item = traitsMap.get(pluginName).get(index).item().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore != null) {
                List<Component> newLore = new ArrayList<>();

                for (Component component : lore) {
                    String text = MiniMessage.miniMessage().serialize(component);

                    for (int i = 0; i < 4; i++) {
                        String placeholder = "{value_" + (i + index * 4) + "}";
                        if (text.contains(placeholder)) {
                            String replacement = String.format("%.1f", values[i]);
                            text = text.replace(placeholder, replacement);
                        }
                    }

                    Component newComponent = MiniMessage.miniMessage().deserialize("<!i>" + text);
                    newLore.add(newComponent);
                }

                meta.lore(newLore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }
}
