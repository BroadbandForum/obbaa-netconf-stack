package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class EditContainmentNodeTest {

    private static final String TEST_LOCAL_NAME = "testLocalName";
    private static final String TEST_LOCAL_NAME2 = "testLocalName2";
    private static final String TEST_LOCAL_NAME3 = "testLocalName3";
    private static final String TEST_NAMESPACE = "testNamespace";
    private static final String TEST_NAMESPACE2 = "testNamespace2";
    private static final String TEST_NAMESPACE3 = "testNamespace3";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_OPERATION2 = "testOperation2";
    private static final String TEST_OPERATION3 = "testOperation3";
    private static final String TEST_NODE_VALUE = "testNodeValue";

    private QName m_qName;
    private EditContainmentNode m_editContainmentNode;
    private EditContainmentNode m_testChildNode;
    private EditChangeNode m_editChangeNode;
    private EditMatchNode m_editMatchNode;

    @Before
    public void initialize() {
        m_qName = QName.create(TEST_NAMESPACE, TEST_LOCAL_NAME);
        m_editContainmentNode = new EditContainmentNode(m_qName, TEST_OPERATION);
        m_testChildNode = new EditContainmentNode(QName.create(TEST_NAMESPACE2, TEST_LOCAL_NAME2), TEST_OPERATION2);
        m_editMatchNode = new EditMatchNode(m_qName, new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE));
        m_editChangeNode = new EditChangeNode(m_qName, new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE));
    }

    @Test
    public void testParentEditContainmentNode() {
        EditContainmentNode child = new EditContainmentNode(m_qName, TEST_NODE_VALUE);
        ModelNodeId parentId = new ModelNodeId();
        parentId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, m_editContainmentNode.getNamespace(), m_editContainmentNode.getName()));
        assertEquals(parentId, m_editContainmentNode.getModelNodeId());

        InsertOperation ins = new InsertOperation("name", "value");
        child.setInsertOperation(ins);
        m_editContainmentNode.addChild(child);
        assertEquals(m_editContainmentNode, child.getParent());
        m_editContainmentNode.removeChild(child);
        assertNull(null, child.getParent());
        assertEquals(ins, child.getInsertOperation());

        QName qname = QName.create(TEST_NAMESPACE, "test");
        child.setQName(qname);
        assertEquals(qname.getLocalName(), child.getName());
        assertEquals(qname.getNamespace().toString(), child.getNamespace());

        ModelNodeId childNodeId = new ModelNodeId();
        ModelNodeId childWithMatchNodeId = new ModelNodeId();
        childNodeId.addRdn(ModelNodeRdn.CONTAINER, m_testChildNode.getNamespace(), m_testChildNode.getName());
        childWithMatchNodeId.addRdn(ModelNodeRdn.CONTAINER, m_testChildNode.getNamespace(), m_testChildNode.getName());
        childWithMatchNodeId.addRdn(new ModelNodeRdn(m_editMatchNode.getQName(), m_editMatchNode.getValue()));

        m_testChildNode.addChild(child);
        m_testChildNode.addMatchNode(m_editMatchNode);
        m_testChildNode.addChangeNode(m_editChangeNode);

        assertEquals(childWithMatchNodeId, m_testChildNode.getModelNodeId());

        EditContainmentNode newChild = new EditContainmentNode(m_testChildNode);
        assertEquals(newChild, newChild.getChildren().get(0).getParent());
        assertEquals(m_editMatchNode, newChild.getMatchNodes().get(0));
        assertEquals(m_editChangeNode, newChild.getChangeNodes().get(0));

        m_testChildNode.removeChangeNode(m_editChangeNode);
        m_testChildNode.removeChild(child);
        m_testChildNode.removeMatchNode(m_editMatchNode);

        assertEquals(childNodeId, m_testChildNode.getModelNodeId());
    }

    @Test
    public void testRemoveChild() {
        m_editContainmentNode.addChild(m_testChildNode);
        assertEquals(m_editContainmentNode, m_testChildNode.getParent());
        assertEquals(m_editContainmentNode, m_editContainmentNode.removeChild(m_testChildNode));
    }

    @Test
    public void testRemoveWhenChildNodeIsNull() {
        EditContainmentNode childNode = null;
        assertEquals(m_editContainmentNode, m_editContainmentNode.removeChild(childNode));
    }

    @Test
    public void testGetEditMatchNode() {
        m_editContainmentNode.addMatchNode(m_editMatchNode);
        assertEquals(m_editMatchNode, m_editContainmentNode.getEditMatchNode(TEST_LOCAL_NAME, TEST_NAMESPACE));
    }

    @Test
    public void testGetEditMatchNodeWhenNamespaceIsNull() {
        m_editContainmentNode.addMatchNode(m_editMatchNode);
        assertEquals(m_editMatchNode, m_editContainmentNode.getEditMatchNode(TEST_LOCAL_NAME, null));
    }

    @Test
    public void testGetEditMatchNodeWhenNodeNameNotMatching() {
        m_editContainmentNode.addMatchNode(m_editMatchNode);
        assertNull(m_editContainmentNode.getEditMatchNode(TEST_LOCAL_NAME2, TEST_NAMESPACE));
    }

    @Test
    public void testGetEditMatchNodeNamespaceNotMatching() {
        m_editContainmentNode.addMatchNode(m_editMatchNode);
        assertNull(m_editContainmentNode.getEditMatchNode(TEST_LOCAL_NAME, TEST_NAMESPACE2));
    }

    @Test
    public void testGetEditMatchNodeReturnsNull() {
        assertNull(m_editContainmentNode.getEditMatchNode(TEST_LOCAL_NAME, TEST_NAMESPACE));
    }

    @Test
    public void testGetEditChangeNode() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        assertEquals(m_editChangeNode, m_editContainmentNode.getEditChangeNode(TEST_LOCAL_NAME, TEST_NAMESPACE));
    }

    @Test
    public void testGetEditChangeNodeWhenNamespaceIsNull() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        assertEquals(m_editChangeNode, m_editContainmentNode.getEditChangeNode(TEST_LOCAL_NAME, null));
    }

    @Test
    public void testGetEditChangeNodeWhenNodeNameNotMatching() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        assertNull(m_editContainmentNode.getEditChangeNode(TEST_LOCAL_NAME2, TEST_NAMESPACE));
    }

    @Test
    public void testGetEditChangeNodeNamespaceNotMatching() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        assertNull(m_editContainmentNode.getEditChangeNode(TEST_LOCAL_NAME, TEST_NAMESPACE2));
    }

    @Test
    public void testGetEditChangeNodeReturnsNull() {
        assertNull(m_editContainmentNode.getEditChangeNode(TEST_LOCAL_NAME, TEST_NAMESPACE));
    }


    @Test
    public void testRemoveChangeNode() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        assertEquals(m_editContainmentNode, m_editContainmentNode.removeChangeNode(m_editChangeNode));
    }

    @Test
    public void testRemoveMatchNode() {
        m_editContainmentNode.addMatchNode(m_editMatchNode);
        assertEquals(m_editContainmentNode, m_editContainmentNode.removeMatchNode(m_editMatchNode));
    }

    @Test
    public void testEquals() {
        assertTrue(m_editContainmentNode.equals(m_editContainmentNode));
    }

    @Test
    public void testEqualsWhenObjectIsNull() {
        assertFalse(m_editContainmentNode.equals(null));
    }

    @Test
    public void testEqualsWhenObjectsDiffer() {
        assertFalse(m_editContainmentNode.equals(m_editChangeNode));
        EditContainmentNode otherNode = new EditContainmentNode(m_qName, TEST_OPERATION);
        assertEquals(m_editContainmentNode, otherNode);
        otherNode.getModelNodeId().addRdn(new ModelNodeRdn(m_qName, "test"));
        assertFalse(m_editContainmentNode.equals(otherNode));
    }

    @Test
    public void testEqualsWhenEditOperationIsNull() {
        EditContainmentNode editContainmentNode2 = new EditContainmentNode(m_qName, null);
        EditContainmentNode editContainmentNode3 = new EditContainmentNode(m_qName, TEST_OPERATION3);
        assertFalse(editContainmentNode2.equals(editContainmentNode3));
        editContainmentNode3 = new EditContainmentNode(m_qName, null);
        assertTrue(editContainmentNode2.equals(editContainmentNode3));
    }

    @Test
    public void testEqualsWhenEditOperationIsNotNull() {
        EditContainmentNode editContainmentNode2 = new EditContainmentNode(m_qName, TEST_OPERATION2);
        EditContainmentNode editContainmentNode3 = new EditContainmentNode(m_qName, TEST_OPERATION3);
        assertFalse(editContainmentNode2.equals(editContainmentNode3));
    }

    @Test
    public void testAddChild() {
        assertEquals(m_editContainmentNode, m_editContainmentNode.addChild(m_testChildNode));
        assertEquals(m_editContainmentNode, m_editContainmentNode.addChild(null));
    }

    @Test
    public void testAddChangeNode() {
        assertEquals(m_editContainmentNode, m_editContainmentNode.addLeafChangeNode(m_qName, new
                GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE)));
    }

    @Test
    public void testGetChangeNodeReturnsNull() {
        assertNull(m_editContainmentNode.getChangeNode(QName.create(TEST_NAMESPACE2, TEST_LOCAL_NAME2)));
    }

    @Test
    public void testGetChangeNodeWhenChangeNodeQNameIsDifferent() {
        m_editContainmentNode.addChangeNode(m_editChangeNode);
        QName qName2 = QName.create(TEST_NAMESPACE3, TEST_LOCAL_NAME3);
        EditChangeNode changeNode = new EditChangeNode(qName2, new GenericConfigAttribute(TEST_LOCAL_NAME3, TEST_NAMESPACE3, TEST_NODE_VALUE));
        m_editContainmentNode.addChangeNode(changeNode);
        assertEquals(changeNode, m_editContainmentNode.getChangeNode(qName2));
    }

    @Test
    public void testGetChildNodeReturnsNull() {
        assertNull(m_editContainmentNode.getChildNode(m_qName));
    }

    @Test
    public void testGetChildNodeReturnsChildNode() {
        m_editContainmentNode.addChild(m_testChildNode);
        assertEquals(m_testChildNode, m_editContainmentNode.getChildNode(QName.create(TEST_NAMESPACE2, TEST_LOCAL_NAME2)));
    }

    @Test
    public void testGetChildNodeWhenChildNodeQNameIsDifferent() {
        m_editContainmentNode.addChild(m_testChildNode);
        QName qName2 = QName.create(TEST_NAMESPACE3, TEST_LOCAL_NAME3);
        EditContainmentNode testChildNode2 = new EditContainmentNode(qName2, TEST_OPERATION3);
        m_editContainmentNode.addChild(testChildNode2);
        assertEquals(testChildNode2, m_editContainmentNode.getChildNode(qName2));
    }

    @Test
    public void testAddChildShouldThrowExceptionInCaseOfDuplicateContainer() {
        EditContainmentNode node1 = new EditContainmentNode(QName.create(TEST_NAMESPACE, "Container1"), TEST_OPERATION);
        QName qname1 = QName.create(TEST_NAMESPACE, "test1");
        EditChangeNode changeNode = new EditChangeNode(qname1, new GenericConfigAttribute("test1", TEST_NAMESPACE, "value1"));
        node1.addChangeNode(changeNode);
        EditContainmentNode node2 = new EditContainmentNode(QName.create(TEST_NAMESPACE, "Container1"), TEST_OPERATION);
        QName qname2 = QName.create(TEST_NAMESPACE, "test2");
        EditChangeNode changeNode2 = new EditChangeNode(qname2, new GenericConfigAttribute("test2", TEST_NAMESPACE, "value2"));
        node2.addChangeNode(changeNode2);

        m_editContainmentNode.addChild(node1);
        try {
            m_editContainmentNode.addChild(node2);
        } catch (EditConfigException e) {
            NetconfRpcError rpcError = e.getRpcError();
            NetConfResponse response = new NetConfResponse().addError(rpcError).setMessageId("1");
            assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-not-unique</error-app-tag>\n" +
                    "    <error-path>/Container1</error-path>\n" +
                    "    <error-message>Duplicate elements in node (testNamespace)Container1</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>", response.responseToString().trim());
        }
    }

    @Test
    public void testAddChildShouldThrowExceptionInCaseOfDuplicateList() {
        EditContainmentNode node1 = new EditContainmentNode(QName.create(TEST_NAMESPACE, "List1"), TEST_OPERATION);
        QName qname1 = QName.create(TEST_NAMESPACE, "name1");
        EditMatchNode matchNode = new EditMatchNode(qname1, new GenericConfigAttribute("name1", TEST_NAMESPACE, "value1"));
        node1.addMatchNode(matchNode);
        EditContainmentNode node2 = new EditContainmentNode(QName.create(TEST_NAMESPACE, "List1"), TEST_OPERATION);
        QName qname2 = QName.create(TEST_NAMESPACE, "name1");
        EditMatchNode matchNode2 = new EditMatchNode(qname2, new GenericConfigAttribute("name1", TEST_NAMESPACE, "value1"));
        node2.addMatchNode(matchNode2);

        m_editContainmentNode.addChild(node1);
        try {
            m_editContainmentNode.addChild(node2);
        } catch (EditConfigException e) {
            NetconfRpcError rpcError = e.getRpcError();
            NetConfResponse response = new NetConfResponse().addError(rpcError).setMessageId("1");
            assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-not-unique</error-app-tag>\n" +
                    "    <error-path>/List1[name1='value1']</error-path>\n" +
                    "    <error-message>Duplicate elements in node (testNamespace)List1</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>", response.responseToString().trim());
        }
    }

    @Test
    public void testAddChildShouldThrowExceptionInCaseOfDuplicateLeaf() {
        EditContainmentNode node1 = new EditContainmentNode(QName.create(TEST_NAMESPACE, "Container1"), TEST_OPERATION);
        QName qname1 = QName.create(TEST_NAMESPACE, "test1");
        ConfigLeafAttribute leaf1 = new GenericConfigAttribute("test1", "testNamespace", "value1");
        ConfigLeafAttribute leaf2 = leaf1;
        node1.addLeafChangeNode(qname1, leaf1);

        try {
            node1.addLeafChangeNode(qname1, leaf2);
        } catch (EditConfigException e) {
            NetconfRpcError rpcError = e.getRpcError();
            NetConfResponse response = new NetConfResponse().addError(rpcError).setMessageId("1");
            assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-not-unique</error-app-tag>\n" +
                    "    <error-path>/Container1/test1</error-path>\n" +
                    "    <error-message>Duplicate elements in node (testNamespace)test1</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>", response.responseToString().trim());
        }
    }

}
