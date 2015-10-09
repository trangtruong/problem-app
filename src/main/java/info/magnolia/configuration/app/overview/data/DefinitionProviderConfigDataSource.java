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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.resourceloader.ResourceOrigin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * {@link ConfigurationDataSource} implementation based on the sets of
 * {@link info.magnolia.config.registry.DefinitionProvider}.
 *
 * <p>Uses the following hierarchy organisation:
 * <ul>
 * <li>Root level nodes represent the groups of {@link info.magnolia.config.registry.DefinitionProvider}.
 * Such nodes correspond to {@link DefinitionProviderGroupId}.
 * </li>
 * <li>Direct children of roots represent instances {@link info.magnolia.config.registry.DefinitionProvider}
 * so that such properties as 'module', 'status', 'source', 'title' etc are easily accessible.
 * Such nodes correspond to {@link DefinitionProviderId}.
 * </li>
 * <li>The rest of the nodes represent the sub-definitions and properties
 * by means of {@link DefinitionRawViewId}.
 * <li/>
 * </p>
 */
public class DefinitionProviderConfigDataSource implements ConfigurationDataSource<Id> {

    private ListMultimap<String, DefinitionProvider> providers = ArrayListMultimap.create();

    private ResourceOrigin origin;

    public DefinitionProviderConfigDataSource(ResourceOrigin origin) {
        this.origin = origin;
    }

    public void registerDefinitionProviderGroup(String name, Iterable<? extends DefinitionProvider> providers) {
        this.providers.putAll(name, providers);
    }

    @Override
    public Collection<?> getSupportedItemProperties() {
        return Arrays.asList(ConfigConstants.PID_ORDER);
    }

    @Override
    public Collection<Id> getRootIds() {
        return Lists.newArrayList(Iterables.transform(providers.asMap().entrySet(), new Function<Map.Entry<String,Collection<DefinitionProvider>>, Id>() {
            @Override
            public Id apply(@Nullable Map.Entry<String, Collection<DefinitionProvider>> input) {
                return new DefinitionProviderGroupId(input.getKey(), input.getValue(), origin);
            }
        }));
    }

    @Override
    public Collection<? extends Id<?>> getChildIds(Id id) {
        return id.getChildren();
    }

    @Override
    public Id getParentId(Id id) {
        return id.getParent();
    }

    @Override
    public boolean isNodeId(Id id) {
        return id.getChildAmount() > 0;
    }

    @Override
    public boolean isRootId(Id id) {
        return id.getParent() == null;
    }

    @Override
    public int getChildAmount(Id id) {
        return id.getChildAmount();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item getVaadinItem(Id id) {
        final Item item = new PropertysetItem();
        final Map<String, Object> properties = id.getProperties();
        final Iterator<Map.Entry<String, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Object> entry = it.next();
            item.addItemProperty(entry.getKey(), new ObjectProperty<Object>(entry.getValue(), Object.class));
        }
        return item;
    }

    public void reset() {
        providers.clear();
    }

    public DefinitionProviderId getDefinitionProviderId(Id id) {
        Id result = id;
        while (result != null && !(result instanceof DefinitionProviderId)) {
            result = result.getParent();
        }
        return (DefinitionProviderId) result;
    }
}
