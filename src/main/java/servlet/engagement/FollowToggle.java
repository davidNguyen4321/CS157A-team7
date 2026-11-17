package servlet.engagement;

import auth.UserDAO;

import database.DbAccess;

import java.sql.Connection;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import servlet.security.ParamSecurity;
import servlet.security.ServletParamException;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Toggle/Follow")
public class FollowToggle extends HttpServlet {
    private static final ToggleConfig CONFIG = new ToggleConfig("follow", "follower_id", "following_id");
    
    private final ToggleDAO toggleDAO = new ToggleDAO();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer sessionUserId = (Integer) request.getAttribute("userId");
        ToggleRequest toggleRequest = new ToggleRequest(ParamSecurity.readRequestBody(request, 100), sessionUserId);
        Connection dbConnection = DbAccess.getConnection();

        if (toggleRequest.getTargetId() == toggleRequest.getUserId()) {
            throw new ServletParamException(HttpServletResponse.SC_FORBIDDEN, "Attempt to follow self");
        }
        if (!UserDAO.userExists(toggleRequest.getTargetId(), dbConnection)) {
            throw new ServletParamException(HttpServletResponse.SC_NOT_FOUND, "User " + toggleRequest.getTargetId() + " does not exist");
        }

        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(toggleDAO.toggle(CONFIG, toggleRequest, dbConnection)));
        }
    }
}