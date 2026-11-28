package servlet.share;

import servlet.dropdown.DropdownItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecipeSuggestDAO {

    public Map<Integer, DropdownItem> options(String table, String idCol, String nameCol, String imageCol, Connection dbConnection) {
        Map<Integer, DropdownItem> map = new LinkedHashMap<>();
        String sql = "SELECT " + idCol + ", " + nameCol + (imageCol == null ? "" : ", " + imageCol)
                   + " FROM " + table + " ORDER BY " + nameCol;
        try (PreparedStatement ps = dbConnection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt(1), new DropdownItem(rs.getInt(1), rs.getString(2), imageCol == null ? null : rs.getString(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(table + " load failed: " + e.getMessage(), e);
        }
        return map;
    }
}