package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


import java.sql.SQLException;

public class SqlBuilder {
    private StringBuilder sb;
    private List<Object> sqlParams = new ArrayList<>();
	
	public SqlBuilder() {
		this.sb = new StringBuilder();
		this.sqlParams = new ArrayList<>();
	}
	
	public SqlBuilder addString(String s) {
		sb.append(s);
		return this;
	}
	
	public SqlBuilder addParam(Object o) {
		sqlParams.add(o);
		return this;
	}
	
	public void addManyInts(List<Integer> ids) {
	    for (Integer id : ids) {
	        sb.append("?, ");
	        sqlParams.add(id);
	    }
	    sb.setLength(sb.length() - 2); // remove the last comma and space
	}

	public void addManyInts(Integer[] ids) {
	    addManyInts(Arrays.asList(ids));
	}
	
	public void truncate(int i) {
		sb.setLength(sb.length() - i);
	}
	
	public PreparedStatement build(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < sqlParams.size(); i++) {
            ps.setObject(i + 1, sqlParams.get(i));
        }
        
        return ps;
    }
}