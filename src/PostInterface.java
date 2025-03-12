//Interface for creating posts

import java.util.List;

public interface PostInterface {

    public void addComment(String comment);
    public void removeComment(String username, String comment);
    public List<String> getComments();

    public void setPostId(String postId);

    //Deletes this post from the post database
    public void deletePost();
    public boolean isDeleted();

    //Edits this post with the new message
    public void editPost(String oldMessage, String newMessage);
    public void setLikes(int likes);
    public void setDislikes(int dislikes);

    //Liking and disliking function for the posts
    public void likePost();
    public void dislikePost();

    public String getPostId();
    public String getAuthorId();
    public String getMessage();
    public int getLikes();
    public int getDislikes();

    public String toString();
    public boolean equals(Object o);
}
