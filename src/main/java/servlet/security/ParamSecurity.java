package servlet.security;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ParamSecurity {
	
    public static Map<String, String> getParams(HttpServletRequest request, String[] parameters) {
        Map<String, String> parameterMap = new HashMap<>();
        
        for (String parameter : parameters) {
            String value = request.getParameter(parameter);
            if (value == null) {
            	throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Missing " + parameter);
            }
            if (value.length() > 1000 || (parameter == "searchTerm" && value.length() > 100)) {
            	throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, parameter + " exceed reasonable length");
            }
            parameterMap.put(parameter, value);
        }
        
        return parameterMap;
    }
    
    public static String getCleanSearch(String rawString) {
    	return rawString.replace("\\", "").replaceAll("[%_]", "").trim();
    }
}