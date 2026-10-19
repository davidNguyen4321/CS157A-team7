package Search;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Users/Search")
public class UserSearch extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override //Mandatory get method for fetch requests
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//verification checks
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String searchTerm = request.getParameter("searchValue");
		String excludeString = request.getParameter("excludeString");
		ObjectMapper mapper = new ObjectMapper();
		Connection dbConnection = DatabaseAccess.getConnection();
		if(searchTerm==null || searchTerm.trim().isEmpty()){
			log("No search term provided.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		
		if(dbConnection==null) {
			log("No dbConnection available.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		

		try {
			//sql connection
			Integer[] excludeList = mapper.readValue(excludeString, Integer[].class);
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT USER_ID, USER_NAME, USER_IMAGE_PATH FROM USERS "
					+ "WHERE USER_STATUS = 'Active' ");
			if(excludeList.length>0) {
				sb.append("AND NOT (USER_ID IN (");
				for(int i = 0; i < excludeList.length; i++) {
					sb.append("?, ");
				}
				sb.setLength(sb.length()-2);//remove the last comma and space
				sb.append(")) ");
			}
			sb.append("AND (USER_NAME LIKE ? OR USER_NAME LIKE ?) LIMIT 3");
			PreparedStatement statement = dbConnection.prepareStatement(sb.toString());
			for(int i = 0; i < excludeList.length; i++) {
				statement.setInt(i+1, excludeList[i]);
			}
    		statement.setString(excludeList.length+1, searchTerm + "%");
    		statement.setString(excludeList.length+2, "% "+ searchTerm + "%");
    		ResultSet users = statement.executeQuery();
    		ObjectNode outerNode = mapper.createObjectNode();
    		Integer counter = 0;
    		
    		// formatting data into json
    		while(users.next()) {
    			Integer userId = users.getInt("user_id");
    			String userName = users.getString("user_name");
    			String userImage = users.getString("user_image_path");
    			ObjectNode innerNode = mapper.createObjectNode();
    			
    			innerNode.put("id",userId);
    			innerNode.put("name",userName);
    			innerNode.put("image",userImage);
    			outerNode.put(counter.toString(), mapper.writeValueAsString(innerNode));
    			counter++;
    		}
    		
    		//writing response
    		PrintWriter pw = response.getWriter();
    		pw.write(mapper.writeValueAsString(outerNode));
    		pw.close();
    		users.close();
    		statement.close();
		}
		catch(SQLException ex) {//error checking
			log("User search failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().write("{}");
		}
	}
}
