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
	    "/Appliances",  new DropdownConfig("APPLIANCES", "APPLIANCE_ID", "APPLIANCE_NAME", "APPLIANCE_IMAGE_PATH", null),
	    "/Cuisines",    new DropdownConfig("CUISINES", "CUISINE_ID", "CUISINE_NAME", null, null),
	    "/Ingredients", new DropdownConfig("INGREDIENTS", "INGREDIENT_ID", "INGREDIENT_NAME", "INGREDIENT_IMAGE_PATH", null),
	    "/DishTypes",   new DropdownConfig("DISH_TYPES", "DISH_TYPE_ID", "DISH_TYPE_NAME", null, null),
	    "/Users",       new DropdownConfig("USERS", "USER_ID", "USER_NAME", "USER_IMAGE_PATH", "USER_STATUS")
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