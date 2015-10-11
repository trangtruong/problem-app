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
import info.magnolia.configuration.app.problem.data.ProblemConstants;
import info.magnolia.configuration.app.problem.toolbar.ProblemToolbarView;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.grid.MagnoliaTable;
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
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link info.magnolia.configuration.app.problem.ProblemView}.
 */
public class ProblemViewImpl extends VerticalLayout implements ProblemView {

    private VerticalLayout layout = new ConfigOverviewLayout();

    private ProblemPresenter presenter;

    private Table treeTable = new MagnoliaTable();

    private Label statusLabel = new Label("", ContentMode.HTML);

    private SimpleTranslator i18n;

    @Inject
    public ProblemViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;
        initLayout();
        initTreeTable();
    }

    protected SimpleTranslator getI18n() {
        return i18n;
    }

    private void initLayout() {
        layout.addStyleName("problem-layout");
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
        treeTable.setSortEnabled(false);

        treeTable.setRowGenerator(groupingRowGenerator);

        // Columns formatter
        treeTable.addGeneratedColumn(ProblemConstants.TYPE_PID, new TypeColumnGenerator());
        treeTable.setColumnWidth(ProblemConstants.TYPE_PID, 60);
        treeTable.addGeneratedColumn(ProblemConstants.PROBLEM_PID, new ProblemColumnGenerator());
        treeTable.setColumnExpandRatio(ProblemConstants.PROBLEM_PID, 1f);
        treeTable.addGeneratedColumn(ProblemConstants.SOURCE_PID, new SourceItemColumnGenerator());
        treeTable.setColumnWidth(ProblemConstants.SOURCE_PID, 100);
        treeTable.addGeneratedColumn(ProblemConstants.TIMESTAMP_PID, new DateColumnFormatter(null));
        treeTable.setColumnWidth(ProblemConstants.TIMESTAMP_PID, 200);
        treeTable.setColumnWidth(ProblemConstants.ELEMENT_PID, 100);
        treeTable.setColumnWidth(ProblemConstants.MODULE_PID, 100);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setDataSource(Container dataSource) {
        treeTable.setContainerDataSource(dataSource);
        treeTable.setVisibleColumns(ProblemConstants.PID_ORDER);
        treeTable.setColumnHeaders("", i18n.translate("problem.browser.column.problem"),
                i18n.translate("problem.browser.column.element"),
                i18n.translate("problem.browser.column.module"),
                i18n.translate("problem.browser.column.source"),
                i18n.translate("problem.browser.column.time"));
    }

    @Override
    public void setPresenter(ProblemPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setStatus(String location) {
        statusLabel.setValue(location);
    }

    @Override
    public void setToolbar(ProblemToolbarView toolbarView) {
        layout.addComponentAsFirst(toolbarView.asVaadinComponent());
    }

    @Override
    public void refresh() {
    }

    /*
* Row generator draws grouping headers if such are present in container
*/
    private Table.RowGenerator groupingRowGenerator = new Table.RowGenerator() {

        @Override
        public Table.GeneratedRow generateRow(Table table, Object itemId) {

            if (itemId.toString().startsWith(ProblemConstants.GROUP_PLACEHOLDER_ITEM_ID)) {
                Table.GeneratedRow row = new Table.GeneratedRow();
                Item item = table.getItem(itemId);
                if (item.getItemProperty(ProblemConstants.PROBLEM_PID).getValue() != null) {
                    String groupValue = StringUtils.removeStart((String) item.getItemProperty(ProblemConstants.PROBLEM_PID).getValue(), ProblemConstants.GROUP_PLACEHOLDER_ITEM_ID);
                    String problemType = StringUtils.substringBefore(groupValue, ProblemConstants.NUMBER_ITEM_SEPARATOR);
                    String numberOfProblems = StringUtils.substringAfter(groupValue, ProblemConstants.NUMBER_ITEM_SEPARATOR);
                    row.setText(" ", getI18n().translate("problem.group.message", numberOfProblems, problemType));

                    return row;
                }

            }

            return null;
        }
    };

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the subject cells in the messages list view.
     */
    class ProblemColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            String text = (String) source.getContainerProperty(itemId, columnId).getValue();
            return String.format("<span class=\"problem-message\">%s</span>", text);
        }
    }

    class TypeColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            Problem.Type type = (Problem.Type) source.getContainerProperty(itemId, ProblemConstants.TYPE_PID).getValue();
            if (type != null && StringUtils.isNotBlank(type.name())) {

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
                                + "</span>",
                        level, shape, mark);
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

            return String.format("<span class=\"activation-status icon %1$s\"></span>", icon);
        }
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the type cells in the messages list view.
     */
    private class ProblemSourceColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            Problem.SourceType messageType = (Problem.SourceType) source.getContainerProperty(itemId, ProblemConstants.SOURCE_PID).getValue();

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

}
