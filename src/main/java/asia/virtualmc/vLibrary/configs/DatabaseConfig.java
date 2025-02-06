package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class DatabaseConfig {
    private String host;
    private int port;
    private String databaseName;
    private String userName;
    private String password;

    public void readDatabaseFile(@NotNull Plugin plugin) {
        File dbConfigFile = new File(plugin.getDataFolder(), "database.yml");
        String pluginName = "[" + plugin.getName() + "] ";

        if (!dbConfigFile.exists()) {
            try {
                plugin.saveResource("database.yml", false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, pluginName + "Failed to save database.yml", e);
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

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, pluginName + "Error reading database.yml", e);
        }
    }

    @NotNull
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @NotNull
    public String getDatabaseName() {
        return databaseName;
    }

    @NotNull
    public String getUsername() {
        return userName;
    }

    @NotNull
    public String getPassword() {
        return password;
    }
}
