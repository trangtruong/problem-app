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
package info.magnolia.configuration.app.overview.data;

import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.util.Noun;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
* DefinitionRawViewId.
*/
public class DefinitionRawViewId extends Id.BaseId<DefinitionRawView.Property> {

    private static Logger log = LoggerFactory.getLogger(DefinitionRawViewId.class);

    private RawViewPropertyToId rawViewPropertyToIdConverter = new RawViewPropertyToId();

    public DefinitionRawViewId(Id parent, DefinitionRawView.Property value) {
        super(value.getName(), parent, value);
    }

    @Override
    public Collection<? extends Id> getChildren() {
        List<DefinitionRawViewId> result;
        switch (getValue().getKind()) {
        case collection:
            result  = FluentIterable.from(getValue().getCollection()).transform(rawViewPropertyToIdConverter).toList();
            break;
        case subBean:
            result = FluentIterable.from(tryGetProperties(getValue().getSubRawView())).transform(rawViewPropertyToIdConverter).toSortedList(new Comparator<DefinitionRawViewId>() {
                @Override
                public int compare(DefinitionRawViewId o1, DefinitionRawViewId o2) {
                    Boolean b1 = o1.getValue().getKind() == DefinitionRawView.Kind.simple;
                    Boolean b2 = o2.getValue().getKind() == DefinitionRawView.Kind.simple;
                    return b1.compareTo(b2);
                }
            });
            break;
        default:
            result = Collections.EMPTY_LIST;
        }

        return result;
    }

    @Override
    public String getName() {
        return resolveName();
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = Maps.newHashMap();
        if (getValue().getKind() == DefinitionRawView.Kind.simple) {
            result.put(ConfigConstants.VALUE_PID, getValue().getSimpleValue());
        }

        result.put(ConfigConstants.TITLE_PID, getName());

        return result;
    }

    @Override
    public String getStyle() {
        switch (getValue().getKind()) {
        case collection:
            return "icon-add-node-content";
        case subBean:
            return "icon-node-content";
        default:
            return "icon-node-data italic";
        }
    }

    @Override
    public int getChildAmount() {
        switch (getValue().getKind()) {
        case collection:
            return getValue().getCollection().size();
        case subBean:
            final DefinitionRawView subRawView = getValue().getSubRawView();
            if (subRawView != null && tryGetProperties(subRawView) != null) {
                return tryGetProperties(subRawView).size();
            }
        default:
            return 0;
        }
    }

    private List<DefinitionRawView.Property> tryGetProperties(DefinitionRawView subRawView) {
        try {
            return subRawView.properties();
        } catch (Exception e) {
            log.debug(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String resolveName() {
        String name = super.getName();
        if (name == null && getValue().getKind() == DefinitionRawView.Kind.subBean) {
            final Optional<DefinitionRawView.Property> propertyOptional = Iterables.tryFind(tryGetProperties(getValue().getSubRawView()), new NamePropertyPredicate());

            if (propertyOptional.isPresent()) {
                name = propertyOptional.get().getSimpleValue();
            } else {
                final Id parent = getParent();
                if (parent instanceof DefinitionRawViewId) {
                    final DefinitionRawViewId parentRawViewId = (DefinitionRawViewId)parent;
                    if (parentRawViewId.getValue().getKind() == DefinitionRawView.Kind.collection) {
                        final int index = Iterables.indexOf(parentRawViewId.getValue().getCollection(), new IsCurrentRawViewValue());
                        name = Noun.singularOf(parentRawViewId.getName()) + (index + 1);
                    }
                }
            }
        }
        return name;
    }

    private class IsCurrentRawViewValue implements Predicate<DefinitionRawView.Property> {
        @Override
        public boolean apply(@Nullable DefinitionRawView.Property input) {
            return input == getValue();
        }
    }

    private static class NamePropertyPredicate implements Predicate<DefinitionRawView.Property> {
        @Override
        public boolean apply(DefinitionRawView.Property input) {
            return "name".equalsIgnoreCase(input.getName());
        }
    }

    private class RawViewPropertyToId implements Function<DefinitionRawView.Property, DefinitionRawViewId> {

        @Override
        public DefinitionRawViewId apply(DefinitionRawView.Property input) {
            return new DefinitionRawViewId(DefinitionRawViewId.this, input);
        }
    }
}
