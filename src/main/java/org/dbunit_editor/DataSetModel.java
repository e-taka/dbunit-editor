package org.dbunit_editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.experimental.Accessors;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.dataset.xml.XmlDataSetWriter;

@Accessors(prefix = "_")
public class DataSetModel {
    private File _path = null;
    private Collection<TableModel> _tables = new ArrayList<>();

    public DataSetModel(final File path, final IDataSet ds)
            throws DataSetException {
        this(path, convert(ds));
    }

    private static Collection<TableModel> convert(final IDataSet ds)
            throws DataSetException {
        List<TableModel> tables = new ArrayList<>();
        for (ITableIterator it = ds.iterator(); it.next(); ) {
            tables.add(new TableModel(it.getTable()));
        }
        return Collections.unmodifiableList(tables);
    }

    public DataSetModel(final Collection<TableModel> tables) {
        this(null, tables);
    }

    public DataSetModel(final File path, final Collection<TableModel> tables) {
        _path = path;
        _tables.addAll(tables);
    }

    public String getName() {
        if (_path == null) {
            return "New File";
        }
        return _path.getName();
    }

    public Collection<TableModel> getTables() {
        return Collections.unmodifiableCollection(_tables);
    }

    public static DataSetModel read(final File path)
            throws IOException, DataSetException {
        try (final BufferedInputStream is =
                new BufferedInputStream(new FileInputStream(path))) {
            IDataSet ds = new XmlDataSet(is);
            return new DataSetModel(path, ds);
        }
    }

    public void save() throws IOException {
        saveTo(_path);
    }

    public void saveTo(final File path) throws IOException {
        try {
            DefaultDataSet ds = new DefaultDataSet();
            for (final TableModel t : _tables) {
                t.writeTo(ds);
            }

            try (final OutputStream os =
                    new BufferedOutputStream(new FileOutputStream(path))) {
                XmlDataSetWriter writer = new XmlDataSetWriter(os, "UTF-8");
                writer.write(ds);
            }

            System.out.println("saved to " + path);
            _path = path;
        } catch (DataSetException e) {
            throw new IllegalStateException(e);
        }
    }
}
