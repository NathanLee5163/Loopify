import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PostTest {
    private Post post;

    @Before
    public void setUp() {
        post = new Post("author1", "This is a post message.");
    }

    @After
    public void tearDown() {
        // No specific teardown required for Post as it's stateless in this context
    }

    // 1. Test successful creation
    @Test
    public void testPostInitialization() {
        assertEquals("author1", post.getAuthorId());
        assertEquals("This is a post message.", post.getMessage());
        assertEquals(0, post.getLikes());
        assertEquals(0, post.getDislikes());
        assertFalse(post.isDeleted());
    }

    // 2. Test constructor with null authorId
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullAuthorId() {
        new Post(null, "Message");
    }

    // 3. Test constructor with empty authorId
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyAuthorId() {
        new Post("", "Message");
    }

    // 4. Test constructor with null message
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMessage() {
        new Post("author1", null);
    }

    // 5. Test like post
    @Test
    public void testLikePost() {
        post.likePost();
        assertEquals(1, post.getLikes());
    }

    // 6. Test dislike post
    @Test
    public void testDislikePost() {
        post.dislikePost();
        assertEquals(1, post.getDislikes());
    }

    // 7. Test edit post with correct message
    @Test
    public void testEditPostWithCorrectMessage() {
        post.editPost("This is a post message.", "Edited message.");
        assertEquals("Edited message.", post.getMessage());
    }

    // 8. Test edit post with incorrect message
    @Test
    public void testEditPostWithIncorrectMessage() {
        post.editPost("Incorrect message", "Edited message.");
        assertEquals("This is a post message.", post.getMessage()); // Message should remain unchanged
    }

    // 9. Test delete post
    @Test
    public void testDeletePost() {
        post.deletePost();
        assertTrue(post.isDeleted());
        assertEquals("[deleted]", post.getMessage());
    }

    // 10. Test get post ID
    @Test
    public void testGetPostId() {
        String postId = post.getPostId();
        assertNotNull(postId);
        assertTrue(postId.startsWith("POST-"));
    }

    // 11. Test toString method
    @Test
    public void testToString() {
        String expectedString = post.getPostId() + ",author1,This is a post message.,0,0";
        assertEquals(expectedString, post.toString());
    }

    // 12. Test equals method
    @Test
    public void testEquals() {
        Post samePost = post;
        assertEquals(post, samePost);

        Post anotherPost = new Post("author1", "This is a post message."); // another post with the same content but different ID
        assertNotEquals(post, anotherPost);
    }
}
