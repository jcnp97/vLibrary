package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.utils.EffectsUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RankGUILib {
    private static final Map<String, List<RankInfo>> rankTable = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> rankToPoints = new ConcurrentHashMap<>();
    private record RankInfo(int pointsRequired, String rankName) {}
    public record RankPlayer(int numericalRank, int currentPoints, int nextPoints,
                             String currentRank, String nextRank) {}

    public static void readRanksFromFile(Plugin plugin, String PATH_FILE) {
        File ranksFile = new File(plugin.getDataFolder(), PATH_FILE);
        if (!ranksFile.exists()) {
            System.out.println("ranks.yml not found!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(ranksFile);
        List<RankInfo> rankList = new ArrayList<>();

        if (!config.contains("ranksList")) {
            System.out.println("ranksList section missing in ranks.yml!");
            return;
        }

        // Get the keys, parse to integers, and sort them
        ConfigurationSection rankSection = config.getConfigurationSection("ranksList");
        Set<String> keys = rankSection.getKeys(false);
        List<Integer> sortedKeys = new ArrayList<>();
        for (String key : keys) {
            try {
                sortedKeys.add(Integer.parseInt(key));
            } catch (NumberFormatException e) {
                System.out.println("Invalid rank key: " + key);
            }
        }
        Collections.sort(sortedKeys);

        Integer previousPoints = null;
        for (int key : sortedKeys) {
            int points = config.getInt("ranksList." + key + ".points");
            String rankName = config.getString("ranksList." + key + ".rankName", "Unknown");

            // Check that each rank's points are higher than the previous rank's points
            if (previousPoints != null && previousPoints >= points) {
                System.out.println("Warning: Rank with key " + key + " has points " + points +
                        " which is not higher than the previous rank's points (" + previousPoints + ").");
            }
            previousPoints = points;

            rankList.add(new RankInfo(points, rankName));
        }

        rankTable.computeIfAbsent(plugin.getName(), key -> new CopyOnWriteArrayList<>()).addAll(rankList);
    }

    public static Integer getPointsRequired(String pluginName, int index) {
        List<RankInfo> ranks = rankTable.get(pluginName);
        if (ranks == null || index < 0 || index >= ranks.size()) {
            return null;
        }
        return ranks.get(index).pointsRequired();
    }

    public static String getRankName(String pluginName, int index) {
        List<RankInfo> ranks = rankTable.get(pluginName);
        if (ranks == null || index < 0 || index >= ranks.size()) {
            return null;
        }
        return ranks.get(index).rankName();
    }

    public static void readRankPointsFromFile(Plugin plugin, String PATH_FILE) {
        File pointsFile = new File(plugin.getDataFolder(), PATH_FILE);
        if (!pointsFile.exists()) {
            System.out.println("Rank points file not found: " + PATH_FILE);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(pointsFile);
        if (!config.contains("settings.rank-points-calculation")) {
            System.out.println("rank-points-calculation section missing in " + PATH_FILE + "!");
            return;
        }

        ConfigurationSection pointsSection = config.getConfigurationSection("settings.rank-points-calculation");
        Map<String, Double> pluginRankPoints = new ConcurrentHashMap<>();

        for (String key : pointsSection.getKeys(false)) {
            double value = pointsSection.getDouble(key);
            pluginRankPoints.put(key, value);
        }

        rankToPoints.put(plugin.getName(), pluginRankPoints);
    }

    public static double getCalculationValue(String pluginName, String statName) {
        return rankToPoints.get(pluginName).get(statName);
    }

    public static void rankingUpEffect(Plugin plugin, Player player, String currentRank, String nextRank) {
        EffectsUtil.sendTitleMessage(player, "<#00FFA2>Ranking Up", "<yellow>" + currentRank + " <gray>→ <yellow>" + nextRank);
        EffectsUtil.playSound(player, "minecraft:cozyvanilla.rankup_sounds", Sound.Source.PLAYER, 1.0f, 1.0f);
        EffectsUtil.spawnFireworks(plugin, player, 5, 5);
    }

    public static StaticPane getProgressBar(RankPlayer rank, ItemStack lore) {
        Map<Integer, GuiItem> progressBar = new HashMap<>();
        StaticPane staticPane = new StaticPane(0, 0, 9, 4);

        for (int x = 1; x <= 7; x++) {
            GuiItem guiItem = new GuiItem(lore);
            staticPane.addItem(guiItem, x, 1);
            progressBar.put(x, guiItem);
        }

        // modifications
        double progress = Math.min(100, ((double) rank.currentPoints/rank.nextPoints) * 100);
        int progressChunk = (int) Math.floor(progress / 15);

        switch (progressChunk) {
            case 0 -> modifyStatButton0(progressBar, progress);
            case 1 -> modifyStatButton1(progressBar, progress - 15);
            case 2 -> modifyStatButton2(progressBar, progress - 30);
            case 3 -> modifyStatButton3(progressBar, progress - 45);
            case 4 -> modifyStatButton4(progressBar, progress - 60);
            case 5 -> modifyStatButton5(progressBar, progress - 75);
            case 6 -> modifyStatButton6(progressBar, progress - 90);
        }

        return staticPane;
    }

    // Modifications
    private static void modifyStatButton0(Map<Integer, GuiItem> buttons, double progress) {
        int progressChunk = (int) progress / 3;
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100000 + progressChunk));

        for (int i = 2; i < 7; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100005));
        }
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton1(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));

        int progressChunk = (int) progress / 3;
        buttons.get(2).setItem(setCustomModelData(buttons.get(2).getItem(), 100005 + progressChunk));

        for (int i = 3; i < 7; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100005));
        }
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton2(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));
        buttons.get(2).setItem(setCustomModelData(buttons.get(2).getItem(), 100009));

        int progressChunk = (int) progress / 3;
        buttons.get(3).setItem(setCustomModelData(buttons.get(3).getItem(), 100005 + progressChunk));

        for (int i = 4; i < 7; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100005));
        }
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton3(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));

        for (int i = 2; i < 4; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100009));
        }

        int progressChunk = (int) progress / 3;
        buttons.get(4).setItem(setCustomModelData(buttons.get(4).getItem(), 100005 + progressChunk));

        for (int i = 5; i < 7; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100005));
        }
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton4(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));

        for (int i = 2; i < 5; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100009));
        }

        int progressChunk = (int) progress / 3;
        buttons.get(5).setItem(setCustomModelData(buttons.get(5).getItem(), 100005 + progressChunk));
        buttons.get(6).setItem(setCustomModelData(buttons.get(6).getItem(), 100005));
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton5(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));

        for (int i = 2; i < 6; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100009));
        }

        int progressChunk = (int) progress / 3;
        buttons.get(6).setItem(setCustomModelData(buttons.get(6).getItem(), 100005 + progressChunk));
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010));
    }

    private static void modifyStatButton6(Map<Integer, GuiItem> buttons, double progress) {
        buttons.get(1).setItem(setCustomModelData(buttons.get(1).getItem(), 100004));

        for (int i = 2; i < 7; i++) {
            buttons.get(i).setItem(setCustomModelData(buttons.get(i).getItem(), 100009));
        }

        int progressChunk = (int) progress / 2;
        buttons.get(7).setItem(setCustomModelData(buttons.get(7).getItem(), 100010 + progressChunk));
    }

    private static ItemStack setCustomModelData(ItemStack item, int modelData) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }

        ItemStack clonedItem = item.clone();
        ItemMeta meta = clonedItem.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(modelData);
            clonedItem.setItemMeta(meta);
        }

        return clonedItem;
    }
}
