package servlet.dropdown;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import util.SqlBuilder;

public class DropdownDAO {

    public ArrayList<DropdownItem> search(DropdownConfig config, DropdownRequest request, Connection dbConnection) {        
    	
    	String table = config.table();
        String idCol = config.idCol();
        String nameCol = config.nameCol();
        String imageCol = config.imageCol();
        String activeCol = config.activeCol();
        
        String searchTerm = request.getSearchTerm();
        Integer[] excludeList = request.getExcludeList();

        SqlBuilder sb = new SqlBuilder();
        sb.addString("SELECT * ")
          .addString("FROM " + table + " ")
          .addString("WHERE ");
        
        if (excludeList.length > 0) {
            sb.addString(idCol).addString(" NOT IN (");
            sb.addManyInts(excludeList);
            sb.addString(") AND ");
        }
        
        if (activeCol != null) {
            sb.addString(activeCol).addString(" = 'Active' ")
              .addString("AND ");
        }
        
        sb.addString("(").addString(nameCol).addString(" LIKE ? OR ").addString(nameCol).addString(" LIKE ?) ");
        	sb.addParam(searchTerm + "%").addParam("% " + searchTerm + "%");
        sb.addString("ORDER BY ").addString(nameCol)
          .addString(" LIMIT 5");

        try (PreparedStatement ps = sb.build(dbConnection);
        	 ResultSet rs = ps.executeQuery()) {
        	
        	ArrayList<DropdownItem> resultsList = new ArrayList<>();
            while (rs.next()) {
            	resultsList.add(new DropdownItem(
        			rs.getInt(idCol), 
        			rs.getString(nameCol), 
        			imageCol != null ? rs.getString(imageCol) : null
            	));
            }
            return resultsList;
        } 
        catch (SQLException ex) {
            throw new RuntimeException(table + " search failed: " + ex.getMessage(), ex);
        }
    }
}