package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InformationalCommand implements SubCommand {

    private SimpleGroups plugin;

    public InformationalCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        UUID playerUUID = ((Player) commandSender).getUniqueId();
        Player player = Bukkit.getPlayer(playerUUID);

        plugin.getPlayer(playerUUID, (simplePlayer, fromMemory) -> {
            if (simplePlayer != null && !simplePlayer.isExpired()) {
                long timeRemaining = simplePlayer.isTemporary()
                        ? (simplePlayer.getExpirationTime() - System.currentTimeMillis())
                        : -1;

                String timeRemainingString = timeRemaining > 0 ? formatTime(timeRemaining) : plugin.getMessage("messages.no-time");

                commandSender.sendMessage(plugin.getMessage("messages.current-group")
                        .replace("{group}", simplePlayer.getGroup().getName())
                        .replace("{time}", timeRemainingString));
                if (commandSender.isOp() || commandSender.hasPermission("simpleplayer.admin")) {
                    commandSender.sendMessage(plugin.getMessage("messages.helpguide"));
                }

            } else {
                if (simplePlayer.getGroup() != null && simplePlayer.isExpired()) {
                    commandSender.sendMessage(plugin.getMessage("messages.expired-group")
                            .replace("{group}", simplePlayer.getGroup().getName()));
                    simplePlayer.setGroup(plugin.getGroup("default"), player, plugin, -1);
                }  else {
                    // This case should never happen but leaving it for now. Cleanup needed here most likely.
                    commandSender.sendMessage(plugin.getMessage("messages.no-group"));
                }
            }
         });
    }
    // Format time excluding 0 values
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) formattedTime.append(days).append(" days, ");
        if (hours > 0) formattedTime.append(hours).append(" hours, ");
        if (minutes > 0) formattedTime.append(minutes).append(" minutes, ");
        if (seconds > 0) formattedTime.append(seconds).append(" seconds");

        // Remove trailing commas and spaces
        if (formattedTime.length() > 2 && formattedTime.charAt(formattedTime.length() - 2) == ',') {
            formattedTime.setLength(formattedTime.length() - 2);
        }
        return formattedTime.toString();
    }
}
