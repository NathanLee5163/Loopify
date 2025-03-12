import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;

public class TerminalClient implements TerminalClientInterface {

    public Socket socket;
    public BufferedReader serverReader;
    public PrintWriter serverWriter;
    private Scanner userInputScanner;
    private String loggedName;
    //For pausing function
    private int bar = 0;
    private boolean pressedBack = false;
    private boolean loggedIn;
    private boolean registerSuccess;
    private boolean keepRunning = true;
    private boolean justLO = false;
    private int incrementer;
    private String[] posts;
    private JFrame mainFrame;
    private JFrame loginFrame;

    public TerminalClient(String serverAddress, int port) {
        JOptionPane.showMessageDialog(null, "Establishing connection with server...", "Connecting...", JOptionPane.INFORMATION_MESSAGE);
        try {
            socket = new Socket(serverAddress, port);
            serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverWriter = new PrintWriter(socket.getOutputStream(), true);
            userInputScanner = new Scanner(System.in);
            JOptionPane.showMessageDialog(null, "Successfully connected to server!", "Connection Established", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public BufferedReader getServerReader() {
        return serverReader;
    }

    public PrintWriter getServerWriter() {
        return serverWriter;
    }

    public void run() {
        JOptionPane.showMessageDialog(null, "Welcome to Loopify!", "Welcome", JOptionPane.PLAIN_MESSAGE);
        loggedIn = false;
        registerSuccess = false;

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton exitButton = new JButton("Exit");

        loginFrame = new JFrame("Login");
        Container content = loginFrame.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        JPanel fields = new JPanel();
        fields.add(usernameField);
        fields.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(exitButton);
        content.add(panel, BorderLayout.SOUTH);
        content.add(fields, BorderLayout.CENTER);

        loginFrame.pack();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String thisPassword = String.valueOf(passwordField.getPassword());
            login(usernameField.getText(), thisPassword);
            usernameField.setText("");
            passwordField.setText("");
            if (loggedIn) {
                loginFrame.setVisible(false);
                initializeMainFrame(); // Initialize mainFrame after login
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String thisPassword = String.valueOf(passwordField.getPassword());
                register(usernameField.getText(), thisPassword);
                usernameField.setText("");
                passwordField.setText("");
            }
        });
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeConnection();
                loginFrame.dispose();
                keepRunning = false;
                System.exit(0);
            }
        });

    }


    private void initializeMainFrame() {
        if (mainFrame == null) {
            mainFrame = new JFrame("Loopify");
            mainFrame.setSize(800, 600);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            Container mainContent = mainFrame.getContentPane();
            mainContent.setLayout(new BorderLayout());

            JTextArea post = new JTextArea();
            post.setFont(new Font("Arial", Font.PLAIN, 18));
            post.setEditable(false);
            post.setLineWrap(true);
            post.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(post);
            mainContent.add(scrollPane, BorderLayout.CENTER);

            JPanel bRow = new JPanel();
            JButton next = new JButton("Next");
            JButton refresh = new JButton("Refresh");
            JButton previous = new JButton("Previous");
            JButton like = new JButton("Like");
            JButton dislike = new JButton("Dislike");
            JButton comment = new JButton("Comment");
            JButton removeComment = new JButton("Remove Comment");

            bRow.add(next);
            bRow.add(refresh);
            bRow.add(previous);
            bRow.add(like);
            bRow.add(dislike);
            bRow.add(comment);
            bRow.add(removeComment);
            mainContent.add(bRow, BorderLayout.SOUTH);

            JPanel topBar = new JPanel(new GridLayout(1, 4));

            JButton viewProfileButton = new JButton("View Profile");
            JButton friends = new JButton("Friends");
            JButton menu = new JButton("Menu");
            JButton logoutButton = new JButton("Logout");

            topBar.add(viewProfileButton);
            topBar.add(friends);
            topBar.add(menu);
            topBar.add(logoutButton);

            mainContent.add(topBar, BorderLayout.NORTH);
            mainFrame.setVisible(true);

            incrementer = 0;
            posts = viewPosts();
            if (posts.length > 0) {
                post.setText(posts[0]);
            } else {
                post.setText("No posts available.");
            }

            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    serverWriter.println("LOGOUT " + loggedName);
                    try {
                        String response = serverReader.readLine();
                        if (response.startsWith("ERROR")) {
                            JOptionPane.showMessageDialog(null, response, "Logout Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException er) {
                        JOptionPane.showMessageDialog(null, "Error communicating with the server: " + er.getMessage(), "Logout Error", JOptionPane.ERROR_MESSAGE);
                    }
                    System.exit(0);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                }
            });

            logoutButton.addActionListener(e -> {
                serverWriter.println("LOGOUT " + loggedName);
                try {
                    String response = serverReader.readLine();

                    if (response.startsWith("ERROR")) {
                        JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        mainFrame.dispose();
                        mainFrame = null;
                        loggedIn = false;
                        loginFrame.setVisible(true);
                        JOptionPane.showMessageDialog(null, "You have been logged out.", "Logout", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException er) {
                    JOptionPane.showMessageDialog(null, "Error communicating with the server: " + er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

            });

            viewProfileButton.addActionListener(e -> {
                initializeProfileWindow();
            });

            menu.addActionListener(e -> {
                initializeMenuWindow();
            });

            friends.addActionListener(e -> {
                initializeFriendWindow();
            });

            next.addActionListener(e -> {
                try {
                    incrementer++;
                    post.setText(posts[incrementer]);
                } catch (IndexOutOfBoundsException r) {
                    JOptionPane.showMessageDialog(null, "No more posts to show", "Error", JOptionPane.ERROR_MESSAGE);
                    incrementer--;
                }
            });

            refresh.addActionListener(e -> {
                posts = viewPosts();
                incrementer = 0;
                if (posts.length > 0) {
                    post.setText(posts[incrementer]);
                } else {
                    post.setText("No posts available.");
                }
            });

            previous.addActionListener(e -> {
                try {
                    incrementer--;
                    post.setText(posts[incrementer]);
                } catch (IndexOutOfBoundsException r) {
                    JOptionPane.showMessageDialog(null, "No more posts to show", "Error", JOptionPane.ERROR_MESSAGE);
                    incrementer++;
                }
            });

            like.addActionListener(e -> {
                if (incrementer >= 0 && incrementer < posts.length) {
                    String postId = extractPostId(posts[incrementer]); // Extract Post ID
                    likePost(postId); // Call likePost with the extracted ID
                    posts = viewPosts();
                    post.setText(posts[incrementer]);
                } else {
                    JOptionPane.showMessageDialog(null, "No post selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dislike.addActionListener(e -> {
                if (incrementer >= 0 && incrementer < posts.length) {
                    String postId = extractPostId(posts[incrementer]); // Dynamically get the post ID
                    dislikePost(postId);
                    posts = viewPosts();
                    post.setText(posts[incrementer]);
                } else {
                    JOptionPane.showMessageDialog(null, "No post selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            comment.addActionListener(e -> {
                if (incrementer >= 0 && incrementer < posts.length) {
                    String postId = extractPostId(posts[incrementer]); // Dynamically get the post ID
                    commentOnPost(postId);
                    posts = viewPosts();
                    post.setText(posts[incrementer]);
                } else {
                    JOptionPane.showMessageDialog(null, "No post selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            removeComment.addActionListener(e -> {
                if (incrementer >= 0 && incrementer < posts.length) {
                    String postId = extractPostId(posts[incrementer]); // Dynamically get the post ID
                    removeCommentOnPost(postId);
                    posts = viewPosts();
                    post.setText(posts[incrementer]);
                } else {
                    JOptionPane.showMessageDialog(null, "No post selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void initializeProfileWindow() {
        // Create a JDialog for the profile window
        JDialog profileDialog = new JDialog(mainFrame, "Profile", true); // Modal dialog
        profileDialog.setSize(400, 300);
        profileDialog.setLocationRelativeTo(mainFrame); // Center on the mainFrame
        profileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create main panel for the profile
        JPanel profilePanel = new JPanel(new BorderLayout());
        JTextArea profileDisplay = new JTextArea();
        profileDisplay.setEditable(false);
        profileDisplay.setFont(new Font("Arial", Font.PLAIN, 16));
        profileDisplay.setLineWrap(true);
        profileDisplay.setWrapStyleWord(true);

        // Fetch profile details from the server
        serverWriter.println("VIEW_PROFILE " + loggedName);
        try {
            String response = serverReader.readLine();
            if (response != null) {
                profileDisplay.setText(response.replace("|", "\n"));
            } else {
                profileDisplay.setText("No profile data available.");
            }
        } catch (IOException e) {
            profileDisplay.setText("Error loading profile: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(profileDisplay);
        profilePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton editBioButton = new JButton("Edit Bio");
        JButton editPasswordButton = new JButton("Edit Password");
        JButton backButton = new JButton("Back");

        buttonPanel.add(editBioButton);
        buttonPanel.add(editPasswordButton);
        buttonPanel.add(backButton);

        profilePanel.add(buttonPanel, BorderLayout.SOUTH);
        profileDialog.add(profilePanel);

        editBioButton.addActionListener(e -> {
            String newBio = JOptionPane.showInputDialog(profileDialog, "Enter your new bio (cannot contain commas):", "Edit Bio", JOptionPane.PLAIN_MESSAGE);
            if (newBio != null && !newBio.contains(",")) {
                serverWriter.println("EDIT_BIO " + loggedName + " " + newBio);
                try {
                    String response = serverReader.readLine();
                    if (response.startsWith("SUCCESS")) {
                        JOptionPane.showMessageDialog(profileDialog, "Bio updated successfully!", "Edit Bio", JOptionPane.INFORMATION_MESSAGE);

                        // Re-fetch the profile from the server to display the updated bio
                        serverWriter.println("VIEW_PROFILE " + loggedName);
                        String updatedProfile = serverReader.readLine();
                        if (updatedProfile != null) {
                            profileDisplay.setText(updatedProfile.replace("|", "\n")); // Update profile display with refreshed content
                        } else {
                            profileDisplay.setText("Error refreshing profile.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(profileDialog, "Error updating bio: " + response, "Edit Bio", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(profileDialog, "Error updating bio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (newBio != null) {
                JOptionPane.showMessageDialog(profileDialog, "Bio cannot contain commas.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        // Add functionality to the "Edit Password" button
        editPasswordButton.addActionListener(e -> {
            String newPassword = JOptionPane.showInputDialog(profileDialog, "Enter your new password:", "Edit Password", JOptionPane.PLAIN_MESSAGE);
            if (newPassword != null && !newPassword.isEmpty()) {
                serverWriter.println("EDIT_PASSWORD " + loggedName + " " + newPassword);
                try {
                    String response = serverReader.readLine();
                    if (response.startsWith("SUCCESS")) {
                        JOptionPane.showMessageDialog(profileDialog, "Password updated successfully!", "Edit Password", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(profileDialog, "Error updating password: " + response, "Edit Password", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(profileDialog, "Error updating password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (newPassword != null) {
                JOptionPane.showMessageDialog(profileDialog, "Password cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> profileDialog.dispose());

        profileDialog.setVisible(true);
    }

    private void initializeFriendWindow() {
        // Create a JDialog for the friend window
        JDialog friendsDialog = new JDialog(mainFrame, "Friends", true); // Modal dialog
        friendsDialog.setSize(400, 300);
        friendsDialog.setLocationRelativeTo(mainFrame); // Center on the mainFrame
        friendsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create main panel for the profile
        JPanel friendsPanel = new JPanel(new BorderLayout());
        JTextArea friendsDisplay = new JTextArea();
        friendsDisplay.setEditable(false);
        friendsDisplay.setFont(new Font("Arial", Font.PLAIN, 16));
        friendsDisplay.setLineWrap(true);
        friendsDisplay.setWrapStyleWord(true);

        // Fetch profile details from the server
        serverWriter.println("VIEW_FRIENDS " + loggedName);
        try {
            String response = serverReader.readLine();
            if (response.startsWith("ERROR") || response.equals("You haven't friended anyone yet.")) {
                friendsDisplay.setText(response);
            } else {
                String[] yourFriends = response.split(",");
                int i = 0;

                while (i < yourFriends.length) {
                    friendsDisplay.append(yourFriends[i] + ": ");
                    i++;
                    if (yourFriends[i].equals("yes")) {
                        friendsDisplay.append("Online\n");
                    } else {
                        friendsDisplay.append("Offline\n");
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            friendsDisplay.setText("Error loading friends: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(friendsDisplay);
        friendsPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addFriend = new JButton("Add Friend");
        JButton removeFriend = new JButton("Remove Friend");
        JButton backButton = new JButton("Back");

        buttonPanel.add(addFriend);
        buttonPanel.add(removeFriend);
        buttonPanel.add(backButton);

        friendsPanel.add(buttonPanel, BorderLayout.SOUTH);
        friendsDialog.add(friendsPanel);


        addFriend.addActionListener(e -> {
            String newFriend = JOptionPane.showInputDialog(friendsDialog, "Enter the name of the user you wish to friend:", "Add Friend", JOptionPane.PLAIN_MESSAGE);
            addFriend(newFriend);
            serverWriter.println("VIEW_FRIENDS " + loggedName);
            friendsDisplay.setText("");
            try {
                String response = serverReader.readLine();
                if (response.startsWith("ERROR") || response.equals("You haven't friended anyone yet.")) {
                    friendsDisplay.setText(response);
                } else {
                    String[] yourFriends = response.split(",");
                    int i = 0;

                    while (i < yourFriends.length) {
                        friendsDisplay.append(yourFriends[i] + ": ");
                        i++;
                        if (yourFriends[i].equals("yes")) {
                            friendsDisplay.append("Online\n");
                        } else {
                            friendsDisplay.append("Offline\n");
                        }
                        i++;
                    }
                }
            } catch (IOException er) {
                friendsDisplay.setText("Error loading friends: " + er.getMessage());
            }
        });
        // Add functionality to the "Edit Password" button
        removeFriend.addActionListener(e -> {
            serverWriter.println("VIEW_FRIENDS " + loggedName);
            try {
                String response = serverReader.readLine();
                if (response.startsWith("ERROR") || response.equals("You haven't friended anyone yet.")) {
                    JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    response = response.replaceAll("no,", "");
                    response = response.replaceAll("yes,", "");
                    response = response.replaceAll("no", "");
                    response = response.replaceAll("yes", "");
                    String[] yourFriends = response.split(",");
                    String unfriend = (String) JOptionPane.showInputDialog(null, "Which friend would you like to remove", "Remove Friend", JOptionPane.PLAIN_MESSAGE, null, yourFriends, yourFriends[0]);
                    if (unfriend != null) {
                        removeFriend(unfriend);
                    }
                }
            } catch (IOException er) {
                JOptionPane.showMessageDialog(null, er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            serverWriter.println("VIEW_FRIENDS " + loggedName);
            friendsDisplay.setText("");
            try {
                String response = serverReader.readLine();
                if (response.startsWith("ERROR") || response.equals("You haven't friended anyone yet.")) {
                    friendsDisplay.setText(response);
                } else {
                    String[] yourFriends = response.split(",");
                    int i = 0;

                    while (i < yourFriends.length) {
                        friendsDisplay.append(yourFriends[i] + ": ");
                        i++;
                        if (yourFriends[i].equals("yes")) {
                            friendsDisplay.append("Online\n");
                        } else {
                            friendsDisplay.append("Offline\n");
                        }
                        i++;
                    }
                }
            } catch (IOException er) {
                friendsDisplay.setText("Error loading friends: " + er.getMessage());
            }
        });

        backButton.addActionListener(e -> friendsDialog.dispose());

        friendsDialog.setVisible(true);
    }

    private void initializeMenuWindow() {
        // Create a modal dialog
        JDialog profileDialog = new JDialog(mainFrame, "Menu", true); // Modal dialog
        profileDialog.setSize(300, 400);
        profileDialog.setLocationRelativeTo(mainFrame); // Center on the mainFrame
        profileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Main panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Arrange buttons vertically

        // Buttons
        JButton createPostButton = new JButton("Create Post");
        JButton deletePostButton = new JButton("Delete Post");
        JButton commentOnPostButton = new JButton("Comment on Post");
        JButton blockButton = new JButton("Block");
        JButton unblockButton = new JButton("Unblock");
        JButton backButton = new JButton("Back"); // Back button

        // Add buttons to the panel with spacing
        createPostButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        deletePostButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //commentOnPostButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        blockButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        unblockButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        backButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        menuPanel.add(Box.createVerticalStrut(20)); // Add spacing
        menuPanel.add(createPostButton);
        menuPanel.add(Box.createVerticalStrut(10)); // Add spacing between buttons
        menuPanel.add(deletePostButton);
        //menuPanel.add(Box.createVerticalStrut(10));
        //menuPanel.add(commentOnPostButton);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(blockButton);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(unblockButton);
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(backButton); // Add back button at the bottom
        menuPanel.add(Box.createVerticalGlue()); // Add flexible spacing

        // Add menuPanel to the dialog
        profileDialog.add(menuPanel);

        // Add action listeners for the buttons
        createPostButton.addActionListener(e -> openCreatePostWindow());
        deletePostButton.addActionListener(e -> openDeletePostWindow());
        commentOnPostButton.addActionListener(e -> openCommentOnPostWindow());
        blockButton.addActionListener(e -> openBlockUserWindow());
        unblockButton.addActionListener(e -> openUnblockUserWindow());

        // Add action listener for the Back button
        backButton.addActionListener(e -> profileDialog.dispose());

        // Display the dialog
        profileDialog.pack();
        profileDialog.setLocationRelativeTo(mainFrame);
        profileDialog.setVisible(true);
    }

    // Methods to open new windows for each button
    private void openCreatePostWindow() {
        JDialog createPostDialog = new JDialog(mainFrame, "Create Post", true);
        createPostDialog.setSize(400, 250);
        createPostDialog.setLocationRelativeTo(mainFrame);
        createPostDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel for input
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextArea postMessageArea = new JTextArea(5, 30); // Multi-line text area for post message
        postMessageArea.setLineWrap(true);
        postMessageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(postMessageArea); // Add scrolling for longer messages

        JButton postButton = new JButton("Post"); // Button to submit post
        JTextArea responseArea = new JTextArea(); // To display server feedback
        responseArea.setEditable(false);

        // Back button
        JButton backButton = new JButton("Back");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(postButton);
        buttonPanel.add(backButton);

        // Add components to input panel
        inputPanel.add(new JLabel("Enter your post message below:"), BorderLayout.NORTH);
        inputPanel.add(scrollPane, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listener for the Post button
        postButton.addActionListener(e -> {
            String message = postMessageArea.getText().trim();
            if (!message.isEmpty()) {
                serverWriter.println("ADD_POST " + loggedName + " " + message); // Send command to server
                try {
                    String response = serverReader.readLine(); // Read server response
                    responseArea.setText("Server response: " + response);
                } catch (IOException ex) {
                    responseArea.setText("Error communicating with the server: " + ex.getMessage());
                }
            } else {
                responseArea.setText("Post message cannot be empty.");
            }
        });

        // Add action listener for the Back button
        backButton.addActionListener(e -> createPostDialog.dispose());

        // Add response area to dialog
        createPostDialog.add(inputPanel, BorderLayout.CENTER);
        createPostDialog.add(responseArea, BorderLayout.SOUTH);

        createPostDialog.setVisible(true);
    }

    private void openDeletePostWindow() {
        JDialog deletePostDialog = new JDialog(mainFrame, "Remove Post", true);
        deletePostDialog.setSize(400, 300);
        deletePostDialog.setLocationRelativeTo(mainFrame);
        deletePostDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel to display posts
        JPanel postDisplayPanel = new JPanel(new BorderLayout());
        JTextArea postDisplayArea = new JTextArea();
        postDisplayArea.setEditable(false); // Read-only
        postDisplayArea.setLineWrap(true);
        postDisplayArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(postDisplayArea); // Scrollable area for posts
        postDisplayPanel.add(scrollPane, BorderLayout.CENTER); // Add the scroll pane to the panel

        // Fetch and display user's posts
        serverWriter.println("GET_USER_POSTS " + loggedName); // Send command to fetch posts
        try {
            StringBuilder fullResponse = new StringBuilder();
            String line;
            while ((line = serverReader.readLine()) != null && !line.equals("\u0002END\u0003")) { // Read until "END"
                fullResponse.append(line).append("\n");
            }
            postDisplayArea.setText(fullResponse.toString()); // Display all posts in the text area
        } catch (IOException e) {
            postDisplayArea.setText("Error fetching posts: " + e.getMessage());
        }

        // Panel for input and buttons
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField postIdField = new JTextField(20); // Input box for post ID
        JButton deleteButton = new JButton("Delete");
        JButton backButton = new JButton("Back");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        inputPanel.add(new JLabel("Enter Post ID to delete:"), BorderLayout.NORTH);
        inputPanel.add(postIdField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listener for Delete button
        deleteButton.addActionListener(e -> {
            String postId = postIdField.getText().trim();
            if (!postId.isEmpty()) {
                serverWriter.println("REMOVE_POST " + postId); // Send command to server
                try {
                    String response = serverReader.readLine(); // Read server response
                    postDisplayArea.setText("Server response: " + response); // Update the post display area with the response
                } catch (IOException ex) {
                    postDisplayArea.setText("Error communicating with the server: " + ex.getMessage());
                }
            } else {
                postDisplayArea.setText("Post ID cannot be empty.");
            }
        });

        // Add action listener for Back button
        backButton.addActionListener(e -> deletePostDialog.dispose());

        // Add components to the dialog
        deletePostDialog.add(postDisplayPanel, BorderLayout.CENTER); // Add the post display panel
        deletePostDialog.add(inputPanel, BorderLayout.SOUTH); // Add the input panel at the bottom

        deletePostDialog.setVisible(true);
    }

    private void openCommentOnPostWindow() {
        JDialog commentDialog = new JDialog(mainFrame, "Comment on Post", true);
        commentDialog.setSize(400, 300);
        commentDialog.setLocationRelativeTo(mainFrame);
        commentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel for inputs
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10)); // 3 rows, 2 columns
        JLabel postIdLabel = new JLabel("Post ID:");
        JTextField postIdField = new JTextField();
        JLabel commentLabel = new JLabel("Comment:");
        JTextField commentField = new JTextField();

        // Add labels and fields to the panel
        inputPanel.add(postIdLabel);
        inputPanel.add(postIdField);
        inputPanel.add(commentLabel);
        inputPanel.add(commentField);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton submitButton = new JButton("Submit");
        JButton backButton = new JButton("Back");

        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        // Add panels to the dialog
        commentDialog.add(inputPanel, BorderLayout.CENTER);
        commentDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Action listener for Submit button
        submitButton.addActionListener(e -> {
            String postId = postIdField.getText().trim();
            String comment = commentField.getText().trim();

            if (postId.isEmpty() || comment.isEmpty()) {
                JOptionPane.showMessageDialog(commentDialog, "Both Post ID and Comment are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Send the COMMENT_POST command to the server
            serverWriter.println("COMMENT_POST " + postId + " " + comment);
            try {
                String response = serverReader.readLine(); // Read server response
                JOptionPane.showMessageDialog(commentDialog, response, "Server Response",
                        response.startsWith("SUCCESS") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(commentDialog, "Error communicating with the server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action listener for Back button
        backButton.addActionListener(e -> commentDialog.dispose());

        commentDialog.setVisible(true);
    }

    private void openBlockUserWindow() {
        JDialog blockUserDialog = new JDialog(mainFrame, "Block User", true);
        blockUserDialog.setSize(300, 250);
        blockUserDialog.setLocationRelativeTo(mainFrame);
        blockUserDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel for input
        JPanel panel = new JPanel(new BorderLayout());
        JTextField usernameField = new JTextField(20); // Input box for username
        JButton blockButton = new JButton("Block"); // Button to block user
        JTextArea responseArea = new JTextArea(); // To display feedback
        responseArea.setEditable(false);

        // Back button panel
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back");

        // Add components to panel
        panel.add(new JLabel("Enter username to block:"), BorderLayout.NORTH);
        panel.add(usernameField, BorderLayout.CENTER);
        panel.add(blockButton, BorderLayout.EAST);

        // Add action listener for block button
        blockButton.addActionListener(e -> {
            String friendUsername = usernameField.getText().trim();
            if (!friendUsername.isEmpty()) {
                serverWriter.println("BLOCK_USER " + loggedName + " " + friendUsername); // Send command to server
                try {
                    String response = serverReader.readLine(); // Read server response
                    responseArea.setText("Server response: " + response);
                } catch (IOException ex) {
                    responseArea.setText("Error communicating with the server: " + ex.getMessage());
                }
            } else {
                responseArea.setText("Please enter a valid username.");
            }
        });

        // Add action listener for back button
        backButton.addActionListener(e -> blockUserDialog.dispose());

        // Add response area and back button
        blockUserDialog.add(panel, BorderLayout.CENTER);
        blockUserDialog.add(responseArea, BorderLayout.SOUTH);
        backPanel.add(backButton);
        blockUserDialog.add(backPanel, BorderLayout.NORTH);

        blockUserDialog.setVisible(true);
    }

    private void openUnblockUserWindow() {
        JDialog unblockUserDialog = new JDialog(mainFrame, "Unblock User", true);
        unblockUserDialog.setSize(300, 250);
        unblockUserDialog.setLocationRelativeTo(mainFrame);
        unblockUserDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel for input
        JPanel panel = new JPanel(new BorderLayout());
        JTextField usernameField = new JTextField(20); // Input box for username
        JButton unblockButton = new JButton("Unblock"); // Button to unblock user
        JTextArea responseArea = new JTextArea(); // To display feedback
        responseArea.setEditable(false);

        // Back button panel
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back");

        // Add components to panel
        panel.add(new JLabel("Enter username to unblock:"), BorderLayout.NORTH);
        panel.add(usernameField, BorderLayout.CENTER);
        panel.add(unblockButton, BorderLayout.EAST);

        // Add action listener for unblock button
        unblockButton.addActionListener(e -> {
            String friendUsername = usernameField.getText().trim();
            if (!friendUsername.isEmpty()) {
                serverWriter.println("UNBLOCK_USER " + loggedName + " " + friendUsername); // Send command to server
                try {
                    String response = serverReader.readLine(); // Read server response
                    responseArea.setText("Server response: " + response);
                } catch (IOException ex) {
                    responseArea.setText("Error communicating with the server: " + ex.getMessage());
                }
            } else {
                responseArea.setText("Please enter a valid username.");
            }
        });

        // Add action listener for back button
        backButton.addActionListener(e -> unblockUserDialog.dispose());

        // Add response area and back button
        unblockUserDialog.add(panel, BorderLayout.CENTER);
        unblockUserDialog.add(responseArea, BorderLayout.SOUTH);
        backPanel.add(backButton);
        unblockUserDialog.add(backPanel, BorderLayout.NORTH);

        unblockUserDialog.setVisible(true);
    }

    public void login(String username, String password) {
        // Send the LOGIN command to the server
        serverWriter.println("LOGIN " + username + " " + password);

        try {
            // Read the server's response
            String response = serverReader.readLine();

            // Handle cases where the server response is null or empty
            if (response == null || response.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No response from server.", "Error", JOptionPane.ERROR_MESSAGE);
                loggedIn = false;
            }

            // Process the response from the server
            if (response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(null, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loggedName = username;
                bar++;
                loggedIn = true;
            } else {
                JOptionPane.showMessageDialog(null, "Login failed: " + response, "Error", JOptionPane.ERROR_MESSAGE);
                loggedIn = false;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            loggedIn = false;
        }
    }

    public void register(String username, String password) {

        serverWriter.println("REGISTER " + username + " " + password); // Send command to the server
        try {
            String response = serverReader.readLine(); // Read server's response
            if (response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(null, response, "Success", JOptionPane.INFORMATION_MESSAGE);
                registerSuccess = true;
            } else {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "TerminalClient: Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addFriend(String friendUsername) { // Second username (target)
        serverWriter.println("ADD_FRIEND " + loggedName + " " + friendUsername); // Send command to server

        try {
            String response = serverReader.readLine();
            if (response.startsWith("ERROR")) {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, friendUsername + " has been added as a friend", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeFriend(String username) {
        serverWriter.println("REMOVE_FRIEND " + loggedName + " " + username);
        try {
            String response = serverReader.readLine();
            if (response.startsWith("ERROR")) {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, username + " has been unfriended.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void commentOnPost(String postId) {
        String trimmedPostId = postId.trim();

        String comment = JOptionPane.showInputDialog(mainFrame, "Enter comment:", "New Comment", JOptionPane.PLAIN_MESSAGE);
        if (comment == null) {
            return;
        }
        while (comment.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You can't submit an empty comment", "Error", JOptionPane.ERROR_MESSAGE);
            comment = JOptionPane.showInputDialog(mainFrame, "Enter comment:", "New Comment", JOptionPane.PLAIN_MESSAGE);
            if (comment == null) {
                return;
            }
        }

        serverWriter.println("COMMENT_POST " + loggedName + " " + trimmedPostId + " " + comment);
        try {
            String response = serverReader.readLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeCommentOnPost(String postId) {
        String trimmedPostId = postId.trim();
        serverWriter.println("GET_COMMENTS " + loggedName + " " + trimmedPostId);

        try {
            String response = serverReader.readLine();

            if (response.startsWith("SUCCESS")) {
                response = response.substring(9);
                String[] userComments = response.split("\u0001\u0002\u0003");

                String commentTR = (String) JOptionPane.showInputDialog(null, "Which comment would you like to delete", "Delete Comment", JOptionPane.PLAIN_MESSAGE, null, userComments, userComments[0]);
                if (commentTR != null && !commentTR.isEmpty()) {
                    serverWriter.println("DELETE_COMMENT " + loggedName + " " + trimmedPostId + " " + commentTR);

                    try {
                        String delco = serverReader.readLine();

                        if (delco.startsWith("SUCCESS")) {
                            JOptionPane.showMessageDialog(null, "Comment deleted", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, delco, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException er) {
                        JOptionPane.showMessageDialog(null, "Error communicating with the server: " + er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void likePost(String postId) {
        String trimmedPostId = postId.trim();
        serverWriter.println("LIKE_POST " + trimmedPostId);

        try {
            // Wait for and log the server response
            String response = serverReader.readLine();

            // Show response to the user
            if (!response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(null, response, "Like Post", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            // Handle any communication errors and log them
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dislikePost(String postId) {
        serverWriter.println("DISLIKE_POST " + postId); // Send the command to the server

        try {
            String response = serverReader.readLine(); // Read server response
            if (!response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(null, response, "Like Post", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractPostId(String post) {
        try {
            // Split the post into lines
            String[] lines = post.split("\\r?\\n");

            // Look for the line that starts with "Post ID:"
            for (String line : lines) {
                if (line.startsWith("Post ID:")) {
                    // Extract the numeric ID by splitting on ":"
                    String postId = line.split(":")[1].trim();
                    return postId;
                }
            }

            // If no "Post ID:" line is found, throw an exception
            throw new IllegalArgumentException("No valid 'Post ID:' line found in post: " + post);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error extracting Post ID: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return "0"; // Default invalid ID
        }
    }

    public void removePost() {
        System.out.print("Enter the post ID to remove: ");
        String postId = userInputScanner.nextLine();

        serverWriter.println("REMOVE_POST " + postId);
        try {
            String response = serverReader.readLine();
            System.out.println(response);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String[] viewPosts() {
        if (loggedName == null || loggedName.isEmpty()) {
            String[] results = {"TerminalClient: You have to log in before viewing posts."};
            return results;
        }
        serverWriter.println("VIEW_POSTS " + loggedName); // Send the request

        try {
            // Read the full response line by line
            StringBuilder fullResponse = new StringBuilder();
            String line;
            while ((line = serverReader.readLine()) != null && !line.equals("\u0002END\u0003")) {
                if (line.startsWith("ERROR")) {
                    String[] results = {line};
                    return results;
                }
                fullResponse.append(line).append(System.lineSeparator());
            }

            String[] posts = fullResponse.toString().split("\u0001\u0002\u0003");
            for (int i = 0; i < posts.length; i++) {
                posts[i] = posts[i].trim();
                if (posts[i].startsWith("\u0002END\u0003")) {
                    posts[i] = posts[i].substring(15);
                }
            }

            return posts;
        } catch (IOException e) {
            String[] results = {"TerminalClient: Error communicating with the server: " + e.getMessage()};
            return results;
        }
    }

    public void closeConnection() {
        try {
            serverWriter.println("EXIT");
            socket.close();
            JOptionPane.showMessageDialog(null, "Connection closed.", "Connection closed", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error closing the connection: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        TerminalClient client = new TerminalClient("localhost", 54321);
        client.run();
    }
}