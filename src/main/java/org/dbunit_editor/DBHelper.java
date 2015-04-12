package org.dbunit_editor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit_editor.config.Configuration;

public class DBHelper {
    private final String _url;
    private final String _username;
    private final String _password;

    public DBHelper(final Configuration conf) {
        this(conf.getDatabase());
    }

    private DBHelper(final Configuration.Database db) {
        this(db.getUrl(), db.getUsername(), db.getPassword());
    }

    public DBHelper(
            final String url, final String username, final String password) {
        _url = url;
        _username = username;
        _password = password;
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(_url, _username, _password);
    }

    public List<String> getTables() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection con = connection()) {
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
        try (Connection con = connection()) {
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
}
