package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import org.bukkit.command.CommandSender;

public class DeleteGroupCommand implements SubCommand {

    private SimpleGroups plugin;

    public DeleteGroupCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    // /sg deletegroup <name>
    @Override
    public void execute(CommandSender sender, String[] args) {
        String groupName = args[1];

        PermissionGroup group = plugin.getGroup(groupName);
        if (group == null) {
            sender.sendMessage(plugin.getMessage("messages.group-not-exist")
                    .replace("{group}", groupName));

        } else {
            if (group.getName().equalsIgnoreCase("default")) {
                // Must be a default rank
                sender.sendMessage(plugin.getMessage("messages.cannot-delete-default"));
                return;
            }
            plugin.removeGroup(group);
        }
    }
}
