package recipe;

import java.util.List;
import java.util.ArrayList;

public class Recipe {
    public int id;
    public String name;
    public String imagePath;
    public String description;
    public String instructions;
    public Integer totalMinutes;
    public String course;
    
    public int bookmarkCount;
    public boolean userBookmarked;
    
    public int reviewCount;
    public String averageRating;

    public int authorId;
    public String authorName;
    public String authorImagePath;

    public List<String> ingredients = new ArrayList<>();
    public List<String> appliances = new ArrayList<>();
    public List<String> cuisines = new ArrayList<>();
    public List<String> dishTypes = new ArrayList<>();
}