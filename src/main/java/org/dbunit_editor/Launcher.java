package org.dbunit_editor;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.dbunit.dataset.DataSetException;
import org.dbunit_editor.Launcher.DataSetWindowChangeEvent.ActiveEvent;

public class Launcher {
    private final JFrame _window = new JFrame("DbUnit XML Editor");
    private final JDesktopPane _pane = new JDesktopPane();
    private final Collection<DataSetWindow> _files = new ArrayList<>();
    private DataSetWindow _actived = null;
    private Collection<DataSetWindowChangeListener> _listeners = new ArrayList<>();

    public Launcher() {
        _window.setContentPane(_pane);
        _window.setJMenuBar(createMenues());
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _window.pack();
        _window.setSize(500, 500);
        _window.setLocationRelativeTo(null);
    }

    private JMenuBar createMenues() {
        JMenuBar menu = new JMenuBar();
        menu.add(createFileMenu());
        menu.add(createEditMenu());
        return menu;
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new JMenuItem(new OpenAction(this)));
        menu.add(new JMenuItem(new SaveAction(this)));
        menu.add(new JMenuItem(new SaveAsAction(this)));
        menu.add(new JMenuItem(new CloseAction(this)));
        menu.add(new JMenuItem(new QuitAction(this)));

        return menu;
    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(new JMenuItem(new CopyAction(this)));
        menu.add(new JMenuItem(new PasteAction(this)));
        return menu;
    }

    public DataSetWindow getActiveWindow() {
        return _actived;
    }

    public void addListener(final DataSetWindowChangeListener l) {
        _listeners.add(l);
    }

    public void start() {
        _window.setVisible(true);
    }

    protected void activated(final DataSetWindow window) {
        _actived = window;
        fireChangeEvent(ActiveEvent.ACTIVATED);
    }

    protected void deactivated(final DataSetWindow window) {
        if (window.equals(_actived)) {
            _actived = null;
            fireChangeEvent(ActiveEvent.DEACTIVATED);
        }
    }

    protected void closed(final DataSetWindow window) {
        _files.remove(window);
    }

    private void fireChangeEvent(final ActiveEvent event) {
        DataSetWindowChangeEvent e = new DataSetWindowChangeEvent(event);
        for (final DataSetWindowChangeListener l : _listeners) {
            l.changePerformed(e);
        }
    }

    public void open(final File path) {
        try {
            DataSetWindow win = new DataSetWindow(_pane, path);
            win.addListener(new DataSetWindowListener(this, win));
            win.show();
            _files.add(win);
        } catch (DataSetException | IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    public void quit() {
        _window.setVisible(false);
        _window.dispose();
    }

    public static void main(final String...args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Launcher launcher = new Launcher();
                launcher.start();

                for (final String arg : args) {
                    File path = new File(arg);
                    if (path.isFile()) {
                        launcher.open(path);
                    }
                }
            }
        });
    }

    static abstract class AbstractMenuAction extends AbstractAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -1506779369000547402L;
        protected final Launcher _launcher;

        protected AbstractMenuAction(
                final String name, final Launcher launcher) {
            super(name);
            _launcher = launcher;
        }

        protected void setMnemonic(final int key) {
            putValue(Action.MNEMONIC_KEY, key);
        }

        protected void setAccelerator(final KeyStroke key) {
            putValue(Action.ACCELERATOR_KEY, key);
        }

        protected void addListnerToChangeEnabled(final Action a) {
            _launcher.addListener(new DataSetWindowChangeListener() {
                @Override
                public void changePerformed(final DataSetWindowChangeEvent e) {
                    a.setEnabled(ActiveEvent.ACTIVATED.equals(e.getEvent()));
                }
            });
        }
    }

    static class OpenAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -2134294147087859001L;

        public OpenAction(final Launcher launcher) {
            super("Open...", launcher);

            setMnemonic(KeyEvent.VK_O);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            switch (chooser.showOpenDialog(_launcher._window)) {
            case JFileChooser.APPROVE_OPTION:
                _launcher.open(chooser.getSelectedFile());
                break;
            default:
            }
        }
    }

    static class SaveAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -8800117080592625926L;

        public SaveAction(final Launcher launcher) {
            super("Save", launcher);

            setMnemonic(KeyEvent.VK_S);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

            setEnabled(false);
            addListnerToChangeEnabled(this);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            DataSetWindow actived = _launcher.getActiveWindow();
            if (actived != null) {
                try {
                    actived.save();
                } catch (IOException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
            }
        }
    }

    static class SaveAsAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -8800117080592625926L;

        public SaveAsAction(final Launcher launcher) {
            super("Save As...", launcher);

            setMnemonic(KeyEvent.VK_A);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

            setEnabled(false);
            addListnerToChangeEnabled(this);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            DataSetWindow actived = _launcher.getActiveWindow();
            if (actived == null) {
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            switch (chooser.showSaveDialog(_launcher._window)) {
            case JFileChooser.APPROVE_OPTION:
                try {
                    actived.saveTo(chooser.getSelectedFile());
                } catch (IOException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
                break;
            default:
            }
        }
    }

    static class CloseAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -5259404202219742959L;

        public CloseAction(final Launcher launcher) {
            super("Close", launcher);

            setMnemonic(KeyEvent.VK_W);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

            setEnabled(false);
            addListnerToChangeEnabled(this);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            DataSetWindow actived = _launcher.getActiveWindow();
            if (actived != null) {
                actived.close();
            }
        }

    }

    static class QuitAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -3811451899041807204L;

        public QuitAction(final Launcher launcher) {
            super("Quit", launcher);

            setMnemonic(KeyEvent.VK_Q);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            _launcher.quit();
        }
    }

    static class CopyAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = 5650954796598394708L;

        protected CopyAction(final Launcher launcher) {
            super("Copy", launcher);

            setMnemonic(KeyEvent.VK_C);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

            setEnabled(false);
            addListnerToChangeEnabled(this);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            _launcher.getActiveWindow().copy();
        }
    }

    static class PasteAction extends AbstractMenuAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -4259549851535646353L;

        protected PasteAction(final Launcher launcher) {
            super("Paste", launcher);

            setMnemonic(KeyEvent.VK_V);
            setAccelerator(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

            setEnabled(false);
            addListnerToChangeEnabled(this);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            _launcher.getActiveWindow().paste();
        }
    }

    static class DataSetWindowListener implements InternalFrameListener {
        private Launcher _parent;
        private DataSetWindow _children;

        public DataSetWindowListener(final Launcher parent, final DataSetWindow children) {
            _parent = parent;
            _children = children;
        }

        @Override
        public void internalFrameOpened(final InternalFrameEvent e) {
            _parent.activated(_children);
        }

        @Override
        public void internalFrameClosing(final InternalFrameEvent e) {
        }

        @Override
        public void internalFrameClosed(final InternalFrameEvent e) {
            _parent.closed(_children);
        }

        @Override
        public void internalFrameIconified(final InternalFrameEvent e) {
        }

        @Override
        public void internalFrameDeiconified(final InternalFrameEvent e) {
        }

        @Override
        public void internalFrameActivated(final InternalFrameEvent e) {
            _parent.activated(_children);
        }

        @Override
        public void internalFrameDeactivated(final InternalFrameEvent e) {
            _parent.deactivated(_children);
        }
    }

    @Accessors(prefix = "_")
    static class DataSetWindowChangeEvent {
        static enum ActiveEvent {
            ACTIVATED,
            DEACTIVATED,
        }

        @Getter
        private final ActiveEvent _event;

        public DataSetWindowChangeEvent(final ActiveEvent event) {
            _event = event;
        }
    }

    interface DataSetWindowChangeListener {
        void changePerformed(final DataSetWindowChangeEvent e);
    }
}
