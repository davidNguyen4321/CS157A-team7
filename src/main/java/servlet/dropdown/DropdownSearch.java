package servlet.dropdown;

import java.io.IOException;
import java.io.PrintWriter;
import database.DbAccess;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.security.ParamSecurity;
import servlet.security.ServletParamException;
import jakarta.servlet.annotation.WebServlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;

@WebServlet("/Dropdown/*")
public class DropdownSearch extends HttpServlet {
    private static final Map<String, DropdownConfig> CONFIGS = Map.of(
	    "/Appliances",  new DropdownConfig("appliances", "appliance_id", "appliance_name", "appliance_image_path", null),
	    "/Cuisines",    new DropdownConfig("cuisines", "cuisine_id", "cuisine_name", null, null),
	    "/Ingredients", new DropdownConfig("ingredients", "ingredient_id", "ingredient_name", "ingredient_image_path", null),
	    "/DishTypes",   new DropdownConfig("dish_types", "dish_type_id", "dish_type_name", null, null),
	    "/Users",       new DropdownConfig("users", "user_id", "user_name", "user_image_path", "user_status")
	);
		    
	private final DropdownDAO dropdownDAO = new DropdownDAO();
	private static final ObjectMapper mapper = new ObjectMapper();
		
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String pathInfo = request.getPathInfo();
		DropdownConfig config = (pathInfo == null) ? null : CONFIGS.get(pathInfo);

		if (config == null) throw new ServletParamException(HttpServletResponse.SC_NOT_FOUND, "No dropdown configured for " + pathInfo);
		
		DropdownRequest dropdownRequest = new DropdownRequest(ParamSecurity.getParams(request, new String[] {"searchTerm", "excludeArray"}));
		Connection dbConnection = DbAccess.getConnection();
            
        try (PrintWriter pw = response.getWriter()) {
            pw.write(mapper.writeValueAsString(dropdownDAO.search(config, dropdownRequest, dbConnection)));
        }
    }
}