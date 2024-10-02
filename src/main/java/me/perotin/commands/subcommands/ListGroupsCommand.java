package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import org.bukkit.command.CommandSender;


public class ListGroupsCommand implements SubCommand {

    private SimpleGroups plugin;

    public ListGroupsCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getGroups().keySet().forEach(sender::sendMessage);
    }
}
