import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;


public class ClientHandler implements Runnable, ClientHandlerInterface {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received request: " + request);

                // Parse the request
                String[] tokens = request.split(" ");
                String command = tokens[0];

                // Handle commands
                String response;
                switch (command.toUpperCase()) {
                    case "LOGIN":
                        response = handleLogin(tokens);
                        break;
                    case "REGISTER":
                        response = handleRegister(tokens);
                        break;
                    case "ADD_USER":
                        response = handleAddUser(tokens);
                        break;
                    case "REMOVE_USER":
                        response = handleRemoveUser(tokens);
                        break;
                    case "VIEW_PROFILE":
                        response = handleViewProfile(tokens);
                        break;
                    case "ADD_FRIEND":
                        response = handleAddFriend(tokens);
                        break;
                    case "REMOVE_FRIEND":
                        response = handleRemoveFriend(tokens);
                        break;
                    case "BLOCK_USER":
                        response = handleBlockUser(tokens);
                        break;
                    case "UNBLOCK_USER":
                        response = handleUnBlockUser(tokens);
                        break;
                    case "ADD_POST":
                        response = handleAddPost(tokens);
                        break;
                    case "REMOVE_POST":
                        response = handleRemovePost(tokens);
                        break;
                    case "COMMENT_POST":
                        response = handleCommentPost(tokens); // New command
                        break;
                    case "LIKE_POST":
                        response = handleLikePost(tokens);
                        break;
                    case "DISLIKE_POST":
                        response = handleDislikePost(tokens);
                        break;
                    case "VIEW_POSTS":
                        response = handleViewPosts(tokens);
                        break;
                    case "EDIT_BIO":
                        response = handleEditBio(tokens);
                        break;
                    case "EDIT_PASSWORD":
                        response = handleEditPassword(tokens);
                        break;
                    case "GET_USER_POSTS":
                        response = handleGetUserPosts(tokens);
                        break;
                    case "LOGOUT":
                        response = handleLogout(tokens);
                        break;
                    case "VIEW_FRIENDS":
                        response = handleViewFriends(tokens);
                        break;
                    case "GET_COMMENTS":
                        response = handleGetComments(tokens);
                        break;
                    case "DELETE_COMMENT":
                        response = handleDeleteComment(tokens);
                        break;
                    default:
                        response = "ERROR: Unknown command.";
                }

                // Send response to the client
                out.println(response);
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close client socket: " + e.getMessage());
            }
        }
    }

    //Fixed view profile to show friends
    public synchronized String handleViewProfile(String[] tokens) {
        if (tokens.length < 2) {
            return "ERROR: Missing arguments for VIEW_PROFILE. Usage: VIEW_PROFILE <username>";
        }

        String username = tokens[1];
        System.out.println("ClientHandler: Processing VIEW_PROFILE for username=" + username);

        try {
            User user = Database.getUserByUsername(username);
            // Format the profile information as a string

            User[] blockList = user.getBlocklist();
            String blocked = "";

            if (blockList == null) {
                blocked = "You haven't blocked anyone yet.";
            } else {
                for (int i = 0; i < blockList.length; i++) {
                    blocked += blockList[i].getUsername() + ", ";
                }
                blocked = blocked.substring(0, blocked.length() - 2);
            }

            String bio = user.getBio();
            if (bio.isEmpty()) {
                bio = "No bio yet.";
            }

            String profile = String.format("Username: %s|Bio: %s|Blocked: %s",
                    user.getUsername(), bio, blocked);

            return profile;
        } catch (UserNotFoundException e) {
            System.err.println("ClientHandler: User not found: " + username);
            return "ERROR: User not found.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Unexpected error occurred while retrieving profile.";
        }
    }

    public synchronized String handleViewFriends(String[] tokens) {
        String username = tokens[1];
        System.out.println("ClientHandler: Processing VIEW_FRIENDS for username=" + username);

        try {
            User user = Database.getUserByUsername(username);

            User[] friendList = user.getFriends();
            String friends = "";
            ArrayList<String> onlineUsers = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader("../Resources/Online.txt"))) {
                String line = br.readLine();
                while (line != null) {
                    onlineUsers.add(line);
                    line = br.readLine();
                }
            }

            if (friendList == null) {
                friends = "You haven't friended anyone yet.";
            } else {
                for (int i = 0; i < friendList.length; i++) {
                    friends += friendList[i].getUsername() + ",";
                    if (onlineUsers.contains(friendList[i].getUsername())) {
                        friends += "yes" + ",";
                    } else {
                        friends += "no" + ",";
                    }
                }
                friends = friends.substring(0, friends.length() - 1);
            }

            return friends;
        } catch (UserNotFoundException e) {
            System.err.println("ClientHandler: User not found: " + username);
            return "ERROR: User not found.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Unexpected error occurred while retrieving profile.";
        }
    }

    public synchronized String handleLogin(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for LOGIN. Usage: LOGIN <username> <password>";
        }

        String username = tokens[1];
        String password = tokens[2];
        System.out.println("Attempting login for username: " + username + ", password: " + password); // Debug

        try {
            User user = Database.getUserByUsername(username);
            if (user.getPassword() == null) {
                System.err.println("Error: Retrieved user has null password."); // Debug
            } else {
                System.out.println("Retrieved password: " + user.getPassword()); // Debug
            }

            if (user.getPassword().equals(password)) {
                File f = new File("../Resources/Online.txt");
                try {
                    f.createNewFile();
                } catch (IOException e1) {
                    System.out.println("Online file found.");
                }

                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line = br.readLine();
                    String onlineUsers = "";

                    while (line != null) {
                        onlineUsers += line + ",";
                        line = br.readLine();
                    }

                    if (!onlineUsers.isEmpty()) {
                        String[] activeUsers = onlineUsers.split(",");

                        for (String activeUser : activeUsers) {
                            if (user.getUsername().equals(activeUser)) {
                                return "ERROR: You already have an active session";
                            }
                        }

                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
                            bw.write(user.getUsername() + System.lineSeparator());
                        } catch (IOException e) {
                            return "ERROR: Error occurred while writing \"Online\" file";
                        }
                    } else {
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                            bw.write(user.getUsername() + System.lineSeparator());
                        } catch (IOException e) {
                            return "ERROR: Error occurred while writing \"Online\" file";
                        }
                    }
                } catch (IOException e) {
                    return "ERROR: Error occurred while reading \"Online\" file";
                }

                user.update();
                return "SUCCESS: Login successful.";
            } else {
                return "ERROR: Incorrect password.";
            }
        } catch (UserNotFoundException e) {
            return "ERROR: User not found.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Unexpected error occurred.";
        }
    }

    public synchronized String handleLogout(String[] tokens) {
        String username = tokens[1];

        File f = new File("../Resources/Online.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            String onlineUsers = "";

            while (line != null) {
                if (!line.equals(username)) {
                    onlineUsers += line + ",";
                }
                line = br.readLine();
            }

            if (!onlineUsers.isEmpty()) {
                String[] activeUsers = onlineUsers.split(",");

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                    for (String activeUser : activeUsers) {
                        bw.write(activeUser + System.lineSeparator());
                    }
                } catch (IOException e) {
                    return "ERROR: Error occurred while writing \"Online\" file";
                }
            } else {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                    bw.write("");
                } catch (IOException e) {
                    return "ERROR: Error occurred while writing \"Online\" file";
                }
            }
        } catch (IOException e) {
            return "ERROR: Error occurred while reading \"Online\" file";
        }

        return "SUCCESS: Logout successful.";
    }

    public synchronized String handleRegister(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for REGISTER. Usage: REGISTER <username> <password>";
        }

        String username = tokens[1];
        String password = tokens[2];

        // Debugging: Log the incoming REGISTER command
        System.out.println("ClientHandler: Processing REGISTER command for username=" + username);

        if (Database.checkForUsername(username)) {
            // Debugging: Log duplicate username detection
            System.out.println("ClientHandler: Username already exists: " + username);
            return "ERROR: Username already exists.";
        }

        // Attempt to add the user
        try {
            User newUser = new User(username, password);
            Database.addUser(newUser); // Add user to the database
            // Debugging: Log successful addition
            System.out.println("ClientHandler: Successfully registered user: " + username);
            return "SUCCESS: Registered successfully.";
        } catch (Exception e) {
            // Debugging: Log any unexpected errors
            System.err.println("ClientHandler: Unexpected error during registration: " + e.getMessage());
            return "ERROR: Unexpected error occurred during registration.";
        }
    }


    // Adds a user to the system
    public synchronized String handleAddUser(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for ADD_USER. Usage: ADD_USER <username> <password>";
        }

        String username = tokens[1];
        String password = tokens[2];

        try {
            User newUser = new User(username, password);
            Database.addUser(newUser); // Ensure this is called only once
            return "SUCCESS: User added.";
        } catch (UsernameTakenException e) {
            return "ERROR: Username already exists."; // Return user-friendly error
        } catch (Exception e) {
            return "ERROR: Unexpected error occurred.";
        }
    }

    // Removes a user from the system
    public synchronized String handleRemoveUser(String[] tokens) {
        if (tokens.length < 2) {
            return "ERROR: Missing arguments for REMOVE_USER. Usage: REMOVE_USER <username>";
        }
        String username = tokens[1];
        try {
            Database.removeUser(username);
            return "SUCCESS: User removed.";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Adds a comment to a post
    public synchronized String handleCommentPost(String[] tokens) {
        String username = tokens[1];
        String postId = tokens[2];
        String comment = username + ": " + String.join(" ", Arrays.copyOfRange(tokens, 3, tokens.length));

        try {
            Post post = Database.getPostById(postId);
            post.addComment(comment);
            System.out.println("Debug: Comment added to file: " + post.getPostId() + "_comments.txt");
            return "SUCCESS: Comment added.";
        } catch (PostNotFoundException e) {
            return "ERROR: Post not found.";
        } catch (Exception e) {
            return "ERROR: Unable to add comment.";
        }
    }

    public synchronized String handleGetComments(String[] tokens) {
        String username = tokens[1];
        String postId = tokens[2];

        try {
            Post post = Database.getPostById(postId);
            List<String> comments = post.getComments();
            String userComments = "";

            for (String comment : comments) {
                if (comment.startsWith(username + ":") && !comment.equals(username + ": [Comment Deleted]")) {
                    userComments += comment.substring(username.length() + 2) + "\u0001\u0002\u0003";
                }
            }

            if (userComments.isEmpty()) {
                return "ERROR: You haven't commented on this post";
            }

            System.out.println("Comments found: " + userComments);
            return "SUCCESS: " + userComments;
        } catch (PostNotFoundException e) {
            return "ERROR: Post not found.";
        } catch (Exception e) {
            return "ERROR: Unexpected error occured";
        }
    }

    public synchronized String handleDeleteComment(String[] tokens) {
        String username = tokens[1];
        String postId = tokens[2];
        String comment = username + ": " + String.join(" ", Arrays.copyOfRange(tokens, 3, tokens.length));

        try {
            Post post = Database.getPostById(postId);
            post.removeComment(username, comment);
            System.out.println("Debug: Comment removed from file: " + post.getPostId() + "_comments.txt");
            return "SUCCESS: Comment removed.";
        } catch (PostNotFoundException e) {
            return "ERROR: Post not found.";
        } catch (Exception e) {
            return "ERROR: Unable to remove comment.";
        }
    }


    // Handles adding a friend
    public synchronized String handleAddFriend(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Debug: Missing arguments for ADD_FRIEND command");
            return "ERROR: Missing arguments for ADD_FRIEND. Usage: ADD_FRIEND <username1> <username2>";
        }

        String username1 = tokens[1];
        String username2 = tokens[2];
        System.out.println("Debug: ADD_FRIEND command - username1=" + username1 + ", username2=" + username2); // Debugging line

        try {
            User user1 = Database.getUserByUsername(username1);
            User user2 = Database.getUserByUsername(username2);

            User[] friends = user1.getFriends();
            if (friends != null) {
                for (User friend : friends) {
                    if (friend.getUserName().equals(username2)) {
                        return "ERROR: You already have this user friended.";
                    }
                }
            }

            if (!(user1.isBlocked(user2) || user2.isBlocked(user1))) {
                user1.addFriend(user2);// Add friend
                user2.addFriend(user1);
            } else {
                return "ERROR: User is blocked/User has blocked you.";
            }

            System.out.println("Debug: Friend added successfully - " + username1 + " -> " + username2);// Debugging line
            friends = user1.getFriends();

            for (int i = 0; i < friends.length; i++) {
                System.out.println(friends[i].toString());
            }
            return "SUCCESS: " + username2 + " added as a friend.";
        } catch (UserNotFoundException e) {
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Unexpected error occurred.";
        }
    }

    // Handles removing a friend
    public synchronized String handleRemoveFriend(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for REMOVE_FRIEND. Usage: REMOVE_FRIEND <username1> <username2>";
        }
        String username1 = tokens[1];
        String username2 = tokens[2];
        try {
            User user1 = Database.getUserByUsername(username1);
            User user2 = Database.getUserByUsername(username2);
            user1.removeFriend(user2);
            user2.removeFriend(user1);

            return "SUCCESS: Friend removed.";
        } catch (UserNotFoundException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public synchronized String handleBlockUser(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Debug: Missing arguments for BLOCK_USER command");
            return "ERROR: Missing arguments for BLOCK_USER. Usage: BLOCK_USER <username1> <username2>";
        }

        String username1 = tokens[1];
        String username2 = tokens[2];
        System.out.println("Debug: BLOCK_USER command - username1=" + username1 + ", username2=" + username2); // Debugging line

        try {
            User user1 = Database.getUserByUsername(username1);
            User user2 = Database.getUserByUsername(username2);

            user1.block(user2); // Add friend
            System.out.println("Debug: User blocked successfully - " + username1 + " -> " + username2);// Debugging line
            User[] blocklist = user1.getBlocklist();

            for (int i = 0; i < blocklist.length; i++) {
                System.out.println(blocklist[i].toString());
            }
            return "SUCCESS: " + username2 + " blocked.";
        } catch (UserNotFoundException e) {
            return "ERROR" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Unexpected error occurred.";
        }
    }

    public synchronized String handleUnBlockUser(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for UNBLOCK_USER. Usage: UNBLOCK_USER <username1> <username2>";
        }
        String username1 = tokens[1];
        String username2 = tokens[2];
        try {
            User user1 = Database.getUserByUsername(username1);
            User user2 = Database.getUserByUsername(username2);
            user1.unBlock(user2);
            return "SUCCESS: User unblocked.";
        } catch (UserNotFoundException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Adds a post
    public synchronized String handleAddPost(String[] tokens) {
        if (tokens.length < 3) {
            return "ERROR: Missing arguments for ADD_POST. Usage: ADD_POST <authorId> <message>";
        }
        String authorId = tokens[1];
        String message = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length));
        try {
            Post newPost = new Post(authorId, message);
            Database.addPost(newPost);
            return "SUCCESS: Post added.";
        } catch (Exception e) {
            return "ERROR: Unable to add post.";
        }
    }

    //Made this simpler, now works.
    public synchronized String handleRemovePost(String[] tokens) {
        if (tokens.length < 2) {
            return "ERROR: Missing arguments for REMOVE_POST. Usage: REMOVE_POST <postId>";
        }
        String postId = tokens[1];
        try {
            Database.removePost(postId); // Remove the post from the database file
            return "SUCCESS: Post removed.";
        } catch (Exception e) {
            return "ERROR: Unable to remove post. Please try again.";
        }
    }

    // Handles post liking
    public synchronized String handleLikePost(String[] tokens) {
        if (tokens.length < 2) {
            System.err.println("Debug: Missing arguments for LIKE_POST. Usage: LIKE_POST <postId>");
            return "ERROR: Missing arguments for LIKE_POST. Usage: LIKE_POST <postId>";
        }

        // Extract the postId from the command tokens
        String postId = tokens[1].trim();
        System.out.println("Debug: Received LIKE_POST command for post ID: " + postId);

        try {
            // Fetch the post from the database using the provided postId
            Post post = Database.getPostById(postId);

            // Debugging: Log the post details
            System.out.println("Debug: Post retrieved - " + post);

            // Check if the post is deleted
            if (post.isDeleted()) {
                System.err.println("Debug: Post is deleted: " + postId);
                return "ERROR: Cannot like a deleted post.";
            }

            // Increment the like count
            post.likePost();
            System.out.println("Debug: Post likes incremented. New likes: " + post.getLikes());

            // Update the post in the database
            Database.updatePost(post);
            System.out.println("Debug: Post updated in database.");

            return "SUCCESS: Post liked.";
        } catch (PostNotFoundException e) {
            // Handle the case where the post is not found
            System.err.println("Debug: PostNotFoundException - " + e.getMessage());
            return "ERROR: Post with ID " + postId + " not found.";
        } catch (IOException e) {
            // Handle IO exceptions during the update
            System.err.println("Debug: IOException during post update - " + e.getMessage());
            return "ERROR: Unable to update post. Please try again.";
        } catch (Exception e) {
            // Catch-all for any unexpected exceptions
            System.err.println("Debug: Unexpected exception - " + e.getMessage());
            e.printStackTrace();
            return "ERROR: An unexpected error occurred. Please contact support.";
        }
    }

    public synchronized String handleDislikePost(String[] tokens) {
        if (tokens.length < 2) {
            return "ERROR: Missing arguments for DISLIKE_POST. Usage: DISLIKE_POST <postId>";
        }
        String postId = tokens[1];
        try {
            Post post = Database.getPostById(postId);// Fetch post from the database
            if (post.isDeleted()) {
                return "ERROR: Cannot dislike a deleted post.";
            }
            post.dislikePost(); // Increment dislikes
            Database.updatePost(post); // Save updated post to the database
            return "SUCCESS: Post disliked.";
        } catch (PostNotFoundException e) {
            return "ERROR: Post not found.";
        } catch (Exception e) {
            return "ERROR: Unable to dislike post. Please try again.";
        }
    }

    // Views all posts
    public synchronized String handleViewPosts(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("DEBUG: Invalid number of arguments for VIEW_POSTS command.");
            return "ERROR: Missing arguments for VIEW_POSTS. Usage: VIEW_POSTS <username>";
        }

        String username = tokens[1];
        System.out.println("DEBUG: Processing VIEW_POSTS for username=" + username);

        try {
            // Retrieve user and verify existence
            User user = Database.getUserByUsername(username);
            System.out.println("DEBUG: User found: " + user.getUsername());

            // Retrieve user's friends list
            User[] friendList = user.getFriends();
            if (friendList == null || friendList.length == 0) {
                System.out.println("DEBUG: Friends list is empty or null for user: " + username);
                return "ERROR: No posts to show. You haven't friended anyone.";
            }

            // Debugging: Log friends' usernames
            System.out.println("DEBUG: Friends list retrieved for user " + username + ":");
            for (User friend : friendList) {
                System.out.println("DEBUG: Friend: " + friend.getUsername());
            }

            // Convert friends list to a Set for quick lookup
            Set<String> friendUsernames = Arrays.stream(friendList)
                    .map(User::getUsername)
                    .collect(Collectors.toSet());
            System.out.println("DEBUG: Converted friends list to a Set for quick lookup.");

            // Retrieve all posts
            List<Post> posts = Database.getAllPosts();
            System.out.println("DEBUG: Retrieved all posts from the database. Total posts: " + posts.size());

            if (posts.isEmpty()) {
                System.out.println("DEBUG: No posts available in the database.");
                return "ERROR: No posts available.";
            }

            // Filter and build response
            StringBuilder response = new StringBuilder("");
            boolean postFound = false;

            for (Post post : posts) {
                System.out.println("DEBUG: Checking post with ID " + (Integer.parseInt(post.getPostId()) - 1) + " by author " + post.getAuthorId());
                if (post.isDeleted()) {
                    System.out.println("DEBUG: Skipping deleted post with ID " + post.getPostId());
                    continue;
                }

                if (friendUsernames.contains(post.getAuthorId())) {
                    System.out.println("DEBUG: Post matches a friend (" + post.getAuthorId() + "). Adding to response.");
                    response.append("Post ID: ").append(post.getPostId())
                            .append("\nAuthor: ").append(post.getAuthorId())
                            .append("\nMessage: ").append(post.getMessage())
                            .append("\nLikes: ").append(post.getLikes())
                            .append("\nDislikes: ").append(post.getDislikes())
                            //.append("\n---\n");
                            .append("\n\n-COMMENTS-\n");

                    List<String> comments = post.getComments();
                    if (comments.isEmpty()) {
                        response.append("\nNo comments");
                        response.append("\n\u0001\u0002\u0003\n");
                    } else {
                        for (String comment : comments) {
                            response.append("\n").append(comment);
                        }
                        response.append("\n\u0001\u0002\u0003\n");
                    }


                    postFound = true;
                } else {
                    System.out.println("DEBUG: Post author (" + post.getAuthorId() + ") is not in the user's friends list.");
                }
            }

            if (!postFound) {
                System.out.println("DEBUG: No posts from friends found for user " + username);
                return "ERROR: No posts to show from your friends.";
            } else {
                response.append("\u0002END\u0003");
            }
            System.out.println("DEBUG: Response built successfully for user " + username);
            System.out.println("DEBUG: Final Response:\n" + response); // Log the final response
            return response.toString();

        } catch (UserNotFoundException e) {
            System.err.println("ERROR: User not found: " + username);
            return "ERROR: User not found.";
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception has occurred.");
            e.printStackTrace();
            return "ERROR: Unexpected error occurred while retrieving posts.";
        }
    }

    public synchronized String handleEditBio(String[] tokens) {
        try {
            User user = Database.getUserByUsername(tokens[1]);
            String bio = "";

            for (int i = 2; i < tokens.length; i++) {
                bio += tokens[i] + " ";
            }

            try {
                System.out.println("Old bio: " + user.getUserName() + ": " + user.getBio());
                user.setBio(bio);
                System.out.println("New bio: " + user.getUserName() + ": " + user.getBio());
            } catch (InvalidInputException e) {
                return "ERROR: " + e.getMessage();
            }
            return "SUCCESS";
        } catch (UserNotFoundException e) {
            return "ERROR: " + e.getMessage();
        }


    }

    public synchronized String handleEditPassword(String[] tokens) {
        try {
            User user = Database.getUserByUsername(tokens[1]);
            try {
                System.out.println("Old password: " + user.getUserName() + ": " + user.getPassword());
                user.setPassword(tokens[2]);
                System.out.println("New password: " + user.getUserName() + ": " + user.getPassword());
                return "SUCCESS";
            } catch (InvalidInputException e) {
                return "ERROR: " + e.getMessage();
            }

        } catch (UserNotFoundException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String handleGetUserPosts(String[] tokens) {
        if (tokens.length < 2) {
            return "ERROR: Missing arguments for GET_USER_POSTS. Usage: GET_USER_POSTS <username>";
        }

        String username = tokens[1]; // Logged-in user's username
        System.out.println("DEBUG: Fetching posts for username: " + username);

        try {
            // Retrieve all posts
            List<Post> posts = Database.getAllPosts(); // Assumes a method to retrieve all posts

            // Filter posts by author (username)
            StringBuilder userPosts = new StringBuilder(); // To store the user's posts
            for (Post post : posts) {
                if (post.getAuthorId().equals(username)) {
                    userPosts.append("Post ID: ").append(post.getPostId())
                            .append(", Message: ").append(post.getMessage())
                            .append(", Likes: ").append(post.getLikes())
                            .append(", Dislikes: ").append(post.getDislikes())
                            .append("\n\n\u0001\u0002\u0003\n\n"); // Separate posts for readability
                }
            }

            // Check if any posts were found
            if (userPosts.length() == 0) {
                return "No posts found for user: " + username;
            }

            return userPosts.toString() + "\n\u0002END\u0003"; // Return all the posts as a single string
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch posts: " + e.getMessage());
            return "ERROR: Failed to fetch posts.";
        }
    }
}
