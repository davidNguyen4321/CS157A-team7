package servlet.dropdown;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

public class DropdownDAO {

    public ArrayList<DropdownItem> search(DropdownConfig config, DropdownRequest requestInfo, Connection dbConnection) {
    	String table = config.table();
        String idCol = config.idCol();
        String nameCol = config.nameCol();
        String imageCol = config.imageCol();
        String activeCol = config.activeCol();
        
        String searchTerm = requestInfo.getSearchTerm();
        Integer[] excludeList = requestInfo.getExcludeList();

        SqlBuilder sb = new SqlBuilder();
        sb.addString("SELECT " + idCol + ", " + nameCol); if (imageCol != null) sb.addString(", " + imageCol);
        sb.addString(" FROM " + table)
          .addString(" WHERE ");
        
        if (excludeList.length > 0) {
            sb.addString(idCol + " NOT IN (").addManyInts(excludeList).addString(")")
              .addString(" AND ");
        }
        
        if (activeCol != null) {
            sb.addString(activeCol + " = 'Active'")
              .addString(" AND ");
        }
        
        sb.addString("(" + nameCol + " LIKE ? OR " + nameCol + " LIKE ?)")
          .addParam(searchTerm + "%").addParam("% " + searchTerm + "%");
        
        sb.addString(" ORDER BY " + nameCol)
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
        catch (SQLException e) {
            throw new RuntimeException(table + " search failed: " + e.getMessage(), e);
        }
    }
}