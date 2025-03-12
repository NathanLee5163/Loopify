import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class User implements UserInterface {
    private String username;
    private String password;
    private ArrayList<User> friends = new ArrayList<>();
    private ArrayList<User> blocklist = new ArrayList<>();
    private String bio;
    File f;
    File b;

    public User(String username, String password) throws InvalidInputException, UsernameTakenException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        setUsername(username);    // Set the username, checking for uniqueness
        setPassword(password);    // Set the password with validation

        f = new File("Resources/" + username + "s_Friends.txt");
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("File already exists");
        }

        b = new File("Resources/" + username + "s_Blocklist.txt");
        try {
            b.createNewFile();
        } catch (IOException e) {
            System.out.println("File already exists");
        }

        this.bio = "";

        // Register this user in the Database to enforce unique usernames
        Database.addUser(this);
    }

    public User(String username, String password, String bio) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        this.username = username;
        this.password = password;
        this.bio = bio == null ? "" : bio;
        this.friends = new ArrayList<>();
        this.blocklist = new ArrayList<>();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String newUsername) throws UsernameTakenException {
        if (Database.checkForUsername(newUsername)) {
            throw new UsernameTakenException("Username already exists");
        } else {
            this.username = newUsername;
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) throws InvalidInputException {
        if (!isValidPassword(password)) {
            throw new InvalidInputException("Password must contain at least one capital letter.");
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader("src/Users.txt"))) {
                String line = br.readLine();
                ArrayList<String[]> tempUsers = new ArrayList<>();

                while (line != null) {
                    if (!line.startsWith(username)) {
                        tempUsers.add(line.split(","));
                    }
                    line = br.readLine();
                }

                try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/Users.txt"))) {
                    for (int i = 0; i < tempUsers.size(); i++) {
                        line = "";
                        for (int j = 0; j < tempUsers.get(i).length; j++) {
                            line += tempUsers.get(i)[j] + ",";
                        }
                        if (tempUsers.get(i).length == 2) {
                            bw.write(line + System.lineSeparator());
                        } else {
                            line = line.substring(0, line.length() - 1);
                            bw.write(line + System.lineSeparator());
                        }
                    }
                    this.password = password;
                    bw.write(this + System.lineSeparator());
                }
            } catch (IOException e) {
                System.out.println("File not found");
            }
        }

    }

    public void addFriend(User user) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Friends.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!friends.contains(Database.getUserByUsername(line))) {
                    try {
                        friends.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Blocklist.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!blocklist.contains(Database.getUserByUsername(line))) {
                    try {
                        blocklist.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean hasThemBlocked = false;

        User[] otherBlocklist = user.getBlocklist();

        if (otherBlocklist != null) {
            for (int i = 0; i < otherBlocklist.length; i++) {
                if (otherBlocklist[i].equals(this)) {
                    hasThemBlocked = true;
                }
            }
        }

        if (blocklist.contains(user)) {
            throw new UserNotFoundException("User is blocked.");
        } else if (hasThemBlocked) {
            throw new UserNotFoundException("User has you blocked.");
        } else if (friends.contains(user)) {
            throw new UserNotFoundException("User is already friend.");
        } else {
            try (BufferedWriter br = new BufferedWriter(new FileWriter("Resources/" + username + "s_Friends.txt", true))) {
                br.write(user.getUsername() + System.lineSeparator());
                friends.add(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeFriend(User user) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Friends.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!friends.contains(Database.getUserByUsername(line))) {
                    try {
                        friends.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!friends.contains(user)) {
            throw new UserNotFoundException("You don't have this user friended.");
        } else {
            friends.remove(user);

            try (BufferedWriter br = new BufferedWriter(new FileWriter("Resources/" + username + "s_Friends.txt"))) {
                for (int i = 0; i < friends.size(); i++) {
                    br.write(friends.get(i).getUsername() + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    //changed getters for friends and blocklist
    public User[] getFriends() {
        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Friends.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!friends.contains(Database.getUserByUsername(line))) {
                    try {
                        friends.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (friends.isEmpty()) {
            return null;
        } else {
            return friends.toArray(new User[friends.size()]);
        }
    }

    @Override
    public void block(User user) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Blocklist.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!blocklist.contains(Database.getUserByUsername(line))) {
                    try {
                        blocklist.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (blocklist.contains(user)) {
            throw new UserNotFoundException("User is already blocked.");
        } else {
            try (BufferedWriter br = new BufferedWriter(new FileWriter("Resources/" + username + "s_Blocklist.txt", true))) {
                br.write(user.getUsername() + System.lineSeparator());
                blocklist.add(user);
                removeFriend(user);
                user.removeFriend(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void unBlock(User user) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Blocklist.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!blocklist.contains(Database.getUserByUsername(line))) {
                    try {
                        blocklist.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!blocklist.contains(user)) {
            throw new UserNotFoundException("You don't have this user blocked.");
        } else {
            blocklist.remove(user);

            try (BufferedWriter br = new BufferedWriter(new FileWriter("Resources/" + username + "s_Blocklist.txt"))) {
                for (int i = 0; i < blocklist.size(); i++) {
                    br.write(blocklist.get(i).getUsername() + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public User[] getBlocklist() {
        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Blocklist.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!blocklist.contains(Database.getUserByUsername(line))) {
                    try {
                        blocklist.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (blocklist.isEmpty()) {
            return null;
        } else {
            return blocklist.toArray(new User[blocklist.size()]);
        }
    }

    public boolean isBlocked(String usernameSearch) {
        try (BufferedReader br = new BufferedReader(new FileReader("Resources/" + username + "s_Blocklist.txt"))) {
            String line = br.readLine();

            while (line != null) {
                if (!blocklist.contains(Database.getUserByUsername(line))) {
                    try {
                        blocklist.add(Database.getUserByUsername(line));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            User user = Database.getUserByUsername(usernameSearch);
            return blocklist.contains(user);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s",
                username,
                password == null ? "" : password, // Ensure password is not null
                bio == null ? "" : bio);
    }


    //let me know if you want the valid password to be more specific but for now it's just checking for an uppercase letter
    private boolean isValidPassword(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) throws InvalidInputException {
        if (bio.isEmpty()) {
            throw new InvalidInputException("Bio cannot be empty");
        } else if (bio.contains(",")) {
            throw new InvalidInputException("Bio cannot contain a comma");
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader("src/Users.txt"))) {
                String line = br.readLine();
                ArrayList<String[]> tempUsers = new ArrayList<>();

                while (line != null) {
                    if (!line.startsWith(username)) {
                        tempUsers.add(line.split(","));
                    }
                    line = br.readLine();
                }

                try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/Users.txt"))) {
                    for (int i = 0; i < tempUsers.size(); i++) {
                        line = "";
                        for (int j = 0; j < tempUsers.get(i).length; j++) {
                            line += tempUsers.get(i)[j] + ",";
                        }
                        if (tempUsers.get(i).length == 2) {
                            bw.write(line + System.lineSeparator());
                        } else {
                            line = line.substring(0, line.length() - 1);
                            bw.write(line + System.lineSeparator());
                        }

                    }
                    this.bio = bio;
                    String newUserInfo = toString();
                    System.out.println("New User Info: " + newUserInfo);
                    bw.write(newUserInfo + System.lineSeparator());
                }
            } catch (IOException e) {
                System.out.println("File not found");
            }
        }
    }

    public boolean isFriend(User user) {
        return friends.contains(user);
    }

    public boolean isBlocked(User user) {
        return blocklist.contains(user);
    }

    public String getUserName() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if they are the same reference
        if (o == null || getClass() != o.getClass()) return false; // Null or class check

        User user = (User) o;
        return username != null && username.equals(user.getUsername());
    }

    private List<String> extractUsernames(List<User> users) {
        if (users == null) return new ArrayList<>();
        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            if (user != null && user.getUsername() != null) {
                usernames.add(user.getUsername());
            }
        }
        return usernames;
    }

    public void update() {
        if (f == null) {
            f = new File("Resources/" + username + "s_Friends.txt");
        }
        if (b == null) {
            b = new File("Resources/" + username + "s_Blocklist.txt");
        }
        if (friends == null) {
            friends = new ArrayList<>();
        }
        if (blocklist == null) {
            blocklist = new ArrayList<>();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();

            while (line != null) {
                try {
                    friends.add(Database.getUserByUsername(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(b))) {
            String line = br.readLine();

            while (line != null) {
                try {
                    blocklist.add(Database.getUserByUsername(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
