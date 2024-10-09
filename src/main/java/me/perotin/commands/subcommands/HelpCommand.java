package me.perotin.commands.subcommands;

import me.perotin.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand implements SubCommand {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        commandSender.sendMessage("/sg create <group-name> > <prefix> - Create group");
        commandSender.sendMessage("/sg setpermission <group> <permission> <true/false> ");
        commandSender.sendMessage("/sg setplayer <group-name> <player-name> <optional: time>");
        commandSender.sendMessage("/sg deletegroup <group>");
        commandSender.sendMessage("/sg listgroups");
        commandSender.sendMessage("/sg listpermissions <player/group>");
    }
}
