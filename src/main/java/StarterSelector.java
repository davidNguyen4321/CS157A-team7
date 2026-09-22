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
	
	public ArrayList<Integer> selectDietaryRestrictionIds(String[] dietaryrestrictions) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("DIETARY_RESTRICTIONS", "ID_DIETARY_RESTRICTION", "DIETARY_RESTRICTION_NAME", dietaryrestrictions);
	}
	
	public ArrayList<Integer> selectIngredientIds(String[] ingredients) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("INGREDIENTS", "ID_INGREDIENT", "INGREDIENT_NAME", ingredients);
	}
	
	public ArrayList<Integer> selectCookingApplianceIds(String[] cookingappliances) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("COOKING_APPLIANCES", "ID_COOKING_APPLIANCE", "COOKING_APPLIANCE_NAME", cookingappliances);
	}
	
	public ArrayList<Integer> selectImageIds(String[] images) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("IMAGES", "ID_IMAGE", "IMAGE_PATH", images);
	}
	
	public ArrayList<Integer> selectUserIds(String[] users) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("USERS", "ID_USER", "USER_NAME", users);
	}
	
	public ArrayList<Integer> selectNationalityIds(String[] nationalities) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("NATIONALITIES", "ID_NATIONALITY", "NATIONALITY_NAME", nationalities);
	}
	
	public ArrayList<Integer> selectDishTypeIds(String[] dishtypes) throws SQLException{ //Automatically formats the request to the selector
		return selectIds("DISH_TYPES", "ID_DISH_TYPE", "DISH_TYPE_NAME", dishtypes);
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
