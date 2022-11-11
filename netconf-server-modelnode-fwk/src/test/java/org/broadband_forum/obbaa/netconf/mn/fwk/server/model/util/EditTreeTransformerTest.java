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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class EditTreeTransformerTest extends XMLTestCase{

    private static final String INTERFACE_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String BBF_QOS_NAMESPACE = "urn:broadband-forum-org:yang:bbf-qos-policies";
    private static final String NAME = "name";
    private static final String ALBUM = "album";
    public static final String YEAR = "year";
    public static final String SONG = "song";

    QName albumQName = QName.create(JB_NS, "2014-07-03", ALBUM);
 	QName nameQName = QName.create(JB_NS, "2014-07-03", NAME);
 	QName yearQName = QName.create(JB_NS, "2014-07-03", YEAR);
 	QName songQName = QName.create(JB_NS, "2014-07-03", SONG);
 	
 	QName interfaceContainerQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "interfaces");
 	
 	QName interfaceQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "interface");
 	QName interfaceNameQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", NAME);
 	QName typeQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "type");
    
 	QName classifierQName = QName.create(BBF_QOS_NAMESPACE, "2016-05-30", "classifier-action-entry-cfg");
 	QName actionTypeQName = QName.create(BBF_QOS_NAMESPACE, "2016-05-30", "action-type");
 	
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }

    @Test
    public void testCreateDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode songNode = new TestEditContainmentNode(songQName, EditConfigOperations.CREATE, m_schemaRegistry).addMatchNode(nameQName, new
                GenericConfigAttribute(NAME, JB_NS, "Thin Ice"));
        EditContainmentNode createData = new TestEditContainmentNode(albumQName, null, m_schemaRegistry).addMatchNode(nameQName, new GenericConfigAttribute
                (NAME, JB_NS, "Circus")).addChild(songNode);
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-add-song.xml", xml);
    }

    @Test
    public void testMergeDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode createData = new TestEditContainmentNode(albumQName, EditConfigOperations.MERGE, m_schemaRegistry).addMatchNode(nameQName,
                new GenericConfigAttribute(NAME, JB_NS, "Circus")).addLeafChangeNode(yearQName, new GenericConfigAttribute(YEAR, JB_NS, "1996"));
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-change-year.xml", xml);
    }

    @Test
    public void testDeleteDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode songNode = new TestEditContainmentNode(songQName, EditConfigOperations.DELETE, m_schemaRegistry).addMatchNode(nameQName, new
                GenericConfigAttribute(SONG, JB_NS, "Thin Ice"));
        EditContainmentNode createData = new TestEditContainmentNode(albumQName, null, m_schemaRegistry).addMatchNode(nameQName, new GenericConfigAttribute
                (ALBUM, JB_NS, "Circus")).addChild(songNode);
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-delete-song.xml", xml);
    }

    @Test
    public void testDeleteLeafDataTransformation() throws IOException, SAXException {
        String namespace = "urn:bbf:yang:bbf-sub-interface-tagging";
        QName matchCriteriaQName = QName.create(namespace, "match-criteria");
        QName untaggedQName = QName.create(namespace,"untagged");

        EditChangeNode editChangeNode = new EditChangeNode(untaggedQName, new GenericConfigAttribute("untagged", namespace, ""));
        editChangeNode.setOperation(EditConfigOperations.DELETE);
        editChangeNode.setChangeSource(EditChangeSource.user);
        EditContainmentNode matchCriteria = new TestEditContainmentNode(matchCriteriaQName, EditConfigOperations.MERGE, m_schemaRegistry).
                addChangeNode(editChangeNode);
        matchCriteria.setChangeSource(EditChangeSource.user);

        Element xml = EditTreeTransformer.toXml(m_schemaRegistry,matchCriteria,true,null);

        assertXMLEquals("/edit-transform-expected-delete-leaf.xml", xml);

    }

    @Test
    public void testEditContainmentNodeToXmlWithIdentityrefPrefix() throws SAXException, IOException, EditConfigException{
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getPrefix(INTERFACE_NAMESPACE)).thenReturn("if");
        when(schemaRegistry.getPrefix(BBF_QOS_NAMESPACE)).thenReturn("bbf-qos-pol");
        EditTreeTransformer.m_includePrefix = true;
        EditContainmentNode interfacesNode = new TestEditContainmentNode(interfaceContainerQName, EditConfigOperations.MERGE, m_schemaRegistry);
        EditContainmentNode interfaceNode = getEditContainmentInterfaceNode();
        interfacesNode.addChild(interfaceNode);
        Element xml = EditTreeTransformer.toXml(schemaRegistry, interfacesNode, true, null);
        assertXMLEquals("/edit-transform-expected-merge-interface.xml", xml);
    }

    @Test
    public void testEditContainmentNodeToXmlWithInsertOperation() throws SAXException, IOException, EditConfigException{
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getPrefix(INTERFACE_NAMESPACE)).thenReturn("if");
        when(schemaRegistry.getPrefix(BBF_QOS_NAMESPACE)).thenReturn("bbf-qos-pol");
        EditTreeTransformer.m_includePrefix = true;
        InsertOperation insertOperation =  InsertOperation.get("insert-before", "insert-before-element");
        EditContainmentNode interfacesNode = new TestEditContainmentNode(interfaceContainerQName, EditConfigOperations.MERGE, m_schemaRegistry);
        EditContainmentNode interfaceNode = getEditContainmentInterfaceNode();
        interfacesNode.addChild(interfaceNode);
        interfacesNode.setInsertOperation(insertOperation);
        Element xml = EditTreeTransformer.toXml(schemaRegistry, interfacesNode, true, null);
        assertXMLEquals("/edit-transform-expected-merge-insert-interface.xml", xml);
    }

    @Test
    public void testEditContainmentNodeToXmlWithInsertOperationAtChangeNodeLevel() throws SAXException, IOException, EditConfigException{
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getPrefix(INTERFACE_NAMESPACE)).thenReturn("if");
        when(schemaRegistry.getPrefix(BBF_QOS_NAMESPACE)).thenReturn("bbf-qos-pol");
        EditTreeTransformer.m_includePrefix = true;
        InsertOperation insertOperation =  InsertOperation.get("insert-before", "insert-before-element");
        EditContainmentNode interfacesNode = new TestEditContainmentNode(interfaceContainerQName, EditConfigOperations.MERGE, m_schemaRegistry);
        EditContainmentNode interfaceNode = getEditContainmentInterfaceNode();
        EditChangeNode dummyChangeNode = new EditChangeNode(
                QName.create("urn:broadband-forum-org:yang:bbf-if-type","2019-09-07", "dummy-change-node"), new GenericConfigAttribute("dummy-change-node" , INTERFACE_NAMESPACE,"3000"));
        dummyChangeNode.setInsertOperation(InsertOperation.get("change-node-insert", "change-node-insert-value"));
        interfaceNode.addChangeNode(dummyChangeNode);
        interfacesNode.addChild(interfaceNode);
        Element xml = EditTreeTransformer.toXml(schemaRegistry, interfacesNode, true, null);
        assertXMLEquals("/edit-transform-expected-merge-insert-interface-change-node-level.xml", xml);
    }

    @Test
    public void testEditContainmentNodeToXmlWithInsertOperationHavingNullKeyValue() throws SAXException, IOException, EditConfigException{
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getPrefix(INTERFACE_NAMESPACE)).thenReturn("if");
        when(schemaRegistry.getPrefix(BBF_QOS_NAMESPACE)).thenReturn("bbf-qos-pol");
        EditTreeTransformer.m_includePrefix = true;
        EditContainmentNode interfacesNode = new TestEditContainmentNode(interfaceContainerQName, EditConfigOperations.MERGE, m_schemaRegistry);
        EditContainmentNode interfaceNode = getEditContainmentInterfaceNode();
        interfacesNode.addChild(interfaceNode);
        InsertOperation insertOperation =  InsertOperation.FIRST_OP;
        interfacesNode.setInsertOperation(insertOperation);
        Element xml = EditTreeTransformer.toXml(schemaRegistry, interfacesNode, true, null);
        assertXMLEquals("/edit-transform-expected-merge-insert-null-key-interface.xml", xml);
    }

    private EditContainmentNode getEditContainmentInterfaceNode() {
        EditContainmentNode interfaceNode = new TestEditContainmentNode(interfaceQName, EditConfigOperations.MERGE, m_schemaRegistry);
        interfaceNode.addMatchNode(interfaceNameQName, new GenericConfigAttribute("interface", INTERFACE_NAMESPACE, "ethernet:1/1/1/1:123456"));
        EditChangeNode changeNode = new EditChangeNode(typeQName,
                new IdentityRefConfigAttribute("urn:broadband-forum-org:yang:bbf-if-type","bbfift","if:type","bbfift:xdsl", INTERFACE_NAMESPACE));
        changeNode.setIdentityRefNode(true);
        interfaceNode.addChangeNode(changeNode);

        EditContainmentNode classifierNode = new TestEditContainmentNode(classifierQName, EditConfigOperations.MERGE, m_schemaRegistry);
        EditMatchNode actionTypeMatchNode = new EditMatchNode(actionTypeQName,
                new IdentityRefConfigAttribute("urn:broadband-forum-org:yang:bbf-if-type","bbfift","bbf-qos-pol:action-type",
                        "bbfift:xdsl","urn:broadband-forum-org:yang:bbf-qos-policies"));
        classifierNode.addMatchNode(actionTypeMatchNode);
        actionTypeMatchNode.setIdentityRefNode(true);
        interfaceNode.addChild(classifierNode);
        return interfaceNode;
    }
}
