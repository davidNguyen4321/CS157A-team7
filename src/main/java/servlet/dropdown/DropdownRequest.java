package servlet.dropdown;

import com.fasterxml.jackson.databind.ObjectMapper;

import servlet.security.ParamSecurity;

import java.io.IOException;
import java.util.Map;

public class DropdownRequest {
    private String searchTerm;
    private Integer[] excludeList;

    public DropdownRequest(Map<String, String> parameterMap) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        this.searchTerm = ParamSecurity.getCleanSearch(parameterMap.get("searchTerm"));
        this.excludeList = mapper.readValue(parameterMap.get("excludeList"), Integer[].class);
    }

    public String getSearchTerm() { return searchTerm; }
    public Integer[] getExcludeList() { return excludeList; }
}