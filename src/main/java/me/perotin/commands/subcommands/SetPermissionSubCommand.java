package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Subcommand for setting a permission of a group
 */
public class SetPermissionSubCommand implements SubCommand {

    private final SimpleGroups plugin;

    public SetPermissionSubCommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 3) {
            commandSender.sendMessage(plugin.getMessage("messages.usage-setpermission"));
            return;
        }

        String groupName = args[1];
        String permission = args[2];
        boolean value = args.length > 3 && Boolean.parseBoolean(args[3]); // Optional arg

        PermissionGroup group = plugin.getGroup(groupName);
        if (group == null) {
            commandSender.sendMessage(plugin.getMessage("messages.group-not-exist")
                    .replace("{group}", groupName));
            return;
        }

        if (value) {
            // Add permission to the group
            group.addPermission(permission, plugin);
            commandSender.sendMessage(plugin.getMessage("messages.permission-added")
                    .replace("{group}", groupName)
                    .replace("{permission}", permission));
        } else {
            if (group.getPermissions().contains(permission)) {
                group.removePermission(permission, plugin);
                commandSender.sendMessage(plugin.getMessage("messages.permission-removed")
                        .replace("{group}", groupName)
                        .replace("{permission}", permission));
            } else {
                commandSender.sendMessage(plugin.getMessage("messages.permission-not-found")
                        .replace("{permission}", permission)
                        .replace("{group}", groupName));
            }
        }
    }

}
