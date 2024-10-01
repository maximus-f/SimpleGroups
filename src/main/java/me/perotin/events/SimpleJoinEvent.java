package me.perotin.events;

import me.perotin.SimpleGroups;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/*
    Class to register new players in memory with their permissions and set scoreboard
 */
public class SimpleJoinEvent implements Listener {

    private SimpleGroups plugin;

    public SimpleJoinEvent(SimpleGroups plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        if (!joiner.hasPlayedBefore()) {
            // First time, set to default and return
           SimplePlayer firstTimer =  new SimplePlayer(joiner.getUniqueId(), plugin.getGroup("default"), -1);
           firstTimer.setPermissions(plugin);
           String joinMsg = ChatColor.translateAlternateColorCodes('&', plugin.getMessage("messages.join-message")
                    .replace("{prefix}", firstTimer.getGroup().getPrefix())
                    .replace("{name}", joiner.getName()));
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&',joinMsg));
            plugin.getPlayers().put(joiner.getUniqueId(), firstTimer);
            createOrUpdateTeamForPlayer(joiner, firstTimer);
            joiner.setPlayerListName(ChatColor.translateAlternateColorCodes('&', firstTimer.getGroup().getPrefix() + " " + joiner.getName()));
            return;
        }

        plugin.getPlayer(joiner.getUniqueId(), (simplePlayer, fromMemory) -> {

            // Always need to set perms since its new player obj on join
            simplePlayer.setPermissions(plugin);


            String joinMsg = ChatColor.translateAlternateColorCodes('&', plugin.getMessage("messages.join-message")
                    .replace("{prefix}", simplePlayer.getGroup().getPrefix())
                    .replace("{name}", joiner.getName()));
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&',joinMsg));

            createOrUpdateTeamForPlayer(joiner, simplePlayer);
            joiner.setPlayerListName(ChatColor.translateAlternateColorCodes('&', simplePlayer.getGroup().getPrefix() + " " + joiner.getName()));

        });
    }

    // Fetch main scoreboard and register team with prefix as group prefix
    public static void createOrUpdateTeamForPlayer(Player target, SimplePlayer player) {
        PermissionGroup group = player.getGroup();
        String prefix = group.getPrefix();
        String teamName = group.getName();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName);

        // new team with prefix
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setPrefix(prefix);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }

        team.addEntry(target.getName());
        target.setScoreboard(scoreboard);
    }
}
