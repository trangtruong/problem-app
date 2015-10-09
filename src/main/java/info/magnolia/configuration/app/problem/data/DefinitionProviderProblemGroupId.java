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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.overview.data.ConfigConstants;
import info.magnolia.configuration.app.overview.data.Id;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.layered.LayeredResource;

import java.util.Collection;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Each DefinitionProviderProblemGroupId will be a DefinitionProvider.
 * And each DefinitionProvider will contain a list of Problems.
 */
public class DefinitionProviderProblemGroupId extends DefinitionProviderProblemId {

    private static Logger log = LoggerFactory.getLogger(DefinitionProviderProblemGroupId.class);

    private ResourceOrigin origin;

    private String style;

    private Collection<DefinitionProviderProblemId> children = null;

    public DefinitionProviderProblemGroupId(BaseId parent, DefinitionProvider provider, String originName, Problem problem) {
        super(parent, provider, originName, problem);
    }

    @Override
    public Collection<? extends Id> getChildren() {
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
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = Maps.newHashMap();
        result.put(ConfigConstants.TITLE_PID, getName());
        return result;
    }

    @Override
    public String getStyle() {
        if (style == null || style.isEmpty()) {
            return "icon-folder";
        }
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
