package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class SetPlayerGroupCommand implements SubCommand {

    private final SimpleGroups plugin;

    public SetPlayerGroupCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Objects.requireNonNull(plugin.getConfig().getString("messages.usage-setplayer")));
            return;
        }

        String groupName = args[1];
        String playerName = args[2];
        PermissionGroup group = plugin.getGroup(groupName);

        if (group == null) {
            sender.sendMessage(plugin.getConfig().getString("messages.group-not-exist")
                    .replace("{group}", groupName));
            return;
        }

        // Run async task for setting another player's group
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin.getConfig().getString("messages.player-not-online")
                        .replace("{player}", playerName)));
                return;
            }

            UUID playerUUID = targetPlayer.getUniqueId();
            SimplePlayer simplePlayer = new SimplePlayer(playerUUID, group, 0, targetPlayer.addAttachment(plugin)); // No expiration for now
            plugin.addPlayer(simplePlayer);

            // Run on the main thread to send the message back
            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin.getConfig().getString("messages.player-added")
                    .replace("{player}", playerName)
                    .replace("{group}", groupName)));
        });
    }
}
