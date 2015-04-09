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
import org.dbunit.dataset.datatype.DataType;

@Accessors(prefix = "_")
public class TableModel extends DefaultTableModel {
    /** シリアルバージョンUID */
    private static final long serialVersionUID = 4471786262599752704L;

    /** テーブル名 */
    @Getter
    private final String _name;

    public TableModel(final ITable t) throws DataSetException {
        super(toColumnNames(t.getTableMetaData()), 0);

        ITableMetaData meta = t.getTableMetaData();
        _name = meta.getTableName();

        for (int i = 0; i < t.getRowCount(); i++) {
            List<Object> row = new ArrayList<>();
            for (final Column c : meta.getColumns()) {
                row.add(t.getValue(i, c.getColumnName()));
            }
            addRow(row.toArray(new Object[row.size()]));
        }
        addRow(new Object[meta.getColumns().length]);
    }

    public TableModel(final String name, final List<String> columns) {
        super(columns.toArray(new String[columns.size()]), 0);

        _name = name;
        addRow(new Object[columns.size()]);
    }

    private static String[] toColumnNames(final ITableMetaData meta)
            throws DataSetException {
        List<String> names = new ArrayList<>();
        for (final Column c : meta.getColumns()) {
            names.add(c.getColumnName());
        }
        return names.toArray(new String[names.size()]);
    }

    private Column[] getColumns() {
        int count = getColumnCount();
        Column[] columns = new Column[count];
        for (int i = 0; i < count; i++) {
            columns[i] = new Column(getColumnName(i), DataType.UNKNOWN);;
        }
        return columns;
    }

    public void writeTo(final DefaultDataSet ds) throws DataSetException {
        DefaultTable t = new DefaultTable(_name, getColumns());
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
