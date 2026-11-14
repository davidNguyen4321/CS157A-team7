package servlet.search;

import java.sql.Connection;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.security.ParamSecurity;
import database.DbAccess;

@WebServlet("/Recipes/Search")
public class RecipeSearch extends HttpServlet {
    
    private final RecipeSearchDAO recipeSearchDAO = new RecipeSearchDAO();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection dbConnection = DbAccess.getConnection();
        
        int sessionUserId = (Integer) request.getAttribute("userId");
        String[] parameters = {"searchTerm", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
        Map<String, String> parameterMap = ParamSecurity.getParams(request, parameters);
        
        RecipeSearchRequest requestInfo = new RecipeSearchRequest(parameterMap);
        
        try {
            String jsonResponse = recipeSearchDAO.search(requestInfo, dbConnection, sessionUserId);
            try (PrintWriter pw = response.getWriter()) {
                pw.write(jsonResponse);
            }
        }
        catch(Exception ex) {
            log("Recipe search failed: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{}");
        }
    }
}