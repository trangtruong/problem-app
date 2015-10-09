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
package info.magnolia.configuration.app.overview.data;

/** * Constants used in the data binding structures of the config app.

 *
 * @see {@link DefinitionProviderConfigDataSource}.
 */
public class ConfigConstants {

    public static final String TITLE_PID = "title";

    public static final String MODULE_PID = "module";

    public static final String TYPE_PID = "type";

    public static final String VALUE_PID = "value";

    public static final String ORIGIN_PID = "origin";

    public static final String[] PID_ORDER = new String[] {TITLE_PID, VALUE_PID, TYPE_PID, MODULE_PID, ORIGIN_PID};

    //public static final String GROUP_PLACEHOLDER_ITEMID = "##SUBSECTION##";
    public static final String GROUP_PLACEHOLDER_ITEMID = "groupProblem";

    public static final String PROBLEM_PID = "problem";

    public static final String ELEMENT_PID = "element";

    public static final String SOURCE_PID = "sourceType";

    public static final String TIMESTAMP_PID = "timestamp";

    public static final String[] PROBLEM_ID_ORDER = new String[] {PROBLEM_PID, ELEMENT_PID, MODULE_PID, SOURCE_PID, TIMESTAMP_PID};
}
