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
package info.magnolia.configuration.app.problem;

import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.overview.ConfigOverviewLayout;
import info.magnolia.configuration.app.overview.data.ConfigConstants;
import info.magnolia.configuration.app.overview.toolbar.ToolbarView;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link info.magnolia.configuration.app.problem.ConfigProblemView}.
 */
public class ConfigProblemViewImpl implements ConfigProblemView {

    public static final String GREEN = "green";
    public static final String RED = "red";

    private VerticalLayout layout = new ConfigOverviewLayout();

    private Presenter presenter;

    private TreeTable treeTable = new MagnoliaTreeTable();

    private Label statusLabel = new Label("", ContentMode.HTML);

    private SimpleTranslator i18n;

    @Inject
    public ConfigProblemViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;
        initLayout();
        initTreeTable();
    }

    protected SimpleTranslator getI18n() {
        return i18n;
    }

    private void initLayout() {
        layout.setMargin(true);
        layout.setSizeFull();
        layout.addComponent(treeTable);
        layout.setExpandRatio(treeTable, 1);
        layout.addComponent(statusLabel);
    }

    private void initTreeTable() {
        treeTable.setSizeFull();
        treeTable.addStyleName("message-table");
        treeTable.setSelectable(true);
        treeTable.setMultiSelect(true);
        treeTable.setHeight("100%");
        for (Object itemId : treeTable.getItemIds()) {
            treeTable.setCollapsed(itemId, false);
        }
    }

    @Override
    public Table.GeneratedRow generateGroupingRow(Item item) {
        Table.GeneratedRow row = new Table.GeneratedRow();
        Problem.SourceType messageType = (Problem.SourceType) item.getItemProperty(ConfigConstants.SOURCE_PID).getValue();

        String key = null;
        switch (messageType) {
        case classpath:
            key = "Classpath";
            break;
        case JCR:
            key = "JCR";
            break;
        default:
            return null;
        }

        row.setText(getI18n().translate(key));
        return row;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override public void setDataSource(Container dataSource) {
        treeTable.setContainerDataSource(dataSource);
        treeTable.setVisibleColumns(ConfigConstants.PROBLEM_ID_ORDER);
        treeTable.setColumnHeaders("Problem", "Element",  "Module", "Source", "Timestamp");
        treeTable.setRowGenerator(groupingRowGenerator);
        treeTable.setCollapsed(ConfigConstants.PROBLEM_ID_ORDER, false);


        //Columns formatter
        treeTable.addGeneratedColumn(ConfigConstants.PROBLEM_PID, new ProblemColumnGenerator());
        treeTable.setColumnWidth(ConfigConstants.PROBLEM_PID, 450);
        treeTable.addGeneratedColumn(ConfigConstants.SOURCE_PID, new SourceItemColumnGenerator());
        treeTable.setColumnWidth(ConfigConstants.SOURCE_PID, 100);
        treeTable.addGeneratedColumn(ConfigConstants.TIMESTAMP_PID, new DateColumnFormatter(null));
        treeTable.setColumnWidth(ConfigConstants.TIMESTAMP_PID, 150);
    }

    /*
     * Row generator draws grouping headers if such are present in container
     */
    private Table.RowGenerator groupingRowGenerator = new Table.RowGenerator() {

        @Override
        public Table.GeneratedRow generateRow(Table table, Object itemId) {

            /*
             * When sorting by type special items are inserted into Container to
             * acts as a placeholder for grouping sub section. This row
             * generator must render those special items.
             */
            if (itemId.toString().startsWith(ConfigConstants.GROUP_PLACEHOLDER_ITEMID)) {
                Item item = table.getItem(itemId);
                return generateGroupingRow(item);
            }

            return null;
        }
    };

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the subject cells in the messages list view.
     */
    // default visibility for tests.
    class ProblemColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            Problem.Type type = (Problem.Type) source.getContainerProperty(itemId, ConfigConstants.TYPE_PID).getValue();
            String text = (String) source.getContainerProperty(itemId, columnId).getValue();

            if (StringUtils.isNotBlank(type.name()) && StringUtils.isNotBlank(text)) {

                String level, shape = "circle", mark;
                switch (type) {
                case Info:
                    level = "info-icon";
                    mark = "icon-info_mark";
                    break;
                case Warning:
                    level = "warning-icon";
                    mark = "icon-warning-mark";
                    break;
                case Error:
                    level = "error-icon";
                    shape = "triangle";
                    mark = "icon-error-mark";
                    break;
                default:
                    return null;
                }

                return String.format("<span class=\"composite-icon %1$s\">"
                                + "<span class=\"icon icon-shape-%2$s-plus\"></span>"
                                + "<span class=\"icon icon-shape-%2$s\"></span>"
                                + "<span class=\"icon %3$s\"></span>"
                                + "</span>&nbsp%4$s",
                        level, shape, mark, text);
            }
            return null;
        }
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for denoting new messages or tasks in the Pulse list views.
     */
    protected class SourceItemColumnGenerator implements Table.ColumnGenerator {

        // void public constructor to instantiate from subclasses in different packages
        public SourceItemColumnGenerator() {
        }

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            Property<Problem.SourceType> newProperty = source.getContainerProperty(itemId, columnId);
            Problem.SourceType type = newProperty.getValue();

            String icon;
            switch (type) {
            case JCR:
                icon = "icon-node-content";
                break;
            case classpath:
                icon = "icon-files";
                break;
            default:
                return null;
            }

            return String.format("<span class=\"icon %1$s\"></span>", icon);
        }
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the type cells in the messages list view.
     */
    private class ProblemSourceColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            Problem.SourceType messageType = (Problem.SourceType) source.getContainerProperty(itemId, ConfigConstants.SOURCE_PID).getValue();

            String level, shape = "circle", mark;
            switch (messageType) {
            case JCR:
                level = "info-icon";
                mark = "icon-info_mark";
                break;
            case classpath:
                level = "warning-icon";
                mark = "icon-warning-mark";
                break;
            default:
                return null;
            }

            return String.format("<span class=\"composite-icon %1$s\">"
                            + "<span class=\"icon icon-shape-%2$s-plus\"></span>"
                            + "<span class=\"icon icon-shape-%2$s\"></span>"
                            + "<span class=\"icon %3$s\"></span>"
                            + "</span>",
                    level, shape, mark);
        }
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
