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

package info.magnolia.configuration.app.problem.data;

import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.overview.data.ConfigConstants;

import java.util.Date;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

public class ProblemContainer<T> {

    protected boolean grouping = false;
    protected HierarchicalContainer container;

    public HierarchicalContainer createDataSource(List<Problem> messages) {
        container = new HierarchicalContainer();
        container.addContainerProperty(ConfigConstants.PROBLEM_PID, String.class, null);
        container.addContainerProperty(ConfigConstants.TYPE_PID, Problem.Type.class, null);
        container.addContainerProperty(ConfigConstants.ELEMENT_PID, String.class, null);
        container.addContainerProperty(ConfigConstants.MODULE_PID, String.class, null);
        container.addContainerProperty(ConfigConstants.SOURCE_PID, Problem.SourceType.class, null);
        container.addContainerProperty(ConfigConstants.TIMESTAMP_PID, Date.class, null);

        createSuperItems();

        for (int i = 0 ; i < messages.size(); i++) {
            addBeanAsItem(messages.get(i), i);
        }

        buildTree();
        return container;
    }

    protected void createSuperItems() {
        for (Problem.SourceType type : Problem.SourceType.values()) {
            Object itemId = getSuperItem(type);
            Item item = container.addItem(itemId);
            item.getItemProperty(ConfigConstants.SOURCE_PID).setValue(type);
            container.setChildrenAllowed(itemId, true);
        }
    }

    private Object getSuperItem(Problem.SourceType type) {
        return ConfigConstants.GROUP_PLACEHOLDER_ITEMID + type;
    }

    public void addBeanAsItem(Problem message, int i) {
        final Item item = container.addItem(i);
        container.setChildrenAllowed(i, false);
        assignPropertiesFromBean(message, item);
    }

    public void assignPropertiesFromBean(Problem message, Item item) {
        if (item != null && message != null) {
            item.getItemProperty(ConfigConstants.PROBLEM_PID).setValue(message.getMessage());
            item.getItemProperty(ConfigConstants.TYPE_PID).setValue(message.getType());
            item.getItemProperty(ConfigConstants.ELEMENT_PID).setValue(message.getElement());
            item.getItemProperty(ConfigConstants.MODULE_PID).setValue("");
            item.getItemProperty(ConfigConstants.SOURCE_PID).setValue(message.getSourceType());
            item.getItemProperty(ConfigConstants.TIMESTAMP_PID).setValue((message.getTimestamp()));
        }
    }

    /*
     * Assign messages under correct parents so that
     * grouping works.
    */
    public void buildTree() {

        for (Object itemId : container.getItemIds()) {
            // Skip super items
            if (!itemId.toString().startsWith(ConfigConstants.GROUP_PLACEHOLDER_ITEMID)) {
                Item item = container.getItem(itemId);
                Problem.SourceType type = (Problem.SourceType) item.getItemProperty(ConfigConstants.SOURCE_PID).getValue();
                Object parentItemId = getSuperItem(type);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }
}
