package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class LocalStorageLib {
    private final VLibrary plugin;
    private final String databaseUrl;

    public LocalStorageLib(@NotNull StorageManagerLib storageManagerLib) {
        this.plugin = storageManagerLib.getMain();
        this.databaseUrl = "jdbc:sqlite:" + new File(plugin.getDataFolder(), "inventory_data.db").getAbsolutePath();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS inventories (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "inventory TEXT NOT NULL)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize inventory database", e);
        }
    }

    // Store a single ItemStack for a player. If a record exists, append the new item.
    public void storeItem(UUID uuid, ItemStack item) {
        List<ItemStack> items = new ArrayList<>();
        if (hasInventory(uuid)) {
            // Retrieve existing item list.
            try (Connection conn = DriverManager.getConnection(databaseUrl);
                 PreparedStatement stmt = conn.prepareStatement("SELECT inventory FROM inventories WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String serializedInventory = rs.getString("inventory");
                    items = deserializeItemStackList(serializedInventory);
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not retrieve inventory for " + uuid, e);
            }
        }
        // Add the new item to the list.
        items.add(item);
        String serializedInventory = serializeItemStackList(items);
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO inventories (uuid, inventory) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, serializedInventory);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not store inventory for " + uuid, e);
        }
    }

    // Give all stored items to the player and delete them from the database.
    // This method checks if the player is online and that their inventory can accept all items.
    public void giveItems(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            Bukkit.getLogger().log(Level.SEVERE, player.getName() + " is not online. Cannot give items.");
            return;
        }

        // Retrieve stored items.
        List<ItemStack> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT inventory FROM inventories WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String serializedInventory = rs.getString("inventory");
                items = deserializeItemStackList(serializedInventory);
            } else {
                // No stored items for this player.
                return;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not retrieve stored items for " + uuid, e);
            return;
        }

        // Check if the player's inventory can accommodate all the items.
        if (!canFitItems(player, items)) {
            Bukkit.getLogger().log(Level.WARNING, "Player " + uuid + " does not have enough space to receive stored items.");
            return;
        }

        // Add each item to the player's inventory.
        for (ItemStack item : items) {
            player.getInventory().addItem(item);
        }

        // Remove the record so that items are not given twice.
        deleteInventory(uuid);
    }

    // Check if a record exists for the UUID in the database.
    public boolean hasInventory(UUID uuid) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM inventories WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not check inventory existence for " + uuid, e);
        }
        return false;
    }

    // Delete the stored inventory record for a UUID.
    public void deleteInventory(UUID uuid) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventories WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete inventory for " + uuid, e);
        }
    }

    // Serialize a list of ItemStack into a Base64 string.
    private String serializeItemStackList(List<ItemStack> items) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(items.toArray(new ItemStack[0]));
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize inventory", e);
            return "";
        }
    }

    // Deserialize a Base64 string back into a list of ItemStack.
    public List<ItemStack> deserializeItemStackList(String data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             ObjectInputStream in = new ObjectInputStream(byteIn)) {
            return new ArrayList<>(Arrays.asList((ItemStack[]) in.readObject()));
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize inventory", e);
            return new ArrayList<>();
        }
    }

    // Helper method to simulate adding a list of ItemStacks to a player's inventory.
    // It returns true if the items can be fully added without exceeding available space.
    private boolean canFitItems(Player player, List<ItemStack> items) {
        // Clone the current inventory slots.
        ItemStack[] slots = player.getInventory().getContents();
        List<ItemStack> simulated = new ArrayList<>();
        for (ItemStack slot : slots) {
            simulated.add(slot != null ? slot.clone() : null);
        }
        int inventorySize = simulated.size();

        // Try to simulate adding each ItemStack.
        for (ItemStack toAdd : items) {
            int amountToAdd = toAdd.getAmount();
            // First, try merging with existing similar stacks.
            for (int i = 0; i < inventorySize; i++) {
                ItemStack slot = simulated.get(i);
                if (slot != null && slot.isSimilar(toAdd)) {
                    int maxStack = slot.getMaxStackSize();
                    int availableSpace = maxStack - slot.getAmount();
                    if (availableSpace > 0) {
                        int adding = Math.min(amountToAdd, availableSpace);
                        amountToAdd -= adding;
                        slot.setAmount(slot.getAmount() + adding);
                        simulated.set(i, slot);
                        if (amountToAdd == 0) break;
                    }
                }
            }
            // Then, if there are still items left, try placing them in empty slots.
            while (amountToAdd > 0) {
                boolean foundEmpty = false;
                for (int i = 0; i < inventorySize; i++) {
                    if (simulated.get(i) == null) {
                        int maxStack = toAdd.getMaxStackSize();
                        int adding = Math.min(amountToAdd, maxStack);
                        ItemStack newStack = toAdd.clone();
                        newStack.setAmount(adding);
                        simulated.set(i, newStack);
                        amountToAdd -= adding;
                        foundEmpty = true;
                        break;
                    }
                }
                if (!foundEmpty) {
                    return false; // No available slot for the remaining items.
                }
            }
        }
        return true;
    }
}
