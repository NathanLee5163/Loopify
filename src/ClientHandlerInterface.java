public interface ClientHandlerInterface {
    public void run();

    public String handleViewProfile(String[] tokens);
    public String handleViewFriends(String[] tokens);

    public String handleLogin(String[] tokens);
    public String handleLogout(String[] tokens);
    public String handleRegister(String[] tokens);

    public String handleAddUser(String[] tokens);
    public String handleRemoveUser(String[] tokens);

    public String handleCommentPost(String[] tokens);
    public String handleGetComments(String[] tokens);
    public String handleDeleteComment(String[] tokens);

    public String handleAddFriend(String[] tokens);
    public String handleRemoveFriend(String[] tokens);

    public String handleBlockUser(String[] tokens);
    public String handleUnBlockUser(String[] tokens);

    public String handleAddPost(String[] tokens);
    public String handleRemovePost(String[] tokens);

    public String handleLikePost(String[] tokens);
    public String handleDislikePost(String[] tokens);

    public String handleViewPosts(String[] tokens);

    public String handleEditBio(String[] tokens);
    public String handleEditPassword(String[] tokens);

    public String handleGetUserPosts(String[] tokens);
}
