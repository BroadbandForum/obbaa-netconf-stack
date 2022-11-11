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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.ENABLE_NEW_NOTIF_STRUCTURE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.resetSystemProperty;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.setSystemPropertyAndReturnResetValue;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
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

	public static final SchemaPath VALIDATION_SCHEMA_PATH = SchemaPath.create(true, QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation"));
	private NetconfClientInfo m_clientTestUser = new NetconfClientInfo("testuser1", 2);
	private LocalSubSystem m_testSubsystem;

	protected SubSystem getSubSystem() {
		return m_testSubsystem;
	}


	@Before
	@Override
	public void setUp() throws ModelNodeInitException, SchemaBuildException {
		m_testSubsystem = new LocalSubSystem();
	    super.setUp();
	    m_schemaRegistry.setName("TEST");
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

		String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
		boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "true");

		//create a non default list case
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED_LIST));

		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> create\n" +
				"  name -> create { previousVal = 'null', currentVal = 'Mixed case with non default list case' }\n" +
				"  leaf-case1 -> create { previousVal = 'null', currentVal = 'Default value 1' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 1']] -> create\n" +
				"   name-case-mixed -> create { previousVal = 'null', currentVal = 'list entry 1' }\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'some value' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 2']] -> create\n" +
				"   name-case-mixed -> create { previousVal = 'null', currentVal = 'list entry 2' }\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'some value 2' }\n" +
				"  container-case2[/validation/choicecase/container-case2] -> create\n" +
				"   container-case2-leaf1 -> create { previousVal = 'null', currentVal = 'Default value for container-type' }\n"));

		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_TWO_ENTRIES, MESSAGE_ID);

		//delete non default list case(first entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_LIST_ENTRY_1));
		//make sure remove notif is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 1']] -> delete\n" +
				"   name-case-mixed -> delete { previousVal = 'list entry 1', currentVal = 'null' }\n" +
				"   value-case-mixed -> delete { previousVal = 'some value', currentVal = 'null' }\n"));

		// expect other list entry to be still there
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_ONE_ENTRY, MESSAGE_ID);

		//remove non existing list entry
		assertSendingSuccessfulEdit(m_server, m_clientTestUser,loadAsXml(REMOVE_NON_EXISTING_LIST_ENTRY));
		//make sure no notif is sent, since there was no concrete change
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPathIsAbsent(VALIDATION_SCHEMA_PATH);

		//delete non default list case(second entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_LIST_ENTRY_2));
		//make sure remove notif is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default list case', currentVal = 'Mixed case with non default list case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 2']] -> delete\n" +
				"   name-case-mixed -> delete { previousVal = 'list entry 2', currentVal = 'null' }\n" +
				"   value-case-mixed -> delete { previousVal = 'some value 2', currentVal = 'null' }\n"));
		// expect leaf default case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT, MESSAGE_ID);

		// create a non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_CONTAINER));
		//This needs to be chnaged once FNMS-9332 is fixed, there needs to be aremvoe notification
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> modify { previousVal = 'Mixed case with non default list case', currentVal = 'Mixed case with non default container case' }\n" +
				"  leaf-case-mixed -> delete { previousVal = 'Default value for mixed case', currentVal = 'null' }\n" +
				"  container-case-mixed[/validation/choicecase/container-case-mixed] -> create\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'mixed case value' }\n"));

		TestUtil.verifyGet(m_server,(NetconfFilter)null,GET_RESPONSE_MIXED_CONTAINER, MESSAGE_ID);

		//delete non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(REMOVE_MIXED_CASE_CONTAINER));
		//make sure remove notif is present and default case merge is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default container case', currentVal = 'Mixed case with non default container case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  container-case-mixed[/validation/choicecase/container-case-mixed] -> delete\n" +
				"   value-case-mixed -> delete { previousVal = 'mixed case value', currentVal = 'null' }\n"));
		//expect default leaf case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT2, MESSAGE_ID);

		resetSystemProperty(toBeReset, previousValue);
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

		String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
		boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "true");

		//create a non default list case
		assertSendingSuccessfulEdit(m_server, m_clientTestUser, loadAsXml(CHOICE_CASE_MIXED_LIST));
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> create\n" +
				"  name -> create { previousVal = 'null', currentVal = 'Mixed case with non default list case' }\n" +
				"  leaf-case1 -> create { previousVal = 'null', currentVal = 'Default value 1' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 1']] -> create\n" +
				"   name-case-mixed -> create { previousVal = 'null', currentVal = 'list entry 1' }\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'some value' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 2']] -> create\n" +
				"   name-case-mixed -> create { previousVal = 'null', currentVal = 'list entry 2' }\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'some value 2' }\n" +
				"  container-case2[/validation/choicecase/container-case2] -> create\n" +
				"   container-case2-leaf1 -> create { previousVal = 'null', currentVal = 'Default value for container-type' }\n"));

		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_TWO_ENTRIES, MESSAGE_ID);

		//delete non default list case(first entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_ENTRY_1));
		//make sure remove notif is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 1']] -> delete\n" +
				"   name-case-mixed -> delete { previousVal = 'list entry 1', currentVal = 'null' }\n" +
				"   value-case-mixed -> delete { previousVal = 'some value', currentVal = 'null' }\n"));

		// expect other list entry to be still there
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_LIST_ONE_ENTRY, MESSAGE_ID);

		//delete non default list case(second entry)
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LIST_ENTRY_2));

		//make sure remove notif is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default list case', currentVal = 'Mixed case with non default list case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  list-case-mixed[/validation/choicecase/list-case-mixed[name-case-mixed='list entry 2']] -> delete\n" +
				"   name-case-mixed -> delete { previousVal = 'list entry 2', currentVal = 'null' }\n" +
				"   value-case-mixed -> delete { previousVal = 'some value 2', currentVal = 'null' }\n"));
		// expect leaf default case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT, MESSAGE_ID);

		// create a non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_CONTAINER));
		//This needs to be changed once FNMS-9332 is fixed, there needs to be aremvoe notification
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> modify { previousVal = 'Mixed case with non default list case', currentVal = 'Mixed case with non default container case' }\n" +
				"  leaf-case-mixed -> delete { previousVal = 'Default value for mixed case', currentVal = 'null' }\n" +
				"  container-case-mixed[/validation/choicecase/container-case-mixed] -> create\n" +
				"   value-case-mixed -> create { previousVal = 'null', currentVal = 'mixed case value' }\n"));

		TestUtil.verifyGet(m_server,(NetconfFilter)null,GET_RESPONSE_MIXED_CONTAINER, MESSAGE_ID);

		//delete non default container case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_CONTAINER));
		//make sure remove notif is present and default case merge is present
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default container case', currentVal = 'Mixed case with non default container case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  container-case-mixed[/validation/choicecase/container-case-mixed] -> delete\n" +
				"   value-case-mixed -> delete { previousVal = 'mixed case value', currentVal = 'null' }\n"));
		//expect default leaf case
		TestUtil.verifyGet(m_server, (NetconfFilter)null, GET_RESPONSE_MIXED_DEFAULT2, MESSAGE_ID);

		//create a non default leaf-list case
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_LEAFLIST));
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> modify { previousVal = 'Mixed case with non default container case', currentVal = 'Mixed case with non default leaf-list case' }\n" +
				"  leaflist-case-mixed -> create { previousVal = 'null', currentVal = 'first leaf-list' }\n" +
				"  leaflist-case-mixed -> create { previousVal = 'null', currentVal = 'second leaf-list' }\n" +
				"  leaf-case-mixed -> delete { previousVal = 'Default value for mixed case', currentVal = 'null' }\n"));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_CHOICE_CASE_MIXED_LEAFLIST_RESPONSE, MESSAGE_ID);

		// delete one of the leaf-list values. Expect the other leaf list to still be there
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRY1));
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default leaf-list case', currentVal = 'Mixed case with non default leaf-list case' }\n" +
				"  leaflist-case-mixed -> modify { previousVal = 'first leaf-list', currentVal = 'null' }\n" +
				"  leaflist-case-mixed -> modify { previousVal = 'second leaf-list', currentVal = 'second leaf-list' }\n"));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE1, MESSAGE_ID);

		// delete the other leaf-list entry and expect the default leaf to be created.
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRY2));
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default leaf-list case', currentVal = 'Mixed case with non default leaf-list case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  leaflist-case-mixed -> delete { previousVal = 'second leaf-list', currentVal = 'null' }\n"));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE2, MESSAGE_ID);

		// create non default leaf-list case again
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(CHOICE_CASE_MIXED_LEAFLIST));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_CHOICE_CASE_MIXED_LEAFLIST_RESPONSE, MESSAGE_ID);

		// delete both leaf-list values at once and expect the default leaf to be created.
		assertSendingSuccessfulEdit(m_server,m_clientTestUser,loadAsXml(DELETE_MIXED_CASE_LEAFLIST_ENTRIES_XML));
		m_testSubsystem.assertChangeTreeNodeForGivenSchemaPath(VALIDATION_SCHEMA_PATH, Collections.singletonList("validation[/validation] -> modify\n" +
				" choicecase[/validation/choicecase] -> modify\n" +
				"  name -> none { previousVal = 'Mixed case with non default leaf-list case', currentVal = 'Mixed case with non default leaf-list case' }\n" +
				"  leaf-case-mixed -> create { previousVal = 'null', currentVal = 'Default value for mixed case' }\n" +
				"  leaflist-case-mixed -> delete { previousVal = 'first leaf-list', currentVal = 'null' }\n" +
				"  leaflist-case-mixed -> delete { previousVal = 'second leaf-list', currentVal = 'null' }\n"));
		TestUtil.verifyGet(m_server,(NetconfFilter)null, GET_MIXED_LEAFLIST_AFTER_DELETE_RESPONSE2, MESSAGE_ID);

		resetSystemProperty(toBeReset, previousValue);
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
        assertEquals("Mandatory choice 'mandatory-choice' is missing", rpcError.getErrorMessage());
        
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
	    
	    
	    
	    ModelNodeId modelNodeId = new ModelNodeId("/container=validation", "urn:org:bbf2:pma:validation");
	    QName validationQName = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
	    QName listQName = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "level1-list");
	    SchemaPath validation_schemaPath = SchemaPath.create(true, validationQName);
	    Map<QName, List<XmlModelNodeImpl>> children = new HashMap<QName, List<XmlModelNodeImpl>>();
	    children.put(listQName, null);
	    SchemaPath mandatoryValidation_schemaPath = new SchemaPathBuilder().withParent(validation_schemaPath).appendLocalName("mandatory-validation").build();
	    
	    String element = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation:mandatory-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"  +
                "   <validation:level1-list>"+
                "    <validation:name>TEST</validation:name>" +                
                "  </validation:level1-list>" +
                "</validation:mandatory-validation>"; 
	    List<Element> elementList = Arrays.asList(TestUtil.transformToElement(element));
	    
	    XmlModelNodeImpl modelnode = new XmlModelNodeImpl(ConfigAttributeFactory.getDocument(), mandatoryValidation_schemaPath, Collections.emptyMap(), elementList, null, modelNodeId, xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, m_modelNodeDsm, null, true, null);
	    when(xmlModelNodeToXmlMapper.getModelNodeFromParentSchemaPath(any(), any(), any(), any(), any(), any(), any())).thenReturn(modelnode);
	    doReturn(null).doReturn(modelnode).when(m_modelNodeDsm).findNode(eq(mandatoryValidation_schemaPath), any(), eq(modelNodeId), any());
	    
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
        assertEquals("Missing mandatory node - mandatory-case2-leaf2", rpcError.getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation/validation:mandatory-case2-leaf2", rpcError.getErrorPath());
    }
	
	@Test
	public void testEditWithMultipleCasesOfChoiceInReq() throws Exception {
        getModelNode();
        // Create op
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>choice</validation>"
                + "</validation>"
                ;
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>choice2</validation>"
                + "</validation>"
                ;
        
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        
        assertEquals("Missing mandatory node - choice2", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:choiceValidation/validation:choice2",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());
	    
        // create the leaf /validation/validation = choice2 and add case2 of choice2
        // Expected to get two rpc errors for two missing mandatory nodes
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
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
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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

	// TODO to be addressed by FNMS-31166
//	@Test
    public void testChoiceCaseChildWithWhenCondition() throws Exception {
        getModelNode();

        // create the leaf /validation/validation = choice
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "   <choice-when-validation>"
                + "      <limit>1</limit>"
                + "      <must-limit>1</must-limit>"
                + "   </choice-when-validation>"
                + "</validation>";
        
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
                + "<validation6 xmlns=\"urn:org:bbf2:pma:validation\">"
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
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "<validation:validation6 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
	
    @Test
    public void testNestedChoiceDeletesLowerLevelNodes() throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">\n"
                + "    <three-level-choice-container>\n" 
                + "        <level3-case1-leaf1>test1</level3-case1-leaf1>\n"
                + "    </three-level-choice-container>\n" 
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "    <data>\n"
                + "        <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n"
                + "            <choice-case-test:three-level-choice-container>\n"
                + "                <choice-case-test:level1Container>\n"
                + "                    <choice-case-test:level1-case2-leaf2>case2</choice-case-test:level1-case2-leaf2>\n"
                + "                </choice-case-test:level1Container>\n"
                + "                <choice-case-test:level2-case2-leaf2>level2Case2</choice-case-test:level2-case2-leaf2>\n"
                + "                <choice-case-test:level3-case1-leaf1>test1</choice-case-test:level3-case1-leaf1>\n"
                + "            </choice-case-test:three-level-choice-container>\n"
                + "        </choice-case-test:choice-container>\n"
                + "        <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "    </data>\n" 
                + "</rpc-reply>";

        verifyGet(responseXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">\n"
                + "    <three-level-choice-container>\n" 
                + "        <level1-case1-leaf1>test1111</level1-case1-leaf1>\n"
                + "    </three-level-choice-container>\n" 
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "    <data>\n"
                + "        <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n"
                + "            <choice-case-test:three-level-choice-container>\n"
                + "                <choice-case-test:level1-case1-leaf1>test1111</choice-case-test:level1-case1-leaf1>\n"
                + "                 <choice-case-test:level1Case1-Container/>"
                + "            </choice-case-test:three-level-choice-container>\n"
                + "        </choice-case-test:choice-container>\n"
                + "        <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "    </data>\n" 
                + "</rpc-reply>";

        verifyGet(responseXml);

    }

    @Test
    public void testNestedChoiceDeletesTopLevelCaseNodes() throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" 
                + " <three-level-choice-container>"
                + "  <level1-case1-leaf1>test1111</level1-case1-leaf1>" 
                + "  </three-level-choice-container>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "  <choice-case-test:three-level-choice-container>"
                + "    <choice-case-test:level1-case1-leaf1>test1111</choice-case-test:level1-case1-leaf1>"
                + "    <choice-case-test:level1Case1-Container/>" 
                + "  </choice-case-test:three-level-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
                + "</rpc-reply>";

        verifyGet(responseXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" 
                + "  <three-level-choice-container>"
                + "    <level3-case1-leaf1>test1</level3-case1-leaf1>" 
                + "  </three-level-choice-container>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                    + "     <data>"
                    + "         <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                    + "             <choice-case-test:three-level-choice-container>"
                    + "                 <choice-case-test:level1Container>"
                    + "                     <choice-case-test:level1-case2-leaf2>case2</choice-case-test:level1-case2-leaf2>"
                    + "                 </choice-case-test:level1Container>"
                    + "                 <choice-case-test:level2-case2-leaf2>level2Case2</choice-case-test:level2-case2-leaf2>"
                    + "                 <choice-case-test:level3-case1-leaf1>test1</choice-case-test:level3-case1-leaf1>"
                    + "             </choice-case-test:three-level-choice-container>" 
                    + "         </choice-case-test:choice-container>"
                    + "         <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                    + "     </data>"
                    + "</rpc-reply>";

        verifyGet(responseXml);

    }

    @Test
    public void testNestedChoiceWithMixedCasesAndCaseChildContainers() throws Exception {
        // The default nodes to be added after FNMS-44195 ia addressed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "     <choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" 
                + "         <three-level-choice-container>"
                + "             <level1Case1-Container>" 
                + "                 <level1-case1-leaf2>test2</level1-case1-leaf2>"
                + "             </level1Case1-Container>" 
                + "         </three-level-choice-container>" 
                + "     </choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                            + "<data>"
                            + "     <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                            + "         <choice-case-test:three-level-choice-container>" 
                            + "             <choice-case-test:level1Case1-Container>"
                            + "                 <choice-case-test:level1-case1-leaf2>test2</choice-case-test:level1-case1-leaf2>"
                            + "             </choice-case-test:level1Case1-Container>" 
                            + "         </choice-case-test:three-level-choice-container>"
                            + "     </choice-case-test:choice-container>"
                            + "     <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                            + "</data>"
                            + "</rpc-reply>";

        verifyGet(responseXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" 
                + "     <three-level-choice-container>"
                + "         <level3-case1-leaf1>test1</level3-case1-leaf1>" 
                + "     </three-level-choice-container>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + "<data>"
                + "     <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "         <choice-case-test:three-level-choice-container>"
                + "                 <choice-case-test:level1Container>"
                + "                     <choice-case-test:level1-case2-leaf2>case2</choice-case-test:level1-case2-leaf2>"
                + "                 </choice-case-test:level1Container>"
                + "                 <choice-case-test:level2-case2-leaf2>level2Case2</choice-case-test:level2-case2-leaf2>"
                + "             <choice-case-test:level3-case1-leaf1>test1</choice-case-test:level3-case1-leaf1>"
                + "         </choice-case-test:three-level-choice-container>" 
                + "     </choice-case-test:choice-container>"
                + "     <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(responseXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" 
                + "     <three-level-choice-container>"
                + "         <level1-case1-leaf1>test1111</level1-case1-leaf1>" 
                + "     </three-level-choice-container>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + "<data>"
                + "     <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "         <choice-case-test:three-level-choice-container>"
                + "             <choice-case-test:level1-case1-leaf1>test1111</choice-case-test:level1-case1-leaf1>"
                + "             <choice-case-test:level1Case1-Container/>"
                + "         </choice-case-test:three-level-choice-container>"
                + "     </choice-case-test:choice-container>"
                + "     <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(responseXml);

    }
	
	@Test
	public void testLeafRefWithChoiceCase() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"		</scheduler-node>"
				+"		<child-scheduler-nodes>"
				+"			<name>test</name>"
				+"		</child-scheduler-nodes>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
				+"	<choice-case-test:interface>"
				+"		<choice-case-test:name>if1</choice-case-test:name>"
				+"		<choice-case-test:tm-root>"
				+"			<choice-case-test:child-scheduler-nodes>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"			</choice-case-test:child-scheduler-nodes>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>10</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"		</choice-case-test:tm-root>"
				+"	</choice-case-test:interface>"
				+" </choice-case-test:choice-container>"
				+"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// invalid leaf-ref value
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test1</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"		</scheduler-node>"
				+"		<child-scheduler-nodes>"
				+"			<name>test2</name>"
				+"		</child-scheduler-nodes>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/choice-case-test:choice-container/choice-case-test:interface[choice-case-test:name='if1']/choice-case-test:tm-root/choice-case-test:child-scheduler-nodes[choice-case-test:name='test2']/choice-case-test:name", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'test2' must exist", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testLeafRefWithChoiceCase_MissingMandatoryNode() throws Exception {
		getModelNode();

		// missing mandatory node
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test</name>"
				+"		</scheduler-node>"
				+"		<child-scheduler-nodes>"
				+"			<name>test</name>"
				+"		</child-scheduler-nodes>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/choice-case-test:choice-container/choice-case-test:interface[choice-case-test:name='if1']/choice-case-test:tm-root/choice-case-test:scheduler-node[choice-case-test:name='test']/choice-case-test:scheduling-level", response.getErrors().get(0).getErrorPath());
		assertEquals("Missing mandatory node - scheduling-level", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafRefWithChoiceCase_RemoveReferenceNode() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"		</scheduler-node>"
				+"		<child-scheduler-nodes>"
				+"			<name>test</name>"
				+"		</child-scheduler-nodes>"
				+"		<scheduler-node>"
				+"			<name>test1</name>"
				+"			<scheduling-level>12</scheduling-level>"
				+"		</scheduler-node>"
				+"		<child-scheduler-nodes>"
				+"			<name>test1</name>"
				+"		</child-scheduler-nodes>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
				+"	<choice-case-test:interface>"
				+"		<choice-case-test:name>if1</choice-case-test:name>"
				+"		<choice-case-test:tm-root>"
				+"			<choice-case-test:child-scheduler-nodes>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"			</choice-case-test:child-scheduler-nodes>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>10</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"			<choice-case-test:child-scheduler-nodes>"
				+"				<choice-case-test:name>test1</choice-case-test:name>"
				+"			</choice-case-test:child-scheduler-nodes>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test1</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>12</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"		</choice-case-test:tm-root>"
				+"	</choice-case-test:interface>"
				+" </choice-case-test:choice-container>"
				+"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"</data>"
				+ "</rpc-reply>";
		
		verifyGet(responseXml);

		// Remove valid leaf-ref case
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+"<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<child-scheduler-nodes xc:operation=\"delete\">"
				+"			<name>test1</name>"
				+"		</child-scheduler-nodes>"
				+"	</tm-root>"
				+" </interface>"
				+"</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
				+"	<choice-case-test:interface>"
				+"		<choice-case-test:name>if1</choice-case-test:name>"
				+"		<choice-case-test:tm-root>"
				+"			<choice-case-test:child-scheduler-nodes>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"			</choice-case-test:child-scheduler-nodes>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>10</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test1</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>12</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"		</choice-case-test:tm-root>"
				+"	</choice-case-test:interface>"
				+" </choice-case-test:choice-container>"
				+"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Remove referenced node
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+"<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node xc:operation=\"delete\">"
				+"			<name>test</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"		</scheduler-node>"
				+"	</tm-root>"
				+" </interface>"
				+"</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/choice-case-test:choice-container/choice-case-test:interface[choice-case-test:name='if1']/choice-case-test:tm-root/choice-case-test:child-scheduler-nodes[choice-case-test:name='test']/choice-case-test:name", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'test' must exist", response.getErrors().get(0).getErrorMessage());

	}


	@Test
	public void testLeafRefWithNestedChoiceCase() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"			<child-scheduler-nodes>"
				+"				<name>test1</name>"
				+"			</child-scheduler-nodes>"
				+"		</scheduler-node>"
				+"		<scheduler-node>"
				+"			<name>test1</name>"
				+"			<scheduling-level>11</scheduling-level>"
				+"		</scheduler-node>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
				+"	<choice-case-test:interface>"
				+"		<choice-case-test:name>if1</choice-case-test:name>"
				+"		<choice-case-test:tm-root>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>10</choice-case-test:scheduling-level>"
				+"				<choice-case-test:child-scheduler-nodes>"
				+"					<choice-case-test:name>test1</choice-case-test:name>"
				+"				</choice-case-test:child-scheduler-nodes>"
				+"			</choice-case-test:scheduler-node>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test1</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>11</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"		</choice-case-test:tm-root>"
				+"	</choice-case-test:interface>"
				+" </choice-case-test:choice-container>"
				+"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// remove leaf-ref referenced node
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+"<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node xc:operation=\"delete\">"
				+"			<name>test1</name>"
				+"		</scheduler-node>"
				+"	</tm-root>"
				+" </interface>"
				+"</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/choice-case-test:choice-container/choice-case-test:interface[choice-case-test:name='if1']/choice-case-test:tm-root/choice-case-test:scheduler-node[choice-case-test:name='test']/choice-case-test:child-scheduler-nodes[choice-case-test:name='test1']/choice-case-test:name", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'test1' must exist", response.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testImpactNodeValidationOnMustConstraints() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test</name>"
				+"			<scheduling-level>10</scheduling-level>"
				+"			<child-scheduler-nodes>"
				+"				<name>test1</name>"
				+"			</child-scheduler-nodes>"
				+"		</scheduler-node>"
				+"		<scheduler-node>"
				+"			<name>test1</name>"
				+"			<scheduling-level>11</scheduling-level>"
				+"		</scheduler-node>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
				+"	<choice-case-test:interface>"
				+"		<choice-case-test:name>if1</choice-case-test:name>"
				+"		<choice-case-test:tm-root>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>10</choice-case-test:scheduling-level>"
				+"				<choice-case-test:child-scheduler-nodes>"
				+"					<choice-case-test:name>test1</choice-case-test:name>"
				+"				</choice-case-test:child-scheduler-nodes>"
				+"			</choice-case-test:scheduler-node>"
				+"			<choice-case-test:scheduler-node>"
				+"				<choice-case-test:name>test1</choice-case-test:name>"
				+"				<choice-case-test:scheduling-level>11</choice-case-test:scheduling-level>"
				+"			</choice-case-test:scheduler-node>"
				+"		</choice-case-test:tm-root>"
				+"	</choice-case-test:interface>"
				+" </choice-case-test:choice-container>"
				+"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+" <interface>"
				+"	<name>if1</name>"
				+"	<tm-root>"
				+"		<scheduler-node>"
				+"			<name>test1</name>"
				+"			<scheduling-level>9</scheduling-level>"
				+"		</scheduler-node>"
				+"	</tm-root>"
				+"  </interface>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals("/choice-case-test:choice-container/choice-case-test:interface[choice-case-test:name='if1']/choice-case-test:tm-root/choice-case-test:scheduler-node[choice-case-test:name='test']/choice-case-test:child-scheduler-nodes[choice-case-test:name='test1']/choice-case-test:name", response.getErrors().get(0).getErrorPath());
		assertEquals("The scheduler-level should be bigger than the value of parent node.", response.getErrors().get(0).getErrorMessage());

	}
	
	@Test
    public void testDefaultsChoiceWithWhen() throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer2>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer2>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer2>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"  </choice-case-test:outerContainer2>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //change the case by failing the when condition
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer2>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer2>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer2>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>deaf11Default</choice-case-test:leaf11>"
                +"  </choice-case-test:outerContainer2>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
	}
	
	@Test
    public void testDefaultsCaseWithWhen() throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer3>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer3>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer3>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>deaf11Default</choice-case-test:leaf11>"
                +"  </choice-case-test:outerContainer3>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer3>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  <leaf22>leaf22Present</leaf22>"
                +"  </outerContainer3>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer3>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf22>leaf22Present</choice-case-test:leaf22>"
                +"  </choice-case-test:outerContainer3>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer3>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer3>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer3>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>deaf11Default</choice-case-test:leaf11>"
                +"  </choice-case-test:outerContainer3>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
	}
	
	@Test
    public void testDefaultsWithWhen() throws Exception {
	    getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  <leaf21>nonDefultLeafPresent</leaf21>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf21>nonDefultLeafPresent</choice-case-test:leaf21>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //change the case by failing the when condition
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //Making the when condition pass
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // Changing the case to case3
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <case3Container>"
                +"      <leaf31>leaf31Default</leaf31>"
                +"      </case3Container>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:case3Container>"
                +"          <choice-case-test:leaf31>leaf31Default</choice-case-test:leaf31>"
                +"      </choice-case-test:case3Container>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //change the case by failing the when condition
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // Changing the case to case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>in</outerLeaf>"
                +"    <case4Container>"
                +"      <leaf42>leaf42Present</leaf42>"
                +"    </case4Container>"
                +"    <leaf43>leaf43Present</leaf43>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:case4Container>"
                +"          <choice-case-test:leaf42>leaf42Present</choice-case-test:leaf42>"
                +"      </choice-case-test:case4Container>"
                +"      <choice-case-test:leaf43>leaf43Present</choice-case-test:leaf43>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //change the case by failing the when condition
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf43>leaf43Present</choice-case-test:leaf43>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"    <leaf43 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"remove\"/>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //Other case of inner choice (case4)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  <leaf41>nonDefultLeafPresent</leaf41>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf41>nonDefultLeafPresent</choice-case-test:leaf41>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        //change the case by failing the when condition
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
	}
	
	@Ignore
    public void testAugmentedDefaultsWithWhen() throws Exception {
        getModelNode();
        // case1- leaf augmented with when outerleaf=in
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer4>"
                + "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // case2- case augmented with when outerleaf=selectCase2 both positive and negative
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <leaf21>leaf21Selected</leaf21>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, false);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>selectCase2</outerLeaf>"
                +"  <leaf21>leaf21Selected</leaf21>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>selectCase2</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf21>leaf21Selected</choice-case-test:leaf21>"
                +"      <choice-case-test:leaf22>leaf22Default</choice-case-test:leaf22>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // Reset back
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // case3- choice augmented with when outerleaf=selectInnerChoice both positive and negative
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <leaf31>leaf31Selected</leaf31>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, false); // augment when not true
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>selectInnerChoice</outerLeaf>"
                +"  <leaf31>leaf31Selected</leaf31>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true); 
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>selectInnerChoice</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf31>leaf31Selected</choice-case-test:leaf31>"
                +"      <choice-case-test:leaf32>leaf32Default</choice-case-test:leaf32>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // Reset back
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer4>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer4>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer4>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer4>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
	}
	
	@Test
    public void testGroupingDefaultsWithWhen() throws Exception {
        getModelNode();
        // case1- leaf grouping with when outerleaf=in
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer5>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer5>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer5>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer5>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer5>"
                +"  <outerLeaf>out</outerLeaf>"
                +"  </outerContainer5>"
                + "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer5>"
                +"      <choice-case-test:outerLeaf>out</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"  </choice-case-test:outerContainer5>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // case3- choice grouping with when outerleaf=selectInnerChoice both positive and negative
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer5>"
                +"  <leaf31>leaf31Selected</leaf31>"
                +"  </outerContainer5>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, false); // augment when not true
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer5>"
                +"  <outerLeaf>selectInnerChoice</outerLeaf>"
                +"  <leaf31>leaf31Selected</leaf31>"
                +"  </outerContainer5>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true); 
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer5>"
                +"      <choice-case-test:outerLeaf>selectInnerChoice</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf31>leaf31Selected</choice-case-test:leaf31>"
                +"      <choice-case-test:leaf32>leaf32Default</choice-case-test:leaf32>"
                +"  </choice-case-test:outerContainer5>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
        
        // Reset back
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
                +" <outerContainer5>"
                +"  <outerLeaf>in</outerLeaf>"
                +"  </outerContainer5>"
                + "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);
        responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"  <choice-case-test:outerContainer5>"
                +"      <choice-case-test:outerLeaf>in</choice-case-test:outerLeaf>"
                +"      <choice-case-test:leaf11>leaf11Default</choice-case-test:leaf11>"
                +"      <choice-case-test:leaf12>leaf12Default</choice-case-test:leaf12>"
                +"  </choice-case-test:outerContainer5>"
                +" </choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                + "</rpc-reply>";
        verifyGet(responseXml);
    }

	@Test
	public void testMandatoryAndMustForLeafListPositiveCase() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ " <containerWithMandatoryAndWhenLeafList>"
				+ "  <mustLeafList>one</mustLeafList>"
				+ "  <mustLeafList>two</mustLeafList>"
				+ " </containerWithMandatoryAndWhenLeafList>"
				+ "  <triggerMustLeaf>mustMan</triggerMustLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer10>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenLeafList>\n" +
				"               <choice-case-test:mustLeafList>one</choice-case-test:mustLeafList>\n" +
				"               <choice-case-test:mustLeafList>two</choice-case-test:mustLeafList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeafList>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:triggerMustLeaf>mustMan</choice-case-test:triggerMustLeaf>\n" +
				"         </choice-case-test:outerContainer10>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <triggerMustLeaf>makingMustFalse</triggerMustLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer10/choice-case-test" +
				":containerWithMandatoryAndWhenLeafList/choice-case-test:mustLeafList", "Violate must constraints: ../." +
				"./triggerMustLeaf='mustMan'");

	}

	@Test
	public void testMandatoryAndMustForLeafList() throws Exception {
		getModelNode();
		// case1- leaf grouping with when triggerLeaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ "  <triggerMustLeaf>in</triggerMustLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ "  <triggerMustLeaf>mustMan</triggerMustLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer10/choice-case-test" +
				":containerWithMandatoryAndWhenLeafList/choice-case-test:mustLeafList", "Minimum elements required for mustLeafList is 1.");
	}

	@Test
	public void testMandatoryAndWhenForLeafListPositiveCase() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ " <containerWithMandatoryAndWhenLeafList>"
				+ "  <whenLeafList>one</whenLeafList>"
				+ "  <whenLeafList>two</whenLeafList>"
				+ "  <mustLeafList>one</mustLeafList>"
				+ " </containerWithMandatoryAndWhenLeafList>"
				+ "  <triggerLeaf>whenMan</triggerLeaf>"
				+ "  <triggerMustLeaf>mustMan</triggerMustLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer10>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenLeafList>\n" +
				"               <choice-case-test:whenLeafList>one</choice-case-test:whenLeafList>\n" +
				"               <choice-case-test:whenLeafList>two</choice-case-test:whenLeafList>\n" +
				"               <choice-case-test:mustLeafList>one</choice-case-test:mustLeafList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeafList>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:triggerLeaf>whenMan</choice-case-test:triggerLeaf>\n" +
				"            <choice-case-test:triggerMustLeaf>mustMan</choice-case-test:triggerMustLeaf>\n" +
				"         </choice-case-test:outerContainer10>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <triggerLeaf>makingWhenFalse</triggerLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer10>\n" +
				"			 <choice-case-test:containerWithMandatoryAndWhenLeafList>\n" +
				"               <choice-case-test:mustLeafList>one</choice-case-test:mustLeafList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeafList>\n"+
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:triggerLeaf>makingWhenFalse</choice-case-test:triggerLeaf>\n" +
				"			 <choice-case-test:triggerMustLeaf>mustMan</choice-case-test:triggerMustLeaf>\n"+
				"         </choice-case-test:outerContainer10>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);
	}

	@Test
	public void testMandatoryAndWhenForLeafList() throws Exception {
		getModelNode();
		// case1- leaf grouping with when triggerLeaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ "  <triggerLeaf>in</triggerLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer10>"
				+ "  <leaf1>in</leaf1>"
				+ "  <triggerLeaf>whenMan</triggerLeaf>"
				+ "  </outerContainer10>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer10/choice-case-test:containerWithMandatoryAndWhenLeafList/choice-case-test:whenLeafList", "Minimum elements required for whenLeafList is 2.");
	}

	@Test
	public void testMandatoryAndMustForLeafPositiveCase() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ " <containerWithMandatoryAndWhenLeaf>"
				+ "  <mustLeaf>in</mustLeaf>"
				+ " </containerWithMandatoryAndWhenLeaf>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer7>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"               <choice-case-test:mustLeaf>in</choice-case-test:mustLeaf>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer7>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <outerMustLeaf>makingMustFalse</outerMustLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer7/choice-case-test" +
				":containerWithMandatoryAndWhenLeaf/choice-case-test:mustLeaf", "Violate must constraints: ../../outerMustLeaf='mustMan'");
	}

	@Test
	public void testMandatoryAndMustForLeafNegativeCase() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerMustLeaf>notMustMan</outerMustLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer7/choice-case-test:containerWithMandatoryAndWhenLeaf/choice-case-test:mustLeaf", "Missing mandatory node - mustLeaf");
	}

	@Test
	public void testMandatoryAndWhenForLeafPositiveCase() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ " <containerWithMandatoryAndWhenLeaf>"
				+ "  <whenLeaf>in</whenLeaf>"
				+ "  <mustLeaf>must</mustLeaf>"
				+ " </containerWithMandatoryAndWhenLeaf>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer7>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"               <choice-case-test:whenLeaf>in</choice-case-test:whenLeaf>\n" +
				"               <choice-case-test:mustLeaf>must</choice-case-test:mustLeaf>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>whenMan</choice-case-test:outerLeaf>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer7>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <outerLeaf>makingWhenFalse</outerLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer7>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"               <choice-case-test:mustLeaf>must</choice-case-test:mustLeaf>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenLeaf>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>makingWhenFalse</choice-case-test:outerLeaf>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer7>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);
	}

	@Test
	public void testMandatoryAndWhenForLeaf() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>in</outerLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer7>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ "  </outerContainer7>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer7/choice-case-test:containerWithMandatoryAndWhenLeaf/choice-case-test:whenLeaf", "Missing mandatory node - whenLeaf");
	}

	@Test
	public void testMandatoryAndMustForListPositiveCase() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ " <containerWithMandatoryAndWhenList>"
				+ " <mustList>"
				+ "  <name>one</name>"
				+ " </mustList>"
				+ " <mustList>"
				+ "  <name>two</name>"
				+ " </mustList>"
				+ " </containerWithMandatoryAndWhenList>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer8>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>one</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>two</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer8>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ " <containerWithMandatoryAndWhenList>"
				+ " <mustList xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"remove\">"
				+ "  <name>one</name>"
				+ " </mustList>"
				+ " </containerWithMandatoryAndWhenList>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, false);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <outerMustLeaf>mustFailed</outerMustLeaf>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer8/choice-case-test" +
				":containerWithMandatoryAndWhenList/choice-case-test:mustList[choice-case-test:name='two']", "Violate must constraints: ." +
				"./../outerMustLeaf='mustMan'");
	}

	@Test
	public void testMandatoryAndMustForListNegativeCase() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerMustLeaf>notMustMan</outerMustLeaf>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);


		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer8>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerMustLeaf>notMustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer8>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ "   <containerWithMandatoryAndWhenList>"
				+ "     <mustList>"
				+ "       <name>must1</name>"
				+ "     </mustList>"
				+ "   </containerWithMandatoryAndWhenList>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer8/choice-case-test:containerWithMandatoryAndWhenList/choice-case-test:mustList", "Minimum elements required for mustList is 2.");
	}

	@Test
	public void testMandatoryAndWhenForListPositiveCase() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ "  <outerMustLeaf>mustMan</outerMustLeaf>"
				+ " <containerWithMandatoryAndWhenList>"
				+ " <list1>"
				+ "  <name>one</name>"
				+ " </list1>"
				+ " <list1>"
				+ "  <name>two</name>"
				+ " </list1>"
				+ " <mustList>"
				+ "   <name>three</name>"
				+ " </mustList>"
				+ " <mustList>"
				+ "   <name>four</name>"
				+ " </mustList>"
				+ " </containerWithMandatoryAndWhenList>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer8>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"               <choice-case-test:list1>\n" +
				"                  <choice-case-test:name>one</choice-case-test:name>\n" +
				"               </choice-case-test:list1>\n" +
				"               <choice-case-test:list1>\n" +
				"                  <choice-case-test:name>two</choice-case-test:name>\n" +
				"               </choice-case-test:list1>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>three</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>four</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>whenMan</choice-case-test:outerLeaf>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer8>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ " <containerWithMandatoryAndWhenList>"
				+ " <list1 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"remove\">"
				+ "  <name>one</name>"
				+ " </list1>"
				+ " </containerWithMandatoryAndWhenList>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, false);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <outerLeaf>whenFailed</outerLeaf>"
				+ "  </outerContainer8>"
				+ "</choice-container>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer8>\n" +
				"			  <choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>three</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"               <choice-case-test:mustList>\n" +
				"                  <choice-case-test:name>four</choice-case-test:name>\n" +
				"               </choice-case-test:mustList>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenList>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>whenFailed</choice-case-test:outerLeaf>\n" +
				"            <choice-case-test:outerMustLeaf>mustMan</choice-case-test:outerMustLeaf>\n" +
				"         </choice-case-test:outerContainer8>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);
	}

	@Test
	public void testMandatoryAndWhenForList() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>in</outerLeaf>"
				+ "  </outerContainer8>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer8>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ "  </outerContainer8>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer8/choice-case-test:containerWithMandatoryAndWhenList/choice-case-test:list1", "Minimum elements required for list1 is 2.");
	}

	@Test
	public void testMandatoryAndWhenForChoicePositiveCase() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer9>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ " <containerWithMandatoryAndWhenChoice>"
				+ "  <case1Leaf>in</case1Leaf>"
				+ " </containerWithMandatoryAndWhenChoice>"
				+ "  </outerContainer9>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer9>\n" +
				"            <choice-case-test:containerWithMandatoryAndWhenChoice>\n" +
				"               <choice-case-test:case1Leaf>in</choice-case-test:case1Leaf>\n" +
				"            </choice-case-test:containerWithMandatoryAndWhenChoice>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>whenMan</choice-case-test:outerLeaf>\n" +
				"         </choice-case-test:outerContainer9>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer9>"
				+ "  <outerLeaf>whenFailed</outerLeaf>"
				+ "  </outerContainer9>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"   <data>\n" +
				"      <choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" +
				"         <choice-case-test:outerContainer9>\n" +
				"            <choice-case-test:leaf1>in</choice-case-test:leaf1>\n" +
				"            <choice-case-test:outerLeaf>whenFailed</choice-case-test:outerLeaf>\n" +
				"         </choice-case-test:outerContainer9>\n" +
				"      </choice-case-test:choice-container>\n" +
				"      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"   </data>\n" +
				"</rpc-reply>";
		verifyGet(output);
	}

	@Test
	public void testMandatoryAndWhenForChoice() throws Exception {
		getModelNode();
		// case1- leaf grouping with when outerleaf=in
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer9>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>in</outerLeaf>"
				+ "  </outerContainer9>"
				+ "</choice-container>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">"
				+ " <outerContainer9>"
				+ "  <leaf1>in</leaf1>"
				+ "  <outerLeaf>whenMan</outerLeaf>"
				+ "  </outerContainer9>"
				+ "</choice-container>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		checkErrors(response, "/choice-case-test:choice-container/choice-case-test:outerContainer9/choice-case-test:containerWithMandatoryAndWhenChoice/choice-case-test:innerChoice", "Missing mandatory node - innerChoice");
	}

	private void checkErrors(NetConfResponse response, String path, String message){
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals(message, error.getErrorMessage());
		assertEquals(path, error.getErrorPath());
	}
}
