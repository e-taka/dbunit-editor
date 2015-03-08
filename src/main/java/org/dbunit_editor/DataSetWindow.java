package org.dbunit_editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.InternalFrameListener;

import org.dbunit.dataset.DataSetException;

public class DataSetWindow {
    private final JDesktopPane _desktop;
    private final JInternalFrame _window;
    private final DataSetModel _model;
    private final JTabbedPane _tabs;

    public DataSetWindow(final JDesktopPane desktop, final File path)
            throws DataSetException, IOException {
        _desktop = desktop;
        _model = DataSetModel.read(path);
        _window = new JInternalFrame(_model.getName());
        _window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        _window.setSize(200, 200);
        _window.setClosable(true);
        _window.setResizable(true);
        _window.setMaximizable(true);

        _tabs = createTabs();
        JPanel buttons = createButtons();
        JPanel p = new JPanel(new BorderLayout());
        p.add(buttons, BorderLayout.NORTH);
        p.add(new JScrollPane(_tabs), BorderLayout.CENTER);
        _window.setContentPane(p);
        _window.pack();

        _desktop.add(_window);
    }

    private JPanel createButtons() {
        JButton append = new JButton("+");
        append.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                getSelectedTab().insertRowAtSelectedRow();
            }
        });

        JButton delete = new JButton("-");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                getSelectedTab().removeRowAtSelectedRow();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));
        buttons.add(append);
        buttons.add(delete);
        return buttons;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        for (final TableModel table : _model.getTables()) {
            TableTab tab = new TableTab(table);
            tabs.addTab(table.getName(), tab);
        }
        return tabs;
    }

    private TableTab getSelectedTab() {
        int index = _tabs.getSelectedIndex();
        return (TableTab) _tabs.getComponentAt(index);
    }

    public void show() {
        _window.setVisible(true);
    }

    public void close() {
        _window.dispose();
    }

    public void save() throws IOException {
        _model.save();
    }

    public void saveTo(final File path) throws IOException {
        _model.saveTo(path);
        _window.setTitle(_model.getName());
    }

    public void addListener(final InternalFrameListener listener) {
        _window.addInternalFrameListener(listener);
    }
}
