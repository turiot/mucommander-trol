/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.viewer;

import java.awt.Frame;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.viewer.audio.AudioFactory;
import com.mucommander.ui.viewer.hex.HexFactory;

/**
 * ViewerRegistrar maintains a list of registered file viewers and provides methods to dynamically register file viewers
 * and create appropriate FileViewer (Panel) and ViewerFrame (Window) instances for a given AbstractFile.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ViewerRegistrar {
	
    /** List of registered file viewers */ 
    private final static List<ViewerFactory> viewerFactories = new ArrayList<>();

    static {
        registerFileViewer(new com.mucommander.ui.viewer.pdf.PdfFactory());

        registerFileViewer(new com.mucommander.ui.viewer.djvu.DjvuFactory());

        registerFileViewer(new com.mucommander.ui.viewer.image.ImageFactory());

        registerFileViewer(new AudioFactory());

        registerFileViewer(new com.mucommander.ui.viewer.html.HtmlFactory());

        registerFileViewer(new com.mucommander.ui.viewer.text.TextFactory());

        // The HexFactory must be the last FileViewer to be registered (otherwise it would open other factories file types)
        registerFileViewer(new com.mucommander.ui.viewer.hex.HexFactory());
    }
    
    
    /**
     * Registers a FileViewer.
     * @param factory file viewer factory to register.
     */
    public static void registerFileViewer(ViewerFactory factory) {
        viewerFactories.add(factory);
    }
        
	
    /**
     * Creates and returns a ViewerFrame to start viewing the given file. The ViewerFrame will be monitored
     * so that if it is the last window on screen when it is closed by the user, it will trigger the shutdown sequence.
     *
     * @param mainFrame the parent MainFrame instance
     * @param file the file that will be displayed by the returned ViewerFrame
     * @param icon window's icon.
     * @return the created ViewerFrame
     */
    public static FileFrame createViewerFrame(MainFrame mainFrame, AbstractFile file, Image icon, ViewerFactory defaultFactory) {
        ViewerFrame frame = new ViewerFrame(mainFrame, file, icon, defaultFactory);

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard)
        if (OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrentOrHigher()) {
            // Displays the document icon in the window title bar, works only for local files
            if (file.getURL().getScheme().equals(FileProtocols.FILE)) {
                frame.getRootPane().putClientProperty("Window.documentFile", file.getUnderlyingFileObject());
            }
        }

        // WindowManager will listen to window closed events to trigger shutdown sequence
        // if it is the last window visible
        frame.addWindowListener(WindowManager.getInstance());
        
        return frame;
    }

    public static FileFrame createViewerFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        return createViewerFrame(mainFrame, file, icon, null);
    }
    
    /**
     * Creates and returns an appropriate FileViewer for the given file type.
     *
     * @param file the file that will be displayed by the returned FileViewer
     * @param frame the frame in which the FileViewer is shown
     * @return the created FileViewer, or null if no suitable viewer was found
     * @throws UserCancelledException if the user has been asked to confirm the operation and canceled
     */
    public static FileViewer createFileViewer(AbstractFile file, ViewerFrame frame, ViewerFactory defaultFactory) throws UserCancelledException {
    	FileViewer viewer = null;
        for (ViewerFactory factory : viewerFactories) {
            if (defaultFactory != null && !factory.getName().equals(defaultFactory.getName())) {
                continue;
            }
            try {
                if (factory.canViewFile(file)) {
                    viewer = factory.createFileViewer();
                    break;
                }
            } catch (WarnUserException e) {
            	// TODO: question the user how does he want to open the file (as image, text..)
                // Todo: display a proper warning dialog with the appropriate icon
            	
                QuestionDialog dialog = new QuestionDialog((Frame)null, Translator.get("warning"), Translator.get(e.getMessage()), frame,
                                                           new String[] {Translator.get("file_viewer.open_anyway"), Translator.get("file_viewer.open_hex"), Translator.get("cancel")},
                                                           new int[]  {0, 1, 2},
                                                           0);

                int ret = dialog.getActionValue();
                if (ret == 0) {
                    // User confirmed the operation
                    viewer = factory.createFileViewer();
                    break;
                } else if (ret == 1) {
                    viewer = new HexFactory().createFileViewer();
                    break;
                } else {
                    // User canceled the operation
                    throw new UserCancelledException();
                }
            }
        }
        if (viewer != null) {
            viewer.setFrame(frame);
        }
        
        return viewer;
    }


    public static List<ViewerFactory> getAllViewers(AbstractFile file) {
        List<ViewerFactory> result = new ArrayList<>();
        for (ViewerFactory factory : viewerFactories) {
            try {
                if (!factory.canViewFile(file)) {
                    continue;
                }
            } catch (WarnUserException e) {}
            result.add(factory);
        }
        return result;
    }
}
