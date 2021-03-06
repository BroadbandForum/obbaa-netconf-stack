package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class EditTreeTransformerTest extends XMLTestCase{

    private static final String INTERFACE_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String BBF_QOS_NAMESPACE = "urn:broadband-forum-org:yang:bbf-qos-policies";
    private static final String NAME = "name";
    private static final String ALBUM = "album";
    public static final String YEAR = "year";
    public static final String SONG = "song";

    QName albumQName = QName.create(Jukebox.NAMESPACE, "2014-07-03", ALBUM);
 	QName nameQName = QName.create(Jukebox.NAMESPACE, "2014-07-03", NAME);
 	QName yearQName = QName.create(Jukebox.NAMESPACE, "2014-07-03", YEAR);
 	QName songQName = QName.create(Jukebox.NAMESPACE, "2014-07-03", SONG);
 	
 	QName interfaceContainerQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "interfaces");
 	
 	QName interfaceQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "interface");
 	QName interfaceNameQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", NAME);
 	QName typeQName = QName.create(INTERFACE_NAMESPACE, "2014-05-08", "type");
    
 	QName classifierQName = QName.create(BBF_QOS_NAMESPACE, "2016-05-30", "classifier-action-entry-cfg");
 	QName actionTypeQName = QName.create(BBF_QOS_NAMESPACE, "2016-05-30", "action-type");
 	
    private SchemaRegistry m_schemaRegistry;

    @Override
    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }
    
    public void testCreateDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode songNode = new EditContainmentNode(songQName, EditConfigOperations.CREATE).addMatchNode(nameQName, new
                GenericConfigAttribute(NAME, Jukebox.NAMESPACE, "Thin Ice"));
        EditContainmentNode createData = new EditContainmentNode(albumQName, null).addMatchNode(nameQName, new GenericConfigAttribute
                (NAME, Jukebox.NAMESPACE, "Circus")).addChild(songNode);
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-add-song.xml", xml);
    }
    
    public void testMergeDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode createData = new EditContainmentNode(albumQName, EditConfigOperations.MERGE).addMatchNode(nameQName,
                new GenericConfigAttribute(NAME, Jukebox.NAMESPACE, "Circus")).addLeafChangeNode(yearQName, new GenericConfigAttribute(YEAR, Jukebox.NAMESPACE, "1996"));
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-change-year.xml", xml);
    }
    
    public void testDeleteDataTransformation() throws SAXException, IOException, EditConfigException{
        EditContainmentNode songNode = new EditContainmentNode(songQName, EditConfigOperations.DELETE).addMatchNode(nameQName, new
                GenericConfigAttribute(SONG, Jukebox.NAMESPACE, "Thin Ice"));
        EditContainmentNode createData = new EditContainmentNode(albumQName, null).addMatchNode(nameQName, new GenericConfigAttribute
                (ALBUM, Jukebox.NAMESPACE, "Circus")).addChild(songNode);
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(m_schemaRegistry, createData, true, null);
        
        assertXMLEquals("/edit-transform-expected-delete-song.xml", xml);
    }

    public void testDeleteLeafDataTransformation() throws IOException, SAXException {
        String namespace = "urn:bbf:yang:bbf-sub-interface-tagging";
        QName matchCriteriaQName = QName.create(namespace, "match-criteria");
        QName untaggedQName = QName.create(namespace,"untagged");

        EditChangeNode editChangeNode = new EditChangeNode(untaggedQName, new GenericConfigAttribute("untagged", namespace, ""));
        editChangeNode.setOperation(EditConfigOperations.DELETE);
        editChangeNode.setChangeSource(EditChangeSource.user);
        EditContainmentNode matchCriteria = new EditContainmentNode(matchCriteriaQName, EditConfigOperations.MERGE).
                addChangeNode(editChangeNode);
        matchCriteria.setChangeSource(EditChangeSource.user);

        Element xml = EditTreeTransformer.toXml(m_schemaRegistry,matchCriteria,true,null);

        assertXMLEquals("/edit-transform-expected-delete-leaf.xml", xml);

    }
    
    public void testEditContainmentNodeToXmlWithIdentityrefPrefix() throws SAXException, IOException, EditConfigException{
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getPrefix(INTERFACE_NAMESPACE)).thenReturn("if");
        when(schemaRegistry.getPrefix(BBF_QOS_NAMESPACE)).thenReturn("bbf-qos-pol");

        EditContainmentNode interfacesNode = new EditContainmentNode(interfaceContainerQName, EditConfigOperations.MERGE);
        
        EditContainmentNode interfaceNode = new EditContainmentNode(interfaceQName, EditConfigOperations.MERGE);
        interfaceNode.addMatchNode(interfaceNameQName, new GenericConfigAttribute("interface", INTERFACE_NAMESPACE, "ethernet:1/1/1/1:123456"));
        EditChangeNode changeNode = new EditChangeNode(typeQName,
                new IdentityRefConfigAttribute("urn:broadband-forum-org:yang:bbf-if-type","bbfift","if:type","bbfift:xdsl", INTERFACE_NAMESPACE));
        changeNode.setIdentityRefNode(true);
        interfaceNode.addChangeNode(changeNode);
        
        EditContainmentNode classifierNode = new EditContainmentNode(classifierQName, EditConfigOperations.MERGE);
        EditMatchNode actionTypeMatchNode = new EditMatchNode(actionTypeQName,
                new IdentityRefConfigAttribute("urn:broadband-forum-org:yang:bbf-if-type","bbfift","bbf-qos-pol:action-type",
                        "bbfift:xdsl","urn:broadband-forum-org:yang:bbf-qos-policies"));
        classifierNode.addMatchNode(actionTypeMatchNode);
        actionTypeMatchNode.setIdentityRefNode(true);
        interfaceNode.addChild(classifierNode);
        
        interfacesNode.addChild(interfaceNode);
        
        EditTreeTransformer.m_includePrefix = true;
        Element xml = EditTreeTransformer.toXml(schemaRegistry, interfacesNode, true, null);
        
        assertXMLEquals("/edit-transform-expected-merge-interface.xml", xml);
    }
}
