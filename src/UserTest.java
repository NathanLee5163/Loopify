import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/*
public class UserTest {
    private User user;
    private User friend;
    private User blockedUser;

    @Before
    public void setUp() throws InvalidInputException, UsernameTakenException {
        user = new User("testUser", "Password1");
        friend = new User("friendUser", "FriendPass1");
        blockedUser = new User("blockedUser", "BlockedPass1");
    }

    @After
    public void tearDown() {
        Database.clearUsers();  // Clears the Users.txt file after each test
    }

    // 1. Test successful creation
    @Test
    public void testUserInitialization() {
        assertEquals("testUser", user.getUserName());
        assertEquals("", user.getBio());
    }

    // 2. Test invalid password
    @Test(expected = InvalidInputException.class)
    public void testPasswordValidationFailure() throws InvalidInputException {
        user.setPassword("nopassword");
    }

    // 3. Test duplicate username
    @Test(expected = UsernameTakenException.class)
    public void testDuplicateUsername() throws InvalidInputException, UsernameTakenException {
        User duplicateUser = new User("testUser", "AnotherPass1");
    }

    // 4. Test null username
    @Test(expected = IllegalArgumentException.class)
    public void testNullUsername() throws InvalidInputException, UsernameTakenException {
        new User(null, "Password1");
    }

    // 5. Test empty username
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUsername() throws InvalidInputException, UsernameTakenException {
        new User("", "Password1");
    }

    // 6. Test null password
    @Test
    public void testNullPassword() {
        try {
            new User("uniqueUserForNullPasswordTest", null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Test passes if IllegalArgumentException is caught
        } catch (UsernameTakenException e) {
            fail("Did not expect UsernameTakenException, but got it: " + e.getMessage());
        } catch (InvalidInputException e) {
            throw new RuntimeException(e);
        }
    }

    // 7. Test invalid password on initialization
    @Test(expected = InvalidInputException.class)
    public void testInvalidPasswordOnInitialization() throws InvalidInputException, UsernameTakenException {
        new User("uniqueUser", "nopassword"); // Use a unique username to avoid UsernameTakenException
    }

    // Friend Management

    // 8. Test add friend successfully
    @Test
    public void testAddFriendSuccess() throws UserNotFoundException {
        user.addFriend(friend);
        assertTrue(user.isFriend(friend));
    }

    // 9. Test add duplicate friend
    @Test(expected = UserNotFoundException.class)
    public void testAddDuplicateFriend() throws UserNotFoundException {
        user.addFriend(friend);
        user.addFriend(friend); // Attempt to add friend again
    }

    // 10. Test remove friend successfully
    @Test
    public void testRemoveFriendSuccess() throws UserNotFoundException {
        user.addFriend(friend);
        user.removeFriend(friend);
        assertFalse(user.isFriend(friend));
    }

    // 11. Test remove non-existent friend
    @Test(expected = UserNotFoundException.class)
    public void testRemoveNonExistentFriend() throws UserNotFoundException {
        user.removeFriend(friend);
    }

    // Blocking Management

    // 12. Test block user successfully
    @Test
    public void testBlockUserSuccess() throws UserNotFoundException {
        user.blockUser(blockedUser);
        assertTrue(user.isBlocked(blockedUser));
    }

    // 13. Test block already blocked user
    @Test
    public void testBlockAlreadyBlockedUser() throws UserNotFoundException {
        user.blockUser(blockedUser);      // Block once
        user.blockUser(blockedUser);      // Attempt to block again
        assertEquals(1, user.getBlocklist().length);  // Expect only one instance
    }

    // 14. Test unblock user successfully
    @Test
    public void testUnblockUserSuccess() throws UserNotFoundException {
        user.blockUser(blockedUser);
        user.unBlock(blockedUser);
        assertFalse(user.isBlocked(blockedUser));
    }

    // 15. Test unblock non-existent user
    @Test
    public void testUnblockNonExistentUser() {
        try {
            user.unBlock(blockedUser);
            fail("Expected UserNotFoundException to be thrown");
        } catch (UserNotFoundException e) {
            // Test passes if UserNotFoundException is caught
        } catch (Exception e) {
            fail("Expected UserNotFoundException but got " + e);
        }
    }


    // 16. Test block null user
    @Test(expected = UserNotFoundException.class)
    public void testBlockNullUser() throws UserNotFoundException {
        user.blockUser(null);
    }

    // 17. Test add friend who is also blocked
    @Test(expected = UserNotFoundException.class)
    public void testAddBlockedUserAsFriend() throws UserNotFoundException {
        user.blockUser(friend);
        user.addFriend(friend);  // Should throw an exception since the user is blocked
    }

    // User Information

    // 18. Test set and get bio
    @Test
    public void testSetAndGetBio() {
        user.setBio("This is my bio");
        assertEquals("This is my bio", user.getBio());
    }

    // 20. Test equals method
    @Test
    public void testEquals() throws InvalidInputException, UsernameTakenException {
        User sameUser = user;
        assertEquals(user, sameUser);

        User newUser = new User("newUser", "Password2");
        assertNotEquals(user, newUser);
    }

    // Additional Tests

    // 21. Test toString format
    @Test
    public void testToStringFormat() {
        user.setBio("This is my bio");
        String expected = "testUser,Password1,user@example.com,This is my bio;[];[]";
        assertEquals(expected, user.toString());
    }

    // 22. Test getFriends when empty
    @Test
    public void testGetFriendsEmpty() {
        assertEquals(0, user.getFriends().length);
    }

    // 23. Test getBlocklist when empty
    @Test
    public void testGetBlocklistEmpty() {
        assertEquals(0, user.getBlocklist().length);
    }
}
*/