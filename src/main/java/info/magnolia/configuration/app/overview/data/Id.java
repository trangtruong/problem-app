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

import java.util.Collection;
import java.util.Map;

/**
* Id.
*/
public interface Id<T> {

    T getValue();

    Id getParent();

    Collection<? extends Id> getChildren();

    int getChildAmount();

    String getName();

    Map<String, Object> getProperties();

    String getStyle();

    String getTooltip();


    static  abstract class BaseId<T> implements Id<T> {

        private String name;

        private T value;

        private Id parent;

        public BaseId(String name, Id parent, T value) {
            this.name = name;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public Id getParent() {
            return this.parent;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTooltip() {
            return "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BaseId)) return false;

            BaseId baseId = (BaseId) o;
            final String name = getName();
            final String otherName = baseId.getName();
            if (name != null ? !name.equals(otherName) : otherName != null) return false;
            if (parent != null ? !parent.equals(baseId.parent) : baseId.parent != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            String name = getName();
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (parent != null ? parent.hashCode() : 0);
            return result;
        }
    }

}
