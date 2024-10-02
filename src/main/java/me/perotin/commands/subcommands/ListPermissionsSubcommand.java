package me.perotin.commands.subcommands;

import me.perotin.SimpleGroups;
import me.perotin.commands.SubCommand;
import me.perotin.objects.PermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.Set;

// Subcommand for listing all permissions of a group or player
public class ListPermissionsSubcommand implements SubCommand {

    private final SimpleGroups plugin;

    public ListPermissionsSubcommand(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/sg listpermissions <group|player>");
            return;
        }

        String target = args[1];

        // Check if the target is a group
        PermissionGroup group = plugin.getGroup(target);
        if (group != null) {
            sender.sendMessage("Permissions for group " + target + ":");
            Set<String> permissions = group.getPermissions();
            if (permissions.isEmpty()) {
                sender.sendMessage("This group has no permissions.");
            } else {
                for (String perm : permissions) {
                    sender.sendMessage("- " + perm);
                }
            }
            return;
        }

        // If not a group, check if the target is a player
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            sender.sendMessage("Permissions for player " + player.getName() + ":");
            for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
                sender.sendMessage("- " + permInfo.getPermission() + " (value: " + permInfo.getValue() + ")");
            }
            return;
        }

        // If neither a group nor a player was found
        sender.sendMessage("No group or online player found with the name " + target);
    }
}
