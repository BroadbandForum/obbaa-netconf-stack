package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XmlChildLeafListHelperTest {
    XmlChildLeafListHelper m_helper;
    @Mock
    private LeafListSchemaNode m_schemaNode;
    public static final String LEAF_LIST = "leaf-list";
    private static final QName QNAME = QName.create("namespace", LEAF_LIST);
    @Mock
    private ModelNodeDataStoreManager m_dsm;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private XmlModelNodeImpl m_modelNode;
    private static final SchemaPath m_schemaPath = SchemaPath.create(true, QNAME);
    @Mock
    private ModelNodeId m_modelNodeId;
    @Mock
    private ModelNodeId m_parentNodeId;

    @Before
    public void setUp() throws DataStoreException {
        MockitoAnnotations.initMocks(this);
        m_helper = new XmlChildLeafListHelper(m_schemaNode, QNAME, m_dsm, m_schemaRegistry);
        when(m_dsm.findNode((SchemaPath) anyObject(), (ModelNodeKey)anyObject(), (ModelNodeId) anyObject())).thenReturn(m_modelNode);
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_modelNode.getModelNodeId()).thenReturn(m_modelNodeId);
        when(m_modelNode.getParentNodeId()).thenReturn(m_parentNodeId);
        when(m_modelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
    }

    @Test
    public void testAddChildByUserOrder() throws Exception{
        m_helper.addChildByUserOrder(m_modelNode, new GenericConfigAttribute(LEAF_LIST, "namespace", "value"), "", new InsertOperation(InsertOperation.FIRST,
                null));
        verify(m_dsm).findNode(eq(m_schemaPath),(ModelNodeKey)anyObject(), eq(m_parentNodeId));
    }

    @Test
    public void testRemoveChild() throws Exception{
        m_helper.removeChild(m_modelNode, new GenericConfigAttribute(LEAF_LIST, "namespace", "value"));
        verify(m_dsm).findNode(eq(m_schemaPath),(ModelNodeKey)anyObject(), eq(m_parentNodeId));
    }

}
