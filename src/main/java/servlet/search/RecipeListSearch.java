package servlet.search;

import java.util.Map;

import database.DbAccess;

import java.sql.Connection;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ParamSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Recipes/Search")
public class RecipeListSearch extends HttpServlet {
    private final RecipeListDAO recipeListDAO = new RecipeListDAO();
	private static final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        String[] parameters = {"searchTerm", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
        Map<String, String> parameterMap = ParamSecurity.getParams(request, parameters);
        
        Integer sessionUserId = (Integer) request.getAttribute("userId");
        String sessionUserTimezone = (String) request.getAttribute("userTimezone");

        RecipeListRequest requestInfo = new RecipeListRequest(parameterMap, sessionUserId, sessionUserTimezone);
        Connection dbConnection = DbAccess.getConnection();
        
        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(recipeListDAO.search(requestInfo, dbConnection)));
        }
    }
}