package Recipe;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Bookmark/Add")
public class BookmarkAdd extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override //Mandatory get method for fetch requests
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
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Integer> bookmark = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, Integer>>() {});
		System.out.println(bookmark.get("userId"));
		System.out.println(bookmark.get("recipeId"));
		Connection dbConnection = DatabaseAccess.getConnection();
		if(dbConnection==null) {
			log("No dbConnection available.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{}");
			return;
		}
		log("Not implemented");
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		response.getWriter().write("{}");
	}
}
