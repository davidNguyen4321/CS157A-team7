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
    
    public static <V> void validateParams(String name, Map<String, V> map, String[] requiredKeys) {
        for (String key : requiredKeys) {
            if (!map.containsKey(key) || map.get(key) == null) {
                throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Missing required key " + key + " from " + name);
            }
        }
    }
    
    public static void validateParams(Map<String, String> parameterMap, String[] requiredParams) {
        if (requiredParams == null) return;

        for (String param : requiredParams) {
            if (!parameterMap.containsKey(param) || parameterMap.get(param) == null) {
                throw new ServletParamException(
                    HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing required parameter: " + param
                );
            }
        }
    }

    public static String getCleanSearch(String rawString) {
    	return rawString.replace("\\", "").replaceAll("[%_]", "").trim();
    }
    
    public static void sanitizeRange(Map<String, Double> range) {
    	Double min = range.get("min");
    	Double max = range.get("max");
    	        
        min = (min == null || !Double.isFinite(min)) ? Integer.MIN_VALUE : min;
        max = (max == null || !Double.isFinite(min)) ? Integer.MAX_VALUE : max;
        
        range.put("min", min);
        range.put("max", max);
    }
}