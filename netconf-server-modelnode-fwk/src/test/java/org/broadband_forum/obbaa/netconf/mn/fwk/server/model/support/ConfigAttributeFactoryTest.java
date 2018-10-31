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

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class ConfigAttributeFactoryTest {

    private SchemaRegistry m_schemaRegistry;
    private SchemaPath m_parentSchemaPath;
    private SchemaPath m_childSchemaPath;

    @Before
    public void setup() throws SchemaBuildException {
        m_schemaRegistry = spy(new SchemaRegistryImpl(TestUtil.getByteSources(
            Arrays.asList("/configattributefactory/children@2014-07-03.yang",
                "/configattributefactory/occupation@2014-07-03.yang" ,
                "/configattributefactory/parents@2014-07-03.yang",
                "/configattributefactory/unionexample.yang")), new NoLockService()));
        m_parentSchemaPath = mock(SchemaPath.class);
        m_childSchemaPath = mock(SchemaPath.class);
    }

    @Test
    public void testGetGenericConfigAttribute() throws ParserConfigurationException, SAXException, IOException,
            InvalidIdentityRefException {

        /**
         * <parent xmlns="parentNs">
         *    <child>value</child>
         *  </parent>
         */
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_childSchemaPath)).thenReturn(leafSchemaNode);
        when(leafSchemaNode.getType()).thenReturn((TypeDefinition) mock(StringTypeDefinition.class));
        QName childQname = QName.create("parentNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);

        Element parentElement = DocumentUtils.getDocumentElement("<parent " +
                "xmlns=\"parentNs\"><child>value</child></parent>");
        Element childElement = (Element) parentElement.getFirstChild();
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                m_parentSchemaPath,
                childQname, childElement);

        assertTrue(configLeafAttribute instanceof GenericConfigAttribute);
        assertEquals("value", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns="parentNs">
         <child xmlns="childNs">value2</child>
         </parent>
         */
        childQname = QName.create("childNs", "2017-02-10", "child");
        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\"><child " +
                "xmlns=\"childNs\">value2</child></parent>");
        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof GenericConfigAttribute);
        assertEquals("value2", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns:p1="parentNs" xmlns:p2="childNs">
         <p2:child>value3</p2:child>
         </parent>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns:p1=\"parentNs\" xmlns:p2=\"childNs\">" +
                "<p2:child>value3</p2:child>\n" +
                "</parent>");
        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof GenericConfigAttribute);
        assertEquals("value3", configLeafAttribute.getStringValue());
    }

    @Test
    public void testGetIdentityRefConfigAttribute() throws ParserConfigurationException, SAXException, IOException,
            InvalidIdentityRefException {

        /**
         * <parent xmlns:p1="parentNs" xmlns:p2="childNs">
         <child>p2:value</child>
         </parent>
         */

        /** Result
         * <child xmlns:ex="childNs">ex:value</child>
         */

        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_childSchemaPath)).thenReturn(leafSchemaNode);
        when(leafSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        QName childQname = QName.create("parentNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);
        when(m_schemaRegistry.getPrefix("childNs")).thenReturn("ex");

        Element parentElement = DocumentUtils.getDocumentElement("<parent xmlns:p1=\"parentNs\" " +
                "xmlns:p2=\"childNs\"><child>p2:value</child></parent>\n");
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("childNs", getNodeValue(configLeafAttribute, "xmlns:ex"));
        assertEquals("ex:value", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns:p1="parentNs" xmlns:p2="childNs" xmlns:p3="valueNs">
         <p2:child>p3:value</p2:child>
         </parent>
         */

        /** Result
         * <child xmlns="childNs" xmlns:p3="valueNs">p3:value</child>
         */

        childQname = QName.create("childNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);
        when(m_schemaRegistry.getPrefix("valueNs")).thenReturn("p3");

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns:p1=\"parentNs\" xmlns:p2=\"childNs\" " +
                "xmlns:p3=\"valueNs\"><p2:child>p3:value</p2:child>\n" +
                "</parent>");
        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("valueNs", getNodeValue(configLeafAttribute, "xmlns:p3"));
        assertEquals("p3:value", configLeafAttribute.getStringValue());
    }

    @Test
    public void testGetIdentityRefWithoutPrefix() throws ParserConfigurationException, SAXException, IOException,
            InvalidIdentityRefException {

        // when namespace cannot be retrieved for the prefix
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_childSchemaPath)).thenReturn(leafSchemaNode);
        when(leafSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        QName childQname = QName.create("parentNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);

        Element parentElement = DocumentUtils.getDocumentElement("<parent " +
                "xmlns:p1=\"parentNs\"><child>p2:value</child></parent>\n");
        try {
            ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                    childQname, parentElement.getFirstChild());
        } catch (InvalidIdentityRefException e) {
            NetconfRpcError expectedError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Cannot get the namespace for the prefix p2. Value \"p2:value\" is not a valid identityref value.");
            assertEquals(expectedError, e.getRpcError());
        }

        // When idRef value doesn't specify a prefix and default namespace is not null
        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\"><child>value</child></parent>\n");
        when(m_schemaRegistry.getPrefix("parentNs")).thenReturn("p1");
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("parentNs", getNodeValue(configLeafAttribute, "xmlns:p1"));
        assertEquals("p1:value", configLeafAttribute.getStringValue());


        parentElement = DocumentUtils.getDocumentElement("<parent><child>value</child></parent>\n");
        try {
            ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                    childQname, parentElement.getFirstChild());
        } catch (InvalidIdentityRefException e) {
            NetconfRpcError expectedError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Default namespace is null. Value \"value\" is not a valid identityref value.");
            assertEquals(expectedError, e.getRpcError());
        }

    }

    @Test
    public void testGetInstanceIdentifierConfigAttr() throws ParserConfigurationException, SAXException, IOException,
            InvalidIdentityRefException {

        /**
         * <parent xmlns:p1="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:system/p3:services/p3:ssh</child>
         </parent>
         */

        /**
         * <child xmlns="parentNs" xmlns:ex1="value2Ns" xmlns:ex2="value3Ns">/ex1:system/ex2:services/ex2:ssh</child>
         */

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_childSchemaPath)).thenReturn(leafListSchemaNode);
        when(leafListSchemaNode.getType()).thenReturn((TypeDefinition) mock(InstanceIdentifierTypeDefinition.class));
        QName childQname = QName.create("parentNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);
        when(m_schemaRegistry.getPrefix("value2Ns")).thenReturn("ex1");
        when(m_schemaRegistry.getPrefix("value3Ns")).thenReturn("ex2");

        Element parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:system/p3:services/p3:ssh</child>\n" +
                "</parent>");
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:system/ex2:services/ex2:ssh", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:system/p3:user[p3:name='fred']</child>
         </parent>
         */

        /**
         * <child xmlns="parentNs" xmlns:ex1="value2Ns"
         * xmlns:ex2="value3Ns">/ex1:system/ex2:user[ex2:name='fred']</child>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:system/p3:user[p3:name='fred']</child>\n" +
                "</parent>");
        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:system/ex2:user[ex2:name='fred']", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:system/p3:user[p3:name='fred']/p3:type</child>
         </parent>
         */

        /**
         *  <child xmlns="parentNs" xmlns:ex1="value2Ns"
         *  xmlns:ex2="value3Ns">/ex1:system/ex2:user[ex2:name='fred']/ex2:type</child>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:system/p3:user[p3:name='fred']/p3:type</child>\n" +
                "</parent>");

        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:system/ex2:user[ex2:name='fred']/ex2:type", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:system/p3:server[p3:ip='192.0.2.1'][p3:port='80']</child>
         </parent>
         */

        /**
         *  <child xmlns="parentNs" xmlns:ex1="value2Ns" xmlns:ex2="value3Ns">/ex1:system/ex2:server[ex2:ip='192.0.2
         *  .1'][ex2:port='80']</child>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:system/p3:server[p3:ip='192.0.2.1'][p3:port='80']</child>\n" +
                "            </parent>");
        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:system/ex2:server[ex2:ip='192.0.2.1'][ex2:port='80']", configLeafAttribute.getStringValue());

        /**
         * <parent xmlns="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:system/p3:services/p3:ssh/p3:cipher[.='blowfish-cbc']</child>
         </parent>
         */

        /**
         *  <child xmlns="parentNs" xmlns:ex1="value2Ns"
         *  xmlns:ex2="value3Ns">/ex1:system/ex2:services/ex2:ssh/ex2:cipher[
         *  .='blowfish-cbc']</child>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:system/p3:services/p3:ssh/p3:cipher[.='blowfish-cbc']</child>\n" +
                "            </parent>");

        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:system/ex2:services/ex2:ssh/ex2:cipher[.='blowfish-cbc']", configLeafAttribute
                .getStringValue());

        /**
         * <parent xmlns="parentNs" xmlns:p2="value2Ns" xmlns:p3="value3Ns">
         <child>/p2:stats/p3:port[3]</child>
         </parent>
         */

        /**
         *  <child xmlns="parentNs" xmlns:ex1="value2Ns" xmlns:ex2="value3Ns">/ex1:stats/ex2:port[3]</child>
         */

        parentElement = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\" xmlns:p2=\"value2Ns\" " +
                "xmlns:p3=\"value3Ns\"><child>/p2:stats/p3:port[3]</child>\n" +
                "            </parent>");

        configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, m_parentSchemaPath,
                childQname, parentElement.getFirstChild());

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("value2Ns", getNodeValue(configLeafAttribute, "xmlns:ex1"));
        assertEquals("value3Ns", getNodeValue(configLeafAttribute, "xmlns:ex2"));
        assertEquals("/ex1:stats/ex2:port[3]", configLeafAttribute.getStringValue());
    }

    @Test
    public void testGetConfigAttributeFromEntity() throws ParserConfigurationException, SAXException, IOException {

        // IdentityRef case
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(m_childSchemaPath)).thenReturn(leafSchemaNode);
        when(leafSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        QName childQname = QName.create("parentNs", "2017-02-10", "child");
        when(m_schemaRegistry.getDescendantSchemaPath(m_parentSchemaPath, childQname)).thenReturn(m_childSchemaPath);

        String attributeNsFromDB = "valueNs";
        when(m_schemaRegistry.getPrefix(attributeNsFromDB)).thenReturn("prefix");
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,
                m_parentSchemaPath, attributeNsFromDB, childQname, "prefix:value");

        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("valueNs", configLeafAttribute.getDOMValue().getAttributes().getNamedItem("xmlns:prefix")
                .getNodeValue());
        assertEquals("prefix:value", configLeafAttribute.getStringValue());

        //Instance identifier
        when(leafSchemaNode.getType()).thenReturn((TypeDefinition) mock(InstanceIdentifierTypeDefinition.class));
        attributeNsFromDB = "p1 ns1,p2 ns2,p3 ns3";
        when(m_schemaRegistry.getPrefix("ns1")).thenReturn("p1");
        when(m_schemaRegistry.getPrefix("ns2")).thenReturn("p2");
        when(m_schemaRegistry.getPrefix("ns3")).thenReturn("p3");

        configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,
                m_parentSchemaPath, attributeNsFromDB, childQname, "/p1:value1/p2:value2/p3:value3");

        assertTrue(configLeafAttribute instanceof InstanceIdentifierConfigAttribute);
        assertEquals("ns1", getNodeValue(configLeafAttribute, "xmlns:p1"));
        assertEquals("ns2", getNodeValue(configLeafAttribute, "xmlns:p2"));
        assertEquals("ns3", getNodeValue(configLeafAttribute, "xmlns:p3"));

        assertEquals("/p1:value1/p2:value2/p3:value3", configLeafAttribute.getStringValue());
    }

    private String getNodeValue(ConfigLeafAttribute configLeafAttribute, String prefix) {
        return configLeafAttribute.getDOMValue().getAttributes().getNamedItem(prefix).getNodeValue();
    }

    @Test
    public void testGetConfigAttributeFromDefaultValue() throws SchemaPathBuilderException {
        SchemaPath sonGenderSp = SchemaPathBuilder.fromString("(unit:test:caft:children?revision=2014-07-03)family," +
                "son,gender");
        LeafSchemaNode sonGender = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(sonGenderSp);
        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                (m_schemaRegistry, sonGender);
        assertTrue(leafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("dactp:male", leafAttribute.getStringValue());
    }

    @Test
    public void testGetConfigAttributeFromDefaultValueWithoutPrefix() throws SchemaPathBuilderException {
        SchemaPath fatherGenderSp = SchemaPathBuilder.fromString("(unit:test:caft:parents?revision=2014-07-03)family," +
                "father,gender");
        LeafSchemaNode fatherGender = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(fatherGenderSp);
        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                (m_schemaRegistry, fatherGender);
        assertTrue(leafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals("dactp:male", leafAttribute.getStringValue());
    }

    @Test
    public void testGetConfigAttributeFromDefaultValueInstanceIdentifier() throws SchemaPathBuilderException {
        SchemaPath daughterFather_occupationSp = SchemaPathBuilder.fromString("" +
                "(unit:test:caft:children?revision=2014-07-03)family,daughter,father-occupation");
        LeafSchemaNode daughterFather_occupation = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode
                (daughterFather_occupationSp);
        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                (m_schemaRegistry, daughterFather_occupation);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);
        Map<String, String> expectedPrefixMap = new HashMap<>();
        expectedPrefixMap.put("unit:test:caft:occupation", "dacto");
        expectedPrefixMap.put("unit:test:caft:parents", "dactp");
        assertEquals(expectedPrefixMap, ((InstanceIdentifierConfigAttribute) leafAttribute).getNsPrefixMap());
        assertEquals("father-occupation", ((InstanceIdentifierConfigAttribute) leafAttribute).getAttributeLocalName());
        String defaultval = "/dactp:family/dactp:father/dacto:occupation";
        String attrLocalName = "father-occupation";
        String attrNamespace = "unit:test:caft:children";
        InstanceIdentifierConfigAttribute attribute = new InstanceIdentifierConfigAttribute(expectedPrefixMap,
                attrNamespace, attrLocalName, defaultval);
        assertEquals(attribute.getAttributeValue(), ((InstanceIdentifierConfigAttribute) leafAttribute)
                .getAttributeValue());
        assertEquals(attribute.getStringValue(), leafAttribute.getStringValue());
    }

    @Test
    public void testGetConfigAttributeFromDefaultValueInstanceIdentifierScenario2() throws SchemaPathBuilderException {
        SchemaPath daughterFatherNameSp = SchemaPathBuilder.fromString("(unit:test:caft:children?revision=2014-07-03)" +
                "family,daughter,father-name");
        LeafSchemaNode daughterFatherName = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(daughterFatherNameSp);
        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                (m_schemaRegistry, daughterFatherName);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);
        Map<String, String> expectedPrefixMap = new HashMap<>();
        expectedPrefixMap.put("unit:test:caft:parents", "dactp");
        assertEquals(expectedPrefixMap, ((InstanceIdentifierConfigAttribute) leafAttribute).getNsPrefixMap());
        assertEquals("father-name", ((InstanceIdentifierConfigAttribute) leafAttribute).getAttributeLocalName());
        String defaultval = "/dactp:family/dactp:father/dactp:name";
        String attrLocalName = "father-name";
        String attrNamespace = "unit:test:caft:children";
        InstanceIdentifierConfigAttribute attribute = new InstanceIdentifierConfigAttribute(expectedPrefixMap,
                attrNamespace, attrLocalName, defaultval);
        assertEquals(attribute.getAttributeValue(), ((InstanceIdentifierConfigAttribute) leafAttribute)
                .getAttributeValue());
        assertEquals(attribute.getStringValue(), leafAttribute.getStringValue());

    }

    @Test
    public void testGetConfigAttributeFromDefaultValueInstanceIdentifierWithoutPrefix() throws
            SchemaPathBuilderException {
        SchemaPath motherHusbandNameSp = SchemaPathBuilder.fromString("(unit:test:caft:parents?revision=2014-07-03)" +
                "family,mother,husband-name");
        LeafSchemaNode motherHusbandName = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(motherHusbandNameSp);
        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                (m_schemaRegistry, motherHusbandName);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);
        Map<String, String> expectedPrefixMap = new HashMap<>();
        assertEquals(expectedPrefixMap, ((InstanceIdentifierConfigAttribute) leafAttribute).getNsPrefixMap());
        assertEquals("husband-name", ((InstanceIdentifierConfigAttribute) leafAttribute).getAttributeLocalName());
        String defaultval = "/family/father/name";
        String attrLocalName = "husband-name";
        String attrNamespace = "unit:test:caft:parents";
        InstanceIdentifierConfigAttribute attribute = new InstanceIdentifierConfigAttribute(expectedPrefixMap,
                attrNamespace, attrLocalName, defaultval);
        assertEquals(attribute.getAttributeValue(), ((InstanceIdentifierConfigAttribute) leafAttribute)
                .getAttributeValue());
        assertEquals(attribute.getStringValue(), leafAttribute.getStringValue());

    }

    @Test
    public void testUnionTypeDefinition() throws ParserConfigurationException, SAXException, IOException, InvalidIdentityRefException {
        SchemaPath schemaPath = SchemaPathBuilder.fromString("(unit:test:union:example?revision=2017-06-07)test-container");
        QName childQname =QName.create("unit:test:union:example","2017-06-07","test-leaf");

        //IdentityRef
        Element parentElement = DocumentUtils.getDocumentElement("<test-container xmlns=\"unit:test:union:example\">" +
            "<test-leaf>test-type1</test-leaf>" +
            "</test-container>");
        Element childElement = (Element) parentElement.getFirstChild();

        ConfigLeafAttribute leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof IdentityRefConfigAttribute);

        //Enumeration
        parentElement = DocumentUtils.getDocumentElement("<test-container xmlns=\"unit:test:union:example\">" +
            "<test-leaf>all</test-leaf>" +
            "</test-container>");
        childElement = (Element) parentElement.getFirstChild();

        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof GenericConfigAttribute);

        //String
        parentElement = DocumentUtils.getDocumentElement("<test-container xmlns=\"unit:test:union:example\">" +
            "<test-leaf>testString</test-leaf>" +
            "</test-container>");
        childElement = (Element) parentElement.getFirstChild();

        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof GenericConfigAttribute);

        //Integer
        parentElement = DocumentUtils.getDocumentElement("<test-container xmlns=\"unit:test:union:example\">" +
            "<test-leaf>12345</test-leaf>" +
            "</test-container>");
        childElement = (Element) parentElement.getFirstChild();

        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof GenericConfigAttribute);

        //IdentityRefWithPrefix and Id-ref defined in another yang
        schemaPath = SchemaPathBuilder.fromString("(unit:test:union:example?revision=2017-06-07)test-container2");
        childQname =QName.create("unit:test:union:example","2017-06-07","test-leaf2");
        parentElement = DocumentUtils.getDocumentElement("<test-container2 xmlns=\"unit:test:union:example\">" +
            "<test-leaf1 xmlns:dactp1=\"unit:test:caft:parents\">dactp1:female</test-leaf1>" +
            "</test-container2>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof IdentityRefConfigAttribute);

        //InstanceIdentifier for a container/leaf
        schemaPath = SchemaPathBuilder.fromString("(unit:test:union:example?revision=2017-06-07)test-container3");
        childQname =QName.create("unit:test:union:example","2017-06-07","test-leaf3");
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\" xmlns:ex1=\"unit:test:union:example\">/ex1:system/ex:services/ex:ssh</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier for a list
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\">/ex:system/ex:user[ex:name='fred']</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier for a leaf in list entry
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\">/ex:system/ex:user[ex:name='fred']/ex:type</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier for a list entry with two keys
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\">/ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier for a leaf-list entry
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\">/ex:system/ex:services/ex:ssh/ex:cipher[.='blowfish-cbc']</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier for a list entry without keys
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:ex=\"unit:test:union:example\">/ex:stats/ex:port[3]</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //InstanceIdentifier is a relative path
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3>../abc</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof InstanceIdentifierConfigAttribute);

        //IdentifyRef Value when InstanceIdentifier is also present
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:dactp1=\"unit:test:caft:parents\">dactp1:female</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        leafAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
        assertTrue(leafAttribute instanceof IdentityRefConfigAttribute);

        //Invalid InstanceIdentifier value
        parentElement = DocumentUtils.getDocumentElement("<test-container3 xmlns=\"unit:test:union:example\">" +
            "<test-leaf3 xmlns:dactp1=\"unit:test:caft:parents\">..abc</test-leaf3>" +
            "</test-container3>");
        childElement = (Element) parentElement.getFirstChild();
        try {
            ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, schemaPath, childQname, childElement);
            fail("Should have failed with exception");
        } catch (RpcValidationException ex) {
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                "Invalid value ..abc for test-leaf3");
            assertEquals(rpcError.getErrorTag(), ex.getRpcError().getErrorTag());
            assertEquals(rpcError.getErrorAppTag(), ex.getRpcError().getErrorAppTag());
            assertEquals(rpcError.getErrorMessage(), ex.getRpcError().getErrorMessage());
            assertEquals(rpcError.getErrorSeverity(), ex.getRpcError().getErrorSeverity());
        }
    }

}
