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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.CreateStrategy;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNodeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vgotagi on 10/24/16.
 */
public class AnnotationChildContainerHelperTest {
    private AnnotationChildContainerHelper m_helper;
    private Method m_getterMethod;
    private Method m_setterMethod;
    ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(mock(SchemaRegistry.class));

    @Before
    public void setUp() throws Exception {
        m_getterMethod = Class.forName(ModelNode.class.getName()).getMethod("getValue");
        m_setterMethod = Class.forName(ModelNode.class.getName()).getMethod("setValue", Object.class);
        SchemaPath childSchemaPath = mock(SchemaPath.class);
        m_modelNodeHelperRegistry.registerModelNodeFactory("HelperDrivenModelNodeFactory", new
                HelperDrivenModelNodeFactory());
        m_helper = new AnnotationChildContainerHelper("name", CreateStrategy.factory, "HelperDrivenModelNodeFactory",
                m_getterMethod, m_setterMethod, childSchemaPath, m_modelNodeHelperRegistry);
    }

    @Test(expected = ModelNodeFactoryException.class)
    public void testRegisterModelNodeFactory() throws Exception {
        m_modelNodeHelperRegistry.registerModelNodeFactory("HelperDrivenModelNodeFactory", null);
    }


    @Test(expected = ModelNodeSetException.class)
    public void testSetValueException() throws Exception {
        ModelNode parent = mock(ModelNode.class);
        ModelNode child = mock(ModelNode.class);
        when(m_helper.getSetterMethod().invoke(parent, child)).thenThrow(new IllegalArgumentException());
        m_helper.setValue(parent, child);
    }

    @Test(expected = ModelNodeCreateException.class)
    public void testCreateChildException() throws Exception {
        HelperDrivenModelNode parent = mock(HelperDrivenModelNode.class);
        HelperDrivenModelNode child = mock(HelperDrivenModelNode.class);
        when(m_helper.getSetterMethod().invoke(parent, child)).thenThrow(new IllegalArgumentException());
        m_helper.createChild(parent, new ConcurrentHashMap<>());
    }


    @Test(expected = ModelNodeDeleteException.class)
    public void testDeleteChildException() throws Exception {
        ModelNode parent = mock(ModelNode.class);
        when(m_helper.getSetterMethod().invoke(parent, (Object) null)).thenThrow(new IllegalArgumentException());
        m_helper.deleteChild(parent);
    }

    @Test(expected = ModelNodeGetException.class)
    public void testGetValueException() throws Exception {
        m_helper = new AnnotationChildContainerHelper(m_getterMethod);
        ModelNode parent = mock(ModelNode.class);
        when(m_helper.getGetterMethod().invoke(parent)).thenThrow(new IllegalArgumentException());
        m_helper.getValue(parent);
    }
}
