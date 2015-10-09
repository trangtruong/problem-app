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
package info.magnolia.configuration.app.overview.toolbar;

import info.magnolia.config.registry.DefinitionType;
import info.magnolia.configuration.app.overview.ConfigOverviewView;

import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;


/**
 * ToolbarViewImpl.
 */
public class ToolbarViewImpl extends HorizontalLayout implements ToolbarView {

    private HorizontalLayout groupByLayout = new HorizontalLayout();

    private HorizontalLayout filterLayout = new HorizontalLayout();

    private Callback callback;

    private Button showFileButton = new Button("show file", new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            callback.showFile();
        }
    });

    private Button openJcrConfig = new Button("jcr config", new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            callback.openJcrConfig();
        }
    });

    private ComboBox groupBySelect = new ComboBox();

    private ComboBox moduleFilterSelect = new ComboBox();

    private ComboBox definitionTypeFilterSelect = new ComboBox();

    private TextField nameField = new TextField();

    private CheckBox onlyIncludeDefinitionsWithErrorsCheckBox = new CheckBox();

    private CheckBox onlyIncludeFileBasedDefinitions = new CheckBox();

    private Property.ValueChangeListener filterChangeListener = new Property.ValueChangeListener() {
        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            callback.updateFilter();
        }
    };

    private Property.ValueChangeListener groupByChangeListener = new Property.ValueChangeListener() {
        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            callback.updateGroupBy();
        }
    };

    @Inject
    public ToolbarViewImpl() {
        Responsive.makeResponsive(this);
        addStyleName("toolbar-layout");
        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        setSpacing(true);
        setMargin(new MarginInfo(true, false, true, false));
        setWidth("100%");

        initFilterInputProperties(moduleFilterSelect);
        initFilterInputProperties(definitionTypeFilterSelect);
        initFilterInputProperties(nameField);
        initFilterInputProperties(onlyIncludeDefinitionsWithErrorsCheckBox);
        initFilterInputProperties(onlyIncludeFileBasedDefinitions);

        groupBySelect.setHeight("25px");
        moduleFilterSelect.setHeight("25px");
        definitionTypeFilterSelect.setHeight("25px");
        groupBySelect.setWidth("160px");
        groupBySelect.setNullSelectionAllowed(false);
        for (ConfigOverviewView.DefinitionAggregationType type : ConfigOverviewView.DefinitionAggregationType.values()) {
            groupBySelect.addItem(type);
        }
        groupBySelect.setValue(ConfigOverviewView.DefinitionAggregationType.registry);
        groupBySelect.addValueChangeListener(groupByChangeListener);

        groupByLayout.setSizeUndefined();
        filterLayout.setSizeUndefined();

        groupByLayout.addComponent(decorateComponent("Group by:", groupBySelect));;

        filterLayout.addComponent(decorateComponent("Module:", moduleFilterSelect));
        filterLayout.addComponent(decorateComponent("Type:", definitionTypeFilterSelect));
        filterLayout.addComponent(decorateComponent("Name:", nameField));
        filterLayout.addComponent(decorateComponent("Invalid:", onlyIncludeDefinitionsWithErrorsCheckBox));
        filterLayout.addComponent(decorateComponent("File-based:", onlyIncludeFileBasedDefinitions));

        moduleFilterSelect.setInputPrompt("All modules");
        definitionTypeFilterSelect.setInputPrompt("All types");
        nameField.setInputPrompt("Any name");

        groupByLayout.addStyleName("entry");
        groupByLayout.setMargin(true);

        filterLayout.addStyleName("entry");
        filterLayout.setSpacing(true);
        filterLayout.setMargin(true);

        addComponent(groupByLayout);
        final Component filterWrapper = decorateComponent("Filter:", filterLayout);
        addComponent(filterWrapper);

        addComponent(showFileButton);
        addComponent(openJcrConfig);
        setExpandRatio(openJcrConfig, 1f);
    }

    private Component decorateComponent(String caption, Component toWrap) {
        final HorizontalLayout result = new HorizontalLayout();
        result.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        result.setSpacing(true);
        result.setSizeUndefined();

        final Label captionLabel = new Label(caption);
        captionLabel.setSizeUndefined();
        captionLabel.addStyleName("caption-label");

        result.addComponent(captionLabel);
        result.addComponent(toWrap);

        return result;
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setModuleNames(List<String> moduleNames) {
        moduleFilterSelect.removeAllItems();
        for (final String moduleName : moduleNames) {
            moduleFilterSelect.addItem(moduleName);
        }
    }

    @Override
    public void setDefinitionTypes(List<DefinitionType> definitionTypes) {
        final IndexedContainer c = new IndexedContainer();
        c.addContainerProperty("name", String.class, null);
        for (final DefinitionType definitionType : definitionTypes) {
            final Item item = c.addItem(definitionType);
            item.getItemProperty("name").setValue(definitionType.name());
        }
        definitionTypeFilterSelect.setContainerDataSource(c);
        definitionTypeFilterSelect.setItemCaptionPropertyId("name");
    }

    @Override
    public boolean isWithErrorsOnly() {
        return onlyIncludeDefinitionsWithErrorsCheckBox.getValue();
    }

    @Override
    public boolean isFileBasedOnly() {
        return onlyIncludeFileBasedDefinitions.getValue();
    }

    @Override
    public boolean isGroupByModules() {
        return ConfigOverviewView.DefinitionAggregationType.module.equals(groupBySelect.getValue());
    }

    @Override
    public String getModule() {
        return (String)moduleFilterSelect.getValue();
    }

    @Override
    public String getName() {
        return nameField.getValue();
    }

    @Override
    public DefinitionType getDefinitionType() {
        return (DefinitionType) definitionTypeFilterSelect.getValue();
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setJcrConfigNavigationAvailable(boolean isAvailable) {
        openJcrConfig.setEnabled(isAvailable);
    }

    @Override
    public void setFilePreviewAvailable(boolean isAvailable) {
        showFileButton.setEnabled(isAvailable);
    }

    private void initFilterInputProperties(AbstractField component) {
        component.setImmediate(true);
        component.addValueChangeListener(filterChangeListener);
        if (component instanceof AbstractSelect) {
            component.addStyleName("filter-input");
            component.setWidth("160px");
            final AbstractSelect select = (AbstractSelect) component;
            select.setNullSelectionAllowed(true);
        }
    }
}
