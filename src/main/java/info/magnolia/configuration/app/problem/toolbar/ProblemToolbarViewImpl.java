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
package info.magnolia.configuration.app.problem.toolbar;

import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.problem.ProblemView;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.extension.ShortcutProtector;
import info.magnolia.ui.vaadin.icon.Icon;

import java.util.Arrays;

import javax.inject.Inject;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * ToolbarViewImpl.
 */
public class ProblemToolbarViewImpl extends HorizontalLayout implements ProblemToolbarView {

    private final SimpleTranslator i18n;

    private HorizontalLayout groupByLayout = new HorizontalLayout();

    private CssLayout searchLayout = new CssLayout();

    private Callback callback;

    private ComboBox groupBySelect = new ComboBox();

    private ComboBox fromSelect = new ComboBox();

    private TextField searchField = new TextField();

    private Property.ValueChangeListener groupByChangeListener = new Property.ValueChangeListener() {
        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            callback.updateGroupBy();
        }
    };

    private final Property.ValueChangeListener searchFieldListener = new Property.ValueChangeListener() {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            callback.onSearch(searchField.getValue());

            boolean hasSearchContent = !searchField.getValue().isEmpty();
            if (hasSearchContent) {
                addStyleName("has-content");
            } else {
                removeStyleName("has-content");
            }
            searchField.focus();
        }
    };

    @Inject
    public ProblemToolbarViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;

        Responsive.makeResponsive(this);
        addStyleName("toolbar-layout");
        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        setSpacing(true);
        setMargin(new MarginInfo(true, false, true, false));
        setWidth("100%");

        initGroupByLayout();

        iniSearchField();
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public ProblemView.SourceType getGroupBy() {
        return (ProblemView.SourceType) groupBySelect.getValue();
    }

    @Override
    public Problem.SourceType getFrom() {
        return (Problem.SourceType) fromSelect.getValue();
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void initGroupByLayout() {
        groupBySelect.setHeight("25px");
        groupBySelect.setWidth("160px");
        groupBySelect.setNullSelectionAllowed(false);
        for (ProblemView.SourceType type : ProblemView.SourceType.values()) {
            groupBySelect.addItem(type);
        }
        groupBySelect.setValue(ProblemView.SourceType.source);
        groupBySelect.addValueChangeListener(groupByChangeListener);

        fromSelect.setHeight("25px");
        fromSelect.setWidth("160px");
        fromSelect.setNullSelectionAllowed(true);
        for (Problem.SourceType type : Problem.SourceType.values()) {
            fromSelect.addItem(type);
        }
        fromSelect.addValueChangeListener(groupByChangeListener);
        fromSelect.setInputPrompt("All sources");

        groupByLayout.setSizeUndefined();

        groupByLayout.addComponent(decorateComponent("Group by:", groupBySelect));
        groupByLayout.addComponent(decorateComponent("From:", fromSelect));

        groupByLayout.addStyleName("entry");
        groupByLayout.setMargin(true);

        addComponent(groupByLayout);
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

    private void iniSearchField() {
        Button clearSearchBoxButton = new Button();
        clearSearchBoxButton.setStyleName("m-closebutton");
        clearSearchBoxButton.addStyleName("icon-delete-search");
        clearSearchBoxButton.addStyleName("searchbox-clearbutton");
        // Preventing the button to spoil the tab-navigation due to its changing display value.
        clearSearchBoxButton.setTabIndex(-1);
        clearSearchBoxButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                searchField.setValue("");
            }
        });

        Icon searchIcon = new Icon("search");
        searchIcon.addStyleName("searchbox-icon");

        Icon searchArrow = new Icon("arrow2_s");
        searchArrow.addStyleName("searchbox-arrow");

        searchField = buildSearchField();

        searchLayout.setVisible(true);
        searchLayout.addComponent(searchField);
        searchLayout.addComponent(clearSearchBoxButton);
        searchLayout.addComponent(searchIcon);
        searchLayout.addComponent(searchArrow);
        searchLayout.setStyleName("searchbox");

        VerticalLayout searchLayoutWrapper = new VerticalLayout();
        searchLayoutWrapper.setStyleName("workbench");
        searchLayoutWrapper.addComponent(searchLayout);

        addComponent(searchLayoutWrapper);
    }

    private TextField buildSearchField() {
        final TextField field = new TextField();
        ShortcutProtector.extend(field, Arrays.asList(ShortcutAction.KeyCode.ENTER));
        final String inputPrompt = i18n.translate("toolbar.search.prompt");

        field.setInputPrompt(inputPrompt);
        field.setSizeUndefined();
        field.addStyleName("searchfield");

        // TextField has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        field.setImmediate(true);
        field.addValueChangeListener(searchFieldListener);

        field.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                // put the cursor at the end of the field
                TextField tf = (TextField) event.getSource();
                tf.setCursorPosition(tf.getValue().length());
            }
        });

        // No blur handler.
        return field;
    }
}
