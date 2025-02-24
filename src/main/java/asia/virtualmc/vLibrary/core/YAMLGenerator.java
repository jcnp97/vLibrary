package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class YAMLGenerator {
    private final VLibrary plugin;
    private CoreManager coreManager;
    private final File inputDir;
    private final File outputDir;

    public YAMLGenerator(@NotNull CoreManager coreManager) {
        this.coreManager = coreManager;
        this.plugin = coreManager.getVLibrary();
        this.inputDir = new File(plugin.getDataFolder(), "input");
        this.outputDir = new File(plugin.getDataFolder(), "output");
    }

    public void generateFile(String fileName) {
        File inputFile = new File(inputDir, fileName);
        File outputFile = new File(outputDir, fileName);
        generateYAML(inputFile, outputFile);
    }

    private boolean createDirectories() {
        return (inputDir.exists() || inputDir.mkdirs()) && (outputDir.exists() || outputDir.mkdirs());
    }

    private void generateYAML(File inputFile, File outputFile) {
        if (!createDirectories()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(inputFile);

        if (!config.contains("settings")) {
            plugin.getLogger().severe("Missing 'settings' section in file: " + inputFile.getName());
            return;
        }

        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if (settingsSection == null) {
            plugin.getLogger().severe("Invalid 'settings' section in file: " + inputFile.getName());
            return;
        }

        ConfigurationSection stringsSection = settingsSection.getConfigurationSection("strings");
        if (stringsSection == null) {
            plugin.getLogger().severe("Missing 'settings.strings' section in file: " + inputFile.getName());
            return;
        }
        Map<String, List<String>> stringMap = new LinkedHashMap<>();
        for (String key : stringsSection.getKeys(false)) {
            List<String> values = stringsSection.getStringList(key);
            if (values.isEmpty()) {
                plugin.getLogger().warning("No values for string variable: " + key + " in file: " + inputFile.getName());
            }
            stringMap.put(key, values);
        }
        if (stringMap.isEmpty()) {
            plugin.getLogger().severe("No string variables found in 'settings.strings' for file: " + inputFile.getName());
            return;
        }

        ConfigurationSection numbersSection = settingsSection.getConfigurationSection("numbers");
        if (numbersSection == null) {
            plugin.getLogger().severe("Missing 'settings.numbers' section in file: " + inputFile.getName());
            return;
        }
        Map<String, Integer> numberMap = new LinkedHashMap<>();
        for (String key : numbersSection.getKeys(false)) {
            numberMap.put(key, numbersSection.getInt(key));
        }

        Map<String, Object> template = new LinkedHashMap<>();
        for (String key : config.getKeys(false)) {
            if (!key.equals("settings")) {
                template.put(key, convertConfigurationSectionToMap(config.getConfigurationSection(key)));
            }
        }

        FileConfiguration outputConfig = new YamlConfiguration();
        generateOutputConfig(template, stringMap, numberMap, outputConfig);

        try {
            outputConfig.save(outputFile);
            plugin.getLogger().info("Generated YAML: " + outputFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving output file: " + outputFile.getName(), e);
        }
    }

    private Map<String, Object> convertConfigurationSectionToMap(ConfigurationSection section) {
        if (section == null) return null;

        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                map.put(key, convertConfigurationSectionToMap(section.getConfigurationSection(key)));
            } else {
                Object value = section.get(key);
                map.put(key, value);
            }
        }
        return map;
    }

    private void generateOutputConfig(Map<String, Object> template,
                                      Map<String, List<String>> stringMap,
                                      Map<String, Integer> numberMap,
                                      FileConfiguration outputConfig) {
        String mainVariable = stringMap.keySet().iterator().next();
        List<String> mainValues = stringMap.get(mainVariable);

        for (int i = 0; i < mainValues.size(); i++) {
            Map<String, String> replacements = createReplacements(stringMap, numberMap, i);

            template.forEach((key, templateValue) -> {
                String processedKey = applyReplacements(key, replacements);
                Object processedValue = processTemplateValue(templateValue, replacements);
                outputConfig.set(processedKey, processedValue);
            });
        }
    }

    private Map<String, String> createReplacements(Map<String, List<String>> stringMap,
                                                   Map<String, Integer> numberMap,
                                                   int iteration) {
        Map<String, String> replacements = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : stringMap.entrySet()) {
            String var = entry.getKey();
            List<String> list = entry.getValue();
            String raw = list.get(iteration);
            replacements.put(var, raw);
            replacements.put(var + "_formatted", formatValue(raw));
        }

        for (Map.Entry<String, Integer> entry : numberMap.entrySet()) {
            String var = entry.getKey();
            int base = entry.getValue();
            // Convert to plain integer without quotes
            replacements.put(var, Integer.toString(base + iteration));
        }

        return replacements;
    }

    private Object processTemplateValue(Object value, Map<String, String> replacements) {
        if (value instanceof String) {
            String original = (String) value;
            String processed = applyReplacements(original, replacements);

            // If the string contains quotes, use literal block style
//            if (processed.contains("\"")) {
//                return "|" + processed;
//            }

            // Handle number replacements
            try {
                return Integer.parseInt(processed);
            } catch (NumberFormatException e) {
                return processed;
            }
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> newList = new ArrayList<>();
            for (Object item : list) {
                newList.add(processTemplateValue(item, replacements));
            }
            return newList;
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, Object> processedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String processedKey = applyReplacements(entry.getKey(), replacements);
                Object processedValue = processTemplateValue(entry.getValue(), replacements);
                processedMap.put(processedKey, processedValue);
            }
            return processedMap;
        }
        return value;
    }

    private String applyReplacements(String text, Map<String, String> replacements) {
        if (text == null) return "";
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("$" + entry.getKey(), entry.getValue());
        }
        // Replace hardcoded symbol <q> with a double quote (")
        result = result.replace("<q>", "\"");
        return result;
    }

    private static String formatValue(String input) {
        String[] parts = input.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)))
                        .append(parts[i].substring(1).toLowerCase());
                if (i < parts.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}