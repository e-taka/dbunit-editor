package org.dbunit_editor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private String _url;

    public DBHelper(final String url) {
        _url = url;
    }

    public List<String> getTables() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(_url)) {
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, "%", new String[] {
                    "TABLE",
            });
            if (rs.first()) {
                do {
                    names.add(rs.getString("TABLE_NAME"));
                } while (rs.next());
            }
        }
        return names;
    }

    public List<String> getColumns(final String table) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(_url)) {
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, "%");
            if (rs.first()) {
                do {
                    names.add(rs.getString("COLUMN_NAME"));
                } while (rs.next());
            }
        }
        return names;
    }

    public static void main(final String...args) throws Exception {
        DBHelper helper = new DBHelper("jdbc:h2:./test");
        List<String> tables = helper.getTables();
        System.out.println(tables);
        for (final String table : tables) {
            List<String> columns = helper.getColumns(table);
            System.out.println(table + " => " + columns);
        }
    }
}
