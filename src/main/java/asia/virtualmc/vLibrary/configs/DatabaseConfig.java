package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class DatabaseConfig {

    private static String host;
    private static int port;
    private static String databaseName;
    private static String userName;
    private static String password;
    private static boolean isLoaded = false;

    public static void readDatabaseFile(Plugin vlib) {
        if (isLoaded) return;

        File dbConfigFile = new File(vlib.getDataFolder(), "database.yml");
        String pluginName = "[" + vlib.getName() + "] ";

        if (!dbConfigFile.exists()) {
            try {
                vlib.saveResource("database.yml", false);
            } catch (Exception e) {
                vlib.getLogger().log(Level.SEVERE, pluginName + "Failed to save database.yml", e);
                return;
            }
        }

        FileConfiguration dbConfig = YamlConfiguration.loadConfiguration(dbConfigFile);
        try {
            host = dbConfig.getString("mysql.host", "localhost");
            port = dbConfig.getInt("mysql.port", 3306);
            databaseName = dbConfig.getString("mysql.database", "minecraft");
            userName = dbConfig.getString("mysql.username", "root");
            password = dbConfig.getString("mysql.password");
            isLoaded = true;

            vlib.getLogger().info(pluginName + "Loaded database.yml successfully!");

        } catch (Exception e) {
            vlib.getLogger().log(Level.SEVERE, pluginName + "Error reading database.yml", e);
        }
    }

    @NotNull
    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    @NotNull
    public static String getDatabaseName() {
        return databaseName;
    }

    @NotNull
    public static String getUsername() {
        return userName;
    }

    @NotNull
    public static String getPassword() {
        return password;
    }
}
