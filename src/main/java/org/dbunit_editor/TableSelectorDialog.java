package org.dbunit_editor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.dbunit_editor.config.ConfigurationRepository;

/**
 * テーブル名のリストを表示し、選択するダイアログ.
 */
public class TableSelectorDialog {
    private JDialog _dialog = null;
    private DefaultListModel<String> _model = new DefaultListModel<>();
    private JList<String> _list = new JList<>(_model);
    private boolean _selected = false;

    public TableSelectorDialog(final Frame owner) {
        _dialog = new JDialog(owner, true);
        _dialog.getContentPane().add(createContainer());
        _dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        _dialog.pack();
        _dialog.setLocationRelativeTo(owner);
    }

    public DataSetModel openDialog() throws IOException, SQLException {
        DBHelper helper = new DBHelper(ConfigurationRepository.get());
        for (final String table : helper.getTables()) {
            _model.addElement(table);
        }

        _dialog.setVisible(true);
        if (!_selected) {
            return null;
        }

        List<TableModel> tables = new ArrayList<>();
        for (final String table : _list.getSelectedValuesList()) {
            tables.add(new TableModel(table, helper.getColumns(table)));
        }
        return new DataSetModel(tables);
    }

    public void close() {
        _dialog.setVisible(false);
        _dialog.dispose();
    }

    private JComponent createContainer() {
        _list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(_list);
        scroll.setPreferredSize(new Dimension(320, 320));

        ActionListener close = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                close();
            }
        };
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _selected = true;
            }
        });
        ok.addActionListener(close);
        JButton cancel = new JButton("cancel");
        cancel.addActionListener(close);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(ok);
        buttons.add(cancel);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(scroll);
        p.add(buttons);
        return p;
    }
}
