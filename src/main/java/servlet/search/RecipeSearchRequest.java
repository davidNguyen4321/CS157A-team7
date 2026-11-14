package servlet.search; 

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import servlet.security.ParamSecurity;

import java.util.LinkedHashMap;
import java.util.Map;

import java.io.IOException;

public class RecipeSearchRequest {
    private String searchTerm;
    private Map<String, String> sortOptions;
    private Map<String, Integer[]> filterIdMap;
    private Map<String, Integer> tolerances;
    private Map<String, Map<String, Double>> ranges;
    private Map<String, Boolean> options;
    private Map<Integer, Integer[]> ingredientMatches = new LinkedHashMap<>(); // wanted id -> ids that satisfy it

    public RecipeSearchRequest(Map<String, String> parameterMap) throws IOException {        
        ObjectMapper mapper = new ObjectMapper();
                
        this.searchTerm = ParamSecurity.getCleanSearch(parameterMap.get("searchTerm"));        
        this.sortOptions = mapper.readValue(parameterMap.get("sortOptions"), new TypeReference<Map<String, String>>() {});
        this.filterIdMap = mapper.readValue(parameterMap.get("filterIds"), new TypeReference<Map<String, Integer[]>>() {});
        this.tolerances = mapper.readValue(parameterMap.get("tolerances"), new TypeReference<Map<String, Integer>>() {});
        this.ranges = mapper.readValue(parameterMap.get("ranges"), new TypeReference<Map<String, Map<String, Double>>>() {});
        this.options = mapper.readValue(parameterMap.get("options"), new TypeReference<Map<String, Boolean>>() {});
        
        ParamSecurity.validateParams("filterIds", this.filterIdMap, new String[] {
	        "Ingredient", "Appliance", "Author", "Cuisine", "Dish Type", "Dietary Restriction", "Course"
	    });
            
        ParamSecurity.validateParams("tolerances", this.tolerances, new String[] {
            "Ingredient", "Appliance"
        });
            
        ParamSecurity.validateParams("ranges", this.ranges, new String[] {
            "Bookmarks", "Cooking Time", "Ratings", "Reviews"
        });
            
        ParamSecurity.validateParams("options", this.options, new String[] {
            "Ai Generated Description", "Ai Generated Appliances", "Ai Generated Cuisines", "Ai Generated Dish Types", "Ai Generated Cooking Minutes", "Ai Generated Courses"
        });
        
        ParamSecurity.sanitizeRange(this.ranges.get("Ratings"));
        ParamSecurity.sanitizeRange(this.ranges.get("Reviews"));
        ParamSecurity.sanitizeRange(this.ranges.get("Bookmarks"));
        ParamSecurity.sanitizeRange(this.ranges.get("Cooking Time"));
    }

    protected String getSearchTerm() { return searchTerm; }
    protected Map<String, String> getSortOptions() { return sortOptions; }
    protected Map<String, Integer[]> getFilterIdMap() { return filterIdMap; }
    protected Map<String, Integer> getTolerances() { return tolerances; }
    protected Map<String, Map<String, Double>> getRanges() { return ranges; }
    protected Map<String, Boolean> getOptions() { return options; }
    protected Map<Integer, Integer[]> getIngredientMatches() { return ingredientMatches; }
}