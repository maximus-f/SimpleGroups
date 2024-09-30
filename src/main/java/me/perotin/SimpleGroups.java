package me.perotin;

import lombok.Getter;
import me.perotin.commands.SimpleGroupsCommand;
import me.perotin.database.DatabaseManager;
import me.perotin.events.SignWriteEvent;
import me.perotin.events.SimpleJoinEvent;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/*
 TODO
 */
public class SimpleGroups extends JavaPlugin {

    // Use Maps for faster #get retrievals
    private Map<String, PermissionGroup> groups;
    @Getter
    private Map<UUID, SimplePlayer> players;

    @Getter
    private DatabaseManager databaseManager; // SQLite db

    @Getter
    private FileConfiguration messagesConfig;
    @Getter
    private String lang;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.lang = getConfig().getString("lang");
        loadMessages();
        players = new HashMap<>();
        groups = new HashMap<>();
        getCommand("simplegroups").setExecutor(new SimpleGroupsCommand(this));
        getServer().getPluginManager().registerEvents(new SimpleJoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new SignWriteEvent(this), this);

        run();
        // TODO: Add code here to function with /reload commands (players found on start up)
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


    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "lang/" + lang+".yml"); // Change to desired locale

        if (!messagesFile.exists()) {
            saveResource("lang/"+lang+".yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // Load database, groups, set memory states for players
    private void run()  {
        // Instantiate db manager and default group
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            databaseManager = new DatabaseManager(getDataFolder().getAbsolutePath() + "/database.db", this);

            // Load all groups from the database
            List<PermissionGroup> allGroups = databaseManager.getAllPermissionGroups();
            boolean defaultNotFound = true;

            for (PermissionGroup group : allGroups) {
                groups.put(group.getName(), group);
                if (group.getName().equalsIgnoreCase("default")) {
                    defaultNotFound = false;
                }
            }

            if (defaultNotFound) {
                createAndAddDefaultGroup();
            }
        } catch (SQLException e) {
            // Disable plugin if connection fail; hard requirement.
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    public void addGroup(PermissionGroup group, boolean isNew) {
        groups.put(group.getName(), group);
        if (isNew) {
            // Write to database
            try {
                databaseManager.writeNewGroup(group);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     *  Deletes group from memory and database. Sets all current users to default.
     * @param group
     */
    public void removeGroup(PermissionGroup group) {
        // Reassign members in memory to default rank
        getPlayersWithRank(group).forEach(simplePlayer -> simplePlayer.setGroup(getGroup("default"), Bukkit.getPlayer(simplePlayer.getPlayerUUID()), this, -1));
        groups.remove(group.getName());

        try {
            databaseManager.deletePermissionGroup(group.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the default group and adds it to the database and memory.
     * This function is called when the default group is not found in the database.
     */
    private void createAndAddDefaultGroup() throws SQLException {
        PermissionGroup defaultGroup = new PermissionGroup("default", getConfig().getString("default-prefix", "[Default] "));
        groups.put("default", defaultGroup);
        databaseManager.writeNewGroup(defaultGroup);
    }

    /**
     * Retrieve all players part of a specified PermissionGroup.
     *
     * @param group
     * @return List of SimplePlayer objects part of a PermissionGroup
     */
    public List<SimplePlayer> getPlayersWithRank(PermissionGroup group) {
        return players.values().stream()
                .filter(player -> group.getName().equalsIgnoreCase(player.getGroup().getName()))
                .collect(Collectors.toList());
    }



    public PermissionGroup getGroup(String name) {
        return groups.get(name);
    }

    /**
     * Asynchronously retrieves a player from memory or the database.
     * If the player is found in memory, the callback is executed immediately.
     * If not found, an asynchronous database query is made, and the result is passed to the callback on the main thread.
     *
     * @param uuid The UUID of the player to retrieve.
     * @param callback The callback to handle the retrieved player.
     */
    public void getPlayer(UUID uuid, PlayerCallback callback) {
        // If the player is already in memory, invoke the callback immediately
        if (players.containsKey(uuid)) {
            callback.onResult(players.get(uuid), true);
        } else {
            // Run the database task asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                SimplePlayer simplePlayer;
                boolean fromMemory = false;

                try {
                    String group = getDatabaseManager().getPlayerGroup(uuid);

                    if (group == null) {
                        // Player not found in the database, assign to default group
                        getDatabaseManager().assignPlayerToGroup(uuid, "default", -1);
                        simplePlayer = new SimplePlayer(uuid, getGroup("default"), -1);

                    } else {
                        // Player found in the database, assign to the retrieved group
                        simplePlayer = getDatabaseManager().loadPlayer(uuid.toString());
                    }
                    // If player is not null, add attachment if null
                    Player targ = Bukkit.getPlayer(uuid);
                    if (targ != null && simplePlayer.getPermissionAttachment() == null) {
                        PermissionAttachment attachment = targ.addAttachment(this);
                        simplePlayer.setPermissionAttachment(attachment);
                    }
                    players.put(uuid, simplePlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                // Return callback on main thread
                Bukkit.getScheduler().runTask(this, () -> callback.onResult(simplePlayer, fromMemory));
            });
        }
    }

    public interface PlayerCallback {
        void onResult(SimplePlayer player, boolean fromMemory);
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(path, "Null text found for: " + path));
    }





}
