package servlet.create;

import database.DbAccess;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ParamSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Recipes/Suggest")
public class RecipeSuggest extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RecipeSuggestRequest requestInfo = new RecipeSuggestRequest(ParamSecurity.readRequestBody(request, 30000));

        String json;
        try {
            json = mapper.writeValueAsString(RecipeAiFiller.fill(requestInfo, DbAccess.getConnection()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) { // Gemini / network failures
            throw new IOException("Recipe suggestion failed: " + e.getMessage(), e);
        }

        response.getWriter().write(json);
    }
}