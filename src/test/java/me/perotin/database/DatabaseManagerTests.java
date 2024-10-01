package me.perotin.database;

import me.perotin.SimpleGroups;
import me.perotin.objects.PermissionGroup;
import org.junit.Before;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class DatabaseManagerTests {
    private DatabaseManager dbManager;

    @BeforeEach
    public void setUp() throws SQLException {
        // Create an in-memory SQLite database for testing
        SimpleGroups mockPlugin = mock(SimpleGroups.class);

        dbManager = new DatabaseManager("jdbc:sqlite::memory:", mockPlugin); // Use in-memory database

    }

    @AfterEach
    public void tearDown() throws SQLException {
        dbManager.closeConnection();
    }


    @Test
    public void testWriteNewGroup() throws SQLException {
        PermissionGroup group = new PermissionGroup("Admin", "[Admin]");
        dbManager.writeNewGroup(group);

        assertTrue(dbManager.groupExists("Admin"));
    }

    @Test
    public void testGetAllPermissionGroups() throws SQLException {
        PermissionGroup group = new PermissionGroup("Admin", "[Admin]");
        dbManager.writeNewGroup(group);
        List<PermissionGroup> groups = dbManager.getAllPermissionGroups();

        assertFalse(groups.isEmpty());
        assertEquals("Admin", groups.get(0).getName());
    }

    @Test
    public void testAssignPlayerToGroup() throws SQLException {
        PermissionGroup group = new PermissionGroup("Admin", "[Admin]");
        dbManager.writeNewGroup(group);

        UUID playerUUID = UUID.randomUUID();
        dbManager.assignPlayerToGroup(playerUUID, "Admin", -1);

        assertEquals("Admin", dbManager.getPlayerGroup(playerUUID));
    }

    @Test
    public void testDeletePermissionGroup() throws SQLException {
        PermissionGroup group = new PermissionGroup("Admin", "[Admin]");
        dbManager.writeNewGroup(group);

        dbManager.deletePermissionGroup("Admin");

        assertFalse(dbManager.groupExists("Admin"));
    }
}
