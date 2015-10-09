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

import info.magnolia.config.registry.DefinitionType;

/**
 * FilterContext.
 */
public class FilterContext {

    private String name;

    private String moduleName;

    private DefinitionType definitionType;

    private boolean errorsOnly;
    private boolean fileBasedOnly;

    public boolean isErrorsOnly() {
        return errorsOnly;
    }

    public DefinitionType getDefinitionType() {
        return definitionType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getName() {
        return name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public FilterContext(String name, String moduleName, DefinitionType definitionType, boolean errorsOnly, boolean fileBasedOnly) {
        this.name = name;
        this.moduleName = moduleName;
        this.definitionType = definitionType;
        this.errorsOnly = errorsOnly;
        this.fileBasedOnly = fileBasedOnly;
    }

    public boolean isFileBasedOnly() {
        return fileBasedOnly;
    }

    public static class Builder {
        private String name;
        private String moduleName;
        private DefinitionType definitionType;
        private boolean errorsOnly;
        private boolean fileBasedOnly;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFileBasedOnly(boolean fileBasedOnly) {
            this.fileBasedOnly = fileBasedOnly;
            return this;
        }

        public Builder withModuleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public Builder withDefinitionType(DefinitionType definitionType) {
            this.definitionType = definitionType;
            return this;
        }

        public Builder withErrorsOnly(boolean errorsOnly) {
            this.errorsOnly = errorsOnly;
            return this;
        }

        public FilterContext build() {
            return new FilterContext(name, moduleName, definitionType, errorsOnly, fileBasedOnly);
        }
    }
}
