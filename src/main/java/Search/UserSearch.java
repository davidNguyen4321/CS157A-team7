package Search;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Users/Search")
public class UserSearch extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Connection dbConnection = DatabaseAccess.getConnection();
		if(dbConnection==null) {
			log("No dbConnection available.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		
		String searchTerm = request.getParameter("searchValue");
		if(searchTerm==null || searchTerm.trim().isEmpty()){
			log("No search term provided.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		
		String sql = "SELECT USER_ID, USER_NAME FROM USERS "
				+ "WHERE USER_STATUS = 'Active' "
				+ "AND (USER_NAME LIKE ? OR USER_NAME LIKE ?) LIMIT 3";
		try {
		PreparedStatement statement = dbConnection.prepareStatement(sql);
    		statement.setString(1, searchTerm + "%");
    		statement.setString(2, "% "+ searchTerm + "%");
    		ResultSet users = statement.executeQuery();
    		
    		StringBuilder sb = new StringBuilder();
    		sb.append("[");
    		while(users.next()) {
    			String userName = users.getString("user_name");
    			Integer userId = users.getInt("user_id");
    			sb.append("{\""+userId+"\":\""+userName+"\"}, ");
    		}
    		if(sb.length()>1) {
    			sb.setLength(sb.length()-2); //remove the last comma and space
    		}
    		sb.append("]");
    		
    		PrintWriter pw = response.getWriter();
    		pw.write(sb.toString());
    		pw.close();
    		users.close();
    		statement.close();
		}
		catch(SQLException ex) {
			log("User search failed." + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().write("{}");
		}
	}
	
}
