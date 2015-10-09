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
package info.magnolia.configuration.app.problem;

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.config.registry.RegistryFacade;
import info.magnolia.config.source.Problem;
import info.magnolia.configuration.app.overview.ConfigOverviewView;
import info.magnolia.configuration.app.overview.filebrowser.FileBrowserHelper;
import info.magnolia.configuration.app.overview.toolbar.FilterContext;
import info.magnolia.configuration.app.overview.toolbar.ToolbarPresenter;
import info.magnolia.configuration.app.problem.data.ProblemContainer;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.framework.app.BaseSubApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;

import com.google.common.collect.Maps;

/**
 * Problem config-app sub-app. Communicates with the {@link info.magnolia.configuration.app.problem.ConfigProblemView} via {@link info.magnolia.configuration.app.problem.ConfigProblemView.Presenter}
 * interface.
 *
 * Responsible for populating the data-source for the view's tabular components.
 */
public class ConfigProblemSubApp extends BaseSubApp<ConfigProblemView> implements ConfigProblemView.Presenter {

    public static final String NO_DEFINITIONS_SELECTED = "No definitions selected";
    private final ResourceOrigin origin;

    private final ModuleRegistry moduleRegistry;

    private final RegistryFacade registryFacade;

    private final ToolbarPresenter toolbarPresenter;

    private final LocationController locationController;

    private ConfigProblemView.SourceType groupBy;

    private FilterContext filter;

    private String currentLocation;

    private ProblemContainer dataSource;

    private FileBrowserHelper fileBrowserHelper;

    @Inject
    public ConfigProblemSubApp(
            SubAppContext subAppContext,
            ConfigProblemView view, ModuleRegistry moduleRegistry,
            RegistryFacade registryFacade,
            ToolbarPresenter toolbarPresenter, ResourceOrigin origin, final LocationController locationController) {
        super(subAppContext, view);
        this.moduleRegistry = moduleRegistry;
        this.registryFacade = registryFacade;
        this.toolbarPresenter = toolbarPresenter;
        this.locationController = locationController;
        this.fileBrowserHelper = new FileBrowserHelper(getSubAppContext());
        this.origin  = origin;
        this.dataSource = new ProblemContainer();
    }

    @Override
    protected void onSubAppStart() {
        super.onSubAppStart();
        toolbarPresenter.setConfigPresenter(this);

        getView().setToolbar(toolbarPresenter.start());
        getView().setPresenter(this);
        groupBy(ConfigProblemView.SourceType.source);
    }

    private String getCapitalizedPluralName(DefinitionType definitionType) {
        return StringUtils.capitalize(English.plural(definitionType.name()).toLowerCase());
    }

//    @Override
    public void groupBy(ConfigProblemView.SourceType type) {
        this.groupBy = type;
        updateView();
    }

    @Override public void groupBy(ConfigOverviewView.DefinitionAggregationType type) {

    }

    @Override
    public void filterBy(FilterContext context) {
        this.filter = context;
        updateView();
    }

    @Override
    public void showSelectedDefinitionFile() {
        try {
            this.fileBrowserHelper.showFile(origin.getByPath(currentLocation).openReader());
        } catch (IOException e) {
            getView().setStatus("Failed to open file: " + e.getMessage());
        }
    }

    @Override
    public void showSelectedDefinitionInJcr() {
    }

    private void updateView() {
        final Map<String, Iterable<? extends DefinitionProvider>> providerMap = Maps.newLinkedHashMap();
        List<Problem> problemList = new ArrayList<Problem>();

        switch (groupBy) {
        case source:
            for (final ModuleDefinition definition : moduleRegistry.getModuleDefinitions()) {
                Collection<DefinitionProvider> definitionProviders = registryFacade.byModule(definition.getName());
                for (DefinitionProvider defProvider : definitionProviders) {
                    if (!defProvider.getErrorMessages().isEmpty()) {
                        problemList.addAll(defProvider.getErrorMessages());
                    }
                }
            }
            break;

        case severity:
            break;
        }

        getView().setDataSource(dataSource.createDataSource(problemList));
        getView().setStatus(NO_DEFINITIONS_SELECTED);
        currentLocation = null;
    }
}
