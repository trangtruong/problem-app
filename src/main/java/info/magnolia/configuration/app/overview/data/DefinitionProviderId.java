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
import info.magnolia.config.registry.DefinitionRawView;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
* DefinitionProviderId.
*/
public class DefinitionProviderId extends Id.BaseId<DefinitionProvider> {

    private DefinitionProvider<?> provider;

    private DefinitionRawViewId delegate;

    private String originName;

    public DefinitionProviderId(BaseId parent, DefinitionProvider provider, String originName) {
        super(provider.getMetadata().getName(), parent, provider);
        this.provider = provider;
        this.originName = originName;
        this.delegate = new DefinitionRawViewId(this, DefinitionRawView.Property.subBean(this.provider.getMetadata().getName(), this.provider.getRaw()));
    }

    @Override
    public DefinitionProvider getValue() {
        return provider;
    }

    @Override
    public String getTooltip() {
        if (!getValue().isValid()) {
            return Joiner.on(" ").join(getValue().getErrorMessages());
        }
        return super.getTooltip();
    }

    @Override
    public Collection<? extends Id> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public int getChildAmount() {
        return delegate.getChildAmount();
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = Maps.newHashMap();
        result.put(ConfigConstants.TITLE_PID, getName());
        result.put(ConfigConstants.MODULE_PID, provider.getMetadata().getModule());
        final String type = String.valueOf(provider.getMetadata().getType().name());
        result.put(ConfigConstants.TYPE_PID, StringUtils.capitalize(type.toLowerCase()));
        result.put(ConfigConstants.ORIGIN_PID, originName);

        if (!getValue().isValid()) {
            result.put(ConfigConstants.VALUE_PID, Joiner.on(" ").join(getValue().getErrorMessages()));
        }
        return result;
    }

    @Override
    public String getStyle() {
        String style = getValue().isValid() ? "icon-node-folder" : "icon-error";
        if (!getValue().isValid()) {
            style += " invalid";
        }
        return style;
    }
}
