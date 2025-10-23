package ca.gbc.comp3074.uiprototype.data.model;

/**
 * Model for community posts
 */
public class CommunityPost {
    private String id;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private String placeId;
    private String placeName;
    private String imageUrl;
    private String caption;
    private String category; // "food", "drink", "atmosphere", "environment"
    private int likesCount;
    private int commentsCount;
    private boolean isLikedByCurrentUser;
    private long timestamp;

    public CommunityPost() {
    }

    public CommunityPost(String id, String userId, String userName, String userAvatarUrl,
            String placeId, String placeName, String imageUrl, String caption,
            String category, int likesCount, int commentsCount,
            boolean isLikedByCurrentUser, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.placeId = placeId;
        this.placeName = placeName;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        isLikedByCurrentUser = likedByCurrentUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
