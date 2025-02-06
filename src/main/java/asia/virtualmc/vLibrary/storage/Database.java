package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import asia.virtualmc.vLibrary.configs.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private HikariDataSource dataSource;
    private final DatabaseConfig dbConfig;
    private final Plugin plugin;

    public Database(Plugin plugin) {
        this.plugin = plugin;
        this.dbConfig = new DatabaseConfig();
        this.dbConfig.readDatabaseFile(plugin); // Read config ONCE
    }

    public boolean setupDatabase() {
        String pluginName = "[" + plugin.getName() + "] ";

        try {
            HikariConfig config = createHikariConfig();
            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    ConsoleMessageUtil.sendConsoleMessage(pluginName + "<#7CFEA7>Successfully connected to the MySQL database.");
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(pluginName + "Failed to connect to database: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Bukkit.getLogger().severe(pluginName + "Error during database setup: " + e.getMessage());
            return false;
        }

        return true;
    }

    private HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbConfig.getHost() + ":" + dbConfig.getPort() + "/" + dbConfig.getDatabaseName());
        config.setUsername(dbConfig.getUsername());
        config.setPassword(dbConfig.getPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return config;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void createTable(String sqlStatement) {
        if (dataSource == null) {
            Bukkit.getLogger().severe("[" + plugin.getName() + "] Database is not initialized. Cannot create table.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(sqlStatement);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[" + plugin.getName() + "] Failed to create tables: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
