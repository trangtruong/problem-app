/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.configuration.app.overview;

import info.magnolia.configuration.app.overview.data.ConfigConstants;
import info.magnolia.configuration.app.overview.data.ConfigurationContainer;
import info.magnolia.configuration.app.overview.data.Id;
import info.magnolia.configuration.app.overview.toolbar.ToolbarView;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link ConfigOverviewView}.
 */
public class ConfigOverviewViewImpl implements ConfigOverviewView {

    public static final String GROUPER_HTML_TEMPLATE = "<span class=\"grouper\">%s(%d)</span>";
    public static final String STATUS_HTML_TEMPLATE = "<span style=\"width: 16px; height: 16px; background-color: %s; display:inline-block;vertical-align: middle;\">";
    public static final String GREEN = "green";
    public static final String RED = "red";

    private VerticalLayout layout = new ConfigOverviewLayout();

    private ConfigOverviewView.Presenter presenter;

    private TreeTable treeTable = new MagnoliaTreeTable() {
        @Override
        protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
            final String value = super.formatPropertyValue(rowId, colId, property);
            if (ConfigConstants.TITLE_PID.equals(colId) && isRoot(rowId)) {
                if (ConfigConstants.TITLE_PID.equals(colId)) {
                    return String.format("%s(%d)", value, getChildren(rowId).size());
                }
            }
            return value;
        }
    };
    private Label statusLabel = new Label("", ContentMode.HTML);

    public ConfigOverviewViewImpl() {
        initLayout();
        initTreeTable();
    }

    private void initLayout() {
        layout.setMargin(true);
        layout.setSizeFull();
        layout.addComponent(treeTable);
        layout.setExpandRatio(treeTable, 1);
        layout.addComponent(statusLabel);
    }

    private void initTreeTable() {
        treeTable.setSelectable(true);
        treeTable.setSizeFull();
        treeTable.addStyleName("no-header-checkbox");
        treeTable.setHeight("100%");
        treeTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Id value = null;
                if (event.getProperty().getValue() != null) {
                    value = (Id) event.getProperty().getValue();
                }
                presenter.onSelectionChanged(value);
            }
        });

        treeTable.setCellStyleGenerator(new Table.CellStyleGenerator() {
            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {
                if (propertyId == null && itemId != null) {
                    return ((Id)itemId).getStyle();
                }

                return null;
            }
        });

        treeTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                if (propertyId == null && itemId != null) {
                    return ((Id)itemId).getTooltip();
                }
                return null;
            }
        });
        treeTable.setSizeFull();
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setConfigDataSource(ConfigurationContainer dataSource) {
        treeTable.setContainerDataSource(dataSource);

        for (Object pid : treeTable.getContainerPropertyIds()) {
            if (ConfigConstants.VALUE_PID.equalsIgnoreCase((String)pid)) {
                treeTable.setColumnExpandRatio(pid, 2f);
            } else if (ConfigConstants.TITLE_PID.equalsIgnoreCase((String)pid)){
                treeTable.setColumnExpandRatio(pid, 3f);
            } else {
                treeTable.setColumnExpandRatio(pid, 1f);
            }
        }

        treeTable.setColumnHeaders("Name", "Value",  "Type", "Module", "Origin");
        treeTable.setValue(null);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setStatus(String location) {
        statusLabel.setValue(location);
    }

    @Override
    public void setToolbar(ToolbarView toolbarView) {
        layout.addComponentAsFirst(toolbarView.asVaadinComponent());
    }
}
