package me.perotin.commands;

import me.perotin.SimpleGroups;
import me.perotin.commands.subcommands.*;
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
        subCommands.put("deletegroup", new DeleteGroupCommand(plugin));
        subCommands.put("listgroups", new ListGroupsCommand(plugin));
        subCommands.put("listpermissions", new ListPermissionsSubcommand(plugin));
    }


    /*
        Command layout : /simplegroups /sg
        /sg create <group-name> > <prefix> - Create group, simplegroups.admin
        /sg setpermission <group> <permission> <optional: true/false> - Set group permission, simplegroup.admin
        /sg setplayer <group-name> <player-name> <optional: time> - Add player to group, simplegroup.admin
        /sg deletegroup <group>
        /sg listgroups
        /sg listpermissions <group/player>
        /sg - Displays current group and time remaining if applicable, no permission

     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) && args.length == 0) {
            commandSender.sendMessage(plugin.getMessage("messages.only-players"));
            return true;
        }

        if (args.length == 0) {
            // Display current group for the player
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
            return true;
        }

        boolean hasPerms = commandSender.isOp() || commandSender.hasPermission("simplegroups.admin");
        if (args[0].equalsIgnoreCase("help") && hasPerms) {
            commandSender.sendMessage("/sg create <group-name> > <prefix> - Create group");
            commandSender.sendMessage("/sg setpermission <group> <permission> <true/false> ");
            commandSender.sendMessage("/sg setplayer <group-name> <player-name> <optional: time>");
            commandSender.sendMessage("/sg deletegroup <group>");
            commandSender.sendMessage("/sg listgroups");
            commandSender.sendMessage("/sg listpermissions <player/group>");
        }
        if (args[0].equalsIgnoreCase("listpermissions") && hasPerms) {
            subCommands.get("listpermissions").execute(commandSender, args);
            return true;

        }
        if (args[0].equalsIgnoreCase("listgroups") && hasPerms) {
            subCommands.get("listgroups").execute(commandSender, args);
            return true;

        }
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
        } if (args[0].equalsIgnoreCase("deletegroup") && hasPerms) {
            // Set permission for a group
            subCommands.get("deletegroup").execute(commandSender, args);
            return true;
        }


        commandSender.sendMessage(plugin.getMessage("messages.unknown-command"));
        return false;
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



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("creategroup", "setplayer", "setpermission", "deletegroup", "listgroups", "listpermissions", "help");
            return filterSuggestions(args[0], suggestions);
        }
    return null;
    }
    private List<String> filterSuggestions(String input, List<String> options) {
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
