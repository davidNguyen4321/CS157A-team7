package servlet.search;

public class RecipeListItem {
    private int id;
    private String name;
    private String imagePath;
    private String description;
    private Integer totalMinutes;
    private String timeAgo;
    private int reviewCount;
    private String averageRating;
    private int bookmarkCount;
    private boolean userBookmarked;

    private int authorId;
    private String authorName;
    private String authorImagePath;

    public RecipeListItem(int id, String name, String imagePath, String description, 
                      Integer totalMinutes, String timeAgo, int reviewCount, 
                      String averageRating, int bookmarkCount, boolean userBookmarked, 
                      int authorId, String authorName, String authorImagePath) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
        this.totalMinutes = totalMinutes;
        this.timeAgo = timeAgo;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
        this.bookmarkCount = bookmarkCount;
        this.userBookmarked = userBookmarked;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorImagePath = authorImagePath;
    }

    public int getRecipeId() { return id; }
    public String getRecipeName() { return name; }
    public String getRecipeImagePath() { return imagePath; }
    public String getRecipeDescription() { return description; }
    public Integer getRecipeTotalMinutes() { return totalMinutes; }
    public String getTimeAgo() { return timeAgo; }
    public int getReviewCount() { return reviewCount; }
    public String getAverageRating() { return averageRating; }
    public int getBookmarkCount() { return bookmarkCount; }
    public boolean isUserBookmarked() { return userBookmarked; }

    public int getUserId() { return authorId; }
    public String getUserName() { return authorName; }
    public String getUserImagePath() { return authorImagePath; }
}