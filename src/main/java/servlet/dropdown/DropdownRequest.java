package servlet.dropdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class DropdownRequest {
    private String searchTerm;
    private Integer[] excludeList;

    public DropdownRequest(Map<String, String> parameterMap) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        this.searchTerm = parameterMap.get("searchTerm");
        
        String excludeArray = parameterMap.get("excludeArray");
        this.excludeList = mapper.readValue(excludeArray, Integer[].class);
    }

    public String getSearchTerm() { return searchTerm; }
    public Integer[] getExcludeList() { return excludeList; }
}