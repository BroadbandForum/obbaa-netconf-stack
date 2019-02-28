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
     * 
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
