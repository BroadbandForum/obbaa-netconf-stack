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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;

/**
 * EditTreeBuidlerTest using Following yang modules.
 * <p>
 * ---------------------------------------------------------------
 * <p>
 * module ietf-interfaces {
 * <p>
 * namespace "urn:ietf:params:xml:ns:yang:ietf-interfaces";
 * <p>
 * prefix if;
 * <p>
 * list interface {
 * <p>
 * key type;
 * <p>
 * leaf name { type string; }
 * <p>
 * leaf type { type identityref { base interface-type }  }
 * <p>
 * leaf instance { type instance-identifer; }
 * <p>
 * }
 * <p>
 * <p>
 * }
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * module bbf-fastdsl {
 * <p>
 * namespace "urn:bbf:yang:bbf-fastdsl";
 * <p>
 * prefix "bbf-fastdsl";
 * <p>
 * import ietf-interface { prefix "if" };
 * <p>
 * identity fastdsl-mode;
 * <p>
 * identity mode-fast {
 * base fastdsl-mode;
 * }
 * <p>
 * typedef fastdsl-mode-ref {
 * type identityref {
 * fastdsl-mode;
 * }
 * }
 * <p>
 * augment "/if:interfaces-state/if:interface" {
 * when "if:type = 'ianaift:fastdsl'";
 * <p>
 * leaf-list configured-mode {
 * type fastdsl-mode-ref;
 * }
 * }
 * }
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * module bbf-qos-policies {
 * <p>
 * namespace "urn:broadband-forum-org:yang:bbf-qos-policies";
 * <p>
 * prefix "bbf-qos-pol";
 * <p>
 * identity action-type ;
 * <p>
 * identity scheduling-traffic-class {
 * base action-type;
 * }
 * <p>
 * list classifier-action-entry-cfg {
 * key "action-type";
 * <p>
 * leaf action-type {
 * type identityref {
 * base action-type;
 * }
 * }
 * }
 * }
 * -----------------------------------------------------------------
 */
public class EditTreeBuilderTest {

    private String xmlns_ns = PojoToDocumentTransformer.XMLNS_NAMESPACE;
    private String xmlns = PojoToDocumentTransformer.XMLNS;
    private ModelNodeId m_modelNodeId;
    private SchemaRegistry schemaRegistry;
    private ModelNodeHelperRegistry registry;
    private ConfigAttributeHelper m_nameLeafhelper;
    private ConfigAttributeHelper m_typeLeafhelper;
    private ConfigAttributeHelper m_instanceLeafhelper;
    private ChildLeafListHelper m_configuredModeLeafListHelper;
    private EditContainmentNode m_rootEditNode;
    private Document document;
    private Element m_interfaceElement;
    private SchemaPath iterfaceModelNodeSchemaPath;
    private QName m_typeQName;
    private QName m_instanceQName;
    private QName m_configuredModeQName;

    private QName classifierQName;
    private QName m_actionTypeQName;
    private SchemaPath m_classifierSchemaPath;
    private SchemaPath m_actionTypeSchemaPath;
    private ConfigAttributeHelper m_actionTypeLeafhelper;

    @Before
    public void setup() throws ParserConfigurationException {
        m_modelNodeId = mock(ModelNodeId.class);
        schemaRegistry = mock(SchemaRegistry.class);
        registry = mock(ModelNodeHelperRegistry.class);
        m_nameLeafhelper = mock(ConfigAttributeHelper.class);
        m_typeLeafhelper = mock(ConfigAttributeHelper.class);
        m_instanceLeafhelper = mock(ConfigAttributeHelper.class);
        m_configuredModeLeafListHelper = mock(ChildLeafListHelper.class);
        m_rootEditNode = new EditContainmentNode();

        /**
         * construct XML
         *          <if:interface>
         *	          <if:name>xdsl:1</if:name>
         *			</if:interface>
         */
        document = getNewDocument();
        m_interfaceElement = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:interface");
        Element nameElement = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:name");
        nameElement.setTextContent("xdsl:1");
        m_interfaceElement.appendChild(nameElement);


        /**
         * create support and mock expected results
         */
        Class<?> qnameClass = QName.class;
        QName qname = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "interface");
        QName nameQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "name");
        m_typeQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "type");
        m_instanceQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "instance");
        m_configuredModeQName = QName.create("urn:bbf:yang:bbf-fastdsl", "configured-mode");
        iterfaceModelNodeSchemaPath = SchemaPath.create(true, qname);
        SchemaPath nameSchemaPath = iterfaceModelNodeSchemaPath.createChild(nameQName);
        SchemaPath typeSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_typeQName);
        SchemaPath instanceSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_instanceQName);
        SchemaPath configuredModeSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_typeQName);


        when(schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "interface")).thenReturn(qname);
        when(schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "name")).thenReturn(nameQName);
        when(schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "type")).thenReturn(m_typeQName);
        when(schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "instance")).thenReturn
                (m_instanceQName);
        when(schemaRegistry.lookupQName("urn:bbf:yang:bbf-fastdsl", "configured-mode")).thenReturn
                (m_configuredModeQName);
        when(schemaRegistry.getPrefix("urn:broadband-forum-org:yang:bbf-if-type")).thenReturn("bbfift");
        when(schemaRegistry.getPrefix("urn:bbf:yang:bbf-fastdsl")).thenReturn("fastdsl");

        when(schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(nameSchemaPath);
        when(schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_typeQName)).thenReturn
                (typeSchemaPath);
        when(schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_instanceQName)).thenReturn
                (instanceSchemaPath);
        when(schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_configuredModeQName)).thenReturn
                (configuredModeSchemaPath);

        LeafSchemaNode nameSchemaNode = mock(LeafSchemaNode.class);
        when(nameSchemaNode.getType()).thenReturn((TypeDefinition) mock(StringTypeDefinition.class));
        when(schemaRegistry.getDataSchemaNode(nameSchemaPath)).thenReturn(nameSchemaNode);

        LeafSchemaNode typeSchemaNode = mock(LeafSchemaNode.class);
        when(typeSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        when(schemaRegistry.getDataSchemaNode(typeSchemaPath)).thenReturn(typeSchemaNode);

        LeafSchemaNode instanceSchemaNode = mock(LeafSchemaNode.class);
        when(instanceSchemaNode.getType()).thenReturn((TypeDefinition) mock(InstanceIdentifierTypeDefinition.class));
        when(schemaRegistry.getDataSchemaNode(instanceSchemaPath)).thenReturn(instanceSchemaNode);

        LeafListSchemaNode configuredModeSchemaNode = mock(LeafListSchemaNode.class);
        when(configuredModeSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        when(schemaRegistry.getDataSchemaNode(configuredModeSchemaPath)).thenReturn(configuredModeSchemaNode);

        when(registry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(m_nameLeafhelper);
        when(registry.getNaturalKeyHelper(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(m_nameLeafhelper);
        when(registry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, m_typeQName)).thenReturn(m_typeLeafhelper);
        when(registry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, m_instanceQName)).thenReturn
                (m_instanceLeafhelper);
        when(registry.getConfigLeafListHelper(iterfaceModelNodeSchemaPath, m_configuredModeQName)).thenReturn
                (m_configuredModeLeafListHelper);

        //load registry with bbf-qos-policies
        QName classifierQName = QName.create("urn:broadband-forum-org:yang:bbf-qos-policies",
                "classifier-action-entry-cfg");
        m_actionTypeQName = QName.create("urn:broadband-forum-org:yang:bbf-qos-policies-interfaces", "action-type");
        m_classifierSchemaPath = SchemaPath.create(true, classifierQName);
        m_actionTypeSchemaPath = m_classifierSchemaPath.createChild(m_actionTypeQName);

        when(schemaRegistry.lookupQName("urn:broadband-forum-org:yang:bbf-qos-policies",
                "classifier-action-entry-cfg")).thenReturn(classifierQName);
        when(schemaRegistry.lookupQName("urn:broadband-forum-org:yang:bbf-qos-policies", "action-type")).thenReturn
                (m_actionTypeQName);

        when(schemaRegistry.getPrefix("urn:broadband-forum-org:yang:bbf-qos-policies")).thenReturn("bbf-qos-pol");

        when(schemaRegistry.getDescendantSchemaPath(m_classifierSchemaPath, m_actionTypeQName)).thenReturn
                (m_actionTypeSchemaPath);

        LeafSchemaNode actionTypeSchemaNode = mock(LeafSchemaNode.class);
        when(actionTypeSchemaNode.getType()).thenReturn((TypeDefinition) mock(IdentityrefTypeDefinition.class));
        when(schemaRegistry.getDataSchemaNode(m_actionTypeSchemaPath)).thenReturn(actionTypeSchemaNode);
        m_actionTypeLeafhelper = mock(ConfigAttributeHelper.class);
        when(registry.getNaturalKeyHelper(m_classifierSchemaPath, m_actionTypeQName)).thenReturn
                (m_actionTypeLeafhelper);

    }


    @Test
    public void testPrepareEditSubTreeWithIdentityrefPrefix() throws EditConfigException, ParserConfigurationException {

        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1) {
            m_interfaceElement.removeChild(nodeList.item(1));
        }

        /**
         * Append if:type child
         *          <if:interface  xmlns:prefix1="urn:bbf:yang:bbf-fastdsl">
         *	          <if:name>xdsl:1</if:name>
         *	          <if:type xmlns:bbfitf="urn:broadband-forum-org:yang:bbf-if-type">bbfitf:xdsl</if:type>
         *            <prefix1:configured-mode>prefix1:modefast</prefix1:configured-mode>
         *			</if:interface>
         */
        m_interfaceElement.setAttributeNS(xmlns_ns, xmlns + "prefix1", "urn:bbf:yang:bbf-fastdsl");
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:type");
        child2.setTextContent("bbfitf:xdsl"); //ultimately this value must be changed to bbfift:xdsl
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        Element mode = document.createElementNS("urn:bbf:yang:bbf-fastdsl", "prefix1:configured-mode");
        mode.setTextContent("prefix1:modefast"); //prefix1 must be changed to fastdsl:modefast
        m_interfaceElement.appendChild(mode);


        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, schemaRegistry,
                registry, m_modelNodeId);

        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_typeQName);
        assertEquals("bbfift:xdsl", changeNode.getValue());

        EditChangeNode LeafListChangeNode = m_rootEditNode.getChangeNode(m_configuredModeQName);
        assertEquals("fastdsl:modefast", LeafListChangeNode.getValue());
    }

    @Test
    public void testPrepareEditSubTreeWithIdentityref() throws EditConfigException, ParserConfigurationException {
        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1) {
            m_interfaceElement.removeChild(nodeList.item(1));
        }

        /**
         * Append if:type child
         *          <if:interface xmlns="urn:bbf:yang:bbf-fastdsl">
         *            <if:name>xdsl:1</if:name>
         *            <if:type xmlns="urn:broadband-forum-org:yang:bbf-if-type">xdsl</if:type>
         *            <configured-mode>modefast</configured-mode>
         *          </if:interface>
         */
        m_interfaceElement.setAttributeNS(xmlns_ns, "xmlns", "urn:bbf:yang:bbf-fastdsl");
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:type");
        child2.setTextContent("xdsl"); //ultimately this value must be changed to bbfift:xdsl
        child2.setAttributeNS(xmlns_ns, "xmlns", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        Element mode = document.createElementNS("urn:bbf:yang:bbf-fastdsl", "configured-mode");
        mode.setTextContent("modefast"); //ultimately this value must be changed to fastdsl:modefast
        m_interfaceElement.appendChild(mode);

        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, schemaRegistry,
                registry, m_modelNodeId);
        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_typeQName);
        assertEquals("bbfift:xdsl", changeNode.getValue());

        EditChangeNode LeafListChangeNode = m_rootEditNode.getChangeNode(m_configuredModeQName);
        assertEquals("fastdsl:modefast", LeafListChangeNode.getValue());
    }


    @Test
    public void testPrepareEditSubTreeWithKeyTypeIdentityrefPrefix() throws EditConfigException,
            ParserConfigurationException {

        /**
         * <ns1:classifier-action-entry-cfg xmlns:ns1="urn:broadband-forum-org:yang:bbf-qos-policies">
         *      <ns1:action-type>ns1:scheduling-traffic-class</ns1:action-type>
         * </ns1:classifier-action-entry-cfg>
         */
        Element classifierElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies",
                "ns1:classifier-action-entry-cfg");
        classifierElement.setAttributeNS(xmlns_ns, "xmlns:ns1", "urn:broadband-forum-org:yang:bbf-qos-policies");
        Element actionTypeElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies",
                "ns1:action-type");
        actionTypeElement.setTextContent("ns1:scheduling-traffic-class");
        classifierElement.appendChild(actionTypeElement);


        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, classifierElement, m_classifierSchemaPath, schemaRegistry,
                registry, m_modelNodeId);

        EditMatchNode matchNode = m_rootEditNode.getEditMatchNode(m_actionTypeQName.getLocalName(), m_actionTypeQName
                .getNamespace().toString());
        assertEquals("bbf-qos-pol:scheduling-traffic-class", matchNode.getValue());
    }


    @Test
    public void testPrepareEditSubTreeWithKeyTypeIdentityref() throws EditConfigException,
            ParserConfigurationException {

        /**
         * <classifier-action-entry-cfg xmlns="urn:broadband-forum-org:yang:bbf-qos-policies">
         *      <action-type>scheduling-traffic-class</action-type>
         * </classifier-action-entry-cfg>
         */
        Element classifierElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies",
                "classifier-action-entry-cfg");
        classifierElement.setAttributeNS(xmlns_ns, "xmlns", "urn:broadband-forum-org:yang:bbf-qos-policies");
        Element actionTypeElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies",
                "action-type");
        actionTypeElement.setTextContent("scheduling-traffic-class");
        classifierElement.appendChild(actionTypeElement);


        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, classifierElement, m_classifierSchemaPath, schemaRegistry,
                registry, m_modelNodeId);

        EditMatchNode matchNode = m_rootEditNode.getEditMatchNode(m_actionTypeQName.getLocalName(), m_actionTypeQName
                .getNamespace().toString());
        assertEquals("bbf-qos-pol:scheduling-traffic-class", matchNode.getValue());
    }

    @Test
    public void testPrepareEditSubTreeMultiPrefix() throws EditConfigException, ParserConfigurationException {
        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1) {
            m_interfaceElement.removeChild(nodeList.item(1));
        }

        /**
         * Append if:type child
         *          <if:interface>
         *	          <if:name>xdsl:1</if:name>
         *	          <if:instance xmlns:bbfitf="urn:broadband-forum-org:yang:bbf-if-type"
         *            xmlns:bbfiitf="urn:broadband-forum-org:yang:bbf-if-type">/bbfitf:xdsl/bbiitf:xdsl</if:instance>
         *			</if:interface>
         */
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:instance");
        child2.setTextContent("/bbfitf:xdsl/bbfiitf:xdsl"); //ultimately this value must be changed to
        // /bbfift:xdsl/bbfift:xdsl
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, schemaRegistry,
                registry, m_modelNodeId);
        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
        assertFalse(changeNode.getValue().contains("bbfitf"));
        assertFalse(changeNode.getValue().contains("bbfiitf"));
        assertEquals("/bbfift:xdsl/bbfift:xdsl", changeNode.getValue());
    }

    @Test
    public void testPrepareEditSubTreeInstanceIdentifierWithKey() throws EditConfigException,
            ParserConfigurationException {
        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1) {
            m_interfaceElement.removeChild(nodeList.item(1));
        }

        /**
         * Append if:type child
         *          <if:interface>
         *	          <if:name>xdsl:1</if:name>
         *	          <if:instance xmlns:bbfitf="urn:broadband-forum-org:yang:bbf-if-type"
         *            xmlns:bbfiitf="urn:broadband-forum-org:yang:bbf-if-type">/bbfitf:xdsl/bbiitf:xdsl</if:instance>
         *			</if:interface>
         */
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:instance");
        child2.setTextContent("/bbfitf:xdsl[bbfitf:key1]/bbfiitf:xdsl");
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, schemaRegistry,
                registry, m_modelNodeId);
        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
        assertFalse(changeNode.getValue().contains("bbfitf"));
        assertFalse(changeNode.getValue().contains("bbfiitf"));
        assertEquals("/bbfift:xdsl[bbfift:key1]/bbfift:xdsl", changeNode.getValue());
    }

    @Test
    public void testPrepareEditSubTreeInstanceIdentifierWithKeys() throws EditConfigException,
            ParserConfigurationException {
        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1) {
            m_interfaceElement.removeChild(nodeList.item(1));
        }

        /**
         * Append if:type child
         *          <if:interface>
         *	          <if:name>xdsl:1</if:name>
         *	          <if:instance xmlns:bbfitf="urn:broadband-forum-org:yang:bbf-if-type"
         *            xmlns:bbfiitf="urn:broadband-forum-org:yang:bbf-if-type">/bbfitf:xdsl/bbiitf:xdsl</if:instance>
         *			</if:interface>
         */
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces", "if:instance");
        child2.setTextContent("/bbfitf:xdsl[bbfitf:key1][bbfitf:key2][.='abc']/bbfiitf:xdsl");
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
        child2.setAttributeNS(xmlns_ns, xmlns + "bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, schemaRegistry, registry, m_modelNodeId);
        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
        assertFalse(changeNode.getValue().contains("bbfitf"));
        assertFalse(changeNode.getValue().contains("bbfiitf"));
        assertEquals("/bbfift:xdsl[bbfift:key1][bbfift:key2][.='abc']/bbfift:xdsl", changeNode.getValue());
    }


}
