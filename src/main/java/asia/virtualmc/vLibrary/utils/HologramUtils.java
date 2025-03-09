package asia.virtualmc.vLibrary.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class HologramUtils {
    private static final Map<String, Hologram> hologramCache = new HashMap<>();
    private static final Cache<UUID, BukkitRunnable> hologramTasks = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public static void addHologram(String holoName, Location holoLocation, List<String> lore) {
        if (holoLocation == null || holoName == null) return;

        Hologram hologram = DHAPI.createHologram(holoName, holoLocation, lore);
        hologram.setDefaultVisibleState(true);
        hologramCache.put(holoName, hologram);
    }

    public static void removeHologram(String holoName) {
        if (!hologramCache.containsKey(holoName)) return;

        Hologram hologram = getHologram(holoName);
        if (hologram != null) {
            hologram.delete();
            hologramCache.remove(holoName);
        }
    }

    public static void addHologram(String holoName, Location holoLocation, List<String> lore,
                                   double x, double y, double z) {

        if (holoLocation == null || holoName == null) return;
        Location hologramLocation = holoLocation.clone().add(x, y, z);
        Hologram hologram = DHAPI.createHologram(holoName, hologramLocation, lore);
        hologram.setDefaultVisibleState(true);

        hologramCache.put(holoName, hologram);
    }

    public static void setDefaultVisibility(String holoName, boolean state) {
        Hologram hologram = getHologram(holoName);

        if (hologram != null) {
            hologram.setDefaultVisibleState(state);
        }
    }

    public static void addHidePlayer(Player player, String holoName) {
        Hologram hologram = getHologram(holoName);

        if (hologram != null) {
            hologram.setHidePlayer(player);
        }
    }

    public static void removeHidePlayer(Player player, String holoName) {
        Hologram hologram = getHologram(holoName);

        if (hologram != null) {
            hologram.removeHidePlayer(player);
        }
    }

    public static boolean hasHologram(String hologramName) {
        return hologramCache.containsKey(hologramName);
    }

    public static Hologram getHologram(String holoName) {
        if (!hasHologram(holoName)) return null;
        return hologramCache.get(holoName);
    }

    public static String getHologramLocation(String holoName) {
        Hologram hologram = getHologram(holoName);

        if (hologram != null) {
            Location loc = hologram.getLocation();
            return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
        }

        return null;
    }

    public static boolean hasHologramTask(UUID uuid) {
        return hologramTasks.getIfPresent(uuid) != null;
    }

    public static BukkitRunnable getHologramTask(UUID uuid) {
        return hologramTasks.getIfPresent(uuid);
    }

    public static void addHologramTask(UUID uuid, BukkitRunnable run) {
        if (!hasHologramTask(uuid)) {
            hologramTasks.put(uuid, run);
        }
    }

    public static void removeHologramTask(UUID uuid) {
        if (hasHologramTask(uuid)) {
            hologramTasks.invalidate(uuid);
        }
    }
}
