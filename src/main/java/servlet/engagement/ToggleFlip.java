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
import servlet.security.ParamSecurity;
import servlet.security.ServletParamException;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Toggle/*")
public class ToggleFlip extends HttpServlet {
    private static final Map<String, ToggleConfig> CONFIGS = Map.of(
        "/Bookmark", new ToggleConfig("bookmark", "user_id", "recipe_id"),
        "/Follow",   new ToggleConfig("follow", "follower_id", "following_id")
    );

    private final ToggleDAO toggleDAO = new ToggleDAO();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        ToggleConfig config = (pathInfo == null) ? null : CONFIGS.get(pathInfo);

		if (config == null) throw new ServletParamException(HttpServletResponse.SC_NOT_FOUND, "No toggle configured for " + pathInfo);

        Integer sessionUserId = (Integer) request.getAttribute("userId");
		ToggleRequest toggleRequest = new ToggleRequest(ParamSecurity.readRequestBody(request, 100), sessionUserId);
        Connection dbConnection = DbAccess.getConnection();

        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(toggleDAO.toggle(config, toggleRequest, dbConnection)));
        }
    }
}