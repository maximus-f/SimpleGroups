package me.perotin.events;

import me.perotin.SimpleGroups;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        plugin.getPlayer(joiner.getUniqueId(), (simplePlayer, fromMemory) -> {
            if (!fromMemory) {
                simplePlayer.setPermissions(plugin);
            }
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("join-message")
                    .replace("{prefix}", simplePlayer.getGroup().getPrefix())
                    .replace("{name}", joiner.getName())));
        });
    }
}
