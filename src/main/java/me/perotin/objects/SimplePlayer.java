package me.perotin.objects;

import java.util.UUID;


/**
 * @author maxfuligni
 *
 *  Base representation of a player object with their group and if
 *  it should expire.
 */
public class SimplePlayer {

        private UUID playerUUID;
        private PermissionGroup group;
        private long expirationTime;

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

        public PermissionGroup getGroup() {
            return group;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }
    }

