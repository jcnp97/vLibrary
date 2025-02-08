package asia.virtualmc.vLibrary.utils;

import asia.virtualmc.vLibrary.VLibrary;
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

public class EffectsUtil {
    private final VLibrary vlib;
    private final UltimateAdvancementAPI uaapi;

    public EffectsUtil(VLibrary vlib) {
        this.vlib = vlib;
        this.uaapi = UltimateAdvancementAPI.getInstance(vlib);
    }

    public void spawnFireworks(@NotNull Player player, int amount, long interval) {
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
                firework.setMetadata("nodamage", new FixedMetadataValue(vlib, true));
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
        }.runTaskTimer(vlib, 0, interval);
    }

    public void playSound(@NotNull Player player, String soundKey, Sound.Source source, float volume, float pitch) {
        if (player == null || !player.isOnline()) {
            return;
        }
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
        Component messageComponent = MiniMessage.miniMessage().deserialize(message);
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

    public void sendCustomToast(@NotNull Player player, Material material, int modelData) {
        ItemStack icon = new ItemStack(material);

        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            icon.setItemMeta(meta);
        }

        uaapi.displayCustomToast(player, icon, "Collection Log Updated", AdvancementFrameType.GOAL);
        playSound(player, "minecraft:cozyvanilla.collection_log_updated", Sound.Source.PLAYER, 1.0f, 1.0f);
    }
}
