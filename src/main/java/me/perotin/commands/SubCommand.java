package me.perotin.commands;

import org.bukkit.command.CommandSender;

/**
 *  SubCommand interface to encapsulate subcommands
 */
public interface SubCommand {
    void execute(CommandSender sender, String[] args);
}
