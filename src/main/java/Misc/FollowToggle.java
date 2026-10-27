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

@WebServlet("/Follow/Toggle")
public class FollowToggle extends HttpServlet {
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
		HashMap<String, Integer> followMap = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, Integer>>() {});
		Integer followingId;
		if((followingId = followMap.get("followingId"))==null) {
			log("No following id provided.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		
		if(sessionUserId.equals(followingId)){
			log("Session user is the same as the following user.");
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
		sb.append("SELECT FOLLOWER_ID, FOLLOWING_ID FROM FOLLOW ");
		sb.append("WHERE FOLLOWER_ID = ? ");
		sb.append("AND FOLLOWING_ID = ? ");
		try(PreparedStatement queryStatement = dbConnection.prepareStatement(sb.toString())){
			queryStatement.setInt(1, sessionUserId);
			queryStatement.setInt(2, followingId);
			try(
				ResultSet bookmark = queryStatement.executeQuery();
				PrintWriter pw = response.getWriter();
					){
        		ObjectNode node = mapper.createObjectNode();
        		sb = new StringBuilder();
				if(bookmark.next()){ // since sessionUserId and followingId together make the primary key, it is guaranteed that there is either one or no matches
					sb.append("DELETE FROM FOLLOW ");
					sb.append("WHERE FOLLOWER_ID = ? ");
					sb.append("AND FOLLOWING_ID = ? ");
					node.put("follow", false); //deleted
				}
				else{
					sb.append("INSERT INTO FOLLOW (FOLLOWER_ID, FOLLOWING_ID) ");
					sb.append("VALUES (?, ?) ");
					node.put("follow", true); //added
				}
				try(PreparedStatement updateStatement = dbConnection.prepareStatement(sb.toString())){
					updateStatement.setInt(1, sessionUserId);
					updateStatement.setInt(2, followingId);
					updateStatement.execute();
					pw.write(mapper.writeValueAsString(node));
				}
			}
		}
		catch(SQLException ex) {//error checking.
			log("Follow toggle failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{}");
		}
	}
}
