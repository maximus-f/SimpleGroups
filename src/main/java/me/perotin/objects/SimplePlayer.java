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

    public void setPermissions(SimpleGroups plugin){
        if (Bukkit.getPlayer(playerUUID) != null) {
            Player p = Bukkit.getPlayer(playerUUID);
           PermissionAttachment attachment =  p.addAttachment(plugin);
           for (String permission : group.getPermissions()) {
               attachment.setPermission(permission, true);
           }
        }
    }
}

