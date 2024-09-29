package me.perotin.objects;

import lombok.Getter;

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


    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

    public void addAllPermissions(List<String> permissions) {
        this.permissions.addAll(permissions);
    }

}
