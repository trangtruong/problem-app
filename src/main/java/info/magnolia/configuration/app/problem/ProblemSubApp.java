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
import info.magnolia.configuration.app.problem.data.ProblemContainer;
import info.magnolia.configuration.app.problem.toolbar.ProblemToolbarPresenter;
import info.magnolia.i18nsystem.SimpleTranslator;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

/**
 * Problem config-app sub-app. Communicates with the {@link info.magnolia.configuration.app.problem.ProblemView} via {@link info.magnolia.configuration.app.problem.ProblemPresenter} interface.
 * Responsible for populating the data-source for the view's tabular components.
 */
public class ProblemSubApp extends BaseSubApp<ProblemView> implements ProblemPresenter {

    private final ResourceOrigin origin;

    private final ModuleRegistry moduleRegistry;

    private final RegistryFacade registryFacade;

    private final ProblemToolbarPresenter toolbarPresenter;

    private final LocationController locationController;

    private final ProblemContainer container;

    private ProblemView.SourceType groupBy = ProblemView.SourceType.source;

    private String searchExpression;

    private Problem.SourceType fromSourceType;

    private String currentLocation;

    private FileBrowserHelper fileBrowserHelper;

    private SimpleTranslator i18n;

    @Inject
    public ProblemSubApp(
            SubAppContext subAppContext,
            ProblemView view, ModuleRegistry moduleRegistry,
            RegistryFacade registryFacade,
            ProblemToolbarPresenter toolbarPresenter,
            ResourceOrigin origin, final LocationController locationController, SimpleTranslator i18n) {
        super(subAppContext, view);
        this.moduleRegistry = moduleRegistry;
        this.registryFacade = registryFacade;
        this.toolbarPresenter = toolbarPresenter;
        this.locationController = locationController;
        this.fileBrowserHelper = new FileBrowserHelper(getSubAppContext());
        this.origin = origin;
        this.container = new ProblemContainer();
        this.i18n = i18n;
    }

    @Override
    protected void onSubAppStart() {
        super.onSubAppStart();
        toolbarPresenter.setProblemPresenter(this);

        getView().setToolbar(toolbarPresenter.start());
        getView().setPresenter(this);
        updateView();
    }

    private String getCapitalizedPluralName(DefinitionType definitionType) {
        return StringUtils.capitalize(English.plural(definitionType.name()).toLowerCase());
    }

    @Override
    public void groupBy(ConfigOverviewView.DefinitionAggregationType type) {
        //Do nothing
    }

    @Override
    public void filterBy(FilterContext context) {
        // Do nothing
    }

    @Override
    public void showSelectedDefinitionFile() {//TODO: need to have button to open
        try {
            this.fileBrowserHelper.showFile(origin.getByPath(currentLocation).openReader());
        } catch (IOException e) {
            getView().setStatus("Failed to open file: " + e.getMessage());
        }
    }

    @Override
    public void showSelectedDefinitionInJcr() {//TODO: need to have button to open
    }

    private void updateView() {
        final Map<String, Iterable<? extends DefinitionProvider>> providerMap = Maps.newLinkedHashMap();
        ListMultimap<String, Problem> problems = ArrayListMultimap.create();
        for (final ModuleDefinition definition : moduleRegistry.getModuleDefinitions()) {
            Collection<DefinitionProvider> definitionProviders = registryFacade.byModule(definition.getName());
            for (DefinitionProvider defProvider : definitionProviders) {
                List<Problem> problemList = defProvider.getErrorMessages();
                for (Problem p : problemList) {
                    if (!isMatchedSearchExpression(p)) {
                        continue;
                    }

                    switch (groupBy) {
                    case source:
                        problems.put(p.getSourceType().name(), p);
                        break;
                    case severity:
                        if (fromSourceType == null || p.getSourceType() == fromSourceType) {
                            problems.put(p.getSeverityLevel().name(), p);
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        getView().setDataSource(container.createDataSource(problems));
        getView().setStatus(createStatus(problems));
        currentLocation = null;

        getView().refresh();
    }

    @Override
    public void groupBy(ProblemView.SourceType sourceType, Problem.SourceType fromSourceType) {
        this.groupBy = sourceType;
        this.fromSourceType = fromSourceType;
        updateView();
    }

    @Override
    public void searchBy(String searchExpression) {
        this.searchExpression = searchExpression;
        updateView();
    }

    private boolean isMatchedSearchExpression(Problem problem) {
        if (StringUtils.isNotBlank(searchExpression) && problem != null) {
            return StringUtils.containsIgnoreCase(problem.getElement(), searchExpression) ||
                    StringUtils.containsIgnoreCase(problem.getMessage(), searchExpression) ||
                    StringUtils.containsIgnoreCase(problem.getSeverityLevel().name(), searchExpression) ||
                    StringUtils.containsIgnoreCase(problem.getType().name(), searchExpression) ||
                    StringUtils.containsIgnoreCase(problem.getSourceType().name(), searchExpression);
        }

        return true;
    }

    private String createStatus(ListMultimap<String, Problem> problems) {
        List<String> status = new ArrayList<String>();
        for (String problemType : problems.keySet()) {
            status.add(i18n.translate("problem.group.message", problems.get(problemType).size(), problemType));
        }

        return StringUtils.join(status, ", ");
    }
}
