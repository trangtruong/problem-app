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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ListMultimap;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

public class ProblemContainer implements Serializable {

    private HierarchicalContainer container;

    public HierarchicalContainer createDataSource(ListMultimap<String, Problem> problems) {
        container = new HierarchicalContainer();
        container.addContainerProperty(ProblemConstants.PROBLEM_PID, String.class, null);
        container.addContainerProperty(ProblemConstants.TYPE_PID, Problem.Type.class, null);
        container.addContainerProperty(ProblemConstants.ELEMENT_PID, String.class, null);
        container.addContainerProperty(ProblemConstants.MODULE_PID, String.class, null);
        container.addContainerProperty(ProblemConstants.SOURCE_PID, Problem.SourceType.class, null);
        container.addContainerProperty(ProblemConstants.TIMESTAMP_PID, Date.class, null);

        initProblemsTree(problems);

        return container;
    }

    private void initProblemsTree(ListMultimap<String, Problem> problems) {
        for (String type : problems.keySet()) {
            List<Problem> groupedProblems = problems.get(type);

            Object groupItemId = getGroupItem(type);
            Item groupItem = container.addItem(groupItemId);
            groupItem.getItemProperty(ProblemConstants.PROBLEM_PID).setValue(type + ProblemConstants.NUMBER_ITEM_SEPARATOR + groupedProblems.size());
            container.setChildrenAllowed(groupItemId, false);

            for (Problem problem : groupedProblems) {
                Object itemId = container.addItem();
                container.setChildrenAllowed(itemId, false);
                container.setParent(itemId, groupItemId);
                assignPropertiesFromBean(problem, container.getItem(itemId));
            }
        }
    }

    private Object getGroupItem(String type) {
        return ProblemConstants.GROUP_PLACEHOLDER_ITEM_ID + type;
    }

    public void assignPropertiesFromBean(Problem problem, Item item) {
        if (item != null && problem != null) {
            item.getItemProperty(ProblemConstants.PROBLEM_PID).setValue(problem.getMessage());
            item.getItemProperty(ProblemConstants.TYPE_PID).setValue(problem.getType());
            item.getItemProperty(ProblemConstants.ELEMENT_PID).setValue(problem.getElement());
            item.getItemProperty(ProblemConstants.MODULE_PID).setValue("");//TODO add module
            item.getItemProperty(ProblemConstants.SOURCE_PID).setValue(problem.getSourceType());
            item.getItemProperty(ProblemConstants.TIMESTAMP_PID).setValue((problem.getTimestamp()));
        }
    }
}
