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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;
import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.EditTreeBuilder.addLeafEditChangeNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.EditTreeBuilder.addLeafListEditChangeNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.EditTreeBuilder.getSchemaPath;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLStringEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountRegistries;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.support.SchemaMountRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaNodeConstraintValidatorRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * EditTreeBuidlerTest using Following yang modules.
 *
 * ---------------------------------------------------------------
 *
 * module ietf-interfaces {
 *
 *    namespace "urn:ietf:params:xml:ns:yang:ietf-interfaces";
 *
 *    prefix if;
 *
 *    list interface {
 *
 *          key name;
 *
 *          leaf name { type string; }
 *
 *          leaf type { type identityref { base interface-type }  }
 *
 *          leaf instance { type instance-identifer; }
 *
 *    }
 *
 *
 * }
 *
 * -----------------------------------------------------------------
 *
 * module bbf-fastdsl {
 *
 *     namespace "urn:bbf:yang:bbf-fastdsl";
 *
 *     prefix "bbf-fastdsl";
 *
 *     import ietf-interface { prefix "if" };
 *
 *     identity fastdsl-mode;
 *
 *     identity mode-fast {
 *          base fastdsl-mode;
 *     }
 *
 *     typedef fastdsl-mode-ref {
 *          type identityref {
 *              fastdsl-mode;
 *          }
 *     }
 *
 *     augment "/if:interfaces-state/if:interface" {
 *         when "if:type = 'ianaift:fastdsl'";
 *
 *         leaf-list configured-mode {
 *             type fastdsl-mode-ref;
 *         }
 *     }
 * }
 *
 * -----------------------------------------------------------------
 *
 * module bbf-qos-policies {
 *
 *     namespace "urn:broadband-forum-org:yang:bbf-qos-policies";
 *
 *     prefix "bbf-qos-pol";
 *
 *     identity action-type ;
 *
 *     identity scheduling-traffic-class {
 *         base action-type;
 *     }
 *
 *     list classifier-action-entry-cfg {
 *          key "action-type";
 *
 *           leaf action-type {
 *              type identityref {
 *                  base action-type;
 *              }
 *           }
 *     }
 * }
 * -----------------------------------------------------------------
 */
@RunWith(RequestScopeJunitRunner.class)
public class EditTreeBuilderTest {

	public static final SchemaPath MP_SP = SchemaPathBuilder.fromString("(ns:ns?revision=2019-03-13)root,mp");
	private static final String NC_OPERATION_MERGE = "merge";
	private static final String NC_OPERATION_DELETE = "delete";
	private static final String NC_OPERATION_REMOVE = "remove";
	public static final String TEST_NAMESPACE = "testNamespace";
	public static final String MAIN_MODULE_NS = "ns:ns";
	private String xmlns_ns = PojoToDocumentTransformer.XMLNS_NAMESPACE;
    private String xmlns = PojoToDocumentTransformer.XMLNS;
    @Mock
    private ModelNodeId m_modelNodeId;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private ModelNodeHelperRegistry m_modelNodeHelperregistry;
    @Mock
    private ConfigAttributeHelper m_nameLeafhelper;
    @Mock
    private ConfigAttributeHelper m_typeLeafhelper;
    @Mock
    private ConfigAttributeHelper m_instanceLeafhelper;
    @Mock
    private ChildLeafListHelper m_configuredModeLeafListHelper;
    @Mock
    private ChildContainerHelper m_childContainerHelper;
    private EditContainmentNode m_rootEditNode;
    private Document document;
    private Element m_interfaceElement;
    private SchemaPath iterfaceModelNodeSchemaPath;
    private QName m_typeQName;
    private QName m_instanceQName;
    private QName m_channelGroupQName;
    private QName m_configuredModeQName;
    private QName m_pollingPeriodQName;
	private QName m_speedQName;

	private QName m_actionTypeQName;
    private SchemaPath m_classifierSchemaPath;
    private SchemaPath m_actionTypeSchemaPath;
    private ConfigAttributeHelper m_actionTypeLeafhelper;

	@Before
	public void setup() throws ParserConfigurationException{
		initMocks(this);
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
		QName qname = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "1996-02-23", "interface");
		QName nameQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces","1996-02-23", "name");
		m_typeQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "1996-02-23", "type");
		m_instanceQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "1996-02-23", "instance");
		m_channelGroupQName = QName.create("urn:bbf:yang:bbf-xponinfra", "1996-02-23", "channel-group");
		m_pollingPeriodQName = QName.create("urn:bbf:yang:bbf-xponinfra", "1996-02-23", "polling-period");
		m_speedQName = QName.create("urn:bbf:yang:bbf-xponinfra", "1996-02-23", "speed");
		m_configuredModeQName = QName.create("urn:bbf:yang:bbf-fastdsl", "1996-02-23", "configured-mode");
		iterfaceModelNodeSchemaPath = SchemaPath.create(true, qname);
		SchemaPath nameSchemaPath = iterfaceModelNodeSchemaPath.createChild(nameQName);
		SchemaPath typeSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_typeQName);
		SchemaPath instanceSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_instanceQName);
		SchemaPath configuredModeSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_typeQName);
		SchemaPath channelGroupSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_channelGroupQName);
		SchemaPath pollingPeriodSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_pollingPeriodQName);
		SchemaPath speedSchemaPath = iterfaceModelNodeSchemaPath.createChild(m_speedQName);

		when(m_schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "interface")).thenReturn(qname);
		when(m_schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "name")).thenReturn(nameQName);
		when(m_schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "type")).thenReturn(m_typeQName);
		when(m_schemaRegistry.lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces", "instance")).thenReturn(m_instanceQName);
		when(m_schemaRegistry.lookupQName("urn:bbf:yang:bbf-fastdsl", "configured-mode")).thenReturn(m_configuredModeQName);
		when(m_schemaRegistry.lookupQName("urn:bbf:yang:bbf-xponinfra", "channel-group")).thenReturn(m_channelGroupQName);
		when(m_schemaRegistry.lookupQName("urn:bbf:yang:bbf-xponinfra", "polling-period")).thenReturn(m_pollingPeriodQName);
		when(m_schemaRegistry.lookupQName("urn:bbf:yang:bbf-xponinfra", "speed")).thenReturn(m_speedQName);
		when(m_schemaRegistry.getPrefix("urn:broadband-forum-org:yang:bbf-if-type")).thenReturn("bbfift");
		when(m_schemaRegistry.getPrefix("urn:bbf:yang:bbf-fastdsl")).thenReturn("fastdsl");

		when(m_schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(nameSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_typeQName)).thenReturn(typeSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_instanceQName)).thenReturn(instanceSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_configuredModeQName)).thenReturn(configuredModeSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(iterfaceModelNodeSchemaPath, m_channelGroupQName)).thenReturn(channelGroupSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(channelGroupSchemaPath, m_pollingPeriodQName)).thenReturn(pollingPeriodSchemaPath);
		when(m_schemaRegistry.getDescendantSchemaPath(channelGroupSchemaPath,m_speedQName)).thenReturn(speedSchemaPath);

		when(m_schemaRegistry.getDataSchemaNode(pollingPeriodSchemaPath)).thenReturn(mock(LeafSchemaNode.class));
		when(m_schemaRegistry.getDataSchemaNode(speedSchemaPath)).thenReturn(mock(LeafSchemaNode.class));
		QName dummyQname = QName.create("urn:ietf:params:xml:ns:yang:1", "dummyLocalName");
		TypeDefinition typeDefinition = mock(StringTypeDefinition.class);
		when(typeDefinition.getQName()).thenReturn(dummyQname);
		LeafSchemaNode nameSchemaNode = mock(LeafSchemaNode.class);
		when(nameSchemaNode.getType()).thenReturn(typeDefinition);
		when(m_schemaRegistry.getDataSchemaNode(nameSchemaPath)).thenReturn(nameSchemaNode);

		typeDefinition = mock(IdentityrefTypeDefinition.class);
		when(typeDefinition.getQName()).thenReturn(dummyQname);
		LeafSchemaNode typeSchemaNode = mock(LeafSchemaNode.class);
		when(typeSchemaNode.getType()).thenReturn(typeDefinition);
        when(m_schemaRegistry.getDataSchemaNode(typeSchemaPath)).thenReturn(typeSchemaNode);

		typeDefinition = mock(InstanceIdentifierTypeDefinition.class);
		when(typeDefinition.getQName()).thenReturn(dummyQname);
        LeafSchemaNode instanceSchemaNode = mock(LeafSchemaNode.class);
        when(instanceSchemaNode.getType()).thenReturn(typeDefinition);
        when(m_schemaRegistry.getDataSchemaNode(instanceSchemaPath)).thenReturn(instanceSchemaNode);

		typeDefinition = mock(IdentityrefTypeDefinition.class);
		when(typeDefinition.getQName()).thenReturn(dummyQname);
        LeafListSchemaNode configuredModeSchemaNode = mock(LeafListSchemaNode.class);
        when(configuredModeSchemaNode.getType()).thenReturn(typeDefinition);
        when(m_schemaRegistry.getDataSchemaNode(configuredModeSchemaPath)).thenReturn(configuredModeSchemaNode);

        when(m_modelNodeHelperregistry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(m_nameLeafhelper);
        when(m_modelNodeHelperregistry.getNaturalKeyHelper(iterfaceModelNodeSchemaPath, nameQName)).thenReturn(m_nameLeafhelper);
		when(m_modelNodeHelperregistry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, m_typeQName)).thenReturn(m_typeLeafhelper);
		when(m_modelNodeHelperregistry.getConfigAttributeHelper(iterfaceModelNodeSchemaPath, m_instanceQName)).thenReturn(m_instanceLeafhelper);
		when(m_modelNodeHelperregistry.getConfigLeafListHelper(iterfaceModelNodeSchemaPath, m_configuredModeQName)).thenReturn(m_configuredModeLeafListHelper);
		when(m_modelNodeHelperregistry.getChildContainerHelper(iterfaceModelNodeSchemaPath, m_channelGroupQName)).thenReturn(m_childContainerHelper);
		when(m_modelNodeHelperregistry.getNaturalKeyHelper(channelGroupSchemaPath, m_pollingPeriodQName)).thenReturn(m_nameLeafhelper);
		when(m_modelNodeHelperregistry.getConfigAttributeHelper(channelGroupSchemaPath, m_speedQName)).thenReturn(m_typeLeafhelper);

		when(m_childContainerHelper.getChildModelNodeSchemaPath()).thenReturn(channelGroupSchemaPath);

		//load registry with bbf-qos-policies
		QName classifierQName = QName.create("urn:broadband-forum-org:yang:bbf-qos-policies", "classifier-action-entry-cfg");
		m_actionTypeQName = QName.create("urn:broadband-forum-org:yang:bbf-qos-policies-interfaces",  "action-type");
        m_classifierSchemaPath = SchemaPath.create(true, classifierQName);
        m_actionTypeSchemaPath = m_classifierSchemaPath.createChild(m_actionTypeQName);

        when(m_schemaRegistry.lookupQName("urn:broadband-forum-org:yang:bbf-qos-policies", "classifier-action-entry-cfg")).thenReturn(classifierQName);
        when(m_schemaRegistry.lookupQName("urn:broadband-forum-org:yang:bbf-qos-policies", "action-type")).thenReturn(m_actionTypeQName);

        when(m_schemaRegistry.getPrefix("urn:broadband-forum-org:yang:bbf-qos-policies")).thenReturn("bbf-qos-pol");

        when(m_schemaRegistry.getDescendantSchemaPath(m_classifierSchemaPath, m_actionTypeQName)).thenReturn(m_actionTypeSchemaPath);

        LeafSchemaNode actionTypeSchemaNode = mock(LeafSchemaNode.class);
        when(actionTypeSchemaNode.getType()).thenReturn((TypeDefinition)mock(IdentityrefTypeDefinition.class));
        when(m_schemaRegistry.getDataSchemaNode(m_actionTypeSchemaPath)).thenReturn(actionTypeSchemaNode);
        m_actionTypeLeafhelper = mock(ConfigAttributeHelper.class);
        when(m_modelNodeHelperregistry.getNaturalKeyHelper(m_classifierSchemaPath, m_actionTypeQName)).thenReturn(m_actionTypeLeafhelper);
	}


	@Test
	public void testPrepareEditSubTreeWithIdentityrefPrefix() throws EditConfigException, ParserConfigurationException{

		// check if there is a second child and remove if one is present
		NodeList nodeList = m_interfaceElement.getChildNodes();

		while (nodeList.getLength() > 1){
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
		m_interfaceElement.setAttributeNS(xmlns_ns, xmlns+"prefix1", "urn:bbf:yang:bbf-fastdsl");
		Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces","if:type");
		child2.setTextContent("bbfitf:xdsl"); //ultimately this value must be changed to bbfift:xdsl
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
		m_interfaceElement.appendChild(child2);

		Element mode = document.createElementNS("urn:bbf:yang:bbf-fastdsl","prefix1:configured-mode");
		mode.setTextContent("prefix1:modefast"); //prefix1 must be changed to fastdsl:modefast
        m_interfaceElement.appendChild(mode);


		/**
		 * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
		 */
		EditTreeBuilder builder = new EditTreeBuilder();
		builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_typeQName);
		assertEquals("bbfift:xdsl", changeNode.getValue());

		EditChangeNode LeafListChangeNode = m_rootEditNode.getChangeNode(m_configuredModeQName);
		assertEquals("fastdsl:modefast", LeafListChangeNode.getValue());
		assertTrue(LeafListChangeNode.isIdentityRefNode());
	}

    @Test
    public void testPrepareEditSubTreeWithIdentityref() throws EditConfigException, ParserConfigurationException{
        // check if there is a second child and remove if one is present
        NodeList nodeList = m_interfaceElement.getChildNodes();

        while (nodeList.getLength() > 1){
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
        Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces","if:type");
        child2.setTextContent("xdsl"); //ultimately this value must be changed to bbfift:xdsl
        child2.setAttributeNS(xmlns_ns, "xmlns", "urn:broadband-forum-org:yang:bbf-if-type");
        m_interfaceElement.appendChild(child2);

        Element mode = document.createElementNS("urn:bbf:yang:bbf-fastdsl","configured-mode");
        mode.setTextContent("modefast"); //ultimately this value must be changed to fastdsl:modefast
        m_interfaceElement.appendChild(mode);

        /**
         * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
         */
        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
        EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_typeQName);
        assertEquals("bbfift:xdsl", changeNode.getValue());

        EditChangeNode LeafListChangeNode = m_rootEditNode.getChangeNode(m_configuredModeQName);
        assertEquals("fastdsl:modefast", LeafListChangeNode.getValue());
        assertTrue(LeafListChangeNode.isIdentityRefNode());
    }


    @Test
    public void testPrepareEditSubTreeWithKeyTypeIdentityrefPrefix() throws EditConfigException, ParserConfigurationException{

        /**
         * <ns1:classifier-action-entry-cfg xmlns:ns1="urn:broadband-forum-org:yang:bbf-qos-policies">
         *      <ns1:action-type>ns1:scheduling-traffic-class</ns1:action-type>
         * </ns1:classifier-action-entry-cfg>
         */
        Element classifierElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies", "ns1:classifier-action-entry-cfg");
        classifierElement.setAttributeNS(xmlns_ns, "xmlns:ns1", "urn:broadband-forum-org:yang:bbf-qos-policies");
        Element actionTypeElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies","ns1:action-type");
        actionTypeElement.setTextContent("ns1:scheduling-traffic-class");
        classifierElement.appendChild(actionTypeElement);


        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, classifierElement, m_classifierSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

        EditMatchNode matchNode = m_rootEditNode.getEditMatchNode(m_actionTypeQName.getLocalName(), m_actionTypeQName.getNamespace().toString());
        assertEquals("bbf-qos-pol:scheduling-traffic-class", matchNode.getValue());
        assertTrue(matchNode.isIdentityRefNode());
    }


    @Test
    public void testPrepareEditSubTreeWithKeyTypeIdentityref() throws EditConfigException, ParserConfigurationException{

        /**
         * <classifier-action-entry-cfg xmlns="urn:broadband-forum-org:yang:bbf-qos-policies">
         *      <action-type>scheduling-traffic-class</action-type>
         * </classifier-action-entry-cfg>
         */
        Element classifierElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies", "classifier-action-entry-cfg");
        classifierElement.setAttributeNS(xmlns_ns, "xmlns", "urn:broadband-forum-org:yang:bbf-qos-policies");
        Element actionTypeElement = document.createElementNS("urn:broadband-forum-org:yang:bbf-qos-policies","action-type");
        actionTypeElement.setTextContent("scheduling-traffic-class");
        classifierElement.appendChild(actionTypeElement);


        EditTreeBuilder builder = new EditTreeBuilder();
        builder.prepareEditSubTree(m_rootEditNode, classifierElement, m_classifierSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

        EditMatchNode matchNode = m_rootEditNode.getEditMatchNode(m_actionTypeQName.getLocalName(), m_actionTypeQName.getNamespace().toString());
        assertEquals("bbf-qos-pol:scheduling-traffic-class", matchNode.getValue());
        assertTrue(matchNode.isIdentityRefNode());
    }

	@Test
	public void testPrepareEditSubTreeMultiPrefix() throws EditConfigException, ParserConfigurationException{
		// check if there is a second child and remove if one is present
		NodeList nodeList = m_interfaceElement.getChildNodes();

		while (nodeList.getLength() > 1){
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
		Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces","if:instance");
		child2.setTextContent("/bbfitf:xdsl/bbfiitf:xdsl"); //ultimately this value must be changed to /bbfift:xdsl/bbfift:xdsl
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
		m_interfaceElement.appendChild(child2);

		/**
		 * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
		 */
		EditTreeBuilder builder = new EditTreeBuilder();
		builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
		EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
		assertFalse(changeNode.getValue().contains("bbfitf"));
		assertFalse(changeNode.getValue().contains("bbfiitf"));
		assertEquals("/bbfift:xdsl/bbfift:xdsl", changeNode.getValue());
	}

	@Test
	public void testPrepareEditSubTreeInstanceIdentifierWithKey() throws EditConfigException, ParserConfigurationException{
		// check if there is a second child and remove if one is present
		NodeList nodeList = m_interfaceElement.getChildNodes();

		while (nodeList.getLength() > 1){
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
		Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces","if:instance");
		child2.setTextContent("/bbfitf:xdsl[bbfitf:key1]/bbfiitf:xdsl");
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
		m_interfaceElement.appendChild(child2);

		/**
		 * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
		 */
		EditTreeBuilder builder = new EditTreeBuilder();
		builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
		EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
		assertFalse(changeNode.getValue().contains("bbfitf"));
		assertFalse(changeNode.getValue().contains("bbfiitf"));
		assertEquals("/bbfift:xdsl[bbfift:key1]/bbfift:xdsl", changeNode.getValue());
	}

	@Test
	public void testPrepareEditSubTreeInstanceIdentifierWithKeys() throws EditConfigException, ParserConfigurationException{
		// check if there is a second child and remove if one is present
		NodeList nodeList = m_interfaceElement.getChildNodes();

		while (nodeList.getLength() > 1){
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
		Element child2 = document.createElementNS("urn:ietf:params:xml:ns:yang:ietf-interfaces","if:instance");
		child2.setTextContent("/bbfitf:xdsl[bbfitf:key1][bbfitf:key2][.='abc']/bbfiitf:xdsl");
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfitf", "urn:broadband-forum-org:yang:bbf-if-type");
		child2.setAttributeNS(xmlns_ns, xmlns+"bbfiitf", "urn:broadband-forum-org:yang:bbf-if-type");
		m_interfaceElement.appendChild(child2);

		/**
		 * The change node must not contain the prefix bbfitf. It should be replaced with bbfift"
		 */
		EditTreeBuilder builder = new EditTreeBuilder();
		builder.prepareEditSubTree(m_rootEditNode, m_interfaceElement, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
		EditChangeNode changeNode = m_rootEditNode.getChangeNode(m_instanceQName);
		assertFalse(changeNode.getValue().contains("bbfitf"));
		assertFalse(changeNode.getValue().contains("bbfiitf"));
		assertEquals("/bbfift:xdsl[bbfift:key1][bbfift:key2][.='abc']/bbfift:xdsl", changeNode.getValue());
	}

	@Test
	public void testPrepareEditSubtree_PopulatesLeafLeafListsFirst() throws Exception {

		SchemaRegistryImpl globalSR = new SchemaRegistryImpl(getGlobalYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		SchemaRegistryImpl mountSR = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());

		ModelNodeHelperRegistry globalModelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(globalSR);
		ModelNodeHelperRegistry mountMNHelperRegistry = new ModelNodeHelperRegistryImpl(mountSR);

		ModelNodeId rootMNId = new ModelNodeId("/container=root", TEST_NAMESPACE);

		EditContainmentNode actualEditNode = new EditContainmentNode();
		EditTreeBuilder builder = new EditTreeBuilder();

		Element elementWithMountPoint = TestUtil.
				transformToElement( "<mm:root xmlns:mm=\"ns:ns\">\n" +
						"  <mm:leaf1>leaf1</mm:leaf1>\n" +
						"  <mm:leaf2>leaf2</mm:leaf2>\n" +
						"  <mm:mp xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">\n" +
						"    <jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\"/>\n" +
						"  </mm:mp>\n" +
						"  <mm:leaf-list1>leaf-list11</mm:leaf-list1>\n" +
						"  <mm:leaf-list1>leaf-list12</mm:leaf-list1>\n" +
						"  <mm:leaf-list2>leaf-list21</mm:leaf-list2>\n" +
						"  <mm:leaf-list2>leaf-list22</mm:leaf-list2>\n" +
						"</mm:root>");

		SubSystemRegistry subSystemRegistry = mock(SubSystemRegistry.class);
		SchemaPath rootSP = fromString("(ns:ns?revision=2019-03-13)root");
		EditContainmentNode expectedRootEditNode = getExpectedRootEditNode(rootSP, globalSR);
		setupRegistriesAndMPProvider(globalSR, mountSR, mountMNHelperRegistry, subSystemRegistry, expectedRootEditNode);
		populateMPEditNode(mountSR, expectedRootEditNode);
		deployHelpers(globalSR, "global", globalModelNodeHelperRegistry, null, subSystemRegistry);
		deployHelpers(mountSR, "mounted", mountMNHelperRegistry, MP_SP, subSystemRegistry);

		builder.prepareEditSubTree(actualEditNode, elementWithMountPoint, rootSP,
				globalSR, globalModelNodeHelperRegistry, rootMNId);
		assertEquals(expectedRootEditNode.toString(), actualEditNode.toString());
		assertEquals(6, actualEditNode.getChangeNodes().size());
        assertEquals(1, actualEditNode.getChildren().size());
        EditContainmentNode mpNode = actualEditNode.getChildren().get(0);
		assertTrue(mpNode.getChangeNodes().isEmpty());
		assertEquals(1, mpNode.getChildren().size());
	}

    @Test
    public void testAddLeafListEditChangeNode() throws SAXException, IOException {

        EditContainmentNode rootNode = new TestEditContainmentNode(QName.create(TEST_NAMESPACE, "Container1"), "testOperation", m_schemaRegistry);
        QName qName = QName.create(TEST_NAMESPACE, "leafList1");
        EditChangeNode changeNode1 = new EditChangeNode(qName, new GenericConfigAttribute("leafList1", TEST_NAMESPACE, "value1"));
        rootNode.addChangeNode(changeNode1);
        QName childQName = QName.create(TEST_NAMESPACE, "leafList1");
        SchemaPath modelNodeSchemaPath = SchemaPath.create(true, childQName);
        Node child = mock(Node.class);
        when(child.getTextContent()).thenReturn("value1");
        try {
            addLeafListEditChangeNode(rootNode, childQName, child, modelNodeSchemaPath, null);
        } catch (EditConfigException e) {
            NetconfRpcError rpcError = e.getRpcError();
            NetConfResponse response = new NetConfResponse().addError(rpcError).setMessageId("1");
            assertXMLStringEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-not-unique</error-app-tag>\n" +
                    "    <error-path>/Container1/leafList1</error-path>\n" +
                    "    <error-message>Duplicate elements in node (testNamespace)leafList1</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>", response.responseToString().trim());
        }
    }

    @Test
    public void testAddLeafEditChangeNode() throws SAXException, IOException {

        EditContainmentNode rootNode = new TestEditContainmentNode(QName.create(TEST_NAMESPACE, "Container1"), "testOperation", m_schemaRegistry);
        QName qName = QName.create(TEST_NAMESPACE, "leaf1");
        EditChangeNode changeNode1 = new EditChangeNode(qName, new GenericConfigAttribute("leafList1", TEST_NAMESPACE, "value1"));
        rootNode.addChangeNode(changeNode1);
        QName childQName = QName.create(TEST_NAMESPACE, "leaf1");
        SchemaPath modelNodeSchemaPath = SchemaPath.create(true, childQName);
        Node child = mock(Node.class);
        when(child.getTextContent()).thenReturn("value2");
        try {
            addLeafEditChangeNode(rootNode, childQName, child, modelNodeSchemaPath, null);
        } catch (EditConfigException e) {
            NetconfRpcError rpcError = e.getRpcError();
            NetConfResponse response = new NetConfResponse().addError(rpcError).setMessageId("1");
            assertXMLStringEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-not-unique</error-app-tag>\n" +
                    "    <error-path>/Container1/leaf1</error-path>\n" +
                    "    <error-message>Duplicate elements in node (testNamespace)leaf1</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>", response.responseToString().trim());
        }
    }

	@Test
	public void testPrepareEditSubTreeWithDeleteOnRootNode() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_DELETE, "");
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertTrue(m_rootEditNode.getChangeNodes().isEmpty());
		assertTrue(m_rootEditNode.getChildren().isEmpty());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
	}

	@Test
	public void testPrepareEditSubTreeWithRemoveOnRootNode() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_REMOVE, "");
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertTrue(m_rootEditNode.getChangeNodes().isEmpty());
		assertTrue(m_rootEditNode.getChildren().isEmpty());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
	}

	@Test
	public void testPrepareEditSubTreeWithMergeOnRootNode() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_MERGE, NC_OPERATION_MERGE);
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertEquals(2, m_rootEditNode.getChangeNodes().size());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
		assertEquals(1, m_rootEditNode.getChildren().size());
	}

	@Test
	public void testPrepareEditSubTreeWithDeleteOnNonKeyNode() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_MERGE, NC_OPERATION_DELETE);
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertEquals(2, m_rootEditNode.getChangeNodes().size());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
		assertEquals(1, m_rootEditNode.getChildren().size());
		assertEquals(1, m_rootEditNode.getChildNode(m_channelGroupQName).getMatchNodes().size());
		assertTrue(m_rootEditNode.getChildNode(m_channelGroupQName).getChangeNodes().isEmpty());
	}

	@Test
	public void testPrepareEditSubTreeWithRemoveOnNonKeyNode() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_MERGE, NC_OPERATION_REMOVE);
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertEquals(2, m_rootEditNode.getChangeNodes().size());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
		assertEquals(1, m_rootEditNode.getChildren().size());
		assertEquals(1, m_rootEditNode.getChildNode(m_channelGroupQName).getMatchNodes().size());
		assertTrue(m_rootEditNode.getChildNode(m_channelGroupQName).getChangeNodes().isEmpty());
	}

	@Test
	public void testPrepareEditSubTreeWithDeleteOnRootAndMergeOnSubtree() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_DELETE, NC_OPERATION_MERGE);
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);

		assertTrue(m_rootEditNode.getChangeNodes().isEmpty());
		assertTrue(m_rootEditNode.getChildren().isEmpty());
		assertEquals(1, m_rootEditNode.getMatchNodes().size());
	}

	@Test
	public void testGetSchemaPath() throws NetconfMessageBuilderException {
		SchemaPath interfaceSP = fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=1996-02-23)interface");
		SchemaPath pollingPeriodSP = fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=1996-02-23)interface", "(urn:bbf:yang:bbf-xponinfra?revision=1996-02-23)channel-group", "(urn:bbf:yang:bbf-xponinfra?revision=1996-02-23)polling-period");
		SchemaPath configuredModeSP = fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=1996-02-23)interface", "(urn:bbf:yang:bbf-fastdsl?revision=1996-02-23)configured-mode");
		Element element = getElementWithOperation(NC_OPERATION_MERGE, NC_OPERATION_MERGE);
		assertEquals(interfaceSP, getSchemaPath(m_schemaRegistry, element));
		assertEquals(pollingPeriodSP, getSchemaPath(m_schemaRegistry, element.getChildNodes().item(5).getChildNodes().item(1)));
		assertEquals(configuredModeSP, getSchemaPath(m_schemaRegistry, element.getChildNodes().item(6)));
	}

	@Test
	public void testExceptionScenario() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();
		Element element = getElementWithOperation(NC_OPERATION_DELETE, NC_OPERATION_MERGE);
		doThrow(new IllegalArgumentException("Testing Exception")).when(m_schemaRegistry).
				lookupQName("urn:ietf:params:xml:ns:yang:ietf-interfaces","name");

		try{
			builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
			fail("Editconfig Exception expected");
		}catch (EditConfigException e){
			assertEquals("Testing Exception", e.getMessage());
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
			assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
			assertEquals("Testing Exception", rpcError.getErrorMessage());
		}
	}

	@Test
	public void testVisibilityExtension() throws NetconfMessageBuilderException {
		EditTreeBuilder builder = new EditTreeBuilder();

		Element element = getElementWithVisibilityExtension("false");
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
		assertFalse(m_rootEditNode.isVisible());

		m_rootEditNode = new EditContainmentNode();
		element = getElementWithVisibilityExtension("true");
		builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
		assertTrue(m_rootEditNode.isVisible());

		try {
			m_rootEditNode = new EditContainmentNode();
			element = getElementWithVisibilityExtension("dummy");
			builder.prepareEditSubTree(m_rootEditNode, element, iterfaceModelNodeSchemaPath, m_schemaRegistry, m_modelNodeHelperregistry, m_modelNodeId);
			fail("Editconfig Exception expected");
		} catch (EditConfigException e) {
			assertEquals("Bad visibility attribute : dummy", e.getMessage());
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
			assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
	}

	private Element getElementWithOperation(String rootOperation, String subTreeOperation) throws NetconfMessageBuilderException{
		Element element = DocumentUtils.stringToDocument("<if:interface xmlns:prefix1=\"urn:bbf:yang:bbf-fastdsl\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:xponinfra=\"urn:bbf:yang:bbf-xponinfra\" ns0:operation=\"" + rootOperation+ "\">\n" +
				"<if:name>xdsl:1</if:name>\n" +
				"<if:type xmlns:bbfitf=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfitf:xdsl</if:type>\n" +
				"<xponinfra:channel-group xmlns:xponinfra=\"urn:bbf:yang:bbf-xponinfra\" ns0:operation=\""+ subTreeOperation +"\" >\n" +
				"      <xponinfra:polling-period>100</xponinfra:polling-period>\n" +
				"      <xponinfra:speed>10</xponinfra:speed>\n" +
				"</xponinfra:channel-group>" +
				"<prefix1:configured-mode>prefix1:modefast</prefix1:configured-mode>\n" +
				"</if:interface>").getDocumentElement();
		return element;
	}

	private Element getElementWithVisibilityExtension(String operation) throws NetconfMessageBuilderException {
		Element element = DocumentUtils.stringToDocument(
				"<if:interface xmlns:prefix1=\"urn:bbf:yang:bbf-fastdsl\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:ncext=\"http://www.test-company.com/solutions/nc-stack-extensions\" ncext:visibility=\"" + operation + "\" ns0:operation=\"merge\">\n" +
					"  <if:name>xdsl:1</if:name>\n" +
					"  <if:type xmlns:bbfitf=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfitf:xdsl</if:type>\n" +
					"</if:interface>").getDocumentElement();
		return element;
	}

	private EditContainmentNode getExpectedRootEditNode(SchemaPath rootSP, SchemaRegistryImpl globalSR) {
		EditContainmentNode rootNode = new EditContainmentNode(rootSP, EditConfigOperations.MERGE, globalSR, null);
		EditChangeNode leaf1EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf1"),
				new GenericConfigAttribute("leaf1", MAIN_MODULE_NS, "leaf1"));
		EditChangeNode leaf2EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf2"),
				new GenericConfigAttribute("leaf2", MAIN_MODULE_NS, "leaf2"));
		EditChangeNode leafList11EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf-list1"),
				new GenericConfigAttribute("leaf-list1", MAIN_MODULE_NS, "leaf-list11"));
		EditChangeNode leafList12EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf-list1"),
				new GenericConfigAttribute("leaf-list1", MAIN_MODULE_NS, "leaf-list12"));
		EditChangeNode leafList21EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf-list2"),
				new GenericConfigAttribute("leaf-list2", MAIN_MODULE_NS, "leaf-list21"));
		EditChangeNode leafList22EditChangeNode = new EditChangeNode(QName.create(MAIN_MODULE_NS, "leaf-list2"),
				new GenericConfigAttribute("leaf-list2", MAIN_MODULE_NS, "leaf-list22"));

		rootNode.addChangeNode(leaf1EditChangeNode);
		rootNode.addChangeNode(leaf2EditChangeNode);
		rootNode.addChangeNode(leafList11EditChangeNode);
		rootNode.addChangeNode(leafList12EditChangeNode);
		rootNode.addChangeNode(leafList21EditChangeNode);
		rootNode.addChangeNode(leafList22EditChangeNode);
		return rootNode;
	}

	private void populateMPEditNode(SchemaRegistryImpl mountSR, EditContainmentNode rootNode) {
		EditContainmentNode mpNode = new EditContainmentNode(MP_SP, EditConfigOperations.REPLACE, mountSR, null);
		rootNode.addChild(mpNode);

		SchemaPath jukeboxSP = new SchemaPathBuilder().withParent(MP_SP).withNamespace(JB_NS).appendLocalName(JUKEBOX_LOCAL_NAME).build();
		EditContainmentNode jukeboxEditNode = new EditContainmentNode(jukeboxSP, EditConfigOperations.REPLACE, mountSR, null);
		mpNode.addChild(jukeboxEditNode);
	}

	private void setupRegistriesAndMPProvider(SchemaRegistryImpl globalSR, SchemaRegistryImpl mountSR,
											  ModelNodeHelperRegistry mountMNHelperRegistry,
											  SubSystemRegistry subSystemRegistry, EditContainmentNode rootEditNode) throws Exception {
		mountSR.loadSchemaContext("ut", Collections.singletonList(getYangUnderMountPoint()),
				Collections.emptySet(), Collections.emptyMap(), false);
		mountSR.setName("Mount-SR");
		mountSR.setMountPath(MP_SP);
		mountSR.setParentRegistry(globalSR);
		SchemaMountRegistry mountRegistry = new SchemaMountRegistryImpl();
		globalSR.setSchemaMountRegistry(mountRegistry);

		MountRegistries mountRegistries = new MountRegistries(new NoLockService());
		mountRegistries.getSchemaRegistry().setMountPath(MP_SP);
		mountRegistries.getSchemaRegistry().setParentRegistry(globalSR);
		TestMPProvider provider = spy(new TestMPProvider(mountRegistries, mountMNHelperRegistry, subSystemRegistry));
		provider.setMountSR(mountSR);
		provider.setRootEditNode(rootEditNode);
		mountRegistry.register(MP_SP, provider);
		RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, MP_SP);
	}

	private YangTextSchemaSource getYangUnderMountPoint() {
		File file = new File(TestUtil.class.getResource("/referenceyangs/example-jukebox@2014-07-03.yang").getFile());
		return YangParserUtil.getYangSource(file.getPath());
	}

	private List<YangTextSchemaSource> getGlobalYangs() {
		List<YangTextSchemaSource> yangs = new ArrayList<>();

		File yangDir = new File(TestUtil.class.getResource("/edittreebuildertest").getFile());
		for (File file : yangDir.listFiles()) {
			if (file.isFile()) {
				yangs.add(YangParserUtil.getYangSource(file.getPath()));
			}
		}
		return yangs;
	}

	private void deployHelpers(SchemaRegistry schemaRegistry, String componentId, ModelNodeHelperRegistry mnHelper,
							   SchemaPath mountPath, SubSystemRegistry subSystemRegistry) {
		List<SchemaRegistryVisitor> visitors = new ArrayList<>();
		visitors.add(new EntityModelNodeHelperDeployer(mnHelper, schemaRegistry, mock(ModelNodeDataStoreManager.class),
				mock(EntityRegistry.class), subSystemRegistry));
		visitors.add(new SchemaPathRegistrar(schemaRegistry, mnHelper, Collections.emptyMap()));
		visitors.add(new SchemaNodeConstraintValidatorRegistrar(schemaRegistry, mnHelper, subSystemRegistry));
		for (Module module : schemaRegistry.getSchemaContext().getModules()) {
			SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(componentId, visitors, schemaRegistry,
					module);
			traverser.setMountPath(mountPath);
			traverser.traverse();
		}
	}

	private class TestMPProvider implements SchemaMountRegistryProvider {

		private MountRegistries m_mountRegistries;
		private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
		private SubSystemRegistry m_subsystemRegistry;
		private SchemaRegistry c_mountSR;
		private EditContainmentNode m_rootEditNode;

		public TestMPProvider(MountRegistries mountRegistries, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry) {
			m_mountRegistries = mountRegistries;
			m_modelNodeHelperRegistry = modelNodeHelperRegistry;
			m_subsystemRegistry = subsystemRegistry;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(ModelNodeId modelNodeId) {
			return c_mountSR;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(Element element) {
			return c_mountSR;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(EditContainmentNode editContainmentNode) {
			EditContainmentNode rootEditNode = editContainmentNode.getParent();
			if(m_rootEditNode.toString().equals(rootEditNode.toString())){
				return c_mountSR;
			}
			return null;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(Map<String, String> keyValues) {
			return c_mountSR;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(String mountKey) {
			return c_mountSR;
		}

		@Override
		public ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNodeId modelNodeId) {
			return m_modelNodeHelperRegistry;
		}

		@Override
		public ModelNodeHelperRegistry getModelNodeHelperRegistry(Element element) {
			return m_modelNodeHelperRegistry;
		}

		@Override
		public ModelNodeHelperRegistry getModelNodeHelperRegistry(EditContainmentNode editContainmentNode) {
			return m_modelNodeHelperRegistry;
		}

		@Override
		public SubSystemRegistry getSubSystemRegistry(ModelNodeId modelNodeId) {
			return m_subsystemRegistry;
		}

		@Override
		public SchemaMountKey getSchemaMountKey() {
			return null;
		}

		@Override
		public MountRegistries getMountRegistries(String mountkey) {
			return m_mountRegistries;
		}

		@Override
		public void setCorrectPlugMountContextInCache(EditContainmentNode node) {

		}

		@Override
		public List<MountContext> getMountContexts() {
			return null;
		}

		@Override
		public boolean isValidMountPoint(ModelNodeId nodeID) {
			return false;
		}

		public void setMountSR(SchemaRegistry mountSR) {
			this.c_mountSR = mountSR;
		}

		public void setRootEditNode(EditContainmentNode rootEditNode) {
			m_rootEditNode = new EditContainmentNode(rootEditNode);
		}
	}
}
