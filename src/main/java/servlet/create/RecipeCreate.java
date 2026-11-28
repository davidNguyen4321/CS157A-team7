package servlet.create;

import database.DbAccess;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.security.ParamSecurity;

@WebServlet("/Recipes/Create")
public class RecipeCreate extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int sessionUserId = (Integer) request.getAttribute("userId");

        RecipeCreateRequest requestInfo = new RecipeCreateRequest(ParamSecurity.readRequestBody(request, 30000));

        int recipeId = RecipeCreateDAO.create(requestInfo, sessionUserId, DbAccess.getConnection());
        response.getWriter().write(mapper.writeValueAsString(Map.of("recipeId", recipeId)));
    }
}