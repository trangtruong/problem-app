/**
 * This file Copyright (c) 2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.configuration.app.overview.filebrowser;

import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.framework.overlay.ViewAdapter;

import java.io.IOException;
import java.io.Reader;

import com.google.common.io.CharStreams;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * FileBrowserHelper.
 */
public class FileBrowserHelper {

    private UiContext uiContext;

    public FileBrowserHelper(UiContext uiContext) {
        this.uiContext = uiContext;
    }

    public void showFile(final Reader reader) {
        final FilePopup popup = buildDialog(reader);
        final OverlayCloser overlayCloser = uiContext.openOverlay(popup, OverlayLayer.ModalityLevel.LIGHT);
        popup.addCloseListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                overlayCloser.close();
            }
        });
    }

    private FilePopup buildDialog(Reader reader) {
        try {
            return new FilePopup(CharStreams.toString(reader));
        } catch (IOException e) {
            return null;
        }
    }

    private static class FilePopup extends ViewAdapter {

        final Label fileText = new Label("", ContentMode.PREFORMATTED);
        final Button closeButton = new Button("OK");

        FilePopup(String content) {
            super(new VerticalLayout());
            final VerticalLayout layout = asVaadinComponent();
            layout.addStyleName("file-browser-layout");
            layout.setWidth("800px");
            layout.setHeight("600px");
            layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            layout.setMargin(true);
            layout.setSpacing(true);

            fileText.setValue(content);

            final Panel panel = new Panel();
            panel.setSizeFull();
            panel.setContent(fileText);

            layout.addComponent(panel);
            layout.addComponent(closeButton);
            layout.setExpandRatio(panel, 1f);
        }

        @Override
        public VerticalLayout asVaadinComponent() {
            return (VerticalLayout) super.asVaadinComponent();
        }

        public void addCloseListener(Button.ClickListener clickListener) {
            closeButton.addClickListener(clickListener);
        }
    }

}
