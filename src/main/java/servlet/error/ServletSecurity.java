package servlet.error;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletSecurity {
	
    
    public static Map<String, String> getValidatedParameters (HttpServletRequest request, String[] parameters, boolean isOptional) {
        Map<String, String> parameterMap = new HashMap<>();
        
        for (String parameter : parameters) {
            String value = request.getParameter(parameter);
            if (value == null || (!isOptional && value.trim().isEmpty())) {
                String isOptionalString = isOptional ? "optional" : "required";
            	throw new ServletRequestException(HttpServletResponse.SC_BAD_REQUEST, "Missing " + isOptionalString + " " + parameter);
            }
            parameterMap.put(parameter, value);
        }
        
        return parameterMap;
    }
    
}