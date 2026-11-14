package servlet.search;

import java.sql.Connection;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.security.ParamSecurity;
import database.DbAccess;

@WebServlet("/Recipes/Search")
public class RecipeListSearch extends HttpServlet {
    private final RecipeListDAO recipeListDAO = new RecipeListDAO();
	private static final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        String[] parameters = {"searchTerm", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
        Map<String, String> parameterMap = ParamSecurity.getParams(request, parameters);
        RecipeListRequest requestInfo = new RecipeListRequest(parameterMap);
        Connection dbConnection = DbAccess.getConnection();
        int sessionUserId = (Integer) request.getAttribute("userId");
        
        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(recipeListDAO.search(requestInfo, dbConnection, sessionUserId)));
        }
    }
}