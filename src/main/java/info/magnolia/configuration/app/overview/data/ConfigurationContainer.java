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
package info.magnolia.configuration.app.overview.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Collapsible;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Vaadin hierarchical {@link com.vaadin.data.Container} implementation used in
 * conjunction with {@link ConfigurationDataSource}.
 */
public class ConfigurationContainer implements Collapsible, Container.Indexed {

    private List<Object> expandedItems = new LinkedList<Object>();

    private final List<Object> visibleIds = new LinkedList<Object>();

    private ConfigurationDataSource dataSource;

    public ConfigurationContainer(ConfigurationDataSource dataSource) {
        this.dataSource = dataSource;
        this.visibleIds.addAll(dataSource.getRootIds());
    }

    @Override
    public void setCollapsed(Object itemId, boolean collapsed) {
        if (collapsed) {
            removeVisibleIdsRecursively(itemId);
        } else {
            expandedItems.add(itemId);
            int parentIndex = visibleIds.indexOf(itemId);
            visibleIds.addAll(parentIndex + 1, getChildren(itemId));
        }
    }

    private void removeVisibleIdsRecursively(Object itemId) {
        expandedItems.remove(itemId);
        Collection<?> children = getChildren(itemId);
        for (final Object child : children) {
            visibleIds.remove(child);
            if (expandedItems.contains(child)) {
                removeVisibleIdsRecursively(child);
            }
        }
    }

    @Override
    public boolean isCollapsed(Object itemId) {
        if (expandedItems.contains(itemId)) {
            return false;
        }

        return true;
    }

    @Override
    public Collection<?> getChildren(Object itemId) {
        return dataSource.getChildIds(itemId);
    }

    @Override
    public Object getParent(Object itemId) {
        return dataSource.getParentId(itemId);
    }

    @Override
    public Collection<?> rootItemIds() {
        return dataSource.getRootIds();
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        return dataSource.isNodeId(itemId);
    }

    @Override
    public boolean isRoot(Object itemId) {
        return dataSource.isRootId(itemId);
    }

    @Override
    public boolean hasChildren(Object itemId) {
        return dataSource.getChildAmount(itemId) > 0;
    }


    @Override
    public Item getItem(Object itemId) {
        return dataSource.getVaadinItem(itemId);
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return dataSource.getSupportedItemProperties();
    }

    @Override
    public Collection<?> getItemIds() {
        return visibleIds;
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        Item item = getItem(itemId);
        return item == null ? null : item.getItemProperty(propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return String.class;
    }

    @Override
    public int size() {
        int size = dataSource.getRootIds().size();
        for (Object expandedObject : expandedItems) {
            size += dataSource.getChildAmount(expandedObject);
        }
        return size;
    }


    //// ORDERED
    @Override
    public boolean containsId(Object itemId) {
        return visibleIds.contains(itemId);
    }


    @Override
    public Object nextItemId(Object itemId) {
        return visibleIds.get(visibleIds.indexOf(itemId) + 1);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return visibleIds.get(visibleIds.indexOf(itemId) - 1);
    }

    @Override
    public Object firstItemId() {
        return visibleIds.get(0);
    }

    @Override
    public Object lastItemId() {
        return visibleIds.get(visibleIds.size() - 1);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return itemId == visibleIds.get(0);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return itemId == visibleIds.get(visibleIds.size() - 1);
    }


    /// INDEXED
    @Override
    public int indexOfId(Object itemId) {
        return visibleIds.indexOf(itemId);
    }

    @Override
    public Object getIdByIndex(int index) {
        return visibleIds.get(index);
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return visibleIds.subList(startIndex, numberOfItems);
    }


    /**
     * UNSUPPORTED OPS.
     */
    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
