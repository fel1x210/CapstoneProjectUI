package ca.gbc.comp3074.uiprototype.data.model;

/**
 * Model for post comments/reviews
 */
public class PostComment {
    private String id;
    private String postId;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private String comment;
    private float rating; // 0-5 stars
    private long timestamp;

    public PostComment() {
    }

    public PostComment(String id, String postId, String userId, String userName,
            String userAvatarUrl, String comment, float rating, long timestamp) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
