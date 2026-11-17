package util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

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
	
	public SqlBuilder addManyInts(List<Integer> ids) {
	    for (Integer id : ids) {
	        sb.append("?, ");
	        sqlParams.add(id);
	    }
	    sb.setLength(sb.length() - 2); // remove the last comma and space
	    return this;
	}

	public SqlBuilder addManyInts(Integer[] ids) {
	    return addManyInts(Arrays.asList(ids));
	}
	
	public SqlBuilder truncate(int i) {
		sb.setLength(sb.length() - i);
		return this;
	}
	
	public PreparedStatement build(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < sqlParams.size(); i++) {
            ps.setObject(i + 1, sqlParams.get(i));
        }
        
        return ps;
    }
}