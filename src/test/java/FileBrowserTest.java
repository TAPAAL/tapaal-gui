package pipe.gui.swingcomponents.filebrowser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBrowserTest {
    @Test
    void usesSwingChooserOnlyOnMacOs() {
        assertTrue(FileBrowser.useSwingMultiFileChooser("Mac OS X"));
        assertTrue(FileBrowser.useSwingMultiFileChooser("macOS"));
        assertFalse(FileBrowser.useSwingMultiFileChooser("Windows 11"));
        assertFalse(FileBrowser.useSwingMultiFileChooser("Linux"));
        assertFalse(FileBrowser.useSwingMultiFileChooser(null));
    }
}
