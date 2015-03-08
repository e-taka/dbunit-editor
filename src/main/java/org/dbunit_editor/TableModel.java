package org.dbunit_editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

@Accessors(prefix = "_")
public class TableModel extends DefaultTableModel {
    /** シリアルバージョンUID */
    private static final long serialVersionUID = 4471786262599752704L;

    /** テーブル名 */
    @Getter
    private final String _name;
    private final ITableMetaData _meta;

    public TableModel(final ITable t) throws DataSetException {
        super(toColumnNames(t.getTableMetaData()), 0);

        _meta = t.getTableMetaData();
        _name = _meta.getTableName();

        for (int i = 0; i < t.getRowCount(); i++) {
            List<Object> row = new ArrayList<>();
            for (final Column c : _meta.getColumns()) {
                row.add(t.getValue(i, c.getColumnName()));
            }
            addRow(row.toArray(new Object[row.size()]));
        }
        addRow(new Object[_meta.getColumns().length]);
    }

    private static String[] toColumnNames(final ITableMetaData meta)
            throws DataSetException {
        List<String> names = new ArrayList<>();
        for (final Column c : meta.getColumns()) {
            names.add(c.getColumnName());
        }
        return names.toArray(new String[names.size()]);
    }

    public void writeTo(final DefaultDataSet ds) throws DataSetException {
        DefaultTable t = new DefaultTable(_meta);
        int rows = getRowCount() - 1;
        int columns = getColumnCount();
        for (int row = 0; row < rows; row++) {
            Object[] values = new Object[columns];
            for (int column = 0; column < columns; column++) {
                values[column] = getValueAt(row, column);
            }
            t.addRow(values);
        }
        ds.addTable(t);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return true;
    }

    public void insertRow(final int rowIndex) {
        List<Object> row = new ArrayList<>();
        for (int i = 0; i < getColumnCount(); i++) {
            row.add(null);
        }
        insertRow(rowIndex, row.toArray(new Object[row.size()]));
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        if (row + 1 == getRowCount()) {
            addRow(new Object[getColumnCount()]);
        }
        super.setValueAt(aValue, row, column);
    }
}
