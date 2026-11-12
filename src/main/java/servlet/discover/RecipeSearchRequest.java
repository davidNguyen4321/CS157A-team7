package servlet.discover; 

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import java.io.IOException;

public class RecipeSearchRequest {
    private String searchTerm;
    private Map<String, String> sortOptions;
    private Map<String, Integer[]> filterIdMap;
    private Map<String, Integer> tolerances;
    private Map<String, Map<String, Integer>> ranges;
    private Map<String, Boolean> options;
    
    private Map<Integer, Integer[]> ingredientMatches = new LinkedHashMap<>(); // wanted id -> ids that satisfy it

    public RecipeSearchRequest(Map<String, String> parameterMap) throws IOException {        
        ObjectMapper mapper = new ObjectMapper();
        this.searchTerm = parameterMap.get("searchTerm").replaceAll("[%_]", "").trim(); // These characters are banned in recipe creation
        
        this.sortOptions = mapper.readValue(
                parameterMap.get("sortOptions"),
                new TypeReference<Map<String, String>>() {}
        );
        
        this.filterIdMap = mapper.readValue(
                parameterMap.get("filterIds"),
                new TypeReference<Map<String, Integer[]>>() {}
        );

        this.tolerances = mapper.readValue(
                parameterMap.get("tolerances"),
                new TypeReference<Map<String, Integer>>() {}
        );

        this.ranges = mapper.readValue(
                parameterMap.get("ranges"),
                new TypeReference<Map<String, Map<String, Integer>>>() {}
        );

        this.options = mapper.readValue(
                parameterMap.get("options"),
                new TypeReference<Map<String, Boolean>>() {}
        );
    }

    protected String getSearchTerm() { return searchTerm; }
    protected Map<String, String> getSortOptions() { return sortOptions; }
    protected Map<String, Integer[]> getFilterIdMap() { return filterIdMap; }
    protected Map<String, Integer> getTolerances() { return tolerances; }
    protected Map<String, Map<String, Integer>> getRanges() { return ranges; }
    protected Map<String, Boolean> getOptions() { return options; }
    
    protected Map<Integer, Integer[]> getIngredientMatches() { return ingredientMatches; }
}