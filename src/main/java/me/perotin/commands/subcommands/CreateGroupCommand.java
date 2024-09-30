package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import org.bukkit.command.CommandSender;

/*
    Subcommand class for /sg creategroup
 */
public class CreateGroupCommand implements SubCommand {


    private final SimpleGroups plugin;

    public CreateGroupCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("messages.usage-create"));
            return;
        }

        String groupName = args[1];
        String prefix = args[2];
        PermissionGroup newGroup = new PermissionGroup(groupName, prefix);

        plugin.addGroup(newGroup, true);
        sender.sendMessage(plugin.getMessage("messages.group-created").replace("{group}", groupName));
    }
}
