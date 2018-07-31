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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StringToObjectTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class AnnotationConfigAttributeHelper extends AnnotationConstraintHelper implements ConfigAttributeHelper {

    private Method m_getterMethod;
    private Method m_setterMethod;

    public AnnotationConfigAttributeHelper(Method getterMethod) {
        super();
        m_getterMethod = getterMethod;
    }

    public AnnotationConfigAttributeHelper(Method getterMethod, Method setterMethod) {
        super();
        m_getterMethod = getterMethod;
        m_setterMethod = setterMethod;
    }

    public Class<?> getAttributeType() {
        return m_getterMethod.getReturnType();
    }

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return null;
    }

    public ConfigLeafAttribute getValue(ModelNode modelNode) throws GetAttributeException {
        try {
            Object value = m_getterMethod.invoke(modelNode);
            if (value != null) {
                return new GenericConfigAttribute(value.toString());
            }
            return null;
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new GetAttributeException("could get value from ModelNode: " + modelNode, e);
        }
    }

    public void setValue(ModelNode modelNode, ConfigLeafAttribute value) throws SetAttributeException {
        if (m_setterMethod != null) {
            String valueToBeUpdated = null;
            if (value != null) {
                valueToBeUpdated = value.getStringValue();
            }
            try {
                Object objValue = StringToObjectTransformer.transform(valueToBeUpdated, m_getterMethod.getReturnType());
                m_setterMethod.invoke(modelNode, objValue);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | TransformerException e) {
                throw new SetAttributeException("could set value to ModelNode: " + modelNode, e);
            }
        } else {
            throw new SetAttributeException("No setter for the attribute" + modelNode);
        }
    }

    @Override
    public String getDefault() {
        return null;
    }

    @Override
    public void removeAttribute(ModelNode abstractModelNode) {

    }

}
