package servlet.engagement;

import auth.ServletSecurity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.util.HashMap;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Bookmark/Toggle")
public class BookmarkToggle extends HttpServlet {	

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		StringBuilder sb = new StringBuilder();
		try(BufferedReader br = request.getReader()){
			String line;
			while((line=br.readLine())!=null) {
				sb.append(line);
			}
		}
		
		Integer sessionUserId = ServletSecurity.getAuthenticatedUserId(request, response);
		if (sessionUserId == null) return;
		
		Connection dbConnection = ServletSecurity.getValidatedDbConnection(request, response);
	    if (dbConnection == null) return;
		
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Integer> bookmarkMap = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, Integer>>() {});
		Integer recipeId;
		if((recipeId = bookmarkMap.get("recipeId"))==null) {
			log("No recipe id provided.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
