package me.perotin.database;

import me.perotin.SimpleGroups;
import me.perotin.objects.PermissionGroup;
import me.perotin.objects.SimplePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.Mockito.*;

// Test class for fetching player data from memory or otherwise
public class SimpleGroupTests {

    private SimpleGroups plugin;
    private DatabaseManager mockDatabaseManager;
    private SimpleGroups.PlayerCallback mockCallback;
    private UUID mockPlayer;

    @BeforeEach
    public void setUp()  {
        MockitoAnnotations.openMocks(this);
        plugin = mock(SimpleGroups.class);
        when(plugin.getDatabaseManager()).thenReturn(mockDatabaseManager);
        mockDatabaseManager = mock(DatabaseManager.class);
        plugin.setDatabaseManager(mockDatabaseManager);
        mockCallback = mock(SimpleGroups.PlayerCallback.class);
        plugin.addGroup(new PermissionGroup("Admin", "Admin"), true);
        mockPlayer = UUID.randomUUID();
        plugin.getPlayers().put(mockPlayer, new SimplePlayer(mockPlayer, plugin.getGroup("Admin"), -1));

    }



    /**
     * Tests that a player can be retrieved from memory,
     * and the callback is invoked with the correct SimplePlayer object.
     */
    @Test
    public void testGetPlayerFromMemory()  {
        UUID playerUUID = UUID.randomUUID();
        SimplePlayer simplePlayer = new SimplePlayer(playerUUID, new PermissionGroup("Admin", "[Admin]"), -1);

        plugin.getPlayers().put(playerUUID, simplePlayer);


        plugin.getPlayer(playerUUID, (player, fromMemory) -> {
            verify(mockCallback).onResult(simplePlayer, true);
        });

    }


    /**
     * Tests that a player who is not found in the database is assigned to the default group.
     */
    @Test
    public void testAssignPlayerToGroupWithoutJoining() throws SQLException {
        UUID playerUUID = UUID.randomUUID();

        // Mock the database response for the player not being found
        when(mockDatabaseManager.getPlayerGroup(playerUUID)).thenReturn(null);


        plugin.getPlayer(playerUUID, (player, fromMemory) -> {
            try {
                verify(mockDatabaseManager).assignPlayerToGroup(playerUUID, "default", -1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }


    /**
     * Tests that a player found in the database is loaded correctly,
     * and the corresponding callback is invoked.
     */
    @Test
    public void testPlayerFoundInDatabase() throws SQLException {
        UUID playerUUID = mockPlayer;

        when(mockDatabaseManager.getPlayerGroup(playerUUID)).thenReturn("Admin");
        when(mockDatabaseManager.loadPlayer(playerUUID.toString()))
                .thenReturn(new SimplePlayer(playerUUID, new PermissionGroup("Admin", "[Admin]"), -1));

        plugin.getPlayer(playerUUID, (player, fromMemory) -> {
            System.out.println("Callback invoked");
            verify(mockCallback).onResult(any(SimplePlayer.class), eq(false));
        });
    }




    /**
     * Tests that when a SQLException occurs while fetching a player's group,
     * the callback is not invoked.
     */
    @Test
    public void testHandleSQLException() throws SQLException {
        UUID playerUUID = UUID.randomUUID();
        when(mockDatabaseManager.getPlayerGroup(playerUUID)).thenThrow(new SQLException("DB Error"));

        plugin.getPlayer(playerUUID, mockCallback);

        verify(mockCallback, never()).onResult(any(), anyBoolean());
    }
}
