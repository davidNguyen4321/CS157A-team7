package servlet.share;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ServletParamException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RecipeSuggestRequest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> FIELDS = Set.of("description", "totalMinutes", "course", "ingredients", "appliances", "cuisines", "dishTypes");

    private static class Body {
        public String name, instructions;
        public List<Integer> ingredientIds;
        public Set<String> fields;
    }

    private final String name, instructions;
    private final List<Integer> ingredientIds;
    private final Set<String> fields;

    public RecipeSuggestRequest(String body) throws IOException {
        Body b = mapper.readValue(body, Body.class);
        if (b == null) throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Invalid recipe suggest request");

        this.name = b.name == null ? "" : b.name.strip();
        this.instructions = b.instructions == null ? "" : b.instructions.strip();
        this.ingredientIds = b.ingredientIds == null ? new ArrayList<>() : b.ingredientIds;
        this.fields = b.fields == null ? new HashSet<>() : b.fields;

        validate();
    }

    private void validate() {
        if (name.length() > 100 || instructions.isEmpty() || instructions.length() > 10000
                || fields.isEmpty() || !FIELDS.containsAll(fields)
                || ingredientIds.size() > 100 || ingredientIds.stream().anyMatch(Objects::isNull)) {
            throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Invalid recipe suggest request");
        }
    }

    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public List<Integer> getIngredientIds() { return ingredientIds; }
    public Set<String> getFields() { return fields; }
}