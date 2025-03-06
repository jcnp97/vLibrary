package asia.virtualmc.vLibrary.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EXPDisplayUtils {

    public static void showEXPBossBar(@NotNull Plugin plugin,
                                         @NotNull Player player,
                                         String skillName,
                                         double currentExp,
                                         int nextLevelExp,
                                         double addedExp,
                                         int currentLevel) {
        float progress = (float) currentExp / Math.max(nextLevelExp, 1);
        double hourlyExp = DigitUtils.roundToPrecision(addedExp * 240, 2);
        String percentProgress = DigitUtils.formattedTwoDecimals(Math.min(100.0, progress * 100.0));

        Component bossBarText = Component.text()
                .append(Component.text(skillName + " Lv. " + currentLevel, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(percentProgress + "%", NamedTextColor.YELLOW))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("+" + addedExp + " EXP", NamedTextColor.GREEN))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(DigitUtils.formattedNoDecimals(hourlyExp) + " XP/HR", NamedTextColor.RED))
                .build();

        BossBar bossBar = BossBar.bossBar(
                bossBarText,
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );
        bossBar.progress(Math.min(progress, 1.0f));

        player.showBossBar(bossBar);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> player.hideBossBar(bossBar), 100L);
    }

    public static void showEXPBossBarMaxed(@NotNull Plugin plugin,
                                           @NotNull Player player,
                                           String skillName,
                                           double currentExp,
                                           double addedExp,
                                           int currentLevel) {

        double hourlyExp = DigitUtils.roundToPrecision(addedExp * 240, 2);
        String formattedEXP = DigitUtils.formattedTwoDecimals(currentExp / 1000000);

        Component bossBarText = Component.text()
                .append(Component.text(skillName + " Lv. " + currentLevel, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(formattedEXP + "M EXP", NamedTextColor.YELLOW))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("+" + addedExp + " EXP", NamedTextColor.GREEN))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(DigitUtils.formattedNoDecimals(hourlyExp) + " XP/HR", NamedTextColor.RED))
                .build();

        BossBar bossBar = BossBar.bossBar(
                bossBarText,
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );

        player.showBossBar(bossBar);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> player.hideBossBar(bossBar), 100L);
    }

    public static void showEXPActionBar(@NotNull Player player,
                                        double currentExp,
                                        double expGain,
                                        double bonusXP,
                                        int nextLevelExp) {

        currentExp /= 100000;
        nextLevelExp /= 100000;

        if (bonusXP > 0) {
            Component actionBarText = Component.text()
                    .append(Component.text("+" + expGain + " EXP (+" + bonusXP + " Bonus EXP) ", NamedTextColor.GREEN))
                    .append(Component.text("(" + DigitUtils.formattedTwoDecimals(currentExp) + "K", NamedTextColor.GRAY))
                    .append(Component.text("/" + DigitUtils.formattedTwoDecimals(nextLevelExp) + "K EXP)", NamedTextColor.GRAY))
                    .build();
            player.sendActionBar(actionBarText);
        } else {
            Component actionBarText = Component.text()
                    .append(Component.text("+" + expGain + " EXP ", NamedTextColor.GREEN))
                    .append(Component.text("(" + DigitUtils.formattedTwoDecimals(currentExp) + "K", NamedTextColor.GRAY))
                    .append(Component.text("/" + DigitUtils.formattedTwoDecimals(nextLevelExp) + "K EXP)", NamedTextColor.GRAY))
                    .build();
            player.sendActionBar(actionBarText);
        }
    }

    public static void showEXPActionBarMaxed(@NotNull Player player,
                                             double currentExp,
                                             double expGain,
                                             double bonusXP
                                             ) {

        currentExp /= 1000000;
        if (bonusXP > 0) {
            Component actionBarText = Component.text()
                    .append(Component.text("+" + expGain + " EXP (+" + bonusXP + " Bonus EXP) ", NamedTextColor.GREEN))
                    .append(Component.text("(" + DigitUtils.formattedTwoDecimals(currentExp) + " M", NamedTextColor.GRAY))
                    .build();
            player.sendActionBar(actionBarText);
        } else {
            Component actionBarText = Component.text()
                    .append(Component.text("+" + expGain + " EXP ", NamedTextColor.GREEN))
                    .append(Component.text("(" + DigitUtils.formattedTwoDecimals(currentExp) + " M)", NamedTextColor.GRAY))
                    .build();
            player.sendActionBar(actionBarText);
        }
    }
}
