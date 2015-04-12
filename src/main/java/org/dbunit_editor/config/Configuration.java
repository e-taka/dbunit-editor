package org.dbunit_editor.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "_")
public class Configuration {
    private Database _database = new Database();

    @Data
    @Accessors(prefix = "_")
    public static class Database {
        private String _url = null;
        private String _username = null;
        private String _password = null;
    }
}
