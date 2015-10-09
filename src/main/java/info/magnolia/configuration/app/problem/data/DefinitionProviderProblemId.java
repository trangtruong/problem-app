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
import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.overview.data.ConfigConstants;
import info.magnolia.configuration.app.overview.data.DefinitionRawViewId;
import info.magnolia.configuration.app.overview.data.Id;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
* DefinitionProviderId.
*/
public class DefinitionProviderProblemId extends Id.BaseId<Problem> {

    private DefinitionProvider provider;

    private DefinitionRawViewId delegate;

    private String originName;
    private String style;
    private Problem problem;

    public DefinitionProviderProblemId(BaseId parent, DefinitionProvider provider, String originName, Problem problem) {
        super(provider.getMetadata().getName(), null, problem);
        this.provider = provider;
        this.originName = originName;
        this.problem = problem;
        this.delegate = new DefinitionRawViewId(this, DefinitionRawView.Property.subBean(this.provider.getMetadata().getName(), this.provider.getRaw()));
    }

    @Override public Collection<? extends Id> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public Problem getValue() {
        return problem;
    }

    @Override public int getChildAmount() {
        return delegate.getChildAmount();
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = Maps.newHashMap();
        result.put(ConfigConstants.PROBLEM_PID, problem.getMessage());
        result.put(ConfigConstants.ELEMENT_PID, problem.getElement());
        result.put(ConfigConstants.MODULE_PID, provider.getMetadata().getModule());
        final String type = String.valueOf(provider.getMetadata().getType().name());
        result.put(ConfigConstants.SOURCE_PID, StringUtils.capitalize(type.toLowerCase()));
        result.put(ConfigConstants.TIMESTAMP_PID, StringUtils.EMPTY);

        return result;
    }

    @Override
    public String getStyle() {
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
