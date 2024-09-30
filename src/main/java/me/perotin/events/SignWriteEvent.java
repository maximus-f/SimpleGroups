package me.perotin.events;

import me.perotin.SimpleGroups;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;

import java.util.UUID;

/*
    Listener to change sign to display information on player.
 */
public class SignWriteEvent implements Listener {

    private final SimpleGroups plugin;

    public SignWriteEvent(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    /*
        Update sign when {group} written on sign to show rank prefix and name.
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("{simplegroups}") || lines[i].contains("{group}") || lines[i].contains("{player}")) {
                UUID playerUUID = player.getUniqueId();
                int finalI = i;
                plugin.getPlayer(playerUUID, (simplePlayer, fromMemory) -> {
                    if (simplePlayer != null) {
                        String prefix = simplePlayer.getGroup().getPrefix();
                        String playerName = player.getName();

                        lines[finalI] = prefix;
                        lines[finalI + 1] = playerName;

                        Sign sign = (Sign) event.getBlock().getState();
                        sign.setLine(0, prefix);
                        sign.setLine(1, playerName);
                        sign.update();
                    }
                });
                break;
            }
        }
    }
}
