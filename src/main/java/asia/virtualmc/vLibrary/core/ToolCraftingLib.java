package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.guis.SalvageGUILib;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import asia.virtualmc.vLibrary.utils.HologramUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ToolCraftingLib {

    public static int[] getRequiredMaterials(int itemID) {
        return switch (itemID) {
            case 1 -> new int[]{256, 128, 0, 0, 0, 0, 0};
            case 2 -> new int[]{512, 256, 64, 32, 0, 0, 0};
            case 3 -> new int[]{768, 512, 128, 64, 16, 0, 0};
            case 4 -> new int[]{1024, 768, 256, 128, 32, 8, 0};
            case 5 -> new int[]{1280, 1024, 512, 256, 64, 16, 4};
            case 6 -> new int[]{1536, 1280, 768, 512, 128, 32, 8};
            case 7 -> new int[]{1792, 1536, 1024, 768, 256, 64, 16};
            case 8 -> new int[]{2048, 1792, 1280, 1024, 512, 128, 32};
            case 9 -> new int[]{2304, 2048, 1536, 1280, 768, 256, 64};
            default -> new int[]{0, 0, 0, 0, 0, 0, 0};
        };
    }

    public static ItemStack getMaterialIcon(int index, int componentsOwned, int compReq) {
        boolean canCraft = componentsOwned >= compReq;
        ItemStack icon;
        if (canCraft) {
            icon = new ItemStack(SalvageGUILib.getComponentMaterial(index));
        } else {
            icon = new ItemStack(Material.BARRIER);
        }
        ItemMeta meta = icon.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(SalvageGUILib.getComponentName(index));
            String prefix = "";

            if (!canCraft) {
                meta.setCustomModelData(100000);
                prefix = "§m";
            }
            meta.setLore(List.of(prefix + "§7Amount: §a" + componentsOwned + "§7/§c" + compReq));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    public static boolean canCraft(int[] compOwned, int[] compReq) {
        for (int i = 0; i < 7; i++) {
            if (compOwned[i] < compReq[i]) return false;
        }

        return true;
    }

    public static void startCrafting(Plugin plugin, Player player, String holoName, ItemStack tool) {
        UUID uuid = player.getUniqueId();
        if (HologramUtils.hasHologramTask(uuid)) return;

        Location location = Objects.requireNonNull(HologramUtils.getHologram(holoName)).getLocation();
        String hologramName = "progress_hologram_" + uuid;

        ArrayList<String> lines = new ArrayList<>();
        lines.add(getProgressChars(0));
        HologramUtils.addHidePlayer(player, holoName);

        Material material = tool.getType();
        int modelData = tool.getItemMeta().getCustomModelData();

        try {
            Hologram hologram = DHAPI.createHologram(hologramName, location);
            hologram.setDefaultVisibleState(false);
            hologram.setShowPlayer(player);

            EffectsUtil.playSound(player, "minecraft:cozyvanilla.restoration_sounds", Sound.Source.PLAYER, 1.0f, 1.0f);

            BukkitRunnable hologramTask = new BukkitRunnable() {
                private int secondsPassed = 0;

                @Override
                public void run() {
                    secondsPassed++;

                    if (secondsPassed <= 8) {
                        DHAPI.setHologramLine(hologram, 0, getProgressChars(secondsPassed - 1));
                    } else if (secondsPassed == 9) {
                        DHAPI.removeHologramLine(hologram, 0);
                        String hologramItem = "#ICON: " + material + " {CustomModelData:" + modelData + "}";
                        DHAPI.addHologramLine(hologram, hologramItem);
                    } else {
                        DHAPI.removeHologram(holoName);
                        hologram.delete();
                        HologramUtils.removeHologramTask(uuid);
                        HologramUtils.removeHidePlayer(player, holoName);
                        EffectsUtil.spawnFireworks(plugin ,player, 6, 3);
                        this.cancel();
                    }
                }
            };

            HologramUtils.addHologramTask(uuid, hologramTask);
            hologramTask.runTaskTimer(plugin, 0L, 20L);

        } catch (Exception e) {
            plugin.getLogger().severe("Error creating hologram: " + e.getMessage());
            player.sendMessage("Error creating hologram. Please contact an administrator.");
        }
    }

    private static String getProgressChars(int index) {
        if (index < 0) return "";

        String[] progressChars = {
                "\uE0F2",
                "\uE0F3",
                "\uE0F4",
                "\uE0F5",
                "\uE0F6",
                "\uE0F7",
                "\uE0F8",
                "\uE0F9"
        };

        return progressChars[Math.min(index, progressChars.length - 1)];
    }


}
