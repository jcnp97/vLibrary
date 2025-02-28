package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class ModelGenerator {
    private final CoreManager coreManager;
    private final VLibrary plugin;

    public ModelGenerator(@NotNull CoreManager coreManager) {
        this.coreManager = coreManager;
        this.plugin = coreManager.getVLibrary();
        generateAllModelFiles();
    }

    /**
     * Converts a raw name by lowercasing, removing punctuation (including apostrophes),
     * and replacing whitespace with underscores.
     */
    private static String convertCollectionName(String rawName) {
        // Lowercase
        String name = rawName.toLowerCase();
        // Remove any character that is not a letter, digit, space, or underscore
        name = name.replaceAll("[^a-z0-9\\s_]", "");
        // Replace whitespace with underscore
        name = name.replaceAll("\\s+", "_");
        return name;
    }

    /**
     * Generates model JSON files for a dependent plugin.
     *
     * @param plugin The plugin requesting model generation
     * @param pathName The texture path (e.g. "namespace:item/collection")
     * @param material The Minecraft material to create overrides for
     * @param names List of raw names to generate models for
     * @param startingModelData The first custom model data value to use
     * @return Map of generated custom model data values to file names
     */
    public static void generateModelsForPlugin(
            @NotNull Plugin plugin,
            @NotNull String pathName,
            @NotNull String material,
            @NotNull List<String> names,
            int startingModelData) {

        if (names.isEmpty()) {
            plugin.getLogger().warning("No names provided for model generation");
            return;
        }

        if (material.isEmpty()) {
            plugin.getLogger().warning("No material specified for model generation");
            return;
        }

        // Process the texture path
        String texturePath = pathName;
        if (texturePath.endsWith("/")) {
            texturePath = texturePath.substring(0, texturePath.length() - 1);
        }

        // Convert the texture path into a file-system path
        String folderPath;
        String[] split = texturePath.split(":");
        if (split.length == 2) {
            folderPath = split[0] + File.separator + split[1].replace("/", File.separator);
        } else {
            folderPath = texturePath.replace("/", File.separator);
        }

        // Create folder under plugin/generated/models/...
        File modelFolder = new File(plugin.getDataFolder(), "generated" + File.separator + "models" + File.separator + folderPath);
        if (!modelFolder.exists() && !modelFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create folder: " + modelFolder.getAbsolutePath());
            return;
        }

        // Mapping of custom model data number to generated file name
        Map<Integer, String> modelMapping = new LinkedHashMap<>();
        int currentModelData = startingModelData;

        // Generate individual model JSON files for each name in the list
        for (String rawName : names) {
            String fileName = convertCollectionName(rawName);
            modelMapping.put(currentModelData, fileName);
            File modelFile = new File(modelFolder, fileName + ".json");

            String modelJson = "{\n" +
                    "  \"parent\": \"minecraft:item/generated\",\n" +
                    "  \"textures\": {\n" +
                    "    \"layer0\": \"" + texturePath + "/" + fileName + "\"\n" +
                    "  }\n" +
                    "}";
            try (FileWriter writer = new FileWriter(modelFile, StandardCharsets.UTF_8)) {
                writer.write(modelJson);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to write model JSON file: " + modelFile.getName(), e);
            }
            currentModelData++;
        }

        // Generate the override JSON file under plugin/generated/models/item/...
        String overrideFileName = material.toLowerCase().replace(" ", "_") + ".json";
        File overrideFolder = new File(plugin.getDataFolder(), "generated" + File.separator + "models" + File.separator + "item");
        if (!overrideFolder.exists() && !overrideFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create folder: " + overrideFolder.getAbsolutePath());
            return;
        }
        File overrideFile = new File(overrideFolder, overrideFileName);

        StringBuilder overridesBuilder = new StringBuilder();
        overridesBuilder.append("  \"overrides\": [\n");

        List<Integer> sortedCmds = new ArrayList<>(modelMapping.keySet());
        Collections.sort(sortedCmds);
        for (int i = 0; i < sortedCmds.size(); i++) {
            int cmd = sortedCmds.get(i);
            String modelName = modelMapping.get(cmd);
            overridesBuilder.append("    {\n")
                    .append("      \"predicate\": {\n")
                    .append("        \"custom_model_data\": ").append(cmd).append("\n")
                    .append("      },\n")
                    .append("      \"model\": \"").append(texturePath).append("/").append(modelName).append("\"\n")
                    .append("    }");
            if (i < sortedCmds.size() - 1) {
                overridesBuilder.append(",");
            }
            overridesBuilder.append("\n");
        }
        overridesBuilder.append("  ]\n");

        String overrideJson = "{\n" +
                "  \"parent\": \"item/generated\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"item/" + material.toLowerCase().replace(" ", "_") + "\"\n" +
                "  },\n" +
                overridesBuilder.toString() +
                "}";

        try (FileWriter writer = new FileWriter(overrideFile, StandardCharsets.UTF_8)) {
            writer.write(overrideJson);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to write override JSON file: " + overrideFile.getName(), e);
        }

        plugin.getLogger().info("Generated " + modelMapping.size() + " model JSON files for " + texturePath);
        plugin.getLogger().info("Generated override JSON file: " + overrideFile.getName() + " with " + modelMapping.size() + " overrides.");
    }

    /**
     * Loads the YAML configuration from <pluginDataFolder>/generated/config.yml,
     * iterates over each group (e.g. file1, file2, etc.), and generates:
     *  - Model JSON files for each name in the group list.
     *  - An override JSON file for the material defined in the group.
     */
    public void generateAllModelFiles() {
        // Load the YAML configuration file from generated/config.yml
        File configFile = new File(plugin.getDataFolder(), "generated" + File.separator + "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("Config file not found: " + configFile.getAbsolutePath());
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Loop through each top-level section (e.g. file1, file2, etc.)
        for (String groupKey : config.getKeys(false)) {
            String material = config.getString(groupKey + ".material");
            int startingModelData = config.getInt(groupKey + ".starting-model-data", 0);
            String texturePath = config.getString(groupKey + ".path");
            List<String> nameList = config.getStringList(groupKey + ".list");

            if (texturePath == null || texturePath.isEmpty()) {
                plugin.getLogger().warning("Missing 'path' for group: " + groupKey);
                continue;
            }
            if (nameList.isEmpty()) {
                plugin.getLogger().warning("No names found in list for group: " + groupKey);
                continue;
            }
            if (material == null || material.isEmpty()) {
                plugin.getLogger().warning("Missing 'material' for group: " + groupKey);
                continue;
            }

            // Remove trailing slash from texturePath if present.
            if (texturePath.endsWith("/")) {
                texturePath = texturePath.substring(0, texturePath.length() - 1);
            }

            // Convert the texture path into a file-system path.
            // E.g. "cozyvanilla:item/archaeology_collection" becomes
            // "cozyvanilla/<separator>item<separator>archaeology_collection"
            String folderPath;
            String[] split = texturePath.split(":");
            if (split.length == 2) {
                folderPath = split[0] + File.separator + split[1].replace("/", File.separator);
            } else {
                folderPath = texturePath.replace("/", File.separator);
            }
            File modelFolder = new File(plugin.getDataFolder(), "generated" + File.separator + "models" + File.separator + folderPath);
            if (!modelFolder.exists() && !modelFolder.mkdirs()) {
                plugin.getLogger().warning("Failed to create folder: " + modelFolder.getAbsolutePath());
                continue;
            }

            // Mapping of custom model data number to generated file name for this group.
            Map<Integer, String> groupMapping = new LinkedHashMap<>();
            int currentModelData = startingModelData;

            // Generate individual model JSON files for each name in the list.
            for (String rawName : nameList) {
                String fileName = convertCollectionName(rawName);
                groupMapping.put(currentModelData, fileName);
                File modelFile = new File(modelFolder, fileName + ".json");

                String modelJson = "{\n" +
                        "  \"parent\": \"minecraft:item/generated\",\n" +
                        "  \"textures\": {\n" +
                        "    \"layer0\": \"" + texturePath + "/" + fileName + "\"\n" +
                        "  }\n" +
                        "}";
                try (FileWriter writer = new FileWriter(modelFile, StandardCharsets.UTF_8)) {
                    writer.write(modelJson);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to write model JSON file: " + modelFile.getName(), e);
                }
                currentModelData++;
            }

            // Now generate the override JSON file for this group.
            // The override file is created in the "generated/models/item" folder.
            String overrideFileName = material.toLowerCase().replace(" ", "_") + ".json";
            File overrideFolder = new File(plugin.getDataFolder(), "generated" + File.separator + "models" + File.separator + "item");
            if (!overrideFolder.exists() && !overrideFolder.mkdirs()) {
                plugin.getLogger().warning("Failed to create folder: " + overrideFolder.getAbsolutePath());
                continue;
            }
            File overrideFile = new File(overrideFolder, overrideFileName);

            StringBuilder overridesBuilder = new StringBuilder();
            overridesBuilder.append("  \"overrides\": [\n");

            List<Integer> sortedCmds = new ArrayList<>(groupMapping.keySet());
            Collections.sort(sortedCmds);
            for (int i = 0; i < sortedCmds.size(); i++) {
                int cmd = sortedCmds.get(i);
                String modelName = groupMapping.get(cmd);
                overridesBuilder.append("    {\n")
                        .append("      \"predicate\": {\n")
                        .append("        \"custom_model_data\": ").append(cmd).append("\n")
                        .append("      },\n")
                        .append("      \"model\": \"").append(texturePath).append("/").append(modelName).append("\"\n")
                        .append("    }");
                if (i < sortedCmds.size() - 1) {
                    overridesBuilder.append(",");
                }
                overridesBuilder.append("\n");
            }
            overridesBuilder.append("  ]\n");

            String overrideJson = "{\n" +
                    "  \"parent\": \"item/generated\",\n" +
                    "  \"textures\": {\n" +
                    "    \"layer0\": \"item/" + material.toLowerCase().replace(" ", "_") + "\"\n" +
                    "  },\n" +
                    overridesBuilder.toString() +
                    "}";

            try (FileWriter writer = new FileWriter(overrideFile, StandardCharsets.UTF_8)) {
                writer.write(overrideJson);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to write override JSON file: " + overrideFile.getName(), e);
            }

            plugin.getLogger().info("[vArchaeology] Generated " + groupMapping.size() + " model JSON files for group: " + groupKey);
            plugin.getLogger().info("[vArchaeology] Generated override JSON file: " + overrideFile.getName() + " with " + groupMapping.size() + " overrides.");
        }
    }
}