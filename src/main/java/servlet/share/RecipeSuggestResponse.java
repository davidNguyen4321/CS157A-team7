package servlet.share;

import java.util.List;

import servlet.dropdown.DropdownItem;

public class RecipeSuggestResponse {
    private final String description;
    private final Integer totalMinutes;
    private final String course;
    private final List<DropdownItem> ingredients, appliances, cuisines, dishTypes;

    public RecipeSuggestResponse(String description, Integer totalMinutes, String course,
                                 List<DropdownItem> ingredients, List<DropdownItem> appliances,
                                 List<DropdownItem> cuisines, List<DropdownItem> dishTypes) {
        this.description = description;
        this.totalMinutes = totalMinutes;
        this.course = course;
        this.ingredients = ingredients;
        this.appliances = appliances;
        this.cuisines = cuisines;
        this.dishTypes = dishTypes;
    }

    public String getDescription() { return description; }
    public Integer getTotalMinutes() { return totalMinutes; }
    public String getCourse() { return course; }
    public List<DropdownItem> getIngredients() { return ingredients; }
    public List<DropdownItem> getAppliances() { return appliances; }
    public List<DropdownItem> getCuisines() { return cuisines; }
    public List<DropdownItem> getDishTypes() { return dishTypes; }
}