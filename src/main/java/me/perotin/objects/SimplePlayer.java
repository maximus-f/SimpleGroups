package me.perotin.objects;

import lombok.Getter;
import lombok.Setter;
import me.perotin.SimpleGroups;
import me.perotin.events.SimpleJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.checkerframework.checker.units.qual.A;

import java.sql.SQLException;
import java.util.*;


/**
 * @author maxfuligni
 *
 *  Base representation of a player object with their group and if
 *  it should expire.
 */

@Getter
public class SimplePlayer {

    private UUID playerUUID;
    @Setter
    private PermissionAttachment permissionAttachment;
    private PermissionGroup group;
    private long expirationTime; // -1 if permanent.

    // PermissionAttachment is set on setPermissions
    public SimplePlayer(UUID playerUUID, PermissionGroup group, long expirationTime) {
        this.playerUUID = playerUUID;
        this.group = group;
        this.expirationTime = expirationTime;
    }


    public boolean isTemporary() {
        return expirationTime > 0;
    }

    /**
     *  Remove all permission attachment of a Player object
     * @param uuid
     */
    public void clearPermissionFromPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        Set<PermissionAttachment> toRemove = new HashSet<>();

        if (player != null && !player.getEffectivePermissions().isEmpty()) {

            player.getEffectivePermissions().forEach(permissionAttachmentInfo -> {
                Bukkit.broadcastMessage(permissionAttachmentInfo.getPermission() + " found in clearPerm");
                if (permissionAttachmentInfo.getAttachment() != null) {
                    toRemove.add(permissionAttachmentInfo.getAttachment());
                }
            });
            toRemove.forEach(perm -> Bukkit.broadcastMessage(perm.toString()));
            for (PermissionAttachment attachment : toRemove) {
                attachment.getPermissions().keySet().forEach(Bukkit::broadcastMessage);
                player.removeAttachment(attachment);
            }

        }
    }

    /**
     *  Changes group of a player. Clears old PermissionAttachment and updates with new permissions and
     *  writes to database.
     * @param group
     */
    public void setGroup(PermissionGroup group, Player player, SimpleGroups plugin, long expirationTime) {
        this.group = group;
        this.expirationTime = expirationTime;
        if (player != null) {
            // Clear all permissions since will be using new one.
            clearPermissionFromPlayer(player.getUniqueId());
        } else {
            /*
                Can occur if group of offline player gets deleted. In this case, unload object
                and let it be loaded next time.
             */
            unload(plugin);
        }
        setPermissions(plugin);
        // TODO this should be refactored to be static member of utility class.
        SimpleJoinEvent.createOrUpdateTeamForPlayer(player, this);
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', getGroup().getPrefix() + " " + player.getName()));

        try {
            plugin.getDatabaseManager().assignPlayerToGroup(getPlayerUUID(), group.getName(), expirationTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

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
            clearPermissionFromPlayer(p.getUniqueId()); // Adding just in case
            if (p != null) {
                PermissionAttachment attachment = p.addAttachment(plugin);
                for (String permission : group.getPermissions()) {
                    attachment.setPermission(permission, true);
                }
                setPermissionAttachment(attachment);
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
        PermissionAttachment attachment = getPermissionAttachment();
            if (attachment != null) {
                if (add) {
                    Bukkit.broadcastMessage("Adding new permission: " + permission + " to " + getGroup().getName());
                    attachment.setPermission(permission, true);
                } else {
                    Bukkit.broadcastMessage("Removing new permission: " + permission + " from " + getGroup().getName());

                    attachment.unsetPermission(permission);
            }
        } else {
            /*
                PermissionAttachment is set on join. If this occurs, then it means that the SimplePlayer object is
                loaded without the Player object ever having been loaded. This theoretically should never occur, but in
                case it does, unload current object so that it will be loaded correctly next time.
             */
            unload(plugin);
        }
    }

    private void unload(SimpleGroups plugin) {
        plugin.getPlayers().remove(playerUUID);
    }

}

