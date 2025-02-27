package asia.virtualmc.vLibrary.utils;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class EffectsUtil {

    public static void spawnFireworks(@NotNull Plugin plugin, @NotNull Player player, int amount, long interval) {
        if (!player.isOnline()) {
            return;
        }
        World world = player.getWorld();
        Location location = player.getLocation();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= amount) {
                    this.cancel();
                    return;
                }
                Firework firework = world.spawn(location, Firework.class);
                firework.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
                FireworkMeta meta = firework.getFireworkMeta();

                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(Color.AQUA, Color.LIME)
                        .withFade(Color.YELLOW)
                        .with(FireworkEffect.Type.BALL)
                        .trail(true)
                        .flicker(true)
                        .build();
                meta.setPower(0);
                meta.addEffect(effect);
                firework.setFireworkMeta(meta);
                count++;
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    public static void playSound(@NotNull Player player, String soundKey, Sound.Source source, float volume, float pitch) {
        if (!player.isOnline()) return;

        String[] parts = soundKey.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "minecraft";
        String key = parts.length > 1 ? parts[1] : parts[0];

        Sound sound = Sound.sound()
                .type(Key.key(namespace, key))
                .source(source)
                .volume(volume)
                .pitch(pitch)
                .build();
        player.playSound(sound);
    }

    public static void sendPlayerMessage(@NotNull Player player, String message) {
        if (!player.isOnline()) {
            return;
        }
        Component messageComponent = MiniMessage.miniMessage().deserialize("<#3FFFA0>" + message);
        player.sendMessage(messageComponent);
    }

    public static void sendTitleMessage(@NotNull Player player, String title, String subtitle) {
        if (!player.isOnline()) {
            return;
        }
        Component titleComponent = MiniMessage.miniMessage().deserialize(title);
        Component subtitleComponent = MiniMessage.miniMessage().deserialize(subtitle);

        Title fullTitle = Title.title(titleComponent, subtitleComponent);
        player.showTitle(fullTitle);
    }

    public static void sendTitleTimedMessage(@NotNull Player player, String title, String subtitle, long duration) {
        if (!player.isOnline()) {
            return;
        }
        Component titleComponent = MiniMessage.miniMessage().deserialize(title);
        Component subtitleComponent = MiniMessage.miniMessage().deserialize(subtitle);
        Title.Times TITLE_TIMES = Title.Times.times(Duration.ZERO, Duration.ofMillis(duration), Duration.ZERO);

        Title fullTitle = Title.title(titleComponent, subtitleComponent, TITLE_TIMES);
        player.showTitle(fullTitle);
    }

    public static void sendCustomToast(@NotNull Plugin plugin, @NotNull Player player, Material material, int modelData) {
        ItemStack icon = new ItemStack(material);

        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            icon.setItemMeta(meta);
        }

        UltimateAdvancementAPI.getInstance(plugin).displayCustomToast(player, icon, "Collection Log Updated", AdvancementFrameType.GOAL);
        playSound(player, "minecraft:cozyvanilla.collection_log_updated", Sound.Source.PLAYER, 1.0f, 1.0f);
    }

    public static void sendADBProgressBarTitle(@NotNull UUID uuid, double adbProgress, double adbAdd) {
        if (adbProgress < 0.0 || adbProgress > 1.0) {
            adbProgress = 1.0;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        int totalBars = 25;
        int filledBars = (int) Math.round(adbProgress * totalBars);
        int emptyBars = totalBars - filledBars;

        String progressBar = "<dark_green>" +
                "❙".repeat(filledBars) +
                "<dark_gray>" +
                "❙".repeat(emptyBars);

        String subtitleString = "<gradient:#EBD197:#B48811>Artefact Discovery: " +
                String.format("%.2f", adbProgress * 100.0) +
                "%" +
                "</gradient>" +
                " <green>(+" +
                String.format("%.2f", adbAdd) +
                "%)";

        Component title = MiniMessage.miniMessage().deserialize(progressBar);
        Component subtitle = MiniMessage.miniMessage().deserialize(subtitleString);

        Title fullTitle = Title.title(title, subtitle);
        player.showTitle(fullTitle);
    }

    public static String convertLegacy(String text) {
        return text.replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>");
    }

    public static String convertAmpersand(String text) {
        return text.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>");
    }

    public static void sendBroadcastMessage(String message) {
        Component messageComponent = MiniMessage.miniMessage().deserialize(message);
        Bukkit.getServer().sendMessage(messageComponent);
    }

    public static void sendActionBarMessage(@NotNull Player player, @NotNull String message) {
        MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
        Component component = MINI_MESSAGE.deserialize(message);
        player.sendActionBar(component);
    }

    public static List<String> convertListToNewFont(List<String> inputList) {
        return inputList.stream()
                .map(EffectsUtil::convertString)
                .collect(Collectors.toList());
    }

    private static String convertString(String input) {
        String NORMAL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String CONVERTED = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder converted = new StringBuilder();
        for (char ch : input.toCharArray()) {
            int index = NORMAL.indexOf(ch);
            converted.append(index != -1 ? CONVERTED.charAt(index) : ch);
        }
        return converted.toString();
    }
}
