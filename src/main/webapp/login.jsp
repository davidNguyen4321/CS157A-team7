<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Auth.*" %>
<%
    if (session.getAttribute("userId") != null) { response.sendRedirect("index.jsp"); return; }

    String error = null;
    String loginValue = "";
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        request.setCharacterEncoding("UTF-8");
        loginValue = request.getParameter("login") == null ? "" : request.getParameter("login").trim();
        String password = request.getParameter("password");
        try {
            UserDAO.User u = (loginValue.isEmpty() || password == null || password.isEmpty())
                    ? null : UserDAO.authenticate(loginValue, password);
            if (u == null) {
                error = "Invalid username/email or password.";
            } else {
                request.changeSessionId(); // prevents session fixation
                session.setAttribute("userId", u.id);
                session.setAttribute("username", u.name);
                session.setAttribute("userImage", u.imagePath);
                response.sendRedirect("index.jsp");
                return;
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
    <title>Log in | Recipe Genie</title>
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/auth.css">
</head>
<body>
<div class="auth-wrapper">
    <form method="post" action="login.jsp" class="auth-card">
        <h1>Recipe Genie</h1>
        <p class="auth-sub">Log in to continue</p>
        <% if (error != null) { %><div class="auth-error"><%= Util.esc(error) %></div><% } %>
        <label for="login">Username or email</label>
        <input type="text" id="login" name="login" value="<%= Util.esc(loginValue) %>" required>
        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>
        <button type="submit" class="auth-primary">Log in</button>
        <a href="create_account.jsp" class="auth-secondary">Create account</a>
    </form>
</div>
</body>
</html>