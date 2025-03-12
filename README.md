# Loopify

Loopify is a social media platform that allows users to create and share posts, interact with content through upvotes/downvotes, and manage comments. This README provides instructions for compiling, running, and testing the project, along with detailed descriptions of each class.

## Table of Contents

- [Overview](#overview)
  - Loopify allows users to create posts, interact with friends' content, upvote/downvote posts, and manage comments. It supports persistence of data, ensuring user and post data are stored between sessions.

- [Contributors](#contributors)
  - Nathan J Lee
  - Odigomma Makuochukwu Oraelosi
  - Davion James Casey Ziolkowski
  - Andrew Michael Baker
  - Gabrielle Pesito

- [Compile](#how-to-compile-and-run)
  - 1.) Compile all classes:javac *.java (This command compiles all Java files in the directory.)
  - 2.) Run the server: java LoopifyServer
  - 3.) Run a client: java TerminalClient
    - Register if user is new.
    - Login if user has registered before.
    - Exit if user wishes to close client.
      - After logging in user can choose between any of the 12 options:
      - 1.) You can choose four options on the top:
        - View Profile: Shows your profile information. You can edit bio or password as well.
        - Friends: It shows which friends are online. You can add and unadd friends.
        - Menu: You can choose four buttons to choose from.
          - Create Post: You write whatever you want to post in the text box then create post.
          - Delete Post: You enter an id number from the given options to delete a post.
          - Block: You enter a user you have friended then block to block.
          - Unblock: You enter a user you have friended then unblock to unblock.
        - Logout: You can click this to logout of the app.
      - 2.) You can click next to go right, and previous to go right(back).
      - 3.) You can click refresh to refresh the page if another user is logged in and made updates.
      - 4.) You can click like to like a post, and dislike to dislike a post.
      - 5.) You can click comment, write what you want to comment, then click comment to comment on a post.
      - 6.) You can click remove comment to remove a comment you wrote.
  - 4.) Test functionality:
    - java DatabaseTest
    - java UserTest
    - java PostTest

- [Testing Setup](#setup)
  - Running Tests. Execute each test file individually to verify functionality:
    - 1.) java DatabaseTest
    - 2.) java UserTest
    - 3.) java PostTest
    - 4.) ClientHandlerTest
    - 5.) TerminalClientTest

  - Data Persistence Verification:
    - Database Testing:
      - The DatabaseTest.java file includes tests that add data to the database, simulate application closure, and then reload data to ensure persistence.
    - How it Works:
      - When data is saved in Database.java, it writes to a file that stores user and post data. Upon reloading, the system retrieves the stored data to confirm it persists correctly across sessions.
    - Expected Outcome:
      - Data added in one session should be available after restarting the application, proving that persistence is functioning as intended.

- [Class Descriptions](#classes)
  - Database.java:
    - purpose: Manages data storage for users and posts. This class acts as a central repository, handling data persistence and retrieval.
Functions: Provides methods for adding, removing, and querying users and posts. This ensures that user actions are reflected in the stored data, allowing for long-term data persistence.
  - User.java:
    - Purpose: Represents a user in the Loopify platform, handling properties like username and password.
Functions: Allows for creation, authentication, and basic user management. Methods include getters and setters for user attributes and validation checks for username and password security.
  - Post.java:
    - Purpose: Represents individual posts created by users. Each post includes content and allows for interactions like upvotes, downvotes, and comments.
Functions: Methods to create a new post, manage upvotes and downvotes, add comments, and handle interactions with other users. This class is essential for the feed functionality, allowing users to engage with content.


- [Networking Components](#server/client)
  - LoopifyServer.java:
    - purpose: The LoopifyServer class is the main entry point for the server-side application. It establishes a server socket to listen for incoming client connections, manages these connections using a thread pool, and delegates each client interaction to an instance of ClientHandler.
    - Key Features:
      - Multi-Client Support: Handles multiple clients simultaneously using a fixed-size thread pool to manage concurrent connections efficiently.
      - Port Configuration: Listens for incoming connections on port 12345 (default). This port can be changed by modifying the PORT constant.
      - Thread Pool Management: Utilizes a thread pool with a maximum of 10 threads (defined by MAX_THREADS) to prevent server overload and manage resource allocation effectively.
      - Error Handling: Includes basic error handling to log exceptions during connection setup and communication.
  - ClientHandler.java:
    - Purpose: The ClientHandler class is responsible for managing communication between the server and a single client. It processes client requests, interacts with the Database to perform operations, and sends appropriate responses back to the client.
    - Key Features:
      - Threaded Design: Each client connection is handled in its own thread, ensuring simultaneous interactions with multiple clients.
      - Command Parsing: Processes client requests by parsing commands and arguments, then delegates operations to corresponding handler methods.
      - Server Operations: Implements the core functionality of the Loopify platform, including:
        - User authentication (LOGIN, REGISTER).
        - User management (VIEW_PROFILE, ADD_FRIEND, REMOVE_FRIEND, BLOCK_USER, UNBLOCK_USER).
        - Post management (ADD_POST, REMOVE_POST, COMMENT_POST, LIKE_POST, DISLIKE_POST, VIEW_POSTS).
        - Profile updates (EDIT_BIO, EDIT_PASSWORD).
      - Error Handling: Ensures invalid or malformed requests are handled gracefully, returning meaningful error messages to the client.
  - TerminalClient.java:
    - Purpose: The TerminalClient is a command-line interface (CLI) client application that enables users to interact with the Loopify platform by sending requests to the server and displaying server responses.
    - Key Features:
      - Connection to Server: Establishes a connection to the Loopify server using sockets.
      - User Authentication: Supports user login and registration.
      - User Management:
      - View and edit profile information (bio and password).
      - Add, remove, block, and unblock friends.
    - Content Interaction:
      - Create and manage posts.
      - View, like, dislike, comment on, and delete posts.
      - Session Management: Maintains a logged-in session and provides an option to log out or exit the application.
      - Data Display: Displays user profiles and posts in a structured and readable format.


- [Test Class Descriptions](#test)
  - DatabaseTest.java: Purpose: Tests all functions of the Database class to ensure accurate data management.
    - Key Tests:
      - Verifies that user and post data is added, retrieved, and removed correctly. Tests data persistence by saving and reloading data within the database.
  - UserTest.java: Purpose: Verifies that the User class handles user attributes and validations accurately.
    - Key Tests:
      - Checks username and password creation, validation, and storage. Ensures each user’s credentials are managed securely.
  - PostTest.java: Purpose: Tests all features of the Post class, including upvoting, downvoting, and commenting.
    - Key Tests:
      - Ensures posts can be created with content, users can interact with them, and comments can be managed appropriately.
  - TerminalClientTest.java: Purpose: Tests designed to verify the functionality of the TerminalClient class. It simulates client-server interactions, tests various user actions, and ensures proper handling of server responses.
  - ClientHandlerTest.java: Purpose: Tests designed to test the functionality and robustness of the ClientHandler class. It ensures proper handling of client-server interactions, socket management, and error conditions.
