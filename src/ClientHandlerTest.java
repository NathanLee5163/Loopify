import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientHandlerTest {

    private ClientHandler clientHandler;

    // 1. Test login command (successful login)
    @Test
    public void testHandleLogin_success() {
        try {
            Database.addUser(new User("testuser", "testpass"));

            String[] tokens = {"LOGIN", "testuser", "testpass"};
            String response = clientHandler.handleLogin(tokens);

            assertEquals("SUCCESS: Login successful.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 2. Test login command (incorrect password)
    @Test
    public void testHandleLogin_incorrectPassword() {
        try {
            Database.addUser(new User("testuser", "testpass"));

            String[] tokens = {"LOGIN", "testuser", "wrongpass"};
            String response = clientHandler.handleLogin(tokens);

            assertEquals("ERROR: Incorrect password.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 3. Test login command (user not found)
    @Test
    public void testHandleLogin_userNotFound() {
        String[] tokens = {"LOGIN", "nonuser", "testpass"};
        String response = clientHandler.handleLogin(tokens);

        assertEquals("ERROR: User not found.", response);
    }

    // 4. Test registration command (successful registration)
    @Test
    public void testHandleRegister_success() {
        String[] tokens = {"REGISTER", "newuser", "newpass"};
        String response = clientHandler.handleRegister(tokens);

        assertEquals("SUCCESS: Registered successfully.", response);
    }

    // 5. Test registration command (username already exists)
    @Test
    public void testHandleRegister_userExists() {
        try {
            Database.addUser(new User("existinguser", "password"));

            String[] tokens = {"REGISTER", "existinguser", "newpass"};
            String response = clientHandler.handleRegister(tokens);

            assertEquals("ERROR: Username already exists.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 6. Test add user command
    @Test
    public void testHandleAddUser_success() {
        String[] tokens = {"ADD_USER", "newuser", "newpass"};
        String response = clientHandler.handleAddUser(tokens);

        assertEquals("SUCCESS: User added.", response);
    }

    // 7. Test add user command (username already taken)
    @Test
    public void testHandleAddUser_usernameTaken() {
        try {
            Database.addUser(new User("existinguser", "password"));

            String[] tokens = {"ADD_USER", "existinguser", "newpass"};
            String response = clientHandler.handleAddUser(tokens);

            assertEquals("ERROR: Username already exists.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 8. Test remove user command (successful removal)
    @Test
    public void testHandleRemoveUser_success() {
        try {
            Database.addUser(new User("testuser", "password"));

            String[] tokens = {"REMOVE_USER", "testuser"};
            String response = clientHandler.handleRemoveUser(tokens);

            assertEquals("SUCCESS: User removed.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 9. Test remove user command (user not found)
    @Test
    public void testHandleRemoveUser_userNotFound() {
        String[] tokens = {"REMOVE_USER", "nonuser"};
        String response = clientHandler.handleRemoveUser(tokens);

        assertEquals("ERROR: User not found.", response);
    }

    // 10. Test view profile command (valid user)
    @Test
    public void testHandleViewProfile_success() {
        try {
            User user = new User("testuser", "password");
            user.setBio("Hello World");
            user.addFriend(new User("friend1", "password"));
            Database.addUser(user);

            String[] tokens = {"VIEW_PROFILE", "testuser"};
            String response = clientHandler.handleViewProfile(tokens);

            String expectedProfile = "Username: testuser|Bio: Hello World|Friends: friend1|Blocked: You haven't blocked anyone yet.";
            assertEquals(expectedProfile, response);
        } catch (InvalidInputException | UsernameTakenException | UserNotFoundException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    // 11. Test view profile command (user not found)
    @Test
    public void testHandleViewProfile_userNotFound() {
        String[] tokens = {"VIEW_PROFILE", "nonuser"};
        String response = clientHandler.handleViewProfile(tokens);

        assertEquals("ERROR: User not found.", response);
    }

    // 12. Test block user command
    @Test
    public void testHandleBlockUser_success() {
        try {
            Database.addUser(new User("testuser", "password"));
            Database.addUser(new User("blockuser", "password"));

            String[] tokens = {"BLOCK_USER", "testuser", "blockuser"};
            String response = clientHandler.handleBlockUser(tokens);

            assertEquals("SUCCESS: User blocked.", response);
        } catch (InvalidInputException | UsernameTakenException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }
}
