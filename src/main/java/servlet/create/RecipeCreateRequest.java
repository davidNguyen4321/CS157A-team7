package servlet.create;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ServletParamException;

public record RecipeCreateRequest(
        String name, String description, String instructions,
        Integer totalMinutes, String course,
        List<IngredientLine> ingredients,
        List<Integer> applianceIds, List<Integer> cuisineIds, List<Integer> dishTypeIds,
        Set<String> aiFill) {

    public record IngredientLine(Integer id, BigDecimal amount, String unit) {}

    private static final Set<String> AI_FIELDS = Set.of("description", "totalMinutes", "course", "appliances", "cuisines", "dishTypes");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999.99"); // amount is DECIMAL(5,2)

    public RecipeCreateRequest {
        name = name == null ? "" : name.strip();
        instructions = instructions == null ? "" : instructions.strip();
        description = (description == null || description.isBlank()) ? null : description.strip();
        course = (course == null || course.isBlank()) ? null : course;
        ingredients = ingredients == null ? List.of() : ingredients;
        applianceIds = applianceIds == null ? List.of() : applianceIds;
        cuisineIds = cuisineIds == null ? List.of() : cuisineIds;
        dishTypeIds = dishTypeIds == null ? List.of() : dishTypeIds;
        aiFill = aiFill == null ? Set.of() : aiFill;
    }
    
    public static RecipeCreateRequest fromForm(HttpServletRequest r) {
        List<IngredientLine> lines = new ArrayList<>();
        for (Integer id : ints(r, "ingredientId")) {
            BigDecimal amount = null;
            String rawAmount = r.getParameter("amount_" + id);
            if (rawAmount != null && !rawAmount.isBlank()) {
                try { amount = new BigDecimal(rawAmount.strip()); }
                catch (NumberFormatException e) { bad("ingredient amount"); }
            }
            String unit = r.getParameter("unit_" + id);
            lines.add(new IngredientLine(id, amount, (unit == null || unit.isBlank()) ? null : unit.strip()));
        }

        Integer totalMinutes = null;
        String rawMinutes = r.getParameter("totalMinutes");
        if (rawMinutes != null && !rawMinutes.isBlank()) {
            try { totalMinutes = Integer.valueOf(rawMinutes.strip()); }
            catch (NumberFormatException e) { bad("totalMinutes"); }
        }

        String[] aiFill = r.getParameterValues("aiFill");

        return new RecipeCreateRequest(
            r.getParameter("name"), r.getParameter("description"), r.getParameter("instructions"),
            totalMinutes, r.getParameter("course"), lines,
            ints(r, "applianceId"), ints(r, "cuisineId"), ints(r, "dishTypeId"),
            aiFill == null ? Set.of() : new HashSet<>(Arrays.asList(aiFill)));
    }

    private static List<Integer> ints(HttpServletRequest r, String key) {
        List<Integer> out = new ArrayList<>();
        String[] values = r.getParameterValues(key);
        if (values == null) return out;
        for (String v : values) {
            try { out.add(Integer.valueOf(v.strip())); }
            catch (NumberFormatException e) { bad(key); }
        }
        return out;
    }

    public void validate() {
        if (name.isEmpty() || name.length() > 100) bad("name");
        if (instructions.isEmpty() || instructions.length() > 10000) bad("instructions");
        if (description != null && description.length() > 2000) bad("description");
        if (totalMinutes != null && (totalMinutes < 1 || totalMinutes > 10080)) bad("totalMinutes");
        if (course != null && !RecipeAiFiller.COURSES.contains(course)) bad("course");
        if (!AI_FIELDS.containsAll(aiFill)) bad("aiFill");

        if (ingredients.isEmpty() || ingredients.size() > 100) bad("ingredients");
        Set<Integer> seen = new HashSet<>();
        for (IngredientLine line : ingredients) {
            if (line == null || line.id() == null || !seen.add(line.id())) bad("ingredients");
            if (line.amount() != null && (line.amount().signum() <= 0 || line.amount().compareTo(MAX_AMOUNT) > 0)) bad("ingredient amount");
            if (line.unit() != null && line.unit().length() > 25) bad("ingredient unit");
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
    
    
}