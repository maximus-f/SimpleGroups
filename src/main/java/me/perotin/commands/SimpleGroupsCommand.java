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
import java.util.concurrent.CompletableFuture;


/*
    Base command class for /simplegroups
 */
public class SimpleGroupsCommand implements CommandExecutor, TabCompleter {


    private final SimpleGroups plugin;

    public SimpleGroupsCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }


    /*
        Command layout : /simplegroups /sg
        /sg create <group-name> <optional: inherit> <optional: name> - Create group, simplegroups.admin
        /sg setpermission <group> <permission> <optional: true/false> - Set group permission, simplegroup.admin
        /sg setplayer <group-name> <player-name> - Add player to group, simplegroup.admin
        /sg - Displays current group and time remaining if applicable, no permission

        TODO: Clean code up with delegation.
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

            plugin.getPlayer(playerUUID, (simplePlayer, fromMemory) -> {
                if (simplePlayer != null && !simplePlayer.isExpired()) {
                    commandSender.sendMessage(plugin.getConfig().getString("messages.current-group")
                            .replace("{group}", simplePlayer.getGroup().getName())
                            .replace("{time}", simplePlayer.isTemporary()
                                    ? (simplePlayer.getExpirationTime() - System.currentTimeMillis()) + "ms"
                                    : plugin.getConfig().getString("messages.no-time")));
                } else {
                    commandSender.sendMessage(plugin.getConfig().getString("messages.no-group"));
                }
            });
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && (commandSender.isOp() || commandSender.hasPermission("simplegroups.admin"))) {
            if (args.length < 2) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.usage-create"));
                return false;
            }

            String groupName = args[1];
            String inheritGroup = args.length > 2 ? args[2] : null; // Optional inherit
            PermissionGroup newGroup = new PermissionGroup(groupName, "prefix_" + groupName); // Assign a prefix

            plugin.addGroup(newGroup, true);
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

            // Run async task for setting another player's group
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Player targetPlayer = Bukkit.getPlayer(playerName);
                if (targetPlayer == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> commandSender.sendMessage(plugin.getConfig().getString("messages.player-not-online")
                            .replace("{player}", playerName)));
                    return;
                }

                UUID playerUUID = targetPlayer.getUniqueId();
                SimplePlayer simplePlayer = new SimplePlayer(playerUUID, group, 0, targetPlayer.addAttachment(plugin)); // No expiration for now
                plugin.addPlayer(simplePlayer);

                // Run on the main thread to send the message back
                Bukkit.getScheduler().runTask(plugin, () -> commandSender.sendMessage(plugin.getConfig().getString("messages.player-added")
                        .replace("{player}", playerName)
                        .replace("{group}", groupName)));
            });
            return true;
        } if (args[0].equalsIgnoreCase("setpermission") && (commandSender.isOp() || commandSender.hasPermission("simplegroups.admin"))) {
            // Set permission for a group
            if (args.length < 3) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.usage-setpermission"));
                return false;
            }

            String groupName = args[1];
            String permission = args[2];
            boolean value = args.length > 3 && Boolean.parseBoolean(args[3]); // Optional arg

            PermissionGroup group = plugin.getGroup(groupName);
            if (group == null) {
                commandSender.sendMessage(plugin.getConfig().getString("messages.group-not-exist")
                        .replace("{group}", groupName));
                return true;
            }

            if (value) {
                // Add permission to the group
                group.addPermission(permission, plugin);
                commandSender.sendMessage(plugin.getConfig().getString("messages.permission-added")
                        .replace("{group}", groupName)
                        .replace("{permission}", permission));
            } else {
                if (group.getPermissions().contains(permission)) {
                    group.removePermission(permission, plugin);
                    commandSender.sendMessage(plugin.getConfig().getString("messages.permission-removed")
                            .replace("{group}", groupName)
                            .replace("{permission}", permission));
                } else {
                    commandSender.sendMessage(plugin.getConfig().getString("messages.permission-not-found")
                            .replace("{permission}", permission)
                            .replace("{group}", groupName));
                }
            }
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
