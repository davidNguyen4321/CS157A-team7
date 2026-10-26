<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Auth.*" %>
<%
    String error = null;
    String username = "", email = "";
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        request.setCharacterEncoding("UTF-8");
        username = request.getParameter("username") == null ? "" : request.getParameter("username").trim();
        email = request.getParameter("email") == null ? "" : request.getParameter("email").trim();
        String password = request.getParameter("password") == null ? "" : request.getParameter("password");
        String timezone = request.getParameter("timezone");
        try {
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                error = "All fields are required.";
            } else {
                error = UserDAO.create(username, email, password, timezone);
                if (error == null) {
                    UserDAO.User u = UserDAO.authenticate(username, password);
                    request.changeSessionId();
                    session.setAttribute("userId", u.id);
                    session.setAttribute("username", u.name);
                    session.setAttribute("userImage", u.imagePath);
                    response.sendRedirect("index.jsp");
                    return;
                }
            }
        } catch (Exception ex) {
            error = "Something went wrong. Please try again.";
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create account | Recipe Genie</title>
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/auth.css">
</head>
<body>
<div class="auth-wrapper">
    <form method="post" action="create_account.jsp" class="auth-card">
        <h1>Create account</h1>
        <% if (error != null) { %><div class="auth-error"><%= Util.esc(error) %></div><% } %>
        <label for="username">Username</label>
        <input type="text" id="username" name="username" maxlength="50" value="<%= Util.esc(username) %>" required>
        <label for="email">Email</label>
        <input type="email" id="email" name="email" maxlength="254" value="<%= Util.esc(email) %>" required>
        <label for="password">Password (min 8 characters)</label>
        <input type="password" id="password" name="password" minlength="8" maxlength="128" required>
        <input type="hidden" id="timezone" name="timezone" value="UTC">
        <button type="submit" class="auth-primary">Sign up</button>
        <a href="login.jsp" class="auth-secondary">Back to log in</a>
    </form>
</div>
<script>
    // Pre-fill the user's timezone so the NOT NULL TIMEZONE column gets a real value
    try { document.getElementById("timezone").value = Intl.DateTimeFormat().resolvedOptions().timeZone; } catch (e) {}
</script>
</body>
</html>