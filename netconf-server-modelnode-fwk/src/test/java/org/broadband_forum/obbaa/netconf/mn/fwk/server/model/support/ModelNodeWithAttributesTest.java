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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.setUpUnwrap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class ModelNodeWithAttributesTest {

    public static final String LEAF_1 = "leaf1";
    public static final String LEAF_2 = "leaf2";
    public static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    public static final String LEAF_3 = "leaf3";
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

        m_schemaPath = SchemaPathBuilder.fromString("(http://example.com/ns/example-jukebox?revision=2015-02-27) , jukebox,"
                + "library ,artist  ");
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        m_subsystemRegistry = mock(SubSystemRegistry.class);
        m_schemaRegistry = mock(SchemaRegistry.class);
        setUpUnwrap(m_modelNodeHelperRegistry);
        setUpUnwrap(m_subsystemRegistry);
        setUpUnwrap(m_schemaRegistry);
        m_modelNodeDSM = mock(ModelNodeDataStoreManager.class);
        m_modelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, m_numberModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
    }

    @Test
    public void testComparatorForSameNumbers() {
        ModelNodeId m_otherNumberModelNodeId = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn("container", TestConstants.ANV_NAMESPACE,
                        "device-manager"), new ModelNodeRdn("container",
                        TestConstants.DEVICE_HOLDER_NAMESPACE,
                        "device-holder"), new ModelNodeRdn("name",
                        TestConstants.DEVICE_HOLDER_NAMESPACE, "100")));
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, m_otherNumberModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
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
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, m_otherStringModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
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
        ModelNodeWithAttributes m_otherModelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath, m_otherNumberModelNodeId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        int actual = m_modelNodeWithAttributes.compareTo(m_otherModelNodeWithAttributes);
        assertTrue(actual > 0);
    }

    @Test
    public void testListKeyOrder(){

        // Case 1 : While setting the attributes in case of a list
        ModelNodeId modelNodeId = new ModelNodeId("/container=device-manager/container=device-holder", TestConstants.ANV_NAMESPACE);
        DataSchemaNode listSchemaNode = mock(ListSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_schemaPath)).thenReturn(listSchemaNode);

        QName key1QName = QName.create(TestConstants.ANV_NAMESPACE, KEY_1);
        QName key2QName = QName.create(TestConstants.ANV_NAMESPACE, KEY_2);
        QName leaf1QName = QName.create(TestConstants.ANV_NAMESPACE, LEAF_1);
        QName leaf2QName = QName.create(TestConstants.ANV_NAMESPACE, LEAF_2);
        List<QName> keyDefinition = new LinkedList<>();
        keyDefinition.add(key1QName);
        keyDefinition.add(key2QName);
        when(((ListSchemaNode)listSchemaNode).getKeyDefinition()).thenReturn(keyDefinition);

        ModelNodeWithAttributes modelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath,modelNodeId,
                m_modelNodeHelperRegistry,m_subsystemRegistry,m_schemaRegistry,m_modelNodeDSM);

        Map<QName, ConfigLeafAttribute> unorderedAttributes = new HashMap<>();
        unorderedAttributes.put(leaf1QName, new GenericConfigAttribute(LEAF_1, TestConstants.ANV_NAMESPACE, "leaf1-value"));
        unorderedAttributes.put(leaf2QName, new GenericConfigAttribute(LEAF_2, TestConstants.ANV_NAMESPACE, "leaf2-value"));
        unorderedAttributes.put(key1QName, new GenericConfigAttribute(KEY_1, TestConstants.ANV_NAMESPACE, "key1-value"));
        unorderedAttributes.put(key2QName, new GenericConfigAttribute(KEY_2, TestConstants.ANV_NAMESPACE, "key2-value"));
        modelNodeWithAttributes.setAttributes(unorderedAttributes);

        Map<QName, ConfigLeafAttribute> orderedAttributes = new LinkedHashMap<>();
        orderedAttributes.put(key1QName, new GenericConfigAttribute(KEY_1, TestConstants.ANV_NAMESPACE, "key1-value"));
        orderedAttributes.put(key2QName, new GenericConfigAttribute(KEY_2, TestConstants.ANV_NAMESPACE, "key2-value"));
        orderedAttributes.put(leaf1QName, new GenericConfigAttribute(LEAF_1, TestConstants.ANV_NAMESPACE, "leaf1-value"));
        orderedAttributes.put(leaf2QName, new GenericConfigAttribute(LEAF_2, TestConstants.ANV_NAMESPACE, "leaf2-value"));

        assertArrayEquals(orderedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet().toArray());

        // Case 2 : While updating the attributes in case of a list
        Map<QName, ConfigLeafAttribute> attributesToBeUpdated = new LinkedHashMap<>();
        attributesToBeUpdated.put(leaf1QName, null);
        QName leaf3QName = QName.create(TestConstants.ANV_NAMESPACE, LEAF_3);
        attributesToBeUpdated.put(leaf3QName, new GenericConfigAttribute(LEAF_3, TestConstants.ANV_NAMESPACE, "leaf3-value"));
        modelNodeWithAttributes.updateConfigAttributes(attributesToBeUpdated);

        Map<QName, ConfigLeafAttribute> updatedAttributes = new LinkedHashMap<>();
        updatedAttributes.put(key1QName, new GenericConfigAttribute(KEY_1, TestConstants.ANV_NAMESPACE, "key1-value"));
        updatedAttributes.put(key2QName, new GenericConfigAttribute(KEY_2, TestConstants.ANV_NAMESPACE, "key2-value"));
        updatedAttributes.put(leaf2QName, new GenericConfigAttribute(LEAF_2, TestConstants.ANV_NAMESPACE, "leaf2-value"));
        updatedAttributes.put(leaf3QName, new GenericConfigAttribute(LEAF_3, TestConstants.ANV_NAMESPACE, "leaf3-value"));

        assertArrayEquals(updatedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet().toArray());

        // Case 1 : While setting the attributes in case of a container
        DataSchemaNode containerSchemaNode = mock(ContainerSchemaNode.class);
        SchemaPath containerSchemaPath = mock(SchemaPath.class);
        when(m_schemaRegistry.getDataSchemaNode(containerSchemaPath)).thenReturn(containerSchemaNode);
        modelNodeWithAttributes = new ModelNodeWithAttributes(containerSchemaPath,modelNodeId,
                m_modelNodeHelperRegistry,m_subsystemRegistry,m_schemaRegistry,m_modelNodeDSM);
        modelNodeWithAttributes.setAttributes(unorderedAttributes);
        assertArrayEquals(unorderedAttributes.keySet().toArray(), modelNodeWithAttributes.getAttributes().keySet().toArray());

        // Case 2 : While updating the attributes in case of a container
        modelNodeWithAttributes.updateConfigAttributes(attributesToBeUpdated);

        updatedAttributes = new LinkedHashMap<>();
        updatedAttributes.put(leaf2QName, new GenericConfigAttribute(LEAF_2, TestConstants.ANV_NAMESPACE, "leaf2-value"));
        updatedAttributes.put(key1QName, new GenericConfigAttribute(KEY_1, TestConstants.ANV_NAMESPACE, "key1-value"));
        updatedAttributes.put(key2QName, new GenericConfigAttribute(KEY_2, TestConstants.ANV_NAMESPACE, "key2-value"));
        updatedAttributes.put(leaf3QName, new GenericConfigAttribute(LEAF_3, TestConstants.ANV_NAMESPACE, "leaf3-value"));

        assertEquals(updatedAttributes.keySet(), modelNodeWithAttributes.getAttributes().keySet());
    }

    @Test
    public void testGetLastNodeDocWithKeys_EmptyDifferenceNodeID() throws ParserConfigurationException {

        ModelNodeWithAttributes modelNodeWithAttributes = new ModelNodeWithAttributes(m_schemaPath,null,
                m_modelNodeHelperRegistry,m_subsystemRegistry,m_schemaRegistry,m_modelNodeDSM);

        assertNull(modelNodeWithAttributes.getLastNodeDocWithKeys(DocumentUtils.getNewDocument(), new ModelNodeId()));

    }

    @Test
    public void testGetLastNodeDocWithKeys_ValidDepth() throws ParserConfigurationException, IOException, SAXException {

        ModelNodeId listModelNodeId = new ModelNodeId("/container=abc/container=xyz/name=qwerty", "testns");

        SchemaPath xyzListSchemaPath = SchemaPathBuilder.fromString("(testns?revision=2014-07-03)abc,xyz");
        ListSchemaNode listSchemaNode = mock(ListSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(xyzListSchemaPath)).thenReturn(listSchemaNode);
        List<QName> keyQNames = new ArrayList<>();
        keyQNames.add(QName.create("testns","name"));
        when(listSchemaNode.getKeyDefinition()).thenReturn(keyQNames);
        ModelNodeWithAttributes xyzListModelNode = new ModelNodeWithAttributes(xyzListSchemaPath,listModelNodeId,
                m_modelNodeHelperRegistry,m_subsystemRegistry,m_schemaRegistry,m_modelNodeDSM);
        xyzListModelNode.setAttributes(Collections.singletonMap(keyQNames.get(0), new GenericConfigAttribute("name","testns","qwerty")));

        SchemaPath abcContainerSchemaPath = SchemaPathBuilder.fromString("(testns?revision=2014-07-03)abc");
        ModelNodeWithAttributes abcContainerModelNode = new ModelNodeWithAttributes(abcContainerSchemaPath,new ModelNodeId("/container=abc", "testns"),
                m_modelNodeHelperRegistry,m_subsystemRegistry,m_schemaRegistry,m_modelNodeDSM);

        when(m_modelNodeDSM.findNode(Mockito.any(SchemaPath.class), Mockito.any(ModelNodeKey.class),Mockito.any(ModelNodeId.class), Mockito.any(SchemaRegistry.class))).thenReturn(abcContainerModelNode);
        String expectedDom = "<abc xmlns=\"testns\">\n" +
                                "<xyz>\n" +
                                    "<name>qwerty</name>\n" +
                                "</xyz>\n" +
                            "</abc>\n";
        assertXMLEquals(DocumentUtils.getDocumentElement(expectedDom), (Element) xyzListModelNode.getLastNodeDocWithKeys(DocumentUtils.getNewDocument(), listModelNodeId).getParentNode());


    }
}