package servlet.discover; 

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class RecipeSearchRequest {
    private String searchTerm;
    private Map<String, Map<String, String>> sortOptions;
    private Map<String, Integer[]> filterIdMap;
    private Map<String, Integer> tolerances;
    private Map<String, Map<String, Integer>> ranges;
    private Map<String, Boolean> options;
    

    public RecipeSearchRequest(Map<String, String> parameterMap) throws IOException {        
        ObjectMapper mapper = new ObjectMapper();
        this.searchTerm = parameterMap.get("searchTerm").trim();
        
        this.sortOptions = mapper.readValue(
        		parameterMap.get("sortOptions"), 
        		new TypeReference<Map<String, Map<String, String>>>() {}
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
    protected Map<String, Map<String, String>> getSortOptions() { return sortOptions; }
    protected Map<String, Integer[]> getFilterIdMap() { return filterIdMap; }
    protected Map<String, Integer> getTolerances() { return tolerances; }
    protected Map<String, Map<String, Integer>> getRanges() { return ranges; }
    protected Map<String, Boolean> getOptions() { return options; }
}