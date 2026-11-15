package servlet.engagement;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;

import database.DbAccess;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import recipe.RecipeDAO;
import servlet.security.ParamSecurity;
import servlet.security.ServletParamException;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Toggle/*")
public class BookmarkToggle extends HttpServlet {
	private static final ToggleConfig CONFIG = new ToggleConfig("bookmark", "user_id", "recipe_id");

    private final ToggleDAO toggleDAO = new ToggleDAO();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer sessionUserId = (Integer) request.getAttribute("userId");
		ToggleRequest toggleRequest = new ToggleRequest(ParamSecurity.readRequestBody(request, 100), sessionUserId);
        Connection dbConnection = DbAccess.getConnection();
        
        Integer authorId = RecipeDAO.findAuthorId(toggleRequest.getTargetId(), dbConnection);

        if (authorId == null) {
            throw new ServletParamException(HttpServletResponse.SC_NOT_FOUND, "Recipe " + toggleRequest.getTargetId() + " does not exist");
        }
        if (authorId == toggleRequest.getUserId()) {
            throw new ServletParamException(HttpServletResponse.SC_FORBIDDEN, "Attempt to bookmark own recipe");
        }

        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(toggleDAO.toggle(CONFIG, toggleRequest, dbConnection)));
        }
    }
}