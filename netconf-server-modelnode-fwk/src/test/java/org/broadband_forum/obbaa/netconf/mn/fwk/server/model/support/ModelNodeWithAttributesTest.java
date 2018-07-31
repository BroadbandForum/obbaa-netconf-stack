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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

public class ModelNodeWithAttributesTest {

    private ModelNodeWithAttributes m_modelNodeWithAttributes;

    private SchemaPath m_schemaPath;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDSM;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subsystemRegistry;

    private ModelNodeId m_numberModelNodeId = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                    "device-manager"), new ModelNodeRdn("container",
                    TestConstants.DEVICE_HOLDER_NAMESPACE,
                    "device-holder"), new ModelNodeRdn("name",
                    TestConstants.DEVICE_HOLDER_NAMESPACE, "100")));
    private ModelNodeId m_stringModelNodeId = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                    "device-manager"), new ModelNodeRdn("container",
                    TestConstants.DEVICE_HOLDER_NAMESPACE,
                    "device-holder"), new ModelNodeRdn("name",
                    TestConstants.DEVICE_HOLDER_NAMESPACE, "OLT1")));

    @Before
    public void setUp() throws Exception {

        m_schemaPath = SchemaPathBuilder.fromString("(http://example.com/ns/example-jukebox?revision=2015-02-27) , " +
                "jukebox,"
                + "library ,artist  ");
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        m_subsystemRegistry = mock(SubSystemRegistry.class);
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_modelNodeDSM = mock(ModelNodeDataStoreManager.class);
        m_modelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, m_numberModelNodeId,
                m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
    }

    @Test
    public void testComparatorForSameNumbers() {
        ModelNodeId m_otherNumberModelNodeId = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                        "device-manager"), new ModelNodeRdn("container",
                        TestConstants.DEVICE_HOLDER_NAMESPACE,
                        "device-holder"), new ModelNodeRdn("name",
                        TestConstants.DEVICE_HOLDER_NAMESPACE, "100")));
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath,
                m_otherNumberModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry,
                m_modelNodeDSM);
        int actual = m_modelNodeWithAttributes.compareTo(m_otherModelNodeWithAttributes);
        assertTrue(actual == 0);
    }

    @Test
    public void testComparatorForDifferentStrings() {
        ModelNodeId m_otherStringModelNodeId = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                        "device-manager"), new ModelNodeRdn("container",
                        TestConstants.DEVICE_HOLDER_NAMESPACE,
                        "device-holder"), new ModelNodeRdn("name",
                        TestConstants.DEVICE_HOLDER_NAMESPACE, "artist")));
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath,
                m_otherStringModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry,
                m_modelNodeDSM);
        int actual = m_modelNodeWithAttributes.compareTo(m_otherModelNodeWithAttributes);
        assertTrue(actual < 0);
    }

    @Test
    public void testComparatorForDifferntNumbers() {
        ModelNodeId m_otherNumberModelNodeId = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                        "device-manager"), new ModelNodeRdn("container",
                        TestConstants.DEVICE_HOLDER_NAMESPACE,
                        "device-holder"), new ModelNodeRdn("name",
                        TestConstants.DEVICE_HOLDER_NAMESPACE, "90")));
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath,
                m_otherNumberModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry,
                m_modelNodeDSM);
        int actual = m_modelNodeWithAttributes.compareTo(m_otherModelNodeWithAttributes);
        assertTrue(actual > 0);
    }

    @Test
    public void testListKeyOrder() {

        // Case 1 : While setting the attributes in case of a list
        ModelNodeId modelNodeId = new ModelNodeId("/container=device-manager/container=device-holder", TestConstants
                .ANV_NAMESPACE);
        DataSchemaNode listSchemaNode = mock(ListSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_schemaPath)).thenReturn(listSchemaNode);

        QName key1QName = QName.create(TestConstants.ANV_NAMESPACE, "key1");
        QName key2QName = QName.create(TestConstants.ANV_NAMESPACE, "key2");
        QName leaf1QName = QName.create(TestConstants.ANV_NAMESPACE, "leaf1");
        QName leaf2QName = QName.create(TestConstants.ANV_NAMESPACE, "leaf2");
        List<QName> keyDefinition = new LinkedList<>();
        keyDefinition.add(key1QName);
        keyDefinition.add(key2QName);
        when(((ListSchemaNode) listSchemaNode).getKeyDefinition()).thenReturn(keyDefinition);

        ModelNodeWithAttributes modelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, modelNodeId,
                m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);

        Map<QName, ConfigLeafAttribute> unorderedAttributes = new HashMap<>();
        unorderedAttributes.put(leaf1QName, new GenericConfigAttribute("leaf1-value"));
        unorderedAttributes.put(leaf2QName, new GenericConfigAttribute("leaf2-value"));
        unorderedAttributes.put(key1QName, new GenericConfigAttribute("key1-value"));
        unorderedAttributes.put(key2QName, new GenericConfigAttribute("key2-value"));
        modelNodeWithAttributes.setAttributes(unorderedAttributes);

        Map<QName, ConfigLeafAttribute> orderedAttributes = new LinkedHashMap<>();
        orderedAttributes.put(key1QName, new GenericConfigAttribute("key1-value"));
        orderedAttributes.put(key2QName, new GenericConfigAttribute("key2-value"));
        orderedAttributes.put(leaf1QName, new GenericConfigAttribute("leaf1-value"));
        orderedAttributes.put(leaf2QName, new GenericConfigAttribute("leaf2-value"));

        assertArrayEquals(orderedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet()
                .toArray());

        // Case 2 : While updating the attributes in case of a list
        Map<QName, ConfigLeafAttribute> attributesToBeUpdated = new LinkedHashMap<>();
        attributesToBeUpdated.put(leaf1QName, null);
        QName leaf3QName = QName.create(TestConstants.ANV_NAMESPACE, "leaf3");
        attributesToBeUpdated.put(leaf3QName, new GenericConfigAttribute("leaf3-value"));
        modelNodeWithAttributes.updateConfigAttributes(attributesToBeUpdated);

        Map<QName, ConfigLeafAttribute> updatedAttributes = new LinkedHashMap<>();
        updatedAttributes.put(key1QName, new GenericConfigAttribute("key1-value"));
        updatedAttributes.put(key2QName, new GenericConfigAttribute("key2-value"));
        updatedAttributes.put(leaf2QName, new GenericConfigAttribute("leaf2-value"));
        updatedAttributes.put(leaf3QName, new GenericConfigAttribute("leaf3-value"));

        assertArrayEquals(updatedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet()
                .toArray());

        // Case 1 : While setting the attributes in case of a container
        DataSchemaNode containerSchemaNode = mock(ContainerSchemaNode.class);
        SchemaPath containerSchemaPath = mock(SchemaPath.class);
        when(m_schemaRegistry.getDataSchemaNode(containerSchemaPath)).thenReturn(containerSchemaNode);
        modelNodeWithAttributes = new ModelNodeWithAttributes(containerSchemaPath, modelNodeId,
                m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        modelNodeWithAttributes.setAttributes(unorderedAttributes);
        assertArrayEquals(unorderedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet()
                .toArray());

        // Case 2 : While updating the attributes in case of a container
        modelNodeWithAttributes.updateConfigAttributes(attributesToBeUpdated);

        updatedAttributes = new LinkedHashMap<>();
        updatedAttributes.put(leaf2QName, new GenericConfigAttribute("leaf2-value"));
        updatedAttributes.put(key1QName, new GenericConfigAttribute("key1-value"));
        updatedAttributes.put(key2QName, new GenericConfigAttribute("key2-value"));
        updatedAttributes.put(leaf3QName, new GenericConfigAttribute("leaf3-value"));

        assertEquals(updatedAttributes.keySet(), modelNodeWithAttributes.getAttributes().keySet());
    }
}