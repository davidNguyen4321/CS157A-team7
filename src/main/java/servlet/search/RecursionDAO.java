package servlet.search;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

public class RecursionDAO {

    public HashSet<Integer> recurseUniDir(Integer[] providedElementIds, String curLevelCol, String nextLevelCol, String tableName, Connection dbConnection) {
        if(providedElementIds.length == 0) return new HashSet<Integer>(); 
        
        HashSet<Integer> closureSet = new HashSet<Integer>(Arrays.asList(providedElementIds));
        ArrayList<Integer> newIdsToRecurse = new ArrayList<Integer>(Arrays.asList(providedElementIds));
        
        while(!newIdsToRecurse.isEmpty()) {
            SqlBuilder sb = new SqlBuilder();
            sb.addString("SELECT DISTINCT ").addString(nextLevelCol)
              .addString(" FROM ").addString(tableName)
              .addString(" WHERE " + curLevelCol + " IN (").addManyInts(newIdsToRecurse).addString(")");
            
            try (PreparedStatement ps = sb.build(dbConnection);
            	 ResultSet nextLevelIds = ps.executeQuery()) {
                newIdsToRecurse = new ArrayList<Integer>();
                while(nextLevelIds.next()) {
                    Integer nextLevelId = nextLevelIds.getObject(nextLevelCol, Integer.class);
                    if (nextLevelId != null && closureSet.add(nextLevelId)) {
                        newIdsToRecurse.add(nextLevelId);
                    }
                }
            }
            catch (SQLException e) {
            	throw new RuntimeException(tableName + " recursive search failed: " + e.getMessage(), e);
            }
        }
        
        return closureSet;
    }
    
    public HashSet<Integer> recurseBiDir(Integer[] providedElementIds, String colName, String baseColName, String tableName, Connection dbConnection) {
        HashSet<Integer> recurseUp = recurseUniDir(providedElementIds, colName, baseColName, tableName, dbConnection);
        HashSet<Integer> recurseDown = recurseUniDir(providedElementIds, baseColName, colName, tableName, dbConnection);
        recurseUp.addAll(recurseDown);
        return recurseUp;
    }
}