package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Test;
import org.w3c.dom.Element;

public class LeafListDSValidatorTest extends AbstractDataStoreValidatorTest {
	
	@Test
	public void testViolateMinMaxElements() throws ModelNodeInitException {
		// Adding 2 leafList elements
		Element configElement = TestUtil.loadAsXml("/datastorevalidatortest/rangevalidation/defaultxml/leaf-list-range-1.xml");
		getModelNode();
		NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
		assertTrue(response.isOk());

		// Deleting 1 leafList - min is 2 elements
		configElement = TestUtil.loadAsXml("/datastorevalidatortest/rangevalidation/defaultxml/leaf-list-range-2.xml");
		response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
		assertFalse(response.isOk());
		NetconfRpcError netconfRpcError = response.getErrors().get(0);
		assertNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "too-few-elements",
				"Minimum elements required for leaflist-type is 2.", "/validation:validation/validation:leaflist-range/validation:leaflist-type", netconfRpcError);

		// Adding 1 leafList. - max 3 elements
		configElement = TestUtil.loadAsXml("/datastorevalidatortest/rangevalidation/defaultxml/leaf-list-range-3.xml");
		response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
		assertFalse(response.isOk());
		netconfRpcError = response.getErrors().get(0);
		assertNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"too-many-elements",
				"Maximum elements allowed for leaflist-type is 3.", "/validation:validation/validation:leaflist-range/validation:leaflist-type", netconfRpcError);
	}
	
	@Test
	public void testCount() throws ModelNodeInitException {
		// Adding 2 leafList elements
		Element configElement = TestUtil.loadAsXml("/datastorevalidatortest/rangevalidation/defaultxml/leaf-list-range-1.xml");
		getModelNode();
		NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
		assertTrue(response.isOk());
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<value1>7</value1> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml2, true);
	}
	
	public void testFalseCount() throws ModelNodeInitException {
		
	}
	
	@Test
	public void testInvalidCountEquals() throws ModelNodeInitException {
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<value1>7</value1> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());	
		assertEquals("Violate when constraints: /validation/leaflist-range[count(leaflist-type) = 2]", response2.getErrors().get(0).getErrorMessage());
	}
	@Test
	public void testInvalidCount() throws ModelNodeInitException {
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<countable>key7key</countable> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<value2>7</value2> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertFalse(response3.isOk());
		assertEquals("Violate when constraints: count(countable) = 0", response3.getErrors().get(0).getErrorMessage());
		assertNull(response3.getData());

		
	}
	
	@Test
	public void testMultiCountLeafList() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<countable>key7key</countable> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<countable1>key7key</countable1> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<twoLeafList>7</twoLeafList> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertTrue(response3.isOk());
		

		
	}
	@Test
	public void testCurrentCount() throws ModelNodeInitException{
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<countable>key7key</countable> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				 "    <count-validation>												" +
				 "		<valueCurrent>key7key</valueCurrent> 										" +
				 "	  </count-validation> 												" +
				 "</validation>													" ;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml2, true);

		

		
	}
	@Test
	public void testValidLeafListRange() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-leaf-list-range-1.xml");
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-leaf-list-range-2.xml");
	}
	
	
	@Test
	public void testValidInstanceIdentifier() throws ModelNodeInitException{
	    String requestXml1 = TestUtil.loadAsString("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf-list.xml");
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/create-instance-identifier-leaf-list.xml");
	}
	
	@Test
	public void testInvalidInstanceIdentifier() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        
        assertTrue(response1.isOk());
        
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/create-invalid-instance-identifier-leaf-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/classz[class-id = 'Cl001']/number-students", "/validation:validation/validation:instance-identifier-example/validation:leaflist");
	}
	
	@Test
	public void testViolateWhenConditions() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaflist.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        
        assertTrue(response1.isOk());
        
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaflist.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: ../result-leaflist >= 5 and ../result-leaflist <= 10", "/validation:validation/validation:when-validation/validation:leaflist-type");
	}
	
	@Test
	public void testValidWhenConditions() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        
        assertTrue(response1.isOk());
	    
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaflist.xml");
	}
	
	@Test
	public void testValidMustConstraint() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-leaflist.xml");
	}
	
	@Test
	public void testViolateMustConstraint() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-leaflist.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
				"An ifEntry must be success or in-progress", "/validation:validation/validation:must-validation/validation:interface/validation:ifEntry");
	}
	
	@Test
	public void testMandatoryLeafListPresenceContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container>"
                + "   <leafValidation>"
                + "     <leaf1>0</leaf1>"
                + "   </leafValidation>"
                + "   <leafListValidation/>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Minimum elements required for leafList is 1.", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:leafListValidation/validation:leafList",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container>"
                + "   <leafValidation>"
                + "     <leaf1>0</leaf1>"
                + "   </leafValidation>"
                + "   <leafListValidation>"
                + "    <leafList>0</leafList>"
                + "  </leafListValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Minimum elements required for leafList1 is 1.", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/validation:validation/validation:mandatory-validation-container/validation:leafListValidation/validation:innerContainer/validation:leafList1",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container>"
                + "   <leafValidation>"
                + "     <leaf1>0</leaf1>"
                + "   </leafValidation>"
                + "   <leafListValidation>"
                + "    <leafList>0</leafList>"
                + "    <innerContainer>"
                + "     <leafList1>0</leafList1>"
                + "    </innerContainer>"                
                + "  </leafListValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
                ;
        
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =  "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:mandatory-validation-container>"
                + "    <validation:leafListValidation>"
                + "     <validation:innerContainer>"
                + "      <validation:leafList1>0</validation:leafList1>"
                + "     </validation:innerContainer>"
                + "     <validation:leafList>0</validation:leafList>"
                + "    </validation:leafListValidation>"
                + "    <validation:leafValidation>"
                + "     <validation:leaf1>0</validation:leaf1>"
                + "    </validation:leafValidation>"
                + "   </validation:mandatory-validation-container>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;

        verifyGet(response);

	}
	
	@Test
	public void testViolateWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaflist-casenode-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        
        assertTrue(response1.isOk());
	    
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaflist-casenode-1.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: validation:result-choice = 'success'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:leaflist-case-success");
        
        requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaflist-casenode-2.xml";
        getModelNode();
        request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaflist-casenode-2.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: validation:result-choice = 'failed'", "/validation:validation/validation:when-validation/validation:choicecase/validation:leaflist-case-failed");
	}
	
    @Test
    public void testForDuplicateLeafList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "   <dummy>10</dummy>"
                + "   <dummy>10</dummy>"                
                + "</validation>"
                ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);

        assertEquals("data-not-unique", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("/validation/dummy", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Duplicate elements in node (urn:org:bbf:pma:validation?revision=2015-12-14)dummy",
                ncResponse.getErrors().get(0).getErrorMessage());

    }
    
    @Test
    public void testForDuplicateLeafListValues() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "   <dummy>10</dummy>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "   <dummy xc:operation='create'>10</dummy>"
                + "</validation>"
                ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Create instance attempted while the instance - dummy = 10 already exists; Request Failed.",
                response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testForParentContainerDeletionWithAttributes() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>10</leaf1>"
                + "  <for-leaf-list>"
                + "   <leafList>0</leafList>"
                + "   <innerContainer>"
                + "    <leaf1>0</leaf1>"
                + "   </innerContainer>"
                + "  </for-leaf-list>" 
                + "  <for-leaf-list1>"
                + "   <leafList>0</leafList>"
                + "   <leafList1>0</leafList1>"
                + "   <innerContainer>"
                + "    <leaf1>0</leaf1>"
                + "   </innerContainer>"
                + "  </for-leaf-list1>" 
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>0</leaf1>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:validate-parent-container-on-when-deletion>"
                + "    <validation:for-leaf-list>"
                + "     <validation:leafList>0</validation:leafList>"
                + "    </validation:for-leaf-list>"
                + "    <validation:for-leaf-list1>"
                + "     <validation:leafList>0</validation:leafList>"
                + "    <validation:leafList1>0</validation:leafList1>"
                + "   </validation:for-leaf-list1>"
                + "   <validation:leaf1>0</validation:leaf1>"
                + "  </validation:validate-parent-container-on-when-deletion>"
                + " </validation:validation>"
                + "</data>"
                + "</rpc-reply>"
               ;                
        verifyGet(response);
       
    }
	
	//@Test
	public void testValidWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist-casenode-1.xml");	
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist-casenode-2.xml");	
	}
	
	@Test
	public void testLeafListAdditionWithWhenConstraint() throws ModelNodeInitException {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-list-add-validation>"
                + "   <configured-mode>vdsl</configured-mode>"
                + " </leaf-list-add-validation>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-list-add-validation>"
                + "   <configured-mode>fast</configured-mode>"
                + "   <fast-leaf>test</fast-leaf>"
                + " </leaf-list-add-validation>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
		
	}
	
	/**
	 * This UT for validating combination of when & mandatory constraints
	 * @throws ModelNodeInitException
	 */
	@Test
	public void testLeafWhenwithMandatoryConstraint() throws ModelNodeInitException {
		getModelNode();
		// Valid request for when-mandatory validation
        String validRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>1</key>"
                + "			<operator>contains</operator>"
                + "			<value>value</value>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
		editConfig(m_server, m_clientInfo, validRequestXml, true);
	}
	
	public void testLeafWhenwithMandatoryConstraint_MissedMandatoryLeaf() throws ModelNodeInitException {
		getModelNode();
		
		// Invalid request for when-mandatory validation and checking the error messages
        String invalidRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>2</key>"
                + "			<operator>contains</operator>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, invalidRequestXml, false);
		assertEquals("Missing mandatory node", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:when-validation-container/validation:list1[validation:key=2]/validation:value",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
		
	}
	
	@Test
	public void testLeafWhenwithMandatoryConstraint_WhenViolation() throws ModelNodeInitException {
		getModelNode();
        String invalidRequestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>3</key>"
                + "			<operator>in</operator>"
                + "			<value>value</value>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, invalidRequestXml1, false);
		assertEquals("Violate when constraints: ../operator != 'in'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:when-validation-container/validation:list1[validation:key='3']/validation:value",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, ncResponse.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", ncResponse.getErrors().get(0).getErrorAppTag());
	}
	
	/**
	 * This UT for validating combination of when & min-elements constraints
	 * @throws ModelNodeInitException
	 */
	@Test
	public void testLeafListWhenwithMinElementsConstraint() throws ModelNodeInitException {
		getModelNode();
		
		// Valid request for when & min-elements validations
        String validRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>1</key>"
                + "			<operator>in</operator>"
                + "			<values>value1</values>"
                + "			<values>value2</values>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
        
		editConfig(m_server, m_clientInfo, validRequestXml, true);
	}
	
	@Test
	public void testWhenwithMinElementsConstraint_MissedMinElementsLeafList() throws ModelNodeInitException {
		getModelNode();
        String invalidRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>1</key>"
                + "			<operator>in</operator>"
                + "			<values>value1</values>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, invalidRequestXml, false);
		assertEquals("Minimum elements required for values is 2.",
				ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:when-validation-container/validation:list1[validation:key='1']/validation:values",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
		assertEquals("too-few-elements", ncResponse.getErrors().get(0).getErrorAppTag());
	}
	
	@Test
	public void testWhenwithMinElementsConstraint_NonExistsMinElementLeafList() throws ModelNodeInitException {
		getModelNode();
		String invalidRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>1</key>"
                + "			<operator>in</operator>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
        
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, invalidRequestXml, false);
		assertEquals("Minimum elements required for values is 2.",
				ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:when-validation-container/validation:list1[validation:key='1']/validation:values",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
		assertEquals("too-few-elements", ncResponse.getErrors().get(0).getErrorAppTag());
	}
	
	@Test
	public void testWhenwithMinElementsConstraint_WhenViolation() throws ModelNodeInitException {
		getModelNode();
        String invalidRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <when-validation-container>"
                + "		<list1>"
                + "			<key>1</key>"
                + "			<operator>contains</operator>"
                + "			<values>value</values>"
                + "		</list1>"
                + " </when-validation-container>"
                + "</validation>"
                ;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, invalidRequestXml, false);
		assertEquals("Violate when constraints: ../operator = 'in'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:when-validation-container/validation:list1[validation:key='1']/validation:values",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, ncResponse.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", ncResponse.getErrors().get(0).getErrorAppTag());
	}
}
