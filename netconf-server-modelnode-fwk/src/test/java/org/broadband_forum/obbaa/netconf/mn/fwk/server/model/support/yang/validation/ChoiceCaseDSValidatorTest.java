package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

public class ChoiceCaseDSValidatorTest extends AbstractDataStoreValidatorTest {

	// leaf
	private static final String CHOICE_CASE_LEAF_NODE1 = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-node1.xml";
	private static final String CHOICE_CASE_LEAF_NODE2 = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-node2.xml";
	private static final String CHOICE_CASE_LEAF_NODE2_FULL = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-node2-full.xml";
	private static final String CHOICE_CASE_MULTI_LEAF_NODES_IN_REQUEST = "/datastorevalidatortest/choicecasevalidation/choice-case-multi-leaf-nodes-in-request.xml";
	private static final String CHOICE_CASE_MULTI_LEAF_NODES_IN_REQUEST2 =
			"/datastorevalidatortest/choicecasevalidation/choice-case-multi-leaf-nodes-in-request2.xml";
	private static final String CREATE_MULTIPLE_CASE_UNDER_DIFFERENT_CHOICES_REQUEST = "/datastorevalidatortest/choicecasevalidation/create-multiple-case-under-different-choice.xml";
	private static final String CHOICE_CASE_LEAF_NODE_WITHOUT_CASE = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-node-without-case.xml";
	private static final String GET_RESPONSE_LEAF_NODE1 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafNodeResponse1.xml";
	private static final String GET_RESPONSE_LEAF_NODE2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafNodeResponse2.xml";
	private static final String GET_RESPONSE_LEAF_NODE2_FULL = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafNodeResponse2-full.xml";
	private static final String GET_RESPONSE_LEAF_NODE_DEFAULT = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafNodeResponseDefaultCase.xml";
	private static final String CHOICE_CASE_LEAF_NODE_DELETE = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-node-delete.xml";
	private static final String GET_RESPONSE_LEAF_NODE_DELETE = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafNodeDelete.xml";
	// leaf-list
	private static final String CHOICE_CASE_LEAFLIST_NODE1 = "/datastorevalidatortest/choicecasevalidation/choice-case-leaflist-node1.xml";
	private static final String CHOICE_CASE_LEAFLIST_NODE2 = "/datastorevalidatortest/choicecasevalidation/choice-case-leaflist-node2.xml";
	private static final String GET_RESPONSE_LEAFLIST_NODE1 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeaflistNodeResponse1.xml";
	private static final String GET_RESPONSE_LEAFLIST_NODE2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeaflistNodeResponse2.xml";
	private static final String CHOICE_CASE_LEAFLIST_LEAFNODE = "/datastorevalidatortest/choicecasevalidation/choice-case-leaflist-leafnode.xml";
	private static final String GET_RESPONSE_LEAFLIST_LEAF = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeaflistNodeResponse3.xml";
	// container
	private static final String CHOICE_CASE_CONTAINER_NODE1 = "/datastorevalidatortest/choicecasevalidation/choice-case-container-node1.xml";
	private static final String CHOICE_CASE_CONTAINER_NODE2 = "/datastorevalidatortest/choicecasevalidation/choice-case-container-node2.xml";
	private static final String GET_RESPONSE_CONTAINER_NODE1 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseContainerNodeResponse1.xml";
	private static final String GET_RESPONSE_CONTAINER_NODE2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseContainerNodeResponse2.xml";
	private static final String CHOICE_CASE_CONTAINER_NODE1_DELETE =  "/datastorevalidatortest/choicecasevalidation/choice-case-container-node1-delete.xml";
	private static final String GET_CHOICE_CASE_DEFAULT_CONTAINER_NODE_RESPONSE =  "/datastorevalidatortest/choicecasevalidation/getChoiceCaseDefaultContainerNodeResponse.xml";
	// list
	private static final String CHOICE_CASE_LIST_NODE1 = "/datastorevalidatortest/choicecasevalidation/choice-case-list-node1.xml";
	private static final String CHOICE_CASE_LIST_NODE2 = "/datastorevalidatortest/choicecasevalidation/choice-case-list-node2.xml";
	private static final String GET_RESPONSE_LIST_NODE1 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseListNodeResponse1.xml";
	private static final String GET_RESPONSE_LIST_NODE2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseListNodeResponse2.xml";
	//mixed case
	private static final String CHOICE_CASE_MIXED1 = "/datastorevalidatortest/choicecasevalidation/choice-case-mixed1.xml";
	private static final String GET_RESPONSE_MIXED1 ="/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedNodeResponse1.xml";
	private static final String CHOICE_CASE_MIXED2 = "/datastorevalidatortest/choicecasevalidation/choice-case-mixed2.xml";
	private static final String DELETE_CASE_1 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case.xml";
	private static final String GET_RESPONSE_MIXED2 ="/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedNodeResponse2.xml";

	private static final String CHOICE_CASE_MIXED_LIST = "/datastorevalidatortest/choicecasevalidation/choice-case-mixed-list.xml";
	private static final String GET_RESPONSE_MIXED_LIST_TWO_ENTRIES = "/datastorevalidatortest/choicecasevalidation/getResponseChoiceCaseMixedTwoListEntries.xml";
	private static final String REMOVE_MIXED_CASE_LIST_ENTRY_1 = "/datastorevalidatortest/choicecasevalidation/remove-mixed-case-list-entry1.xml";
	private static final String REMOVE_NON_EXISTING_LIST_ENTRY = "/datastorevalidatortest/choicecasevalidation/remove-non-existing-list-entry.xml";
	private static final String DELETE_MIXED_CASE_LIST_ENTRY_1 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-list-entry1.xml";
	private static final String REMOVE_MIXED_CASE_LIST_ENTRY_2 = "/datastorevalidatortest/choicecasevalidation/remove-mixed-case-list-entry2.xml";
	private static final String DELETE_MIXED_CASE_LIST_ENTRY_2 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-list-entry2.xml";
	private static final String GET_RESPONSE_MIXED_LIST_ONE_ENTRY = "/datastorevalidatortest/choicecasevalidation/getResponseChoiceCaseMixedOneListEntry.xml";
	private static final String GET_RESPONSE_MIXED_DEFAULT = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedNodeResponseDefaultLeaf.xml";

	private static final String CHOICE_CASE_MIXED_CONTAINER = "/datastorevalidatortest/choicecasevalidation/choice-case-mixed-container.xml";
	private static final String GET_RESPONSE_MIXED_CONTAINER = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedContainerResponse.xml";
	private static final String REMOVE_MIXED_CASE_CONTAINER = "/datastorevalidatortest/choicecasevalidation/remove-mixed-case-container.xml";
	private static final String DELETE_MIXED_CASE_CONTAINER = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-container.xml";
	private static final String GET_RESPONSE_MIXED_DEFAULT2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedNodeResponseDefaultLeaf2.xml";

	public static final String CHOICE_CASE_MIXED_LEAFLIST = "/datastorevalidatortest/choicecasevalidation/choice-case-mixed-leaf-list.xml";
	public static final String GET_CHOICE_CASE_MIXED_LEAFLIST_RESPONSE = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseMixedLeaflistResponse.xml";
	public static final String DELETE_MIXED_CASE_LEAFLIST_ENTRY1 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-leaflist-entry1.xml";
	public static final String GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE1 =  "/datastorevalidatortest/choicecasevalidation/getMixedLeaflistAfterDeleteResponse1.xml";
	public static final String DELETE_MIXED_CASE_LEAFLIST_ENTRY2 =  "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-leaflist-entry2.xml";
	public static final String GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE2 = "/datastorevalidatortest/choicecasevalidation/getMixedLeaflistAfterDeleteResponse2.xml";
	public static final String DELETE_MIXED_CASE_LEAFLIST_ENTRIES_XML = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-leaflist-entries.xml";

	// leaf of empty type
	public static final String CREATE_CHOICE_CASE_LEAF_EMPTY_TYPE = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-empty-type.xml";
	public static final String GET_CHOICE_CASE_LEAF_EMPTY_TYPE_RESPONSE1 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafEmptyTypeResponse1.xml";
	public static final String CREATE_CHOICE_CASE_LEAF_EMPTY_TYPE_NONDEFAULT = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-empty-type-nondefault.xml";
	public static final String GET_CHOICE_CASE_LEAF_EMPTY_TYPE_RESPONSE2 = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseLeafEmptyTypeResponse2.xml";
	public static final String CHOICE_CASE_LEAF_EMPTY_TYPE_DELETE_NONDEFAULT = "/datastorevalidatortest/choicecasevalidation/choice-case-leaf-empty-type-delete-nondefault.xml";

	private static final String CREATE_MIXED_CASE_DEFAULT_CONTAINER_LIST = "/datastorevalidatortest/choicecasevalidation/create-mixed-case-default-container-and-list.xml";
	private static final String GET_RESPONSE_MIXED_CASE_DEFAULT_CONTAINER_LIST = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseDefaultContainerAndList.xml";

    private static final String CREATE_MIXED_CASE_CONTAINER_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/create-mixed-case-container-default-leaf.xml";
    private static final String CREATE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/create-mixed-case-default-container-leaf.xml";
	private static final String DELETE_MIXED_CASE_CONTAINER_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-container-default-leaf.xml";
    private static final String DELETE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-container-default-leaf2.xml";
	private static final String GET_RESPONSE_MIXED_CONTAINER_DEFAULT = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseContainerDefault.xml";
    private static final String GET_RESPONSE_MIXED_CONTAINER_DEFAULT2 = "/datastorevalidatortest/choicecasevalidation/getResponseForDefContainerCaseWoDefLeafValue.xml";
    private static final String GET_RESPONSE_MIXED_CASE_CONTAINER_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseContainerDefaultLeaf.xml";
    private static final String GET_RESPONSE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseContainerDefaultLeaf2.xml";
    private static final String CREATE_MIXED_CASE_LIST_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/create-mixed-case-list-default-leaf.xml";
    private static final String CREATE_MIXED_CASE_LIST_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/create-mixed-case-list-default-leaf2.xml";
    private static final String GET_RESPONSE_MIXED_CASE_LIST_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseListDefaultLeaf.xml";
    private static final String GET_RESPONSE_MIXED_CASE_LIST_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseListDefaultLeaf2.xml";
    private static final String DELETE_MIXED_CASE_LIST_DEFAULT_LEAF = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-list-default-leaf.xml";
    private static final String DELETE_MIXED_CASE_LIST_DEFAULT_LEAF2 = "/datastorevalidatortest/choicecasevalidation/delete-mixed-case-list-default-leaf2.xml";
    private static final String GET_RESPONSE_MIXED_LIST_DEFAULT = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseListDefault.xml";
    private static final String GET_RESPONSE_MIXED_LIST_DEFAULT2 = "/datastorevalidatortest/choicecasevalidation/getResponseMixedCaseListDefault2.xml";


    private NetconfClientInfo m_clientTestUser = new NetconfClientInfo("testuser1", 2);
	private TestSubsystem m_testSubsystem;

	protected SubSystem getSubSystem() {
		return m_testSubsystem;
	}


	@Before
	@Override
	public void setUp() throws ModelNodeInitException, SchemaBuildException {
		m_testSubsystem = new TestSubsystem();
	    super.setUp();
	    getModelNode();
        initialiseInterceptor();
	}
	
	/*
	 * Test case: Create container choicecase without any cases.
	 * 
	 */
	@Test
	public void testDefaultChoiceCaseLeafNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE_WITHOUT_CASE));
		TestUtil.verifyGet(m_server, (NetconfFilter) null, GET_RESPONSE_LEAF_NODE_DEFAULT, MESSAGE_ID);
	}
	
	@Test
	public void testDefaultChoiceCase() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE_WITHOUT_CASE));
		TestUtil.verifyGet(m_server, (NetconfFilter) null, GET_RESPONSE_LEAF_NODE_DEFAULT, MESSAGE_ID);
	}
	
	@Test
	public void testDeleteChoiceCaseLeafNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE1));
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE2));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAF_NODE2, MESSAGE_ID);
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE_DELETE));
		TestUtil.verifyGetConfig(m_server, StandardDataStores.RUNNING, null, GET_RESPONSE_LEAF_NODE_DELETE, MESSAGE_ID);
	}
	
	@Test
	public void testChoiceCaseLeafNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE1));

    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAF_NODE1, MESSAGE_ID);
    	assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE2));

    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAF_NODE2, MESSAGE_ID);
    	assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_NODE2_FULL));

    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAF_NODE2_FULL, MESSAGE_ID);
	}
	
	@Test
	public void testChoiceMultiCaseLeafNodes() throws ModelNodeInitException, SAXException, IOException {
		NetConfResponse response = assertSendingFailedEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MULTI_LEAF_NODES_IN_REQUEST));
		assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());

		response = assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MULTI_LEAF_NODES_IN_REQUEST2));
		assertTrue(response.isOk());
		assertEquals(0, response.getErrors().size());
	}
	
	@Test
    public void testCreateMultipleCaseUnderDifferentChoices() throws ModelNodeInitException, SAXException, IOException {
       assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CREATE_MULTIPLE_CASE_UNDER_DIFFERENT_CHOICES_REQUEST));
    }
	
	@Test
	public void testChoiceCaseLeafListNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAFLIST_NODE1));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAFLIST_NODE1, MESSAGE_ID);

		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAFLIST_NODE2));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAFLIST_NODE2, MESSAGE_ID);
    	
    	assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAFLIST_NODE2));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAFLIST_NODE2, MESSAGE_ID);

		// Create leaf case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser, loadAsXml(CHOICE_CASE_LEAFLIST_LEAFNODE));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAFLIST_LEAF, MESSAGE_ID);

		// create leaf-list case again
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAFLIST_NODE1));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LEAFLIST_NODE1, MESSAGE_ID);
	}

	@Test
	public void testChoiceCaseLeafEmptyType() throws IOException, SAXException {
		// Default leaf is created
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CREATE_CHOICE_CASE_LEAF_EMPTY_TYPE));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_CHOICE_CASE_LEAF_EMPTY_TYPE_RESPONSE1, MESSAGE_ID);

		// create non default leaf of empty type
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CREATE_CHOICE_CASE_LEAF_EMPTY_TYPE_NONDEFAULT));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_CHOICE_CASE_LEAF_EMPTY_TYPE_RESPONSE2, MESSAGE_ID);

		// delete non default leaf and expect default type is created
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LEAF_EMPTY_TYPE_DELETE_NONDEFAULT));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_CHOICE_CASE_LEAF_EMPTY_TYPE_RESPONSE1, MESSAGE_ID);
	}
	
	@Test
	public void testChoiceCaseContainerNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_CONTAINER_NODE1));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_CONTAINER_NODE1, MESSAGE_ID);
    	
    	assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_CONTAINER_NODE2));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_CONTAINER_NODE2, MESSAGE_ID);

		// create a non-default container
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_CONTAINER_NODE1));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_CONTAINER_NODE1, MESSAGE_ID);

		// delete non-default container and expect default to be created.
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_CONTAINER_NODE1_DELETE));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_CHOICE_CASE_DEFAULT_CONTAINER_NODE_RESPONSE, MESSAGE_ID);
	}

	@Test
	public void testChoiceCaseListNode() throws ModelNodeInitException, SAXException, IOException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LIST_NODE1));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LIST_NODE1, MESSAGE_ID);
    	
    	assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_LIST_NODE2));
    	TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_LIST_NODE2, MESSAGE_ID);
	}

	@Test
	public void testChoiceMixedCase() throws IOException, SAXException {
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED1));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED1, MESSAGE_ID);

		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_CASE_1));

		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED2));
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED2, MESSAGE_ID);
	}

	@Test
	public void testChoiceMixedCaseRemoveNonDefaultCase() throws IOException, SAXException {
		/**
		 * When a container has choice with mixed cases(leaf,container,list etc) and a non default case is deleted,
		 * Default case needs to be present in DS after delete.
		 */

		//create a non default list case
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED_LIST));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation], m_change=ModelNodeChange [m_changeType=create, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,name,Mixed case with non default list case,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 1,urn:org:bbf:pma:validation]\n" +
				"  Change [merge,value-case-mixed,some value,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 2,urn:org:bbf:pma:validation]\n" +
				"  Change [merge,value-case-mixed,some value 2,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");

		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_TWO_ENTRIES, MESSAGE_ID);

		//delete non default list case(first entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_LIST_ENTRY_1));
		//make sure remove notif is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=remove, " +
				"m_changeData=Containment [remove,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				" Match [name-case-mixed,list entry 1,urn:org:bbf:pma:validation]\n" +
				"], m_dataStore=running]");

		// expect other list entry to be still there
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_ONE_ENTRY, MESSAGE_ID);

		//remove non existing list entry
		assertSendingSuccessfulEdit(m_server, m_clientTestUser,loadAsXml(REMOVE_NON_EXISTING_LIST_ENTRY));
		//make sure no notif is sent, since there was no concrete change
		m_testSubsystem.assertContainsNoNotification();

		//delete non default list case(second entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_LIST_ENTRY_2));
		//make sure remove notif is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=merge, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				" Containment [remove,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 2,urn:org:bbf:pma:validation]\n" +
				"], m_dataStore=running]");
		// expect leaf default case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT, MESSAGE_ID);

		// create a non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_CONTAINER));
		//This needs to be chnaged once FNMS-9332 is fixed, there needs to be aremvoe notification
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=merge, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,name,Mixed case with non default container case,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaf-case-mixed,,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,container-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Change [merge,value-case-mixed,mixed case value,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");

		TestUtil.verifyGet(m_server,(NetconfFilter)null,GET_RESPONSE_MIXED_CONTAINER, MESSAGE_ID);

		//delete non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_CONTAINER));
		//make sure remove notif is present and default case merge is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], " +
				"m_change=ModelNodeChange [m_changeType=merge, m_changeData=Containment [merge,choicecase," +
				"urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				" Containment [remove,container-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Change [merge,value-case-mixed,mixed case value,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");
		//expect default leaf case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT2, MESSAGE_ID);
	}

	private NetConfResponse assertSendingSuccessfulEdit(NetConfServerImpl server, NetconfClientInfo clientTestUser, Element element) {
		NetConfResponse response = sendEditConfig(server, clientTestUser, element, MESSAGE_ID);
		assertTrue(response.isOk());
		assertEquals(0, response.getErrors().size());
		return response;
	}

	private NetConfResponse assertSendingFailedEdit(NetConfServerImpl server, NetconfClientInfo clientTestUser, Element element) {
		NetConfResponse response = sendEditConfig(server, clientTestUser, element, MESSAGE_ID);
		assertFalse(response.isOk());
		return response;
	}

	@Test
	public void testChoiceMixedCaseDeleteNonDefaultCase() throws IOException, SAXException {
		/**
		 * When a container has choice with mixed cases(leaf,container,list etc) and a non default case is deleted,
		 * Default case needs to be present in DS after delete.
		 */

		//create a non default list case
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED_LIST));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation], m_change=ModelNodeChange [m_changeType=create, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,name,Mixed case with non default list case,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 1,urn:org:bbf:pma:validation]\n" +
				"  Change [merge,value-case-mixed,some value,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 2,urn:org:bbf:pma:validation]\n" +
				"  Change [merge,value-case-mixed,some value 2,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");

		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_TWO_ENTRIES, MESSAGE_ID);

		//delete non default list case(first entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_ENTRY_1));
		//make sure remove notif is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=delete, " +
				"m_changeData=Containment [delete,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				" Match [name-case-mixed,list entry 1,urn:org:bbf:pma:validation]\n" +
				"], m_dataStore=running]");

		// expect other list entry to be still there
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_ONE_ENTRY, MESSAGE_ID);

		//delete non default list case(second entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_ENTRY_2));

		//make sure remove notif is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=merge, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				" Containment [delete,list-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Match [name-case-mixed,list entry 2,urn:org:bbf:pma:validation]\n" +
				"], m_dataStore=running]");
		// expect leaf default case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT, MESSAGE_ID);

		// create a non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_CONTAINER));
		//This needs to be changed once FNMS-9332 is fixed, there needs to be aremvoe notification
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange [m_changeType=merge, " +
				"m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,name,Mixed case with non default container case,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaf-case-mixed,,urn:org:bbf:pma:validation,user]\n" +
				" Containment [merge,container-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Change [merge,value-case-mixed,mixed case value,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");

		TestUtil.verifyGet(m_server,(NetconfFilter)null,GET_RESPONSE_MIXED_CONTAINER, MESSAGE_ID);

		//delete non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_CONTAINER));
		//make sure remove notif is present and default case merge is present
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], " +
				"m_change=ModelNodeChange [m_changeType=merge, m_changeData=Containment [merge,choicecase," +
				"urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				" Containment [delete,container-case-mixed,urn:org:bbf:pma:validation,user]\n" +
				"  Change [merge,value-case-mixed,mixed case value,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");
		//expect default leaf case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT2, MESSAGE_ID);

		//create a non default leaf-list case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_LEAFLIST));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange " +
				"[m_changeType=merge, m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,name,Mixed case with non default leaf-list case,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaflist-case-mixed,first leaf-list,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaflist-case-mixed,second leaf-list,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_CHOICE_CASE_MIXED_LEAFLIST_RESPONSE, MESSAGE_ID);

		// delete one of the leaf-list values. Expect the other leaf list to still be there
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRY1));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange " +
				"[m_changeType=merge, m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaflist-case-mixed,first leaf-list,urn:org:bbf:pma:validation,user]\n" +
				"], m_dataStore=running]");
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE1, MESSAGE_ID);

		// delete the other leaf-list entry and expect the default leaf to be created.
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRY2));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange " +
				"[m_changeType=merge, m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaflist-case-mixed,second leaf-list,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				"], m_dataStore=running]");
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE2, MESSAGE_ID);

		// create non default leaf-list case again
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_LEAFLIST));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_CHOICE_CASE_MIXED_LEAFLIST_RESPONSE, MESSAGE_ID);

		// delete both leaf-list values at once and expect the default leaf to be created.
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRIES_XML));
		m_testSubsystem.assertContainsNotification("EditConfigChangeNotification " +
				"[m_modelNodeId=ModelNodeId[/container=validation/container=choicecase], m_change=ModelNodeChange " +
				"[m_changeType=merge, m_changeData=Containment [merge,choicecase,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaflist-case-mixed,first leaf-list,urn:org:bbf:pma:validation,user]\n" +
				" Change [delete,leaflist-case-mixed,second leaf-list,urn:org:bbf:pma:validation,user]\n" +
				" Change [merge,leaf-case-mixed,Default value for mixed case,urn:org:bbf:pma:validation,system]\n" +
				"], m_dataStore=running]");
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE2, MESSAGE_ID);


	}

	private NetConfResponse sendEditConfig(NetConfServerImpl server, NetconfClientInfo clientTestUser, Element element, String messageId) {
		m_testSubsystem.clearNotifs();
		return TestUtil.sendEditConfig(server, clientTestUser, element, messageId);
	}

    @Test
    public void testMixedChoiceCaseForDefaultValuesOfContainerAndList() throws IOException, SAXException {

        NetConfResponse response = sendEditConfig(m_server, m_clientTestUser, loadAsXml(CREATE_MIXED_CASE_DEFAULT_CONTAINER_LIST),MESSAGE_ID);
        assertTrue(response.isOk());

        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CASE_DEFAULT_CONTAINER_LIST, MESSAGE_ID);
    }

    @Test
    public void testMixedChoiceCaseContainerDefault() throws IOException, SAXException {

        //create a non default leaf case
        NetConfResponse response = sendEditConfig(m_server, m_clientTestUser, loadAsXml(CREATE_MIXED_CASE_CONTAINER_DEFAULT_LEAF),MESSAGE_ID);
        assertTrue(response.isOk());

        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CASE_CONTAINER_DEFAULT_LEAF, MESSAGE_ID);

		/*delete non default leaf*/
        sendEditConfig(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_CONTAINER_DEFAULT_LEAF),MESSAGE_ID);

        // expect container default case
        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CONTAINER_DEFAULT, MESSAGE_ID);
    }

	@Test
	public void testDefaultContainerCaseWithoutDefaultLeafValue() throws IOException, SAXException {

		//create a non default leaf case
		NetConfResponse response = sendEditConfig(m_server, m_clientTestUser, loadAsXml(CREATE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2),MESSAGE_ID);
		assertTrue(response.isOk());

		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2, MESSAGE_ID);

		//delete non default leaf
		sendEditConfig(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_CONTAINER_DEFAULT_LEAF2),MESSAGE_ID);

		// Verify empty container is created when there is no default value specified for the container leaf
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CONTAINER_DEFAULT2, MESSAGE_ID);
	}

    @Test
    public void testMixedChoiceCaseListDefault() throws IOException, SAXException {

        //create a non default leaf case
        NetConfResponse response = sendEditConfig(m_server, m_clientTestUser, loadAsXml(CREATE_MIXED_CASE_LIST_DEFAULT_LEAF),MESSAGE_ID);
        assertTrue(response.isOk());

        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CASE_LIST_DEFAULT_LEAF, MESSAGE_ID);

		/*delete non default leaf*/
        sendEditConfig(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_DEFAULT_LEAF),MESSAGE_ID);

        // expect list default case
        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_DEFAULT, MESSAGE_ID);
    }

    @Test
    public void testDefaultListCase_WhenDafaultValueIsNotSpecifiedForSomeKey() throws IOException, SAXException {

        //create a non default leaf case
        NetConfResponse response = sendEditConfig(m_server, m_clientTestUser, loadAsXml(CREATE_MIXED_CASE_LIST_DEFAULT_LEAF2),MESSAGE_ID);
        assertTrue(response.isOk());

        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_CASE_LIST_DEFAULT_LEAF2, MESSAGE_ID);

		//delete non default leaf
        sendEditConfig(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_DEFAULT_LEAF2),MESSAGE_ID);

        // Verify that the default list case is not created when default value is not specified for 1/more keys
        TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_DEFAULT2, MESSAGE_ID);
    }

	/*
	 * Add no choice - Failing case & Success case
	 */
	@Test
    public void testMandatoryChoiceCaseNoChoice1() throws ModelNodeInitException, SAXException, IOException {
	    String xmlPath = "/datastorevalidatortest/choicecasevalidation/choice-case-test-mandatory1.xml";
        // Failing case
	    NetConfResponse response = assertSendingFailedEdit(m_server, m_clientTestUser, loadAsXml(xmlPath));
        assertFalse(response.isOk());
        assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Mandatory choice mandatory-choice is missing", rpcError.getErrorMessage());
        
        // Success case
        xmlPath = "/datastorevalidatortest/choicecasevalidation/choice-case-test-with-mandatory-leaf.xml";
        assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(xmlPath));
        String responseXml = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseTestWithMandatoryLeaf.xml";
        TestUtil.verifyGet(m_server, (NetconfFilter) null, responseXml, MESSAGE_ID);
    }
	
	/*
	 * Add full case 1
	 */
	@Test
    public void testMandatoryChoiceCase2() throws ModelNodeInitException, SAXException, IOException {
        String xmlPath = "/datastorevalidatortest/choicecasevalidation/choice-case-test-mandatory2.xml";
        assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(xmlPath));
        String responseXml = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseTestMandatoryWithDefault2.xml";
        TestUtil.verifyGet(m_server, (NetconfFilter) null, responseXml, MESSAGE_ID);
    }
	
	/*
	 * Mandatory choice node without mandatory/default child node inside case nodes
	 */
	
	@Test
	public void testMandatoryChoiceCaseWithNonMandatoryChildNodes() throws SAXException, IOException{	    
	    
	    XmlModelNodeToXmlMapper xmlModelNodeToXmlMapper = mock(XmlModelNodeToXmlMapper.class);
	    
	    
	    
	    ModelNodeId modelNodeId = new ModelNodeId("/container=validation", "urn:org:bbf:pma:validation");
	    QName validationQName = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
	    QName listQName = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "level1-list");
	    SchemaPath validation_schemaPath = SchemaPath.create(true, validationQName);
	    Map<QName, List<XmlModelNodeImpl>> children = new HashMap<QName, List<XmlModelNodeImpl>>();
	    children.put(listQName, null);
	    SchemaPath mandatoryValidation_schemaPath = new SchemaPathBuilder().withParent(validation_schemaPath).appendLocalName("mandatory-validation").build();
	    
	    String element = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation:mandatory-validation xmlns:validation=\"urn:org:bbf:pma:validation\">"  +
                "   <validation:level1-list>"+
                "    <validation:name>TEST</validation:name>" +                
                "  </validation:level1-list>" +
                "</validation:mandatory-validation>"; 
	    List<Element> elementList = Arrays.asList(TestUtil.transformToElement(element));
	    
	    XmlModelNodeImpl modelnode = new XmlModelNodeImpl(mandatoryValidation_schemaPath, Collections.EMPTY_MAP, elementList, null, modelNodeId, xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, m_modelNodeDsm);
	    when(xmlModelNodeToXmlMapper.getModelNodeFromParentSchemaPath(any(), any(), any(), any(), any())).thenReturn(modelnode);
	    doReturn(null).doReturn(modelnode).when(m_modelNodeDsm).findNode(eq(mandatoryValidation_schemaPath), any(), eq(modelNodeId));
	    
	    String xmlPath = "/datastorevalidatortest/choicecasevalidation/choice-case-test-mandatory4.xml";
        assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(xmlPath));
        String responseXml = "/datastorevalidatortest/choicecasevalidation/getChoiceCaseTestMandatory.xml";
        TestUtil.verifyGet(m_server, (NetconfFilter) null, responseXml, MESSAGE_ID);
	}
	
	/*
	 * Add a part of case 2. Missing 1 mandatory leaf of case 2
	 */
	@Test
    public void testMandatoryChoiceCase3() throws ModelNodeInitException, SAXException, IOException {
        String xmlPath = "/datastorevalidatortest/choicecasevalidation/choice-case-test-mandatory3.xml";
        NetConfResponse response = assertSendingFailedEdit(m_server, m_clientTestUser, loadAsXml(xmlPath));
        assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Missing mandatory node", rpcError.getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation/validation:mandatory-case2-leaf2", rpcError.getErrorPath());
    }
	
	@Test
	public void testEditWithMultipleCasesOfChoiceInReq() throws Exception {
        getModelNode();
        // Create op
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <choicecase>"+
                "    <list1-type  xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"create\">"+
                "      <list-key>key</list-key>"+
                "    <case3Container>"+
                "      <case-leaf3>10</case-leaf3>"+
                "    </case3Container>"+
                "    <case4Container>"+
                "      <case-leaf4>10</case-leaf4>"+
                "    </case4Container>"+
                "    </list1-type>"+
                "  </choicecase>" +
                "</validation>" ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml, false);
        verifyErrorOnMultipleCasesOfChoiceInReq(ncResponse);
        
        // Create merge
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <choicecase>"+
                "    <list1-type  xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"merge\">"+
                "      <list-key>key</list-key>"+
                "    <case3Container>"+
                "      <case-leaf3>10</case-leaf3>"+
                "    </case3Container>"+
                "    <case4Container>"+
                "      <case-leaf4>10</case-leaf4>"+
                "    </case4Container>"+
                "    </list1-type>"+
                "  </choicecase>" +
                "</validation>" ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml, false);
        verifyErrorOnMultipleCasesOfChoiceInReq(ncResponse);
        
        // Correct merge case 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <choicecase>"+
                "    <list1-type  xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"merge\">"+
                "      <list-key>key</list-key>"+
                "    <case3Container>"+
                "      <case-leaf3>10</case-leaf3>"+
                "    </case3Container>"+
                "    </list1-type>"+
                "  </choicecase>" +
                "</validation>" ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml, true);
        assertTrue(ncResponse.isOk());
        
        //Merging individual cases which is wrong
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <choicecase>"+
                "    <list1-type>"+
                "      <list-key>key</list-key>"+
                "    <case3Container xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"merge\">"+
                "      <case-leaf3>10</case-leaf3>"+
                "    </case3Container>"+
                "    <case4Container xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"merge\">"+
                "      <case-leaf4>10</case-leaf4>"+
                "    </case4Container>"+
                "    </list1-type>"+
                "  </choicecase>" +
                "</validation>" ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml, false);
        verifyErrorOnMultipleCasesOfChoiceInReq(ncResponse);
	}


	private void verifyErrorOnMultipleCasesOfChoiceInReq(NetConfResponse ncResponse) {
		assertFalse(ncResponse.isOk());
        assertEquals(1, ncResponse.getErrors().size());
        assertEquals("Invalid element in choice node ", ncResponse.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testMultiChoice() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container>"
                + "   <leafValidation>"
                + "     <leaf1>0</leaf1>"
                + "   </leafValidation>"
                + "   <choiceValidation/>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        editConfig(m_server, m_clientInfo, requestXml1, true);

        // create the leaf /validation/validation = choice
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>choice</validation>"
                + "</validation>"
                ;
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:mandatory-validation-container>"
                + "    <validation:choiceValidation/>"
                + "    <validation:leafValidation>"
                + "     <validation:leaf1>0</validation:leaf1>"
                + "    </validation:leafValidation>"
                + "   </validation:mandatory-validation-container>"
                + "   <validation:validation>choice</validation:validation>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);

        // create the leaf /validation/validation = choice2. 
        // this is expected to throw exception for mandatory choice2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>choice2</validation>"
                + "</validation>"
                ;
        
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        
        assertEquals("Missing mandatory node", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:choiceValidation/validation:choice2",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());
	    
        // create the leaf /validation/validation = choice2 and add case2 of choice2
        // Expected to get two rpc errors for two missing mandatory nodes
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>choice2</validation>"
                + " <mandatory-validation-container>"
                + "  <choiceValidation>"
                + "   <leaf12>0</leaf12>"
                + "  </choiceValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
       
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(2, ncResponse.getErrors().size());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:choiceValidation/validation:leaf13",ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:choiceValidation/validation:leafList11",ncResponse.getErrors().get(1).getErrorPath());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation>choice3</validation>"
                + " <mandatory-validation-container>"
                + "  <choiceValidation xc:operation='replace'>"
                + "  </choiceValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);

        // create the leaf /validation/validation = choice2 and case1 of choice2
        // this will internal create leaf-leaf11B and leaf11C
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation>choice2</validation>"
                + " <mandatory-validation-container>"
                + "  <choiceValidation>"
                + "   <leaf11>0</leaf11>"
                + "  </choiceValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:mandatory-validation-container>"
                + "    <validation:choiceValidation>"
                + "     <validation:leaf11>0</validation:leaf11>"
                + "     <validation:leaf11B>0</validation:leaf11B>"
                + "     <validation:leaf11C>0</validation:leaf11C>"
                + "    </validation:choiceValidation>"
                + "    <validation:leafValidation>"
                + "     <validation:leaf1>0</validation:leaf1>"
                + "    </validation:leafValidation>"
                + "   </validation:mandatory-validation-container>"
                + "   <validation:validation>choice2</validation:validation>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);

        // create the leaf /validation/validation = choice and change leaf11 to 1
        // this should delete leaf11C
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>choice2</validation>"
                + " <mandatory-validation-container>"
                + "  <choiceValidation>"
                + "   <leaf11>1</leaf11>"
                + "  </choiceValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:mandatory-validation-container>"
                + "    <validation:choiceValidation>"
                + "     <validation:leaf11>1</validation:leaf11>"
                + "     <validation:leaf11B>0</validation:leaf11B>"
                + "    </validation:choiceValidation>"
                + "    <validation:leafValidation>"
                + "     <validation:leaf1>0</validation:leaf1>"
                + "    </validation:leafValidation>"
                + "   </validation:mandatory-validation-container>"
                + "   <validation:validation>choice2</validation:validation>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	}
	
	@Test
    public void testChoiceCaseChildWithWhenCondition() throws Exception {
        getModelNode();

        // create the leaf /validation/validation = choice
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "   <choice-when-validation>"
                + "      <limit>1</limit>"
                + "      <must-limit>1</must-limit>"
                + "   </choice-when-validation>"
                + "</validation>";
        
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "     <validation:choice-when-validation>"
                + "      <validation:limit>1</validation:limit>"
                + "      <validation:must-limit>1</validation:must-limit>"
                + "      <validation:leaf1>0</validation:leaf1>"
                + "     </validation:choice-when-validation>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        
        verifyGet(responseXml);
	}
	
	@Test
	public void testMandatoryLeafForPresenceContainer() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation6 xmlns=\"urn:org:bbf:pma:validation\">"
                + "   <access-lists>"
                + "      <acl>"
                + "			<acl-type>type</acl-type>"
                + "			<acl-name>name</acl-name>"
                +"			<access-list-entries>"
                +"				<ace>"
                +"					<rule-name>rule1</rule-name>"  
                +"					<matches>"
                +"						<source-ipv4>1.1.1.1</source-ipv4>"
                +"						<destination-ipv4>1.1.1.1</destination-ipv4>"
                +"					</matches>"
                +"				</ace>"
                +"			</access-list-entries>"
                + "		</acl>"
                + "   </access-lists>"
                + "</validation6>";
        
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(ncResponse.isOk());
		assertTrue(ncResponse.getErrors().isEmpty());
		
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ "<data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "<validation:validation6 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "		<validation:access-lists>" 
				+ "			<validation:acl>" 
				+ "				<validation:access-list-entries>"
				+ "					<validation:ace>" 
				+ "						<validation:matches>"
				+ "							<validation:destination-ipv4>1.1.1.1</validation:destination-ipv4>"
				+ "							<validation:destination-port-range>" 
				+"								<validation:lower-port>1</validation:lower-port>"
				+ "							</validation:destination-port-range>"
				+ "							<validation:source-ipv4>1.1.1.1</validation:source-ipv4>"
				+ "						</validation:matches>" 
				+ "						<validation:rule-name>rule1</validation:rule-name>" 
				+ "					</validation:ace>"
				+ "				</validation:access-list-entries>" 
				+ "			<validation:acl-name>name</validation:acl-name>"
				+ "			<validation:acl-type>type</validation:acl-type>" 
				+ "		</validation:acl>" + "</validation:access-lists>"
				+ "	</validation:validation6>" 
				+ "</data>" 
				+ "</rpc-reply>";

        verifyGet(responseXml);
	}
}
