package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;


import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collections;
import java.util.HashMap;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlContainerModelNodeHelperTest {

    public static final ModelNodeId EMPTY_NODE_ID = new ModelNodeId();
    private XmlContainerModelNodeHelper m_xmlContainerModelNodeHelper;
    private ModelNodeDataStoreManager m_dataStoreManager;
    private SchemaRegistry m_schemaRegistry;
    private ContainerSchemaNode m_containerSchemaNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));

    @Before
    public void setUp() throws AnnotationAnalysisException, DataStoreException {

        m_schemaRegistry = mock(SchemaRegistry.class);
        m_containerSchemaNode = mock(ContainerSchemaNode.class);
        m_dataStoreManager = mock(ModelNodeDataStoreManager.class);
        when(m_dataStoreManager.createNode(new ModelNodeWithAttributes(LIBRARY_SCHEMA_PATH,m_jukeboxNodeId,m_modelNodeHelperRegistry,null, m_schemaRegistry, m_dataStoreManager), EMPTY_NODE_ID)).thenReturn(null);
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        when(m_containerSchemaNode.getQName()).thenReturn(LIBRARY_QNAME);
        when(m_containerSchemaNode.getPath()).thenReturn(LIBRARY_SCHEMA_PATH);
        when(m_modelNodeHelperRegistry.getNaturalKeyHelpers(JUKEBOX_SCHEMA_PATH)).thenReturn(Collections.<QName, ConfigAttributeHelper>emptyMap());
        m_xmlContainerModelNodeHelper = new XmlContainerModelNodeHelper(m_containerSchemaNode, m_dataStoreManager,m_schemaRegistry);
    }

    @Test
    public void testCreateChild() throws ModelNodeCreateException, DataStoreException {

        //Case 1 : When parentnode is not an instance of XmlModelNode
        ModelNodeWithAttributes parentNode = new ModelNodeWithAttributes(JUKEBOX_SCHEMA_PATH, EMPTY_NODE_ID,m_modelNodeHelperRegistry, null, m_schemaRegistry, m_dataStoreManager);
        ModelNode newNode = m_xmlContainerModelNodeHelper.createChild(parentNode, new HashMap<QName, ConfigLeafAttribute>());
        assertTrue(newNode instanceof XmlModelNodeImpl);
        assertEquals(LIBRARY_QNAME, newNode.getQName());
        assertEquals(null, newNode.getParent());
        assertEquals(m_libraryNodeId,newNode.getModelNodeId());
        assertEquals(parentNode.getModelNodeId(), ((XmlModelNodeImpl) newNode).getParentNodeId());

        //Case 1 : When parentnode is an instance of XmlModelNode
        parentNode = new XmlModelNodeImpl(JUKEBOX_SCHEMA_PATH,Collections.EMPTY_MAP, Collections.EMPTY_LIST,null,EMPTY_NODE_ID,null,m_modelNodeHelperRegistry,m_schemaRegistry,null, m_dataStoreManager);
        when(m_dataStoreManager.findNode((SchemaPath)Matchers.anyObject(), (ModelNodeKey)Matchers.anyObject(), (ModelNodeId)Matchers.anyObject())).thenReturn(parentNode);
        newNode = m_xmlContainerModelNodeHelper.createChild(parentNode, new HashMap<QName, ConfigLeafAttribute>());
        assertTrue(newNode instanceof XmlModelNodeImpl);
        assertEquals(LIBRARY_QNAME, newNode.getQName());
        assertEquals(parentNode,((XmlModelNodeImpl)newNode).getParentModelNode());
        assertEquals(m_libraryNodeId, newNode.getModelNodeId());
        assertEquals(parentNode.getModelNodeId(),((XmlModelNodeImpl)newNode).getParentNodeId());
    }
}
