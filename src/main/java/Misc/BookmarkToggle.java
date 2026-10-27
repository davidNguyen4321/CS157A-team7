package Misc;
import java.sql.*;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import Search.DatabaseAccess;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Bookmark/Toggle")
public class BookmarkToggle extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override //Mandatory post method for fetch requests
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//verification checks
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		StringBuilder sb = new StringBuilder();
		try(BufferedReader br = request.getReader()){
			String line;
			while((line=br.readLine())!=null) {
				sb.append(line);
			}
		}
		
		HttpSession session = request.getSession(false);
		Integer sessionUserId;
		//assumes that if the userId exists, that it is a valid id
		if(!request.isRequestedSessionIdValid() || session == null || (sessionUserId = (Integer) session.getAttribute("userId")) == null) {
			log("Invalid session.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{}");
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Integer> bookmarkMap = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, Integer>>() {});
		Integer recipeId;
		if((recipeId = bookmarkMap.get("recipeId"))==null) {
			log("No recipe id provided.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		
		Connection dbConnection = DatabaseAccess.getConnection();
		if(dbConnection==null) {
			log("No connection available.");
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().write("{}");
			return;
		}
		
		sb = new StringBuilder(); //checking if to toggle on or off
		sb.append("SELECT 1 FROM BOOKMARK ");
		sb.append("WHERE USER_ID = ? ");
		sb.append("AND RECIPE_ID = ? ");
		try(PreparedStatement queryStatement = dbConnection.prepareStatement(sb.toString())){
			queryStatement.setInt(1, sessionUserId);
			queryStatement.setInt(2, recipeId);
			try(
				ResultSet bookmark = queryStatement.executeQuery();
				PrintWriter pw = response.getWriter();
					){
        		ObjectNode node = mapper.createObjectNode();
        		sb = new StringBuilder();
				if(bookmark.next()){ // since userId and recipeId together make the primary key, it is guaranteed that there is either one or no matches
					sb.append("DELETE FROM BOOKMARK ");
					sb.append("WHERE USER_ID = ? ");
					sb.append("AND RECIPE_ID = ? ");
					node.put("bookmark", false); //deleted
				}
				else{
					sb.append("INSERT INTO BOOKMARK (USER_ID, RECIPE_ID) ");
					sb.append("VALUES (?, ?) ");
					node.put("bookmark", true); //added
				}
				try(PreparedStatement updateStatement = dbConnection.prepareStatement(sb.toString())){
					updateStatement.setInt(1, sessionUserId);
					updateStatement.setInt(2, recipeId);
					updateStatement.execute();
					sb = new StringBuilder();
					sb.append("SELECT COUNT(USER_ID) AS BOOKMARK_COUNT FROM BOOKMARK ");
					sb.append("WHERE RECIPE_ID = ? ");
					try(PreparedStatement countStatement = dbConnection.prepareStatement(sb.toString())){ //rather unsatisfying to use a third
						countStatement.setInt(1, recipeId);
						try(ResultSet countSet = countStatement.executeQuery()){
							if(countSet.next()) {
								node.put("bookmarkCount", countSet.getInt("BOOKMARK_COUNT")); //new bookmark count
							}
							else {
								node.put("bookmarkCount", "?");
							}
							pw.write(mapper.writeValueAsString(node));
						}
					}
				}
			}
		}
		catch(SQLException ex) {//error checking.
			log("Bookmark toggle failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{}");
		}
	}
}
