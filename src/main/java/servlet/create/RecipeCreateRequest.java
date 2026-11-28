package servlet.create;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ServletParamException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RecipeCreateRequest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> AI_FIELDS = Set.of("description", "totalMinutes", "course", "appliances", "cuisines", "dishTypes");
    private static final double MAX_AMOUNT = 999.99; // amount is DECIMAL(5,2)

    // Shape of the JSON body, only used for parsing
    private static class Body {
        public String name, description, instructions, course;
        public Integer totalMinutes;
        public List<IngredientLine> ingredients;
        public List<Integer> applianceIds, cuisineIds, dishTypeIds;
        public Set<String> aiFill;
    }

    private final String name, description, instructions, course;
    private final Integer totalMinutes;
    private final List<IngredientLine> ingredients;
    private final List<Integer> applianceIds, cuisineIds, dishTypeIds;
    private final Set<String> aiFill;

    public RecipeCreateRequest(String body) throws IOException {
        Body b = mapper.readValue(body, Body.class);
        if (b == null) bad("body");

        this.name = b.name == null ? "" : b.name.strip();
        this.instructions = b.instructions == null ? "" : b.instructions.strip();
        this.description = (b.description == null || b.description.isBlank()) ? null : b.description.strip();
        this.course = (b.course == null || b.course.isBlank()) ? null : b.course;
        this.totalMinutes = b.totalMinutes;
        this.ingredients = b.ingredients == null ? new ArrayList<>() : b.ingredients;
        this.applianceIds = b.applianceIds == null ? new ArrayList<>() : b.applianceIds;
        this.cuisineIds = b.cuisineIds == null ? new ArrayList<>() : b.cuisineIds;
        this.dishTypeIds = b.dishTypeIds == null ? new ArrayList<>() : b.dishTypeIds;
        this.aiFill = b.aiFill == null ? new HashSet<>() : b.aiFill;

        validate();
    }

    private void validate() {
        if (name.isEmpty() || name.length() > 100) bad("name");
        if (instructions.isEmpty() || instructions.length() > 10000) bad("instructions");
        if (description != null && description.length() > 2000) bad("description");
        if (totalMinutes != null && (totalMinutes < 1 || totalMinutes > 10080)) bad("totalMinutes");
        if (course != null && !RecipeAiFiller.COURSES.contains(course)) bad("course");
        if (!AI_FIELDS.containsAll(aiFill)) bad("aiFill");

        if (ingredients.isEmpty() || ingredients.size() > 100) bad("ingredients");
        Set<Integer> seen = new HashSet<>();
        for (IngredientLine line : ingredients) {
            if (line == null || line.id == null || !seen.add(line.id)) bad("ingredients");
            if (line.amount != null && (!Double.isFinite(line.amount) || line.amount < 0.01 || line.amount > MAX_AMOUNT)) bad("ingredient amount");
            if (line.unit != null && line.unit.strip().length() > 25) bad("ingredient unit");
        }

        checkIds("applianceIds", applianceIds);
        checkIds("cuisineIds", cuisineIds);
        checkIds("dishTypeIds", dishTypeIds);
    }

    private static void checkIds(String field, List<Integer> ids) {
        if (ids.size() > 50 || ids.stream().anyMatch(Objects::isNull) || new HashSet<>(ids).size() != ids.size()) bad(field);
    }

    private static void bad(String field) {
        throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + field + " in recipe create request");
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }
    public String getCourse() { return course; }
    public Integer getTotalMinutes() { return totalMinutes; }
    public List<IngredientLine> getIngredients() { return ingredients; }
    public List<Integer> getApplianceIds() { return applianceIds; }
    public List<Integer> getCuisineIds() { return cuisineIds; }
    public List<Integer> getDishTypeIds() { return dishTypeIds; }
    public Set<String> getAiFill() { return aiFill; }
}