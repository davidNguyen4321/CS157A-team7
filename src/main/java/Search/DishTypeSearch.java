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

@WebServlet("/DishTypes/Search")
public class DishTypeSearch extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override //Mandatory get method for fetch requests
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//verification checks
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String searchTerm = request.getParameter("searchTerm");
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
		
		// string building
		Integer[] excludeList = mapper.readValue(excludeString, Integer[].class);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISH_TYPE_ID, DISH_TYPE_NAME FROM DISH_TYPES WHERE ");
		if(excludeList.length>0) {
			sb.append("NOT (DISH_TYPE_ID IN (");
			for(int i = 0; i < excludeList.length; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")) AND");
		}
		sb.append(" (DISH_TYPE_NAME LIKE ? OR DISH_TYPE_NAME LIKE ?) ORDER BY DISH_TYPE_NAME LIMIT 5");
		
		//connection
		try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString());){
			for(int i = 0; i < excludeList.length; i++) {
				statement.setInt(i+1, excludeList[i]);
			}
    		statement.setString(excludeList.length+1, searchTerm + "%");
    		statement.setString(excludeList.length+2, "% "+ searchTerm + "%");
    		try(
    		ResultSet ingredients = statement.executeQuery();
    	    PrintWriter pw = response.getWriter();
    		){
        		ObjectNode outerNode = mapper.createObjectNode();
        		Integer counter = 0;
        		
        		// formatting data into json
        		while(ingredients.next()) {
        			Integer ingredientId = ingredients.getInt("dish_type_id");
        			String ingredientName = ingredients.getString("dish_type_name");
        			ObjectNode innerNode = mapper.createObjectNode();
        			
        			innerNode.put("id",ingredientId);
        			innerNode.put("name",ingredientName);
        			outerNode.put(counter.toString(), mapper.writeValueAsString(innerNode));
        			counter++;
        		}
        		
        		//writing response
        		pw.write(mapper.writeValueAsString(outerNode));
    		}
		}
		catch(SQLException ex) {//error checking
			log("Dish type search failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().write("{}");
		}
	}
}
