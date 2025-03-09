package asia.virtualmc.vLibrary.blocks;

import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import asia.virtualmc.vLibrary.utils.HologramUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BlockGeneration {
    public static Location getLocation(@NotNull Plugin plugin, String FILE_NAME, String SECTION_NAME) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try {
                plugin.saveResource(FILE_NAME, false);
            } catch (Exception e) {
                plugin.getLogger().severe(FILE_NAME + " not found!");
                return null;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("blocksList." + SECTION_NAME)) {
            String rawLocation = config.getString("blocksList." + SECTION_NAME + ".location");

            if (rawLocation == null) {
                plugin.getLogger().severe("Block location in " + FILE_NAME + " is null.");
                return null;
            }

            String[] parts = rawLocation.split(";");
            if (parts.length != 2) {
                plugin.getLogger().severe("Invalid block format in " + FILE_NAME  + ". Expected format: <world>;<x, y, z>");
                return null;
            }

            String worldName = parts[0].trim();
            String coordinatesPart = parts[1].trim();
            String[] coordinates = coordinatesPart.split(",");
            if (coordinates.length != 3) {
                plugin.getLogger().severe("Invalid block coordinates in " + FILE_NAME + ". Expected three coordinates separated by commas.");
                return null;
            }
            try {
                double x = Double.parseDouble(coordinates[0].trim());
                double y = Double.parseDouble(coordinates[1].trim());
                double z = Double.parseDouble(coordinates[2].trim());

                World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().severe("World '" + worldName + "' for " + SECTION_NAME + " not found.");
                    return null;
                }

                return new Location(world, x, y, z);

            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Invalid " + SECTION_NAME + " coordinates in " + FILE_NAME);
                return null;
            }
        } else {
            plugin.getLogger().severe("No " + SECTION_NAME + " location found in " + FILE_NAME);
            return null;
        }
    }

    public static List<String> getLore(@NotNull Plugin plugin, String FILE_NAME, String SECTION_PATH) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try {
                plugin.saveResource(FILE_NAME, false);
            } catch (Exception e) {
                plugin.getLogger().severe(FILE_NAME + " not found!");
                return null;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("blocksList." + SECTION_PATH)) {
            return config.getStringList("blocksList." + SECTION_PATH + ".hologram-lore");

        } else {
            plugin.getLogger().severe("No " + SECTION_PATH + " lore found in " + FILE_NAME);
            return null;
        }
    }

    public static void generateBlock(@NotNull Plugin plugin, Location blockLocation, String blockName, List<String> lore) {
        if (blockLocation.getBlockX() == 0 && blockLocation.getBlockY() == 0 && blockLocation.getBlockZ() == 0) {
            plugin.getLogger().severe(blockName + " coordinates are (0, 0, 0). Block will not be created.");
            return;
        }

        try {
            ensureBlockExists(plugin, blockLocation);
            HologramUtils.addHologram(blockName, blockLocation, lore, 0.5, 1.5, 0.5);
            ConsoleMessageUtil.pluginPrint(plugin.getName(), " Successfully generated " + blockName + " at " +
                    HologramUtils.getHologramLocation(blockName));
        } catch (Exception e) {
            plugin.getLogger().severe("There was an error trying to generate " + blockName);
        }
    }

    public static void createBlockConfig(Plugin plugin, Player player,
                                         String FILE_PATH,
                                         String LOCATION_PATH,
                                         String LORE_PATH,
                                         List<String> lore) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock != null) {
            Location blockLocation = targetBlock.getLocation();

            File blocksFile = new File(plugin.getDataFolder(), FILE_PATH);
            YamlConfiguration blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);

            String worldName = blockLocation.getWorld().getName();
            String locationString = String.format("%s;%d,%d,%d",
                    worldName,
                    blockLocation.getBlockX(),
                    blockLocation.getBlockY(),
                    blockLocation.getBlockZ());

            blocksConfig.set(LOCATION_PATH, locationString);

            if (!blocksConfig.contains(LORE_PATH)) {
                blocksConfig.set(LORE_PATH, lore);
            }

            try {
                blocksConfig.save(blocksFile);
                plugin.getLogger().info("Saved new crafting station location: " + locationString);
                player.sendMessage("§aCrafting station has been set successfully!");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save blocks.yml: " + e.getMessage());
                player.sendMessage("§cFailed to save the crafting station location!");
                e.printStackTrace();
            }
        } else {
            player.sendMessage("§cNo block in view to set as a crafting station!");
        }
    }

    private static void ensureBlockExists(Plugin plugin, Location blockLocation) {
        if (blockLocation != null) {
            Block block = blockLocation.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.STONE);
                plugin.getLogger().info("Generated block at " + formatLocation(blockLocation));
            }
        }
    }

    private static String formatLocation(Location location) {
        return String.format("(%.1f, %.1f, %.1f)",
                location.getX(), location.getY(), location.getZ());
    }


}
