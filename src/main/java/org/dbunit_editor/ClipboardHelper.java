package org.dbunit_editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

class ClipboardHelper {
    private static Clipboard _clipboard;
    static {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        _clipboard = toolkit.getSystemClipboard();
    }

    public static String getData() {
        try {
            return (String) _clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setData(final String value) {
        StringSelection ss = new StringSelection(value);
        _clipboard.setContents(ss, null);
    }
}
