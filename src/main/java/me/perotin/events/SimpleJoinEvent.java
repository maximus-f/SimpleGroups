package me.perotin.events;

import me.perotin.SimpleGroups;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/*
    Class to register new players in memory with their permissions
 */
public class SimpleJoinEvent implements Listener {

    private SimpleGroups plugin;

    public SimpleJoinEvent(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        SimpleGroups.Pair player = plugin.getPlayer(joiner.getUniqueId()); // Should handle all cases in #getPlayer

        // Set permissions if not previously found in memory (meaning not been set yet)
        if (player != null) {
            if (!player.loaded) {
                player.player.setPermissions(plugin);
            }
        }
    }
}
