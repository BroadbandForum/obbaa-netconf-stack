/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;

public class ModelNodeDynaClass extends BasicDynaClass {

    private static final long serialVersionUID = 1L;

    public ModelNodeDynaClass() {
        super(null, ModelNodeDynaBean.class);
    }

    public ModelNodeDynaClass(String name, Class<?> dynaBeanClass) {
        super(name, dynaBeanClass == null ? ModelNodeDynaBean.class : dynaBeanClass);
    }

    public ModelNodeDynaClass(String name, Class<?> dynaBeanClass, DynaProperty[] properties) {
        super(name, dynaBeanClass == null ? ModelNodeDynaBean.class : dynaBeanClass, properties);
    }

    public static Object current(ModelNodeWithAttributes modelNode) {
        return modelNode.getValue();
    }

    public static Object current(DynaBean dynaBean) {
        if (dynaBean instanceof ModelNodeDynaBean) {
            return dynaBean;
        } else {
            return dynaBean.get(ModelNodeWithAttributes.LEAF_VALUE);
        }
    }

    /**
     * for a given property verifies if the dyna class property contains an attribute even if the provided name does not
     * conform to jave bean getters/setters naming convention.
     * <p>
     * Eg: an attribute/name of a dynabean could be in-flow-level-L1.2 --> this does not conform to standards
     * and PropertyUtils.isReadable() will returns false even if an attribute exists.
     */
    public static boolean containsProperty(DynaBean bean, String name) {
        DynaProperty[] properties = bean.getDynaClass().getDynaProperties();
        for (DynaProperty property : properties) {
            if (property.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static String getContextBeanName(DynaBean contextBean) {
        if (contextBean != null) {
            return contextBean.getDynaClass().getName();
        } else {
            return null;
        }
    }

}
