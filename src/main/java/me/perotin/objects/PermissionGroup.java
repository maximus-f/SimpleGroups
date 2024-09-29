package me.perotin.objects;

import lombok.Getter;
import me.perotin.SimpleGroups;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @dateBegan 9/28/24
 * @author maxfuligni
 *
 *  Base class for a permissions group to encompass list of permission attachments and
 *  prefixes.
 */

@Getter
public class PermissionGroup {

    private String name;
    private String prefix;
    private final Set<String> permissions; // Set to avoid duplicates

    public PermissionGroup(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.permissions = new HashSet<>();
    }


    /**
     *  Writes new permission to database and attaches new PermissionAttachment to all loaded
     *  players.
     * @param permission
     * @param plugin
     */
    public void addPermission(String permission, SimpleGroups plugin) {
        permissions.add(permission);
        updatePermissionsForGroup(permission, plugin, true);
        try {
            plugin.getDatabaseManager().addPermissionForGroup(getName(), permission);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void removePermission(String permission, SimpleGroups plugin) {
        permissions.remove(permission);
        updatePermissionsForGroup(permission, plugin, false);
        try {
            plugin.getDatabaseManager().removePermissionForGroup(getName(), permission);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePermissionsForGroup(String permission, SimpleGroups plugin, boolean add) {
        for (SimplePlayer player : plugin.getPlayersWithRank(this)) {
            player.updateNewPermission(plugin, permission, add);
        }
    }


    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

    public void addAllPermissions(List<String> permissions) {
        this.permissions.addAll(permissions);
    }

}
