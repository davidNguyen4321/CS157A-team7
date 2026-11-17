package servlet.engagement;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToggleDAO {

    public ToggleResponse toggle(ToggleConfig config, ToggleRequest requestInfo, Connection dbConnection) {
    	try {
	    	boolean exists = checkExists(config, requestInfo, dbConnection);
	    	
	    	boolean isActive;
	    	try {
	    	    isActive = exists 
	    	        ? deleteRecord(config, requestInfo, dbConnection) 
	    	        : insertRecord(config, requestInfo, dbConnection);
	    	} catch (java.sql.SQLIntegrityConstraintViolationException e) {
	    	    isActive = true; // Handled concurrently by another tab
	    	}
	    	
	    	int count = getCount(config, requestInfo, dbConnection);
    	
	    	return new ToggleResponse(isActive, count);
    	}
    	catch (SQLException e) {
    		throw new RuntimeException("Toggle flip failed: " + e.getMessage(), e);
    	}   
    }
    
    private boolean checkExists(ToggleConfig config, ToggleRequest request, Connection dbConnection) throws SQLException {
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("SELECT 1 FROM " + config.table())
          .addString(" WHERE " + config.userCol() + " = ? AND " + config.targetCol() + " = ?")
          .addParam(request.getUserId()).addParam(request.getTargetId());
          
        try (PreparedStatement ps = sb.build(dbConnection);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
    
    private boolean deleteRecord(ToggleConfig config, ToggleRequest request, Connection dbConnection) throws SQLException {
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("DELETE FROM " + config.table())
          .addString(" WHERE " + config.userCol() + " = ? AND " + config.targetCol() + " = ?")
          .addParam(request.getUserId()).addParam(request.getTargetId());

        try (PreparedStatement ps = sb.build(dbConnection)) {
            ps.execute();
        }
        
        return false;
    }
    
    private boolean insertRecord(ToggleConfig config, ToggleRequest request, Connection dbConnection) throws SQLException {
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("INSERT INTO " + config.table() + "(" + config.userCol() + ", " + config.targetCol() + ")" + " VALUES")
          .addString(" (?, ?)")
          .addParam(request.getUserId()).addParam(request.getTargetId());

        try (PreparedStatement ps = sb.build(dbConnection)) {
            ps.execute();
        }
        
        return true;
    }
    
    private int getCount(ToggleConfig config, ToggleRequest request, Connection dbConnection) throws SQLException {
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("SELECT COUNT(*) AS count")
          .addString(" FROM " + config.table() + " t JOIN users u ON t." + config.userCol() + " = u.user_id")
          .addString(" WHERE t." + config.targetCol() + " = ? AND u.user_status = 'Active'")
          .addParam(request.getTargetId());

        int count = 0;
        try (PreparedStatement ps = sb.build(dbConnection);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) count = rs.getInt("count");
        }
        
        return count;
    }
}