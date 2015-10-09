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
package info.magnolia.configuration.app.overview.toolbar;

import info.magnolia.configuration.app.overview.ConfigOverviewView;
import info.magnolia.configuration.app.overview.ConfigPresenter;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.config.registry.Registry;
import info.magnolia.config.registry.RegistryFacade;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * ToolbarPresenter.
 */
public class ToolbarPresenter implements ToolbarView.Callback {

    private ToolbarView view;
    private ModuleRegistry moduleRegistry;
    private RegistryFacade registryFacade;
    private ConfigPresenter configPresenter;

    @Inject
    public ToolbarPresenter(ToolbarView view, ModuleRegistry moduleRegistry, RegistryFacade registryFacade) {
        this.view = view;
        this.moduleRegistry = moduleRegistry;
        this.registryFacade = registryFacade;
    }

    public void setConfigPresenter(ConfigPresenter configPresenter) {
        this.configPresenter = configPresenter;
    }

    public ToolbarView start() {
        this.view.setModuleNames(getModuleNames());
        this.view.setCallback(this);
        this.view.setDefinitionTypes(getDefinitionTypes());
        return this.view;
    }

    private List<DefinitionType> getDefinitionTypes() {
        return FluentIterable.from(registryFacade.all()).transform(new Function<Registry, DefinitionType>() {
            @Override
            public DefinitionType apply(Registry input) {
                return input.type();
            }
        }).toSet().asList();
    }

    private List<String> getModuleNames() {
        return Lists.transform(moduleRegistry.getModuleDefinitions(), new Function<ModuleDefinition, String>() {
            @Override
            public String apply(ModuleDefinition input) {
                return input.getName();
            }
        });
    }

    @Override
    public void updateFilter() {
        final FilterContext filterContext = FilterContext.builder().
                withDefinitionType(view.getDefinitionType()).
                withModuleName(view.getModule()).
                withName(view.getName()).
                withErrorsOnly(view.isWithErrorsOnly()).
                withFileBasedOnly(view.isFileBasedOnly()).
                build();

        configPresenter.filterBy(filterContext);
    }

    @Override
    public void updateGroupBy() {
        configPresenter.groupBy(view.isGroupByModules() ? ConfigOverviewView.DefinitionAggregationType.module : ConfigOverviewView.DefinitionAggregationType.registry);
    }

    @Override
    public void showFile() {
        configPresenter.showSelectedDefinitionFile();
    }

    @Override
    public void openJcrConfig() {
        configPresenter.showSelectedDefinitionInJcr();
    }

    public ToolbarView getView() {
        return view;
    }
}
