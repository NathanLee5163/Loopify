import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;

public class DatabaseTest {
    private User user;
    private User duplicateUser;
    private Post post;

    @Before
    public void setUp() throws InvalidInputException, UsernameTakenException {
        user = new User("testUser", "Password1");
        duplicateUser = new User("duplicateUser", "Password1");
        post = new Post("testPostID", "Test Content");
        Database.clearUsers();
    }

    @After
    public void tearDown() {
        Database.clearUsers();
    }


    // 1. Test add user to database
    @Test
    public void testAddUserToDatabase() throws UsernameTakenException {
        Database.addUser(user);
        assertTrue(Database.checkForUsername("testUser"));
    }

    // 2. Test add duplicate user
    @Test(expected = UsernameTakenException.class)
    public void testAddDuplicateUser() throws UsernameTakenException {
        Database.addUser(user);
        Database.addUser(user); // Should throw UsernameTakenException
    }

    // 3. Test remove user from database
    @Test
    public void testRemoveUserFromDatabase() throws UsernameTakenException {
        Database.addUser(user);
        Database.removeUser("testUser");
        assertFalse(Database.checkForUsername("testUser"));
    }

    // 4. Test clear users
    @Test
    public void testClearUsers() throws UsernameTakenException {
        Database.addUser(user);
        Database.addUser(duplicateUser);
        Database.clearUsers();
        assertFalse(Database.checkForUsername("testUser"));
        assertFalse(Database.checkForUsername("duplicateUser"));
    }

    // Username Check

    // 5. Test check for existing username
    @Test
    public void testCheckForExistingUsername() throws UsernameTakenException {
        Database.addUser(user);
        assertTrue(Database.checkForUsername("testUser"));
    }

    // 6. Test check for non-existent username
    @Test
    public void testCheckForNonExistentUsername() {
        assertFalse(Database.checkForUsername("nonexistentUser"));
    }

    // Post Management

    // 7. Test add post
    @Test
    public void testAddPost() {
        Database.addPost(post);
        File postFile = new File("src/Posts.txt");
        assertTrue(postFile.exists() && postFile.length() > 0); // Verify post added
    }

    // 8. Test remove post
    @Test
    public void testRemovePost() {
        Database.addPost(post);
        Database.removePost("testPostID");
        assertFalse(new File("src/Posts.txt").toString().contains("testPostID"));
    }

    // File Operations

    // 9. Test ensureFileExists for creating a new file
    @Test
    public void testEnsureFileExistsForNewFile() {
        File file = new File("src/NewFile.txt");
        if (file.exists()) file.delete(); // Ensure file doesn't exist initially
        Database.ensureFileExists("src/NewFile.txt");
        assertTrue(file.exists());
        file.delete(); // Clean up
    }

    // Additional Tests

    // 10. Test remove non-existent user
    @Test
    public void testRemoveNonExistentUser() {
        Database.removeUser("nonexistentUser");
        assertFalse(Database.checkForUsername("nonexistentUser"));
    }

    // 11. Test add null user
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullUser() throws UsernameTakenException {
        Database.addUser(null);
    }


    // 12. Test database is empty after clearUsers
    @Test
    public void testDatabaseEmptyAfterClearUsers() throws UsernameTakenException {
        Database.addUser(user);
        Database.clearUsers();
        File userFile = new File(Database.USER_OUTPUT);
        assertEquals(0, userFile.length());
    }

    // 13. Test remove non-existent post
    @Test
    public void testRemoveNonExistentPost() {
        Database.removePost("nonexistentPostID");
        File postFile = new File("src/Posts.txt");
        assertFalse(postFile.toString().contains("nonexistentPostID"));
    }

    // 14. Test check for username on empty database
    @Test
    public void testCheckForUsernameOnEmptyDatabase() {
        Database.clearUsers();
        assertFalse(Database.checkForUsername("anyUser"));
    }
}
