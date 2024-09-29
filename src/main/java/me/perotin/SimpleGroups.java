package me.perotin;

import lombok.Getter;
import me.perotin.commands.SimpleGroupsCommand;
import me.perotin.database.DatabaseManager;
import me.perotin.events.SimpleJoinEvent;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/*
 TODO
    Test. Currently viable to function but surely has errors.
 */
public class SimpleGroups extends JavaPlugin {

    // Use Maps for faster #get retrievals
    private Map<String, PermissionGroup> groups;
    private Map<UUID, SimplePlayer> players;

    @Getter
    private DatabaseManager databaseManager; // SQLite db


    @Override
    public void onEnable() {
        saveDefaultConfig();
        players = new HashMap<>();
        groups = new HashMap<>();
        getCommand("simplegroups").setExecutor(new SimpleGroupsCommand(this));
        getServer().getPluginManager().registerEvents(new SimpleJoinEvent(this), this);
        run();
    }


    @Override
    public void onDisable() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    // Load database, groups, set memory states for players
    private void run()  {
        // Instantiate db manager and default group
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            databaseManager = new DatabaseManager(getDataFolder().getAbsolutePath() + "/database.db");
           List<PermissionGroup> allGroups = databaseManager.getAllPermissionGroups();
           for (PermissionGroup group : allGroups) {
               groups.put(group.getName(), group);
           }
        } catch (SQLException e) {
            // Disable plugin if connection fail; hard requirement.
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        // Sets permissions in case of a reload
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                getPlayer(player.getUniqueId()).player.setPermissions(this);
            }
        }
    }

    public void addGroup(PermissionGroup group) {
        groups.put(group.getName(), group);
    }

    public void removeGroup(PermissionGroup group) {
        groups.remove(group.getName());
    }


    public PermissionGroup getGroup(String name) {
        return groups.get(name);
    }

    /**
     * Checks memory for object, then database, then creates newly if none found.
     * Returns true if previously in memory and permissions do not need to be set, false if otherwise.
     *
     * @param uuid
     * @return player object always
     *
     * TODO May need to use a Future Callback here instead of AtomicReference. But ok for now.
     */
    public Pair getPlayer(UUID uuid) {
        if (players.containsKey(uuid)) {
            return new Pair(players.get(uuid), false);
        } else {
            AtomicReference<SimplePlayer> player = new AtomicReference<>();
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {

                    String group = getDatabaseManager().getPlayerGroup(uuid);
                    if (group == null) {
                        // Not in database, assert as default
                        getDatabaseManager().assignPlayerToGroup(uuid, "default");
                         player.set(new SimplePlayer(uuid, getGroup("default"), -1));;
                    } else {
                        // Pull from database.
                         player.set(new SimplePlayer(uuid, getGroup(group), -1));

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return new Pair(player.get(), false);
        }
    }

    public void addPlayer(SimplePlayer player) {
        players.put(player.getPlayerUUID(), player);
    }

    public static class Pair {
        public final SimplePlayer player;
        public final boolean loaded;
        public Pair(SimplePlayer x, boolean y) {
            this.player = x;
            this.loaded = y;
        }
    }


}
