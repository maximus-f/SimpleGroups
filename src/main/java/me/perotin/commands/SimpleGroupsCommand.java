package me.perotin.commands;

import me.perotin.SimpleGroups;
import me.perotin.commands.subcommands.CreateGroupCommand;
import me.perotin.commands.subcommands.SetPermissionSubCommand;
import me.perotin.commands.subcommands.SetPlayerGroupCommand;
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

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public SimpleGroupsCommand(SimpleGroups plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }


    private void registerSubCommands() {
        subCommands.put("creategroup", new CreateGroupCommand(plugin));
        subCommands.put("setplayer", new SetPlayerGroupCommand(plugin));
        subCommands.put("setpermission", new SetPermissionSubCommand(plugin));
    }


    /*
        Command layout : /simplegroups /sg
        /sg create <group-name> <optional: inherit> <optional: name> - Create group, simplegroups.admin
        /sg setpermission <group> <permission> <optional: true/false> - Set group permission, simplegroup.admin
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

        boolean hasPerms = commandSender.isOp() || commandSender.hasPermission("simplegroups.admin");
        if (args[0].equalsIgnoreCase("creategroup") && hasPerms) {
            subCommands.get("creategroup").execute(commandSender, args);
            return true;

        } else if (args[0].equalsIgnoreCase("setplayer") && hasPerms) {
            // Set player to group
          subCommands.get("setplayer").execute(commandSender, args);
            return true;
        } if (args[0].equalsIgnoreCase("setpermission") && hasPerms) {
            // Set permission for a group
          subCommands.get("setpermission").execute(commandSender, args);
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
