import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class Post implements PostInterface {
    private static int postCounter = initializePostCounter(); // Shared counter across all Post instances
    private final String postId; // Unique post ID generated using synchro
    private String authorId; // ID of the post author (username)
    private String message;
    private int likes;
    private int dislikes;
    private boolean deleted = false;
    private List<String> comments;
    private final String commentsFilePath;

    public Post(String authorId, String message) {
        if (authorId == null || authorId.isEmpty())
            throw new IllegalArgumentException("Author ID cannot be null or empty.");
        if (message == null) throw new IllegalArgumentException("Message cannot be null.");
        this.postId = generateUniquePostId();
        this.authorId = authorId;
        this.message = message;
        this.likes = 0;
        this.dislikes = 0;
        this.comments = new ArrayList<>();
        this.commentsFilePath = "Resources/" + this.postId + "_comments.txt";
        ensureCommentsFileExists();

    }

    public Post(String postId, String authorId, String message, int likes, int dislikes) {
        this.postId = postId;  // Use the postId from Posts.txt
        this.authorId = authorId;
        this.message = message;
        this.likes = likes;
        this.dislikes = dislikes;
        this.deleted = false;  // Default value since `deleted` is not in the file
        this.commentsFilePath = "Resources/" + this.postId + "_comments.txt";
        ensureCommentsFileExists();
    }

    public Post(String postId, String authorId, String message) {
        this.postId = postId;
        this.authorId = authorId;
        this.message = message;
        this.likes = 0;        // Default value
        this.dislikes = 0;     // Default value
        this.deleted = false;  // Default value
        this.commentsFilePath = "Resources/" + this.postId + "_comments.txt";
        ensureCommentsFileExists();
    }

    public static void resetPostCounter(int newCounter) {
        postCounter = newCounter; // Reset counter to the given value
        System.out.println("DEBUG: Post counter reset to " + newCounter);
    }

    private void ensureCommentsFileExists() {
        File file = new File(commentsFilePath);
        try {
            File dir = new File("Resources"); // Ensure directory matches commentsFilePath
            if (!dir.exists()) dir.mkdir();  // Create the directory if it doesn't exist
            if (file.createNewFile()) {
                System.out.println("Comments file created for post ID: " + postId);
            }
        } catch (IOException e) {
            System.err.println("Error creating comments file: " + e.getMessage());
        }
    }

    public void addComment(String comment) {
        System.out.println("Adding comment to file: " + "Resources/" + this.postId + "_comments.txt");  // Debugging
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Resources/" + this.postId + "_comments.txt", true))) {
            writer.write(comment);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing comment to file: " + e.getMessage());
        }
    }

    public void removeComment(String username, String comment) {
        System.out.println("Removing comment from file: " + "Resources/" + this.postId + "_comments.txt");
        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + this.postId + "_comments.txt"))) {
            String line = br.readLine();
            ArrayList<String> comments = new ArrayList<>();

            while (line != null) {
                if (line.equals(comment)) {
                    comments.add(username + ": [Comment Deleted]");
                } else {
                    comments.add(line);
                }
                line = br.readLine();
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter("Resources/" + this.postId + "_comments.txt"))) {
                for (String s : comments) {
                    bw.write(s + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.err.println("Error deleting comment from file: " + e.getMessage());
        }
    }

    public List<String> getComments() {
        List<String> comments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("Resources/" + this.postId + "_comments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                comments.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading comments: " + e.getMessage());
        }
        return comments;
    }

    // Initialize the post counter based on the number of lines in the posts file
    private static int initializePostCounter() {
        if (postCounter > 0) {
            // Prevent multiple resets
            return postCounter;
        }
        String postsFilePath = "src/Posts.txt";
        File postsFile = new File(postsFilePath);
        if (!postsFile.exists()) {
            return 0; // Start from 0 if the file doesn't exist
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(postsFile))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > 0) {
                    int id = Integer.parseInt(fields[0].trim());
                    maxId = Math.max(maxId, id);
                }
            }
            System.out.println("PostCounter set to: " + maxId);
            postCounter = maxId; // Set postCounter directly here
            return maxId;
        } catch (IOException e) {
            System.err.println("Error reading posts file: " + e.getMessage());
            return 0;
        }
    }

    private static synchronized String generateUniquePostId() {
        return "" + (++postCounter); // Increment and return the post counter
    }


    public void setPostId(String postId) {
        System.err.println("DEBUG: Attempting to set a final postId. This should only happen when reconstructing posts.");
    }

    @Override
    public void deletePost() {
        this.message = "[deleted]";
        this.deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void editPost(String oldMessage, String newMessage) {
        if (Objects.equals(this.message, oldMessage)) {
            this.message = newMessage;
        } else {
            System.out.println("The original message does not match.");
        }
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    @Override
    public void likePost() {
        likes++;
    }

    @Override
    public void dislikePost() {
        dislikes++;
    }

    public String getPostId() {
        return postId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getMessage() {
        return message;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%d,%d", postId, authorId, message, likes, dislikes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return postId.equals(post.postId);
    }

}
