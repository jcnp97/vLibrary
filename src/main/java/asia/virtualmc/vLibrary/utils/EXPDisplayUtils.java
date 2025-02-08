package asia.virtualmc.vLibrary.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EXPDisplayUtils {

    public static void showEXPBossBar(@NotNull JavaPlugin plugin,
                                         @NotNull Player player,
                                         String skillName,
                                         double currentExp,
                                         int nextLevelExp,
                                         double addedExp,
                                         int currentLevel) {
        UUID uuid = player.getUniqueId();
        float progress = (float) currentExp / Math.max(nextLevelExp, 1);
        double hourlyExp = DigitUtils.roundToPrecision(addedExp * 360, 2);
        String percentProgress = DigitUtils.formattedNumber(Math.min(100.0, progress * 100.0));

        Component bossBarText = Component.text()
                .append(Component.text(skillName + currentLevel, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(percentProgress + "%", NamedTextColor.YELLOW))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("+" + addedExp + " EXP", NamedTextColor.GREEN))
                .append(Component.text(" (" + DigitUtils.formattedNumber(hourlyExp) + ")", NamedTextColor.GREEN))
                .build();

        BossBar bossBar = BossBar.bossBar(
                bossBarText,
                Math.max(progress, 1.0f),
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );

        player.showBossBar(bossBar);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> player.hideBossBar(bossBar), 100L);
    }

    public static void showEXPActionBar(@NotNull Player player,
                                        double currentExp,
                                        double expGain,
                                        int nextLevelExp) {
        Component actionBarText = Component.text()
                .append(Component.text("+" + expGain + " Archaeology EXP ", NamedTextColor.GREEN))
                .append(Component.text("(" + DigitUtils.formattedNumber(currentExp), NamedTextColor.GRAY))
                .append(Component.text("/" + DigitUtils.formattedNumber(nextLevelExp) + ")", NamedTextColor.GRAY))
                .build();

        player.sendActionBar(actionBarText);
    }
}
