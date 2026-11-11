package servlet.discover; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecipeSearchRequest {
    private String searchTerm;
    private Map<String, Map<String, String>> sortOptions;
    private Map<String, Integer[]> filterIdMap;
    private String[] courses;
    private Map<String, Integer> tolerances;
    private Map<String, Map<String, Integer>> ranges;
    private Map<String, Boolean> options;
    
    // 1. Direct Constructor
    public RecipeSearchRequest(
            String searchTerm,
            Map<String, Map<String, String>> sortOptions,
            Map<String, Integer[]> filterIdMap,
            String[] courses,
            Map<String, Integer> tolerances,
            Map<String, Map<String, Integer>> ranges,
            Map<String, Boolean> options
    ) {
        this.searchTerm = searchTerm;
        this.sortOptions = sortOptions;
        this.filterIdMap = filterIdMap;
        this.courses = courses;
        this.tolerances = tolerances;
        this.ranges = ranges;
        this.options = options;
    }

    // 2. Optional: HTTP Request Parser Constructor (moves readRequest logic here!)
    public RecipeSearchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parameters = {"searchTerm", "sortOrder", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
        Map<String, String> parameterMap = auth.ServletSecurity.getValidatedParameters(request, response, parameters, true);
        if (parameterMap == null) return; 
        
        ObjectMapper mapper = new ObjectMapper();
        this.searchTerm = parameterMap.get("searchTerm").trim();
        
        this.sortOptions = mapper.readValue(
                parameterMap.get("sortOptions"),
                new TypeReference<Map<String, Map<String, String>>>() {});
        
        HashMap<String, String[]> filterIds = mapper.readValue(
                parameterMap.get("filterIds"),
                new TypeReference<HashMap<String, String[]>>() {});
        
        this.filterIdMap = new HashMap<>();
        this.courses = new String[]{}; // placeholder array
        
        for (HashMap.Entry<String, String[]> set : filterIds.entrySet()) {
            String[] stringArray = set.getValue();
            if(set.getKey().equals("Course")) {
                this.courses = stringArray;
            } else {
                Integer[] idArray = new Integer[stringArray.length];
                for(int i = 0; i < stringArray.length; i++) {
                    idArray[i] = Integer.parseInt(stringArray[i]);
                }
                this.filterIdMap.put(set.getKey(), idArray);
            }
        }
        
        this.tolerances = mapper.readValue(
                parameterMap.get("tolerances"),
                new TypeReference<HashMap<String, Integer>>() {}
        );
        
        this.ranges = mapper.readValue(
                parameterMap.get("ranges"),
                new TypeReference<Map<String, Map<String, Integer>>>() {}
        );
        
        this.options = mapper.readValue(
                parameterMap.get("options"),
                new TypeReference<HashMap<String, Boolean>>() {}
        );
    }

    // Getters
    public String getSearchTerm() { return searchTerm; }
    public Map<String, Map<String, String>> getSortOptions() { return sortOptions; }
    public Map<String, Integer[]> getfilterIdMap() { return filterIdMap; }
    public String[] getCourses() { return courses; }
    public Map<String, Integer> getTolerances() { return tolerances; }
    public Map<String, Map<String, Integer>> getRanges() { return ranges; }
    public Map<String, Boolean> getOptions() { return options; }
}