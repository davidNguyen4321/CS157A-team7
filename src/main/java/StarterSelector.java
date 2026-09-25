import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StarterSelector {
	Connection connection;
	public StarterSelector(Connection connection) {
		this.connection = connection;
	}	
	
	public ArrayList<Integer> selectApplianceIds(String[] appliances) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("APPLIANCES", "APPLIANCE_ID", "APPLIANCE_NAME", appliances);
	}
	
	public ArrayList<Integer> selectCuisineIds(String[] cuisines) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("CUISINES", "CUISINE_ID", "CUISINE_NAME", cuisines);
	}
	
	public ArrayList<Integer> selectDietaryRestrictionIds(String[] dietaryrestrictions) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("DIETARY_RESTRICTIONS", "DIETARY_RESTRICTION_ID", "DIETARY_RESTRICTION_NAME", dietaryrestrictions);
	}
	
	public ArrayList<Integer> selectDishTypeIds(String[] dishtypes) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("DISH_TYPES", "DISH_TYPE_ID", "DISH_TYPE_NAME", dishtypes);
	}
	
	public ArrayList<Integer> selectIngredientIds(String[] ingredients) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("INGREDIENTS", "INGREDIENT_ID", "INGREDIENT_NAME", ingredients);
	}
	
	public ArrayList<Integer> selectUserIds(String[] users) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("USERS", "USER_ID", "USER_NAME", users);
	}
	
	//recipes and reviews do not have candidate keys, so they are omitted
	
	public ArrayList<Integer> selectIds(String tablename, String idname, String namename, String[] names) throws SQLException{
		Statement statement = connection.createStatement(); // grab IDs. ensure that namename is a candidate key
		String selectIds = "SELECT " + idname + " FROM " + tablename
												+ " WHERE " + namename + " IN ('" + String.join("', '", names) +"')";
		ResultSet resultSet = statement.executeQuery(selectIds);
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		while (resultSet.next()) {
			ids.add(resultSet.getInt(idname));
		}
		
		statement.close();
		resultSet.close();
		return ids;
	}
}
