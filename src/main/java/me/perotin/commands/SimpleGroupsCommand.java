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
        subCommands.put("help", new HelpCommand());
        subCommands.put("informational", new InformationalCommand(plugin));
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
           subCommands.get("informational").execute(commandSender, args);
        }

        boolean hasPerms = commandSender.isOp() || commandSender.hasPermission("simplegroups.admin");

        if (subCommands.containsKey(args[0]) && hasPerms) {
            subCommands.get(args[0]).execute(commandSender, args);
            return true;

        }



        commandSender.sendMessage(plugin.getMessage("messages.unknown-command"));
        return false;
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
