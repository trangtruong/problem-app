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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.layered.LayeredResource;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

/**
 * .
 */
public class DefinitionProviderGroupId extends Id.BaseId<Collection<DefinitionProvider>> {

    private static Logger log = LoggerFactory.getLogger(DefinitionProviderGroupId.class);

    private Collection<Id> children = null;

    private ResourceOrigin origin;

    public DefinitionProviderGroupId(String name, Collection<DefinitionProvider> value, ResourceOrigin origin) {
        super(name, null, value);
        this.origin = origin;
    }

    @Override
    public Collection<? extends Id> getChildren() {
        if (children == null) {
            children = Collections2.transform(getValue(), new Function<DefinitionProvider, Id>() {
                @Nullable
                @Override
                public Id apply(DefinitionProvider input) {
                    return new DefinitionProviderId(DefinitionProviderGroupId.this, input, resolveOriginName(input));
                }
            });
        }
        return children;
    }

    private String resolveOriginName(DefinitionProvider input) {
        final String location = input.getMetadata().getLocation();

        if (origin.hasPath(location)) {
            final Resource path = origin.getByPath(location);
            if (path instanceof LayeredResource) {
                return ((LayeredResource) path).getFirst().getOrigin().getName();
            } else {
                path.getOrigin().getName();
            }
        }

        try {
            if (MgnlContext.getJCRSession(RepositoryConstants.CONFIG).nodeExists(location)) {
                return "JCR node";
            }
        } catch (RepositoryException e) {
            return null;
        }
        return null;
    }

    @Override
    public int getChildAmount() {
        return getValue().size();
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = Maps.newHashMap();
        result.put(ConfigConstants.TITLE_PID, getName());
        return result;
    }

    @Override
    public String getStyle() {
        return "icon-folder";
    }
}
