//Interface for User class

public interface UserInterface {
    //Username and password getters and setters
    //might update with more getters and setters if we add like age for example
    public String getUsername();
    public void setUsername(String username) throws UsernameTakenException;
    public String getPassword();
    //Example exception InvalidInputException, for when the password doesn't
    //meet certain criteria like having a capital letter
    public void setPassword(String password) throws InvalidInputException;

    //Other potential exception for when a user doesn't exist or is
    //on the blocklist
    //I've included the friend function as part of the User class itself
    //since I think it'll be easier but we can make it a separate class
    public void addFriend(User user) throws UserNotFoundException;
    public void removeFriend(User user) throws UserNotFoundException;
    public User[] getFriends();

    //Blocking and unblocking users. Should make you unable to see their
    //posts even in public feeds and unable to find them when searching
    //(and vice versa for both)
    public void block(User user) throws UserNotFoundException;
    public void unBlock(User user) throws UserNotFoundException;
    public User[] getBlocklist();
    public boolean isBlocked(String usernameSearch);

    public boolean isFriend(User user);
    public boolean isBlocked(User user);
    public String getUserName();

    //toString to return a String format of the User's profile
    public String toString();

    //Returns true if the two objects have the same username
    //Returns false if the given object is not of type User
    public boolean equals(Object o);

    public void update();
}
