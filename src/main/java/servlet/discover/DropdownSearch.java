package servlet.discover;

import auth.ServletSecurity; 

import java.util.LinkedHashMap;
import java.util.ArrayList;
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

public abstract class DropdownSearch extends HttpServlet {

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
		    
		    Map<String, String> requiredMap = ServletSecurity.getValidatedParameters(request, response, new String[] {"searchTerm", "excludeArray"}, false);
		    if (requiredMap == null) return;
		    
		    String searchTerm = requiredMap.get("searchTerm");	
		    String excludeArray = requiredMap.get("excludeArray");
		    
		    ObjectMapper mapper = new ObjectMapper();
			Integer[] excludeList = mapper.readValue(excludeArray, Integer[].class);
			
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
                  .append(" = 'Active' AND ");
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
	                
	            	ArrayList<Map<String, Object>> resultsList = new ArrayList<>();

	                while (rs.next()) {
	                	Map<String, Object> resultMap = new LinkedHashMap<>();
	                	resultMap.put("id", rs.getInt(getIdCol()));
	                	resultMap.put("name", rs.getString(getNameCol()));

	                    String imageCol = getImageCol();
	                    if (imageCol != null) {
	                        String imagePath = rs.getString(imageCol);
	                        resultMap.put("image", imagePath);
	                    }

	                    resultsList.add(resultMap);
	                }

	                pw.write(mapper.writeValueAsString(resultsList));
	            }
	        } 
			catch (SQLException ex) {
	            log(getTable() + " search failed: " + ex.getMessage());
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            response.getWriter().write("{}");
	        }
	    }
}