package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Subcommand for setting a player's group.
 */
public class SetPlayerGroupCommand implements SubCommand {

    private final SimpleGroups plugin;

    public SetPlayerGroupCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Objects.requireNonNull(plugin.getMessage("messages.usage-setplayer")));
            return;
        }

        String groupName = args[1];
        String playerName = args[2];
        PermissionGroup group = plugin.getGroup(groupName);

        if (group == null) {
            sender.sendMessage(plugin.getMessage("messages.group-not-exist")
                    .replace("{group}", groupName));
            return;
        }
        Duration duration = null;
        if (args.length >= 4) {
            try {
                duration = parseDuration(args[3]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid time format. Use 4d3h2m30s as an example.");
                return;
            }
        }


        // Run async task for setting another player's group
        Duration finalDuration = duration;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin.getMessage("messages.player-not-online")
                        .replace("{player}", playerName)));
                return;
            }

            UUID playerUUID = targetPlayer.getUniqueId();
            plugin.getPlayer(playerUUID, (player, fromMemory) -> {
                long expirationTime = (finalDuration != null) ? System.currentTimeMillis() + finalDuration.toMillis() : -1;

                player.setGroup(group, targetPlayer, plugin, expirationTime);
                targetPlayer.sendMessage(plugin.getMessage("messages.group-changed")
                        .replace("{group}", group.getName()));
            });

            // Run on the main thread to send the message back
            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin.getMessage("messages.player-added")
                    .replace("{player}", playerName)
                    .replace("{group}", groupName)));
        });
    }

    // Regex parser for duration in format: 4d3h2m30s -> 4 days, 3 hours, 2 minutes and 30 seconds.
    private Duration parseDuration(String time) {
        Pattern pattern = Pattern.compile("(\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?");
        Matcher matcher = pattern.matcher(time);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format");
        }

        int days = parseValue(matcher.group(1));
        int hours = parseValue(matcher.group(2));
        int minutes = parseValue(matcher.group(3));
        int seconds = parseValue(matcher.group(4));

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    // Fetch the numeric value per unit of time
    private int parseValue(String group) {
        if (group == null || group.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(group.substring(0, group.length() - 1));  // Remove the time unit (e.g., 'd', 'h')
    }
}
