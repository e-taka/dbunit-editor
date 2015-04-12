package org.dbunit_editor.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigurationRepository {
    private static File _path = new File("conf/config.yaml");
    private static long _lastModified = 0L;
    private static Configuration _conf = null;

    public static Configuration get() throws IOException {
        if (_path.lastModified() == _lastModified) {
            return _conf;
        }
        return read(_path);
    }

    public static Configuration read(final File path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Configuration conf = mapper.readValue(path, Configuration.class);
        return conf;
    }
}
