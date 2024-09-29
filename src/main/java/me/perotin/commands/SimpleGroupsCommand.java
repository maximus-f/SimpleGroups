package me.perotin.commands;

import me.perotin.SimpleGroups;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;


public class SimpleGroupsCommand implements CommandExecutor, TabCompleter {


    private final SimpleGroups plugin;

    public SimpleGroupsCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }


    /*
        Command layout : /simplegroups /sg
        /sg create <group-name> <optional: inherit> <optional: name> - Create group, simplegroups.admin
        /sg setplayer <group-name> <player-name> - Add player to group, simplegroup.admin
        /sg - Displays current group and time remaining if applicable, no permission
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) && args.length == 0) {
            commandSender.sendMessage(plugin.getConfig().getString("messages.only-players"));
            return true;
        }

        if (args.length == 0) {
            // Display current group for the player
            UUID playerUUID = ((Player) commandSender).getUniqueId(); // Safe to cast
            SimpleGroups.Pair pair = plugin.getPlayer(playerUUID);
            SimplePlayer simplePlayer = pair.player;
            if (simplePlayer != null && !simplePlayer.isExpired()) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.current-group")
                        .replace("{group}", simplePlayer.getGroup().getName())
                        .replace("{time}", simplePlayer.isTemporary()
                                ? (simplePlayer.getExpirationTime() - System.currentTimeMillis()) + "ms"
                                : plugin.getConfig().getString("messages.no-time")));
            } else {
                commandSender.sendMessage(plugin.getConfig().getString("messages.no-group"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && (commandSender.isOp() || commandSender.hasPermission("simplegroups.admin"))) {
          // Create Group
            if (args.length < 2) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.usage-create"));
                return false;
            }

            String groupName = args[1];
            String inheritGroup = args.length > 2 ? args[2] : null; // Optional inherit, implement later
            PermissionGroup newGroup = new PermissionGroup(groupName, "prefix_" + groupName); // Assign a prefix

            plugin.addGroup(newGroup);
            commandSender.sendMessage(plugin.getConfig().getString("messages.group-created")
                    .replace("{group}", groupName));
            return true;

        } else if (args[0].equalsIgnoreCase("setplayer") && (commandSender.isOp() || commandSender.hasPermission("simplegroups.admin"))) {
            // Set player to group
            if (args.length < 3) {
                commandSender.sendMessage(Objects.requireNonNull(plugin.getConfig().getString("messages.usage-setplayer")));
                return false;
            }

            String groupName = args[1];
            String playerName = args[2];
            PermissionGroup group = plugin.getGroup(groupName);

            if (group == null) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.group-not-exist")
                        .replace("{group}", groupName));
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.player-not-online")
                        .replace("{player}", playerName));
                return true;
            }

            UUID playerUUID = targetPlayer.getUniqueId();
            SimplePlayer simplePlayer = new SimplePlayer(playerUUID, group, 0); // No expiration for now
            plugin.addPlayer(simplePlayer);
            commandSender.sendMessage(plugin.getConfig().getString("messages.player-added")
                    .replace("{player}", playerName)
                    .replace("{group}", groupName));
            return true;
        }

        commandSender.sendMessage(plugin.getConfig().getString("messages.unknown-command"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
