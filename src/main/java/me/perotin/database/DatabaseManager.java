package me.perotin.database;

import me.perotin.objects.PermissionGroup;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author maxfuligni
 *
 * Class to represent SQLite database to store groups and player permissions.

 SCHEMAS:

    Group_names (used as super list for getAll):
        Group_Name:
     Groups:
        Group Name
        Permission
     Player_Groups:
        UUID:
        Group Name:
 */
public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(String path) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement statement = connection.createStatement()) {
            // store unique names
            statement.execute("CREATE TABLE IF NOT EXISTS group_names (" +
                    "group_name TEXT PRIMARY KEY," + // Unique group names
                    "group_prefix TEXT NOT NULL)");

            // store permissions per group
            statement.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "group_name TEXT NOT NULL, " +
                    "permission TEXT NOT NULL, " +
                    "PRIMARY KEY (group_name, permission), " +
                    "FOREIGN KEY (group_name) REFERENCES group_names(group_name) ON DELETE CASCADE" +
                    ")");

            // store uuids with groups
            statement.execute("CREATE TABLE IF NOT EXISTS player_groups (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "group_name TEXT NOT NULL, " +
                    "FOREIGN KEY (group_name) REFERENCES group_names(group_name) ON DELETE CASCADE" +
                    ")");
        }
    }


    /**
     *  Writes newly created group to groups_name database before permissions are added
     * @param group
     * @throws SQLException
     */
    public void writeNewGroup(PermissionGroup group) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO group_names(group_name, group_prefix) VALUES(?, ?)")) {
            statement.setString(1, group.getName());
            statement.setString(2, group.getPrefix());
            statement.executeUpdate();

        }
    }
    /**
     *  Fetch names through list of group names, then individually fetch all permissions per group
     * @return All Permission groups
     * @throws SQLException
     */
    public List<PermissionGroup> getAllPermissionGroups() throws SQLException {
        List<PermissionGroup> permissionGroups = new ArrayList<>();
        // Select all unique names from group_names
        try (PreparedStatement groupNameStatement = connection.prepareStatement(
                "SELECT group_name, group_prefix FROM group_names"
        )) {
            ResultSet groupNameResultSet = groupNameStatement.executeQuery();

            while (groupNameResultSet.next()) {
                String groupName = groupNameResultSet.getString("group_name");
                String groupPrefix = groupNameResultSet.getString("group_prefix");

                // Fetch permissions per group
                List<String> permissions = getGroupPermissions(groupName);
                PermissionGroup permissionGroup = new PermissionGroup(groupName, groupPrefix);
                permissionGroup.addAllPermissions(permissions);
                permissionGroups.add(permissionGroup);
            }
        }

        return permissionGroups;
    }
    /**
     * Adds a new group to the database with a list of permissions.
     */
    public void addPermissionForGroup(String group, String permission) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO groups (group_name, permission) VALUES (?, ?)")) {
           statement.setString(1, group);
           statement.setString(2, permission);
           statement.executeUpdate();
        }
    }



    /**
     * Adds a new group to the database with a list of permissions.
     */
    public void removePermissionForGroup(String group, String permission) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM groups WHERE group_name = ? AND permission = ?"
        )) {
            statement.setString(1, group);
            statement.setString(2, permission);
            statement.executeUpdate();
        }
    }


    /**
     * Checks if a group exists in the database.
     */
    public boolean groupExists(String groupName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM group_names WHERE group_name = ?")) {
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    /**
     * Adds a player to a group in the player_groups table.
     */
    public void assignPlayerToGroup(UUID uuid, String groupName) throws SQLException {
        if (!groupExists(groupName)) {
            throw new SQLException("Group " + groupName + " does not exist.");
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_groups (uuid, group_name) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, groupName);
            statement.executeUpdate();
        }
    }

    /**
     * Retrieves the group a player belongs to.
     */
    public String getPlayerGroup(UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT group_name FROM player_groups WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("group_name");
            }
        }
        return null; // No group found
    }

    /**
     * Removes a player from the player_groups table.
     */
    public void removePlayerFromGroup(Player player) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM player_groups WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.executeUpdate();
        }
    }

    /**
     * Retrieves a list of permissions for a specific group.
     */
    public List<String> getGroupPermissions(String groupName) throws SQLException {
        List<String> permissions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT permission FROM groups WHERE group_name = ?")) {
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                permissions.add(resultSet.getString("permission"));
            }
        }
        return permissions;
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

}
