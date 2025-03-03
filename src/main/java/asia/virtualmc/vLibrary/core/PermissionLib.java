package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PermissionLib {
    private final Permission permission;

    public PermissionLib(@NotNull CoreManager coreManager) {
        VLibrary plugin = coreManager.getVLibrary();
        this.permission = plugin.getPermission();

        if (this.permission == null) {
            throw new IllegalStateException("Vault permission is not properly initialized!");
        }
    }

    /**
     * Adds a permission to a player
     *
     * @param player The player to add the permission to
     * @param permissionNode The permission node to add
     * @return true if the permission was added successfully, false otherwise
     */
    public boolean addPermission(@NotNull Player player, @NotNull String permissionNode) {
        if (permissionNode.isEmpty()) {
            return false;
        }

        // Check if player already has the permission
        if (permission.playerHas(player, permissionNode)) {
            return true;
        }

        boolean result = permission.playerAdd(player, permissionNode);
        if (result) {
            EffectsUtil.sendPlayerMessage(player, "You have been granted the permission: " + permissionNode);
        }

        return result;
    }

    /**
     * Adds a permission to a player in a specific world
     *
     * @param player The player to add the permission to
     * @param world The world name where the permission applies
     * @param permissionNode The permission node to add
     * @return true if the permission was added successfully, false otherwise
     */
    public boolean addPermission(@NotNull Player player, @NotNull String world, @NotNull String permissionNode) {
        if (permissionNode.isEmpty() || world.isEmpty()) {
            return false;
        }

        // Check if player already has the permission in this world
        if (permission.playerHas(world, player, permissionNode)) {
            return true;
        }

        boolean result = permission.playerAdd(world, player, permissionNode);
        if (result) {
            EffectsUtil.sendPlayerMessage(player, "You have been granted the permission: " + permissionNode + " in world: " + world);
        }

        return result;
    }

    /**
     * Removes a permission from a player
     *
     * @param player The player to remove the permission from
     * @param permissionNode The permission node to remove
     * @return true if the permission was removed successfully, false otherwise
     */
    public boolean removePermission(@NotNull Player player, @NotNull String permissionNode) {
        if (permissionNode.isEmpty()) {
            return false;
        }

        // Check if player even has the permission
        if (!permission.playerHas(player, permissionNode)) {
            return true;
        }

        boolean result = permission.playerRemove(player, permissionNode);
        if (result) {
            EffectsUtil.sendPlayerMessage(player, "The permission has been removed: " + permissionNode);
        }

        return result;
    }

    /**
     * Removes a permission from a player in a specific world
     *
     * @param player The player to remove the permission from
     * @param world The world name where the permission applies
     * @param permissionNode The permission node to remove
     * @return true if the permission was removed successfully, false otherwise
     */
    public boolean removePermission(@NotNull Player player, @NotNull String world, @NotNull String permissionNode) {
        if (permissionNode.isEmpty() || world.isEmpty()) {
            return false;
        }

        // Check if player even has the permission in this world
        if (!permission.playerHas(world, player, permissionNode)) {
            return true;
        }

        boolean result = permission.playerRemove(world, player, permissionNode);
        if (result) {
            EffectsUtil.sendPlayerMessage(player, "The permission has been removed: " + permissionNode + " in world: " + world);
        }

        return result;
    }

    /**
     * Checks if a player has a specific permission
     *
     * @param player The player to check
     * @param permissionNode The permission node to check
     * @return true if the player has the permission, false otherwise
     */
    public boolean checkPermission(@NotNull Player player, @NotNull String permissionNode) {
        if (permissionNode.isEmpty()) {
            return false;
        }

        return permission.playerHas(player, permissionNode);
    }

    /**
     * Checks if a player has a specific permission in a specific world
     *
     * @param player The player to check
     * @param world The world name to check in
     * @param permissionNode The permission node to check
     * @return true if the player has the permission in the specified world, false otherwise
     */
    public boolean checkPermission(@NotNull Player player, @NotNull String world, @NotNull String permissionNode) {
        if (permissionNode.isEmpty() || world.isEmpty()) {
            return false;
        }

        return permission.playerHas(world, player, permissionNode);
    }

    /**
     * Gets the underlying Vault permission system
     *
     * @return The Vault Permission instance
     */
    public @NotNull Permission getPermission() {
        return permission;
    }
}