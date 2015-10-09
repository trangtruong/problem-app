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
package info.magnolia.configuration.app.overview;

import info.magnolia.configuration.app.overview.data.DefinitionProviderConfigDataSource;
import info.magnolia.configuration.app.overview.data.DefinitionProviderId;
import info.magnolia.configuration.app.overview.data.Id;
import info.magnolia.configuration.app.overview.filebrowser.FileBrowserHelper;
import info.magnolia.configuration.app.overview.toolbar.FilterContext;
import info.magnolia.configuration.app.overview.toolbar.ToolbarPresenter;
import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.config.registry.Registry;
import info.magnolia.config.registry.RegistryFacade;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.configuration.app.overview.data.ConfigurationContainer;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.framework.app.BaseSubApp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Overview config-app sub-app. Communicates with the {@link ConfigOverviewView} via {@link ConfigOverviewView.Presenter}
 * interface.
 *
 * Responsible for populating the data-source for the view's tabular components.
 */
public class ConfigOverviewSubApp extends BaseSubApp<ConfigOverviewView> implements ConfigOverviewView.Presenter {

    public static final Pattern PATH_PATTERN = Pattern.compile(".*");
    public static final String NO_DEFINITIONS_SELECTED = "No definitions selected";
    private final ResourceOrigin origin;

    private final ModuleRegistry moduleRegistry;

    private final RegistryFacade registryFacade;

    private final ToolbarPresenter toolbarPresenter;

    private final LocationController locationController;

    private ConfigOverviewView.DefinitionAggregationType groupBy;

    private FilterContext filter;

    private String currentLocation;

    private DefinitionProviderConfigDataSource dataSource;

    private FileBrowserHelper fileBrowserHelper;

    @Inject
    public ConfigOverviewSubApp(
            SubAppContext subAppContext,
            ConfigOverviewView view, ModuleRegistry moduleRegistry,
            RegistryFacade registryFacade,
            ToolbarPresenter toolbarPresenter, ResourceOrigin origin, final LocationController locationController) {
        super(subAppContext, view);
        this.moduleRegistry = moduleRegistry;
        this.registryFacade = registryFacade;
        this.toolbarPresenter = toolbarPresenter;
        this.locationController = locationController;
        this.fileBrowserHelper = new FileBrowserHelper(getSubAppContext());
        this.origin  = origin;
        this.dataSource = new DefinitionProviderConfigDataSource(origin);
    }

    @Override
    protected void onSubAppStart() {
        super.onSubAppStart();
        toolbarPresenter.setConfigPresenter(this);
        toolbarPresenter.getView().setJcrConfigNavigationAvailable(false);
        toolbarPresenter.getView().setFilePreviewAvailable(false);

        getView().setToolbar(toolbarPresenter.start());
        getView().setPresenter(this);
        getView().setStatus(NO_DEFINITIONS_SELECTED);
        groupBy(ConfigOverviewView.DefinitionAggregationType.registry);
    }

    @Override
    public void onSelectionChanged(final Id id) {
        if (id == null) {
            getView().setStatus(NO_DEFINITIONS_SELECTED);
            toolbarPresenter.getView().setJcrConfigNavigationAvailable(false);
            toolbarPresenter.getView().setFilePreviewAvailable(false);
        }

        final DefinitionProviderId definitionProviderId = dataSource.getDefinitionProviderId(id);
        if (definitionProviderId != null) {
            final DefinitionProvider definitionProvider = definitionProviderId.getValue();
            this.currentLocation = definitionProvider.getMetadata().getLocation();
            String status = "<b>Location:</b> " + currentLocation;
            if (!definitionProvider.isValid()) {
                status += " <b>Issues</b>: " + Joiner.on(" ").join(definitionProvider.getErrorMessages());
            }
            getView().setStatus(status);
            toolbarPresenter.getView().setJcrConfigNavigationAvailable(nodeExists(currentLocation));
            toolbarPresenter.getView().setFilePreviewAvailable(origin.hasPath(currentLocation));
        }
    }

    private boolean nodeExists(String path) {
        try {
            return MgnlContext.getJCRSession(RepositoryConstants.CONFIG).itemExists(path);
        } catch (RepositoryException e) {
            return false;
        }
    }

    private String getCapitalizedPluralName(DefinitionType definitionType) {
        return StringUtils.capitalize(English.plural(definitionType.name()).toLowerCase());
    }

    @Override
    public void groupBy(ConfigOverviewView.DefinitionAggregationType type) {
        this.groupBy = type;
        updateView();
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
        locationController.goTo(new BrowserLocation("configuration", "browser", currentLocation));
    }

    private void updateView() {
        dataSource.reset();

        final Map<String, Iterable<? extends DefinitionProvider>> providerMap = Maps.newLinkedHashMap();
        switch (groupBy) {
        case module:
            for (final ModuleDefinition definition : moduleRegistry.getModuleDefinitions()) {
                providerMap.put(definition.getName(),  registryFacade.byModule(definition.getName()));
            }
            break;

        case registry:
            for (final Registry<?> registry : registryFacade.all()) {
                final DefinitionType definitionType = registry.type();
                providerMap.put(getCapitalizedPluralName(definitionType), registry.getAllProviders());
            }
            break;

        }

        final Iterator<Map.Entry<String,Iterable<? extends DefinitionProvider>>> it = providerMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String,Iterable<? extends DefinitionProvider>> entry = it.next();
            dataSource.registerDefinitionProviderGroup(entry.getKey(), Iterables.filter(entry.getValue(), new Predicate<DefinitionProvider>() {

                private boolean blankOrContains(String src, String sample) {
                    return StringUtils.isBlank(sample) || src.contains(sample);
                }

                @Override
                public boolean apply(DefinitionProvider input) {
                    final DefinitionMetadata metadata = input.getMetadata();
                    if (filter == null) {
                        return true;
                    }
                    return blankOrContains(metadata.getName(), filter.getName()) &&
                            blankOrContains(metadata.getModule(), filter.getModuleName()) &&
                            (filter.getDefinitionType() == null || filter.getDefinitionType() == metadata.getType()) &&
                            (!filter.isErrorsOnly() || !input.isValid()) &&
                            (!filter.isFileBasedOnly() || origin.hasPath(metadata.getLocation()));

                }
            }));

        }

        getView().setConfigDataSource(new ConfigurationContainer(dataSource));
        getView().setStatus(NO_DEFINITIONS_SELECTED);
        currentLocation = null;
    }
}
