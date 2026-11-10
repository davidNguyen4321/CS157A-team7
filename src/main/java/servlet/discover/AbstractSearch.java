package servlet.discover;

import auth.ServletSecurity; 

import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractSearch extends HttpServlet {

		protected abstract String getTable();
	    protected abstract String getIdCol();
	    protected abstract String getNameCol();
	    protected String getImageCol() {return null;};
	    protected String getActiveCol() {return null;};
		
		@Override
		public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
						
			if(ServletSecurity.getAuthenticatedUserId(request, response) == null) return;
			
		    Connection dbConnection = ServletSecurity.getValidatedDbConnection(request, response);
		    if (dbConnection == null) return;
		    
		    Map<String, String> requiredMap = ServletSecurity.getValidatedParameters(request, response, new String[] {"searchTerm", "imageBoolean", "excludeString"}, false);
		    if (requiredMap == null) return;
		    
		    String searchTerm = requiredMap.get("searchTerm");	
		    String excludeString = requiredMap.get("excludeString");
		    boolean imageBoolean = Boolean.parseBoolean(requiredMap.get("imageBoolean"));
		    
		    ObjectMapper mapper = new ObjectMapper();
			Integer[] excludeList = mapper.readValue(excludeString, Integer[].class);
			
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ")
			  .append(getTable())
			  .append(" WHERE ");
			
			if(excludeList.length > 0) {
				sb.append(getIdCol())
				  .append(" NOT IN (");
				for (int i = 0; i < excludeList.length; i++) sb.append("?, ");
				sb.setLength(sb.length() - 2); // remove last comma and space
				sb.append(") AND ");
			}
			
			String activeCol = getActiveCol();
			if (activeCol != null) {
                sb.append(activeCol)
                  .append(" = 1 AND ");
            }
			
			sb.append("(")
			  .append(getNameCol())
			  .append(" LIKE ? OR ")
			  .append(getNameCol())
			  .append(" LIKE ?) ORDER BY ")
			  .append(getNameCol())
			  .append(" LIMIT 5");
			
			try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString())) {
	            int paramIndex = 1;
	            for (Integer excludeId : excludeList) {
	                statement.setInt(paramIndex++, excludeId);
	            }
	            statement.setString(paramIndex++, searchTerm + "%");
	            statement.setString(paramIndex++, "% " + searchTerm + "%");

	            try (ResultSet rs = statement.executeQuery();
	                 PrintWriter pw = response.getWriter()) {
	                
	                ObjectNode outerNode = mapper.createObjectNode();
	                int counter = 0;

	                while (rs.next()) {
	                    Integer id = rs.getInt(getIdCol());
	                    String name = rs.getString(getNameCol());
	                    ObjectNode innerNode = mapper.createObjectNode();

	                    innerNode.put("id", id);
	                    innerNode.put("name", name);

	                    if (imageBoolean) {
	                        String imagePath = rs.getString(getImageCol());
	                        innerNode.put("image", imagePath);
	                    }

	                    outerNode.put(String.valueOf(counter), mapper.writeValueAsString(innerNode));
	                    counter++;
	                }

	                pw.write(mapper.writeValueAsString(outerNode));
	            }
	        } 
			catch (SQLException ex) {
	            log(getTable() + " search failed: " + ex.getMessage());
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            response.getWriter().write("{}");
	        }
	    }
}