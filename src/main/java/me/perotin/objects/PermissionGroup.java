package me.perotin.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @dateBegan 9/28/24
 * @author maxfuligni
 *
 *  Base class for a permissions group to encompass list of permission attachments and
 *  prefixes.
 */
public class PermissionGroup {

    private String name;
    private String prefix;
    private List<String> permissions;

    public PermissionGroup(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.permissions = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

}
