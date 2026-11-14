package servlet.security;

import util.Loggers;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

@WebFilter(urlPatterns = {"/Bookmark/*", "/Follow/*", "/Recipes/*", "/Dropdown/*"})
public class ErrorHandlingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest genericRequest, ServletResponse genericResponse, FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest request = (HttpServletRequest) genericRequest;
        HttpServletResponse response = (HttpServletResponse) genericResponse;
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    	
    	try {
    		HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                throw new ServletParamException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access attempt");
            }
            
            int userId = (Integer) session.getAttribute("userId");
    		request.setAttribute("userId", userId);
    		chain.doFilter(request, response); 
    	}
    	catch (ServletParamException e) { 
            String message = describe(request) + " => " + e.getMessage();
            Loggers.SECURITY.warning(message);
            sendJsonError(response, e.getStatus());
        }
    	catch (JsonProcessingException e) { // Malformed JSON which browser should never send
            String message = describe(request) + " => " + "Invalid JSON: " + e.getOriginalMessage();
    		Loggers.SECURITY.warning(message);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST);
        }
    	catch (Exception e) { // SQL failures, bugs, etc
            String message = describe(request) + " => " + "Unexpected error: " + e.getMessage();
    		Loggers.OPERATION.severe(message);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private String describe(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI() + " from " + request.getRemoteAddr();
    }
    
    private void sendJsonError(HttpServletResponse response, int status) throws IOException {
        if (response.isCommitted()) return; // too late to change anything
        response.resetBuffer();
        response.setStatus(status);
        response.getWriter().write("{}");
    }
}