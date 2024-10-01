package me.perotin.events;

import me.perotin.SimpleGroups;
import me.perotin.objects.SimplePlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class SimpleChatEvent implements Listener {

    private final SimpleGroups plugin;

    public SimpleChatEvent(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
       String form =  plugin.getConfig().getString("chat-format");
       plugin.getPlayer(player.getUniqueId(), (sPlayer, fromMemory) -> {
            if (sPlayer != null) {
                String prefix = sPlayer.getGroup().getPrefix();
                event.setFormat(ChatColor.translateAlternateColorCodes('&', form.replace("{prefix}", prefix)
                        .replace("{name}", player.getName())
                        .replace("{message}", event.getMessage())));
            } else {
                event.setFormat(ChatColor.translateAlternateColorCodes('&', form
                        .replace("{name}", player.getName())
                        .replace("{message}", event.getMessage())));            }
        });
    }
}
