import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.io.*;

public class Database implements DatabaseInterface {

    // Adds a user string to userOutput
    public static synchronized void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        ensureFileExists(USER_OUTPUT);

        // Debugging: Log the username being checked
        System.out.println("Database: Checking if username already exists: " + user.getUsername());

        if (checkForUsername(user.getUsername())) {
            return; // Exit early to prevent duplicate addition
        }

        // Write the user to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_OUTPUT, true))) {
            // Debugging: Log the user being added
            System.out.println("Database: Adding user to file: " + user);
            writer.write(user.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Database: Error writing to USER_OUTPUT: " + e.getMessage());
        }
    }

    // Removes a user string from userOutput based on username
    public static synchronized void removeUser(String username) {
        File inputFile = new File(USER_OUTPUT);
        File tempFile = new File("src/tempFile.txt");

        ensureFileExists(USER_OUTPUT);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // Write the line only if it does not match the line to remove
                if (!currentLine.split(",")[0].trim().equals(username.trim())) {
                    writer.write(currentLine + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the original file and rename the temp file to the original file name
        if (inputFile.delete()) {
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Failed to rename the temp file.");
            }
        } else {
            System.out.println("Failed to delete the original file.");
        }
    }

    public static synchronized User getUserByUsername(String username) throws UserNotFoundException {
        ensureFileExists(USER_OUTPUT);

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_OUTPUT))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Checking line: " + line); // Debug
                String[] fields = line.split(",");
                if (fields.length >= 1 && fields[0].trim().equalsIgnoreCase(username.trim())) {
                    String password = fields[1].trim();
                    String bio = fields.length > 2 ? fields[2].trim() : "";
                    return new User(username, password, bio); // Use the fixed constructor
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the user file: " + e.getMessage());
        }

        throw new UserNotFoundException("User with username '" + username + "' not found.");
    }

    // Adds a post string to postOutput
    public static synchronized void addPost(Post post) {
        String postsFilePath = "Posts.txt";
        File postsFile = new File(postsFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(postsFile, true))) {
            writer.write(post.getPostId() + "," + post.getAuthorId() + "," + post.getMessage() + ","
                    + post.getLikes() + "," + post.getDislikes());
            writer.newLine();
            System.out.println("Post added: " + post.getPostId());
        } catch (IOException e) {
            System.err.println("Error writing to Posts.txt: " + e.getMessage());
        }
    }


    // Removes a post string from postOutput using postID
    public static synchronized void removePost(String postID) {
        File inputFile = new File(POST_OUTPUT);
        File tempFile = new File("src/tempFile.txt");

        ensureFileExists(POST_OUTPUT);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // Write the line only if it does not match the line to remove
                if (!currentLine.split(",")[0].trim().equals(postID.trim())) {
                    writer.write(currentLine + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the original file and rename the temp file to the original file name
        if (inputFile.delete()) {
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Failed to rename the temp file.");
            }
        } else {
            System.out.println("Failed to delete the original file.");
        }
    }

    // Checks if a username exists in userOutput
    public static synchronized boolean checkForUsername(String username) {
        ensureFileExists(USER_OUTPUT);

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_OUTPUT))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Debugging: Log each line being checked
                System.out.println("Database: Checking line: " + line);
                String[] fields = line.split(",");
                if (fields[0].trim().equalsIgnoreCase(username.trim())) {
                    // Debugging: Log the match
                    System.out.println("Database: Username found: " + username);
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Database: Error reading USER_OUTPUT: " + e.getMessage());
        }

        // Debugging: Log when username is not found
        System.out.println("Database: Username not found: " + username);
        return false;
    }

    // Ensures the file exists; if not, creates it
    public static void ensureFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Created file: " + filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File exists: " + filePath);
        }
    }

    public static synchronized Post getPostById(String postId) throws PostNotFoundException {
        ensureFileExists(POST_OUTPUT);

        try (BufferedReader reader = new BufferedReader(new FileReader(POST_OUTPUT))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(","); // Assuming posts are stored as comma-separated values
                if (fields.length < 5) {
                    System.err.println("Debug: Skipping malformed line: " + line);
                    continue; // Skip malformed lines
                }
                // Debugging: Log parsed fields
                System.out.println("Debug: Checking post with ID: " + fields[0]);

                if (fields[0].trim().equals(postId.trim())) {
                    try {
                        // Create and return the Post object
                        String filePostId = fields[0].trim();
                        String authorId = fields[1];
                        String message = fields[2];
                        int likes = Integer.parseInt(fields[3]);
                        int dislikes = Integer.parseInt(fields[4]);
                        boolean deleted = (fields.length > 5 && Boolean.parseBoolean(fields[5]));

                        Post post = new Post(postId, authorId, message, likes, dislikes);
                        post.setLikes(likes);
                        post.setDislikes(dislikes);
                        if (deleted) {
                            post.deletePost();
                        }

                        // Set the postId explicitly to match the file's ID
                        Field postIdField = Post.class.getDeclaredField("postId");
                        postIdField.setAccessible(true);
                        postIdField.set(post, filePostId);
                        // Ensure post ID is correctly set
                        System.out.println("Debug: Post found with ID: " + fields[0]);
                        return post;
                    } catch (Exception e) {
                        System.err.println("Debug: Error parsing post fields: " + Arrays.toString(fields));
                        throw new PostNotFoundException("Malformed post entry for ID: " + postId);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Debug: Error reading post file: " + e.getMessage());
            e.printStackTrace();
        }

        throw new PostNotFoundException("Post with ID '" + postId + "' not found.");
    }


    public static synchronized List<Post> getAllPosts() {
        ensureFileExists(POST_OUTPUT);

        List<Post> posts = new ArrayList<>();
        int maxPostId = 0; // Track the highest post ID in the file
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(POST_OUTPUT))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                System.out.println("DEBUG: Processing line " + lineNumber + ": " + line);

                String[] fields = line.split(",");
                if (fields.length != 5) {
                    System.out.println("DEBUG: Invalid format for line " + lineNumber + ": " + line);
                    continue;
                }

                try {
                    String postId = fields[0].trim();
                    String authorId = fields[1].trim();
                    String message = fields[2].trim();
                    int likes = Integer.parseInt(fields[3].trim());
                    int dislikes = Integer.parseInt(fields[4].trim());

                    // Create a post with the exact ID from the file
                    Post post = new Post(postId, authorId, message, likes, dislikes);
                    posts.add(post);

                    // Track the highest post ID
                    maxPostId = Math.max(maxPostId, Integer.parseInt(postId));
                    System.out.println("DEBUG: Successfully added post with ID " + postId);
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("DEBUG: Error reading posts file.");
            e.printStackTrace();
        }

        // Reset the counter after loading posts
        Post.resetPostCounter(maxPostId + 1);
        System.out.println("DEBUG: Post counter reset to " + (maxPostId + 1));
        System.out.println("DEBUG: Total posts loaded: " + posts.size());

        return posts;
    }

    public static synchronized void updatePost(Post updatedPost) throws IOException {
        ensureFileExists(POST_OUTPUT); // Ensure the file exists
        File tempFile = new File("src/Posts_temp.txt"); // Temporary file for updates
        File originalFile = new File(POST_OUTPUT); // Original file

        boolean found = false; // To track if the post was found

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Debug: Reading line - " + line); // Debug: Print each line read

                String[] fields = line.split(","); // Split the line into fields
                if (fields.length < 5) {
                    System.out.println("Debug: Skipping invalid line - " + line); // Debug: Invalid line
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                String filePostId = fields[0].trim();
                String updatedPostId = updatedPost.getPostId().trim();
                System.out.println("Debug: Comparing filePostId=" + filePostId + " with updatedPostId=" + updatedPostId);

                if (filePostId.equals(updatedPostId)) {
                    // Match found, write the updated post to the temp file
                    writer.write(updatedPost.toString());
                    writer.newLine();
                    found = true;
                    System.out.println("Debug: Post updated successfully - " + updatedPost);
                } else {
                    // Write the original line to the temp file
                    writer.write(line);
                    writer.newLine();
                }
            }

            if (!found) {
                System.err.println("Debug: Post not found in file for ID - " + updatedPost.getPostId());
                throw new IOException("Post not found for update: " + updatedPost.getPostId());
            }
        }

        // Replace the original file with the updated file
        if (originalFile.delete()) {
            if (!tempFile.renameTo(originalFile)) {
                throw new IOException("Failed to rename temp file to original file.");
            }
        } else {
            throw new IOException("Failed to delete the original posts file.");
        }
    }

    public static void clearUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_OUTPUT))) {
            // Overwrite the file with nothing to clear its content
            writer.print("");
            System.out.println("USER_OUTPUT has been cleared.");
        } catch (IOException e) {
            System.err.println("Error clearing USER_OUTPUT: " + e.getMessage());
        }
    }
}

