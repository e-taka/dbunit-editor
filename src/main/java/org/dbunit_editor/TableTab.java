package org.dbunit_editor;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class TableTab extends JPanel {
    /** シリアルバージョンUID */
    private static final long serialVersionUID = -7539948731673187688L;

    private final JTable _table;

    public TableTab(final TableModel model) {
        _table = new TableViewer(model);
        _table.addMouseListener(new TablePopupMenu(_table, model));
        JScrollPane scroll = new JScrollPane(_table);
        add(scroll);

        setupKeybindings(_table);
    }

    private void setupKeybindings(final JComponent c) {
        InputMap im =
                c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = c.getActionMap();

        im.put(KeyStroke.getKeyStroke("control c"), "copy");
        am.put("copy", new AbstractAction() {
            /** シリアルバージョンUID */
            private static final long serialVersionUID = 5634784048650987552L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                copy();
            }
        });

        im.put(KeyStroke.getKeyStroke("control v"), "paste");
        am.put("paste", new AbstractAction() {
            /** シリアルバージョンUID */
            private static final long serialVersionUID = 8931828697935085260L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                paste();
            }
        });
    }

    public void addSelectionListener(final ListSelectionListener l) {
        _table.getSelectionModel().addListSelectionListener(l);
    }

    public void removeSelectionListener(final ListSelectionListener l) {
        _table.getSelectionModel().removeListSelectionListener(l);
    }

    public void insertRowAtSelectedRow() {
        TableModel model = (TableModel) _table.getModel();
        int row = _table.getSelectedRow();
        if (row >= 0) {
            model.insertRow(row);
            _table.clearSelection();
            _table.addRowSelectionInterval(row, row);
        }
    }

    public void removeRowAtSelectedRow() {
        TableModel model = (TableModel) _table.getModel();
        int row = _table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }

    public void copy() {
        int row = _table.getSelectedRow();
        int column = _table.getSelectedColumn();
        String value = (String) _table.getValueAt(row, column);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection ss = new StringSelection(value);
        clipboard.setContents(ss, null);
    }

    public void paste() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();

        try {
            String value = (String) clipboard.getData(DataFlavor.stringFlavor);
            int row = _table.getSelectedRow();
            int column = _table.getSelectedColumn();
            _table.setValueAt(value, row, column);
        } catch (UnsupportedFlavorException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static class TableViewer extends JTable {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -9055663175699014940L;

        private static TableCellRenderer _renderNull = new NullRender();

        public TableViewer(final TableModel model) {
            super(model);
        }

        @Override
        public TableCellRenderer getCellRenderer(final int row, final int column) {
            if (getModel().getValueAt(row, column) == null) {
                return _renderNull;
            }
            return super.getCellRenderer(row, column);
        }

    }

    static class NullRender extends DefaultTableCellRenderer implements TableCellRenderer {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -2544552766003438599L;

        public NullRender() {
            setOpaque(true);
            setText("(null)");
            setEnabled(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                final JTable table,
                final Object value, final boolean isSelected, final boolean hasFocus,
                final int row, final int column) {
            return super.getTableCellRendererComponent(
                    table, "(null)", isSelected, hasFocus, row, column);
        }

    }

    static class TablePopupMenu extends MouseAdapter {
        /** テーブル */
        private JTable _table;
        /** テーブルモデル */
        private TableModel _model;

        public TablePopupMenu(final JTable table, final TableModel model) {
            _table = table;
            _model = model;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                break;
            default:
                return;
            }

            final int column = _table.columnAtPoint(e.getPoint());
            final int row = _table.rowAtPoint(e.getPoint());
            Object value = _model.getValueAt(row, column);
            final Object nonNull = value == null ? "" : value;

            JCheckBoxMenuItem nil = new JCheckBoxMenuItem("null", value == null);
            nil.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(final ItemEvent e) {
                    switch (e.getStateChange()) {
                    case ItemEvent.SELECTED:
                        _table.setValueAt(null, row, column);
                        break;
                    case ItemEvent.DESELECTED:
                        _table.setValueAt(nonNull, row, column);
                        break;
                    default:
                    }
                    _table.repaint();
                }
            });
            JPopupMenu menu = new JPopupMenu("DBUnit");
            menu.add(nil);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
