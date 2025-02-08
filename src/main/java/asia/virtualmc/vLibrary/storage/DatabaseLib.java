package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.configs.DatabaseConfig;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

import static org.bukkit.Bukkit.*;

public class DatabaseLib {
    private static DatabaseLib instance;
    private static HikariDataSource dataSource;
    private final Plugin plugin;

    private DatabaseLib(Plugin plugin) {
        this.plugin = plugin;
        if (!setupDatabase()) {
            getLogger().severe("[vLibrary] Cannot connect to your database. Disabling plugin..");
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public static synchronized DatabaseLib getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new DatabaseLib(plugin);
        }
        return instance;
    }

    private boolean setupDatabase() {
        try {
            DatabaseConfig.readDatabaseFile(plugin);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "/" + DatabaseConfig.getDatabaseName());
            config.setUsername(DatabaseConfig.getUsername());
            config.setPassword(DatabaseConfig.getPassword());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    ConsoleMessageUtil.pluginPrint("Successfully connected to the MySQL database.");
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[vLibrary] Failed to connect to database: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[vLibrary] Error during database setup: " + e.getMessage());
            return false;
        }

        return true;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            instance = null;
        }
    }
}
