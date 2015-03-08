package org.dbunit_editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        return menu;
    }

    private JMenu createFileMenu() {
        final Launcher that = this;

        JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                switch (chooser.showOpenDialog(_window)) {
                case JFileChooser.APPROVE_OPTION:
                    File path = chooser.getSelectedFile();
                    try {
                        DataSetWindow win = new DataSetWindow(_pane, path);
                        win.addListener(new DataSetWindowListener(that, win));
                        win.show();
                        _files.add(win);
                    } catch (DataSetException | IOException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                    break;
                default:
                }
            }
        });

        JMenuItem save = new JMenuItem(new SaveAction(this));

        JMenuItem saveAs = new JMenuItem("Save As...", KeyEvent.VK_A);
        saveAs.setEnabled(false);
        saveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (_actived == null) {
                    return;
                }
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                switch (chooser.showSaveDialog(_window)) {
                case JFileChooser.APPROVE_OPTION:
                    File path = chooser.getSelectedFile();
                    try {
                        _actived.saveTo(path);
                    } catch (IOException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                    break;
                default:
                }
            }
        });

        JMenuItem close = new JMenuItem(new CloseAction(this));

        JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _window.setVisible(false);
                _window.dispose();
            }
        });

        final List<JMenuItem> disables = Arrays.asList(new JMenuItem[] {
                saveAs,
        });
        addListener(new DataSetWindowChangeListener() {
            @Override
            public void changePerformed(final DataSetWindowChangeEvent e) {
                boolean enabled = ActiveEvent.ACTIVATED.equals(e.getEvent());
                for (final JMenuItem menu : disables) {
                    menu.setEnabled(enabled);
                }
            }
        });

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(open);
        menu.add(save);
        menu.add(saveAs);
        menu.add(close);
        menu.add(exit);

        return menu;
    }

    public DataSetWindow getActiveWindow() {
        return _actived;
    }

    static class SaveAction extends AbstractAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -8800117080592625926L;
        private final Launcher _launcher;

        public SaveAction(final Launcher launcher) {
            super("Save");

            _launcher = launcher;
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
            KeyStroke key =
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
            putValue(Action.ACCELERATOR_KEY, key);

            setEnabled(false);
            launcher.addListener(new DataSetWindowChangeListener() {
                @Override
                public void changePerformed(final DataSetWindowChangeEvent e) {
                    setEnabled(ActiveEvent.ACTIVATED.equals(e.getEvent()));
                }
            });
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

    static class CloseAction extends AbstractAction {
        /** シリアルバージョンUID */
        private static final long serialVersionUID = -5259404202219742959L;
        private final Launcher _launcher;

        public CloseAction(final Launcher launcher) {
            super("Close");

            _launcher = launcher;
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
            KeyStroke key =
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
            putValue(Action.ACCELERATOR_KEY, key);


            setEnabled(false);
            launcher.addListener(new DataSetWindowChangeListener() {
                @Override
                public void changePerformed(final DataSetWindowChangeEvent e) {
                    setEnabled(ActiveEvent.ACTIVATED.equals(e.getEvent()));
                }
            });
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            DataSetWindow actived = _launcher.getActiveWindow();
            if (actived != null) {
                actived.close();
            }
        }

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

    public static void main(final String...args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Launcher launcher = new Launcher();
                launcher.start();
            }
        });
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
            System.out.println("Opened.");
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
            System.out.println("Activated.");
            _parent.activated(_children);
        }

        @Override
        public void internalFrameDeactivated(final InternalFrameEvent e) {
            System.out.println("Deactivated.");
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
