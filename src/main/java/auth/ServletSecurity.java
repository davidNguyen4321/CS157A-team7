package auth;

import database.DbAccess;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;

public class ServletSecurity {
	private static final Logger LOGGER = Logger.getLogger("SecurityLog");

	public static Integer getAuthenticatedUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        
        if (request.isRequestedSessionIdValid() && session != null) {
        	Integer userId = (Integer) session.getAttribute("userId");
        	if (userId != null) return userId;
        }
        
        LOGGER.warning("Unauthorized access attempt for URI: " + request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{}");
        return null;
    }

    public static Connection getValidatedDbConnection(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection dbConnection = DbAccess.getConnection();
        
        if (dbConnection == null) {
            LOGGER.severe("CRITICAL: Failed to retrieve database connection for URI: " + request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{}");
            return null;
        }
        
        return dbConnection;
    }
    
    public static Map<String, String> getValidatedParameters(
            HttpServletRequest request, 
            HttpServletResponse response, 
            String[] parameters, boolean isOptional) throws IOException {
        
        Map<String, String> parameterMap = new HashMap<>();
        String isOptionalString = isOptional ? "optional" : "required";
        
        for (String parameter : parameters) {
            String value = request.getParameter(parameter);
            if (value == null || (!isOptional && value.trim().isEmpty())) {
                LOGGER.warning("Missing " + isOptionalString + " " + parameter);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{}");
                return null;
            }
            parameterMap.put(parameter, value);
        }
        
        return parameterMap;
    }
    
}