package me.gatogamer.dynamicpremium.commons.database.type;

import lombok.Getter;
import lombok.Setter;
import me.gatogamer.dynamicpremium.commons.config.IConfigParser;
import me.gatogamer.dynamicpremium.commons.database.Database;
import me.gatogamer.dynamicpremium.commons.database.DatabaseManager;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Getter
@Setter
public class MySQLDatabase implements Database {

    private Connection connection = null;

    @Override
    public void loadDatabase(IConfigParser iConfigParser, DatabaseManager databaseManager) {
        if (connection != null) {
            try {
                if (connection.isClosed()) {
                    System.out.println("DynamicPremium > MySQL connection is already closed");
                } else {
                    System.out.println("DynamicPremium > Closing current MySQL connection");
                    connection.close();
                }
            } catch (Throwable ignored) { }
            connection = null;
            System.out.println("DynamicPremium > Reconnecting to MySQL...");
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", iConfigParser.getString("MySQL.Username"));
            properties.setProperty("password", iConfigParser.getString("MySQL.Password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", "false");
            properties.setProperty("requireSSL", "false");

            iConfigParser.getStringList("MySQL.Properties").forEach(s ->
                    properties.setProperty(s.split("=")[0], s.split("=")[1])
            );

            connection = DriverManager.getConnection("jdbc:mysql://" + iConfigParser.getString("MySQL.Host") + ":" + iConfigParser.getString("MySQL.Port") + "/" + iConfigParser.getString("MySQL.Database"), properties);
            update("CREATE TABLE IF NOT EXISTS PremiumUsers (PlayerName VARCHAR(100), Enabled VARCHAR(100))");
            update("ALTER TABLE PremiumUsers ADD COLUMN Full VARCHAR(100) AFTER Enabled");
            update("CREATE UNIQUE INDEX premiumIndex ON PremiumUsers (PlayerName, Enabled, Full)");
            System.out.println("DynamicPremium > Connected to MySQL!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DynamicPremium > Oh no, I can't connect to MySQL!");
            System.out.println("DynamicPremium > Send this error to gatogamer#6666!");
        }
    }

    @Override
    public PlayerState playerState(String name) {
        try {
            ResultSet rs = query("SELECT * FROM PremiumUsers WHERE PlayerName='" + name + "'");
            if (rs.next() && rs.getString("PlayerName") != null) {
                String enabled = rs.getString("Enabled");
                if (enabled != null && enabled.equalsIgnoreCase("true")) {
                    String full = rs.getString("Full");
                    if (full != null && full.equalsIgnoreCase("true")) {
                        return PlayerState.FULL_PREMIUM;
                    } else {
                        return PlayerState.PREMIUM;
                    }
                }
            }
        } catch (SQLException ignored) { }
        return PlayerState.NO_PREMIUM;
    }

    @Override
    public boolean playerIsPremium(String name) {
        return userExist(name);
    }

    @Override
    public void updatePlayer(String name) {
        createUser(name);
    }

    @Override
    public void updatePlayer(String name, PlayerState state) {
        if (state == PlayerState.NO_PREMIUM) {
            removePlayer(name);
        } else {
            createUser(name, state == PlayerState.FULL_PREMIUM);
        }
    }

    @Override
    public void removePlayer(String name) {
        deleteUser(name);
    }

    public void update(String qry) {
        try {
            connection.createStatement().executeUpdate(qry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes a query.
     *
     * @param query The thing than you'll ask to MySQL.
     */
    public ResultSet query(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
            stmt.executeQuery(query);
            return stmt.getResultSet();
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error I/O información:");
            System.out.println("---------------------------------------------------------------");
            e.printStackTrace();
            System.out.println("---------------------------------------------------------------");
            return null;
        }
    }

    /**
     * Makes a request to MySQL to get if a player exist in database.
     *
     * @param name The player name.
     */
    public boolean userExist(String name) {
        try {
            ResultSet rs = query("SELECT * FROM PremiumUsers WHERE PlayerName='" + name + "'");
            return (rs.next() && rs.getString("PlayerName") != null);
        } catch (SQLException e) {
            return false;
        }
        //try (Statement stmt = connection.createStatement()) {
        //    stmt.executeQuery("SELECT * FROM PremiumUsers WHERE PlayerName='" + name + "'");
        //    ResultSet rs = stmt.getResultSet();
        //    return (rs.next() && rs.getString("PlayerName") != null);
        //} catch (Throwable t) {
        //    System.out.println("Ha ocurrido un error I/O información:");
        //    System.out.println("---------------------------------------------------------------");
        //    t.printStackTrace();
        //    System.out.println("---------------------------------------------------------------");
        //}
        //return false;
    }

    /**
     * Creates a new user in MySQL.
     *
     * @param name The player name.
     */
    public void createUser(String name) {
        createUser(name, false);
    }

    /**
     * Creates a new user in MySQL.
     *
     * @param name The player name.
     * @param full True if the player as full premium state.
     */
    public void createUser(String name, boolean full) {
        if (userExist(name)) {
            update("UPDATE PremiumUsers SET Enabled = 'true', Full = '" + full + "' WHERE PlayerName = '" + name + "'");
        } else {
            update("INSERT INTO PremiumUsers (PlayerName, Enabled, Full) VALUES ('" + name + "', 'true', '" + full + "')");
        }
    }

    /**
     * Deletes an user from MySQL.
     * @param name: The name from user to delete on MySQL.
     */
    public void deleteUser(String name) {
        update("DELETE FROM PremiumUsers WHERE PlayerName='"+name+"'");
    }
}
