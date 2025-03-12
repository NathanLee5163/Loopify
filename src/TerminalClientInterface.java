import java.io.BufferedReader;
import java.io.PrintWriter;

public interface TerminalClientInterface {
    public void run();

    public BufferedReader getServerReader();
    public PrintWriter getServerWriter();

    public void login(String username, String password);
    public void register(String username, String password);

    public void addFriend(String friendUsername);
    public void removeFriend(String username);

    public void commentOnPost(String postId);
    public void removeCommentOnPost(String postId);
    public void likePost(String postId);
    public void dislikePost(String postId);
    public void removePost();
    public String[] viewPosts();

    public void closeConnection();
}
