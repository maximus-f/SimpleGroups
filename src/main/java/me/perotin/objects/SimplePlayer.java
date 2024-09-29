package me.perotin.objects;

import lombok.Getter;
import me.perotin.SimpleGroups;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;


/**
 * @author maxfuligni
 *
 *  Base representation of a player object with their group and if
 *  it should expire.
 */

@Getter
public class SimplePlayer {

    private UUID playerUUID;
    private final PermissionGroup group;
    private final long expirationTime;

    public SimplePlayer(UUID playerUUID, PermissionGroup group, long expirationTime) {
        this.playerUUID = playerUUID;
        this.group = group;
        this.expirationTime = expirationTime;
    }

    public boolean isTemporary() {
        return expirationTime > 0;
    }

    public boolean isExpired() {
        return isTemporary() && System.currentTimeMillis() > expirationTime;
    }

    /**
     * Sets PermissionAttachments for all permissions of given SimplePlayer obj.
     * Used on start-up and loading for first time.
     * @param plugin
     */
    public void setPermissions(SimpleGroups plugin){
        if (Bukkit.getPlayer(playerUUID) != null) {
            Player p = Bukkit.getPlayer(playerUUID);
           PermissionAttachment attachment =  p.addAttachment(plugin);
           for (String permission : group.getPermissions()) {
               attachment.setPermission(permission, true);
           }
        }
    }

    /**
     * Updates the PermissionAttachment for the given SimplePlayer object.
     * Adds or removes a permission at runtime based on the provided value.
     *
     * @param plugin The instance of the plugin.
     * @param permission The permission to add or remove.
     * @param add If true, adds the permission; if false, removes it.
     *
     */
    public void updateNewPermission(SimpleGroups plugin, String permission, boolean add) {
        if (Bukkit.getPlayer(playerUUID) != null) {
            Player p = Bukkit.getPlayer(playerUUID);
            PermissionAttachment attachment = p.addAttachment(plugin);

            if (add) {
                attachment.setPermission(permission, true);
            } else {
                attachment.unsetPermission(permission);
            }
        } else {
            // If not online when a change occurs, kick from memory so that
            // when queried again will have accurate permissions.
            // Note that this will probably cause issues with how PermissionAttachments are stored. Need to update
            // accordingly.
            unload(plugin);
        }
    }

    private void unload(SimpleGroups plugin) {
        plugin.getPlayers().remove(playerUUID);
    }

}

