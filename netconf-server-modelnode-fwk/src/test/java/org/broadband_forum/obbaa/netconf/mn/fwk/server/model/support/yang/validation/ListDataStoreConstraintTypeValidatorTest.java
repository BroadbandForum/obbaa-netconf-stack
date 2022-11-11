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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class ListDataStoreConstraintTypeValidatorTest extends AbstractDataStoreValidatorTest {
	
	@Test
	public void testViolateMaxElements() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/list-range-1.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "too-many-elements",
				"Maximum elements allowed for list-range is 2.", "/validation:validation/validation:list-range");
	}
	
	@Test
	public void testViolateWhenConditions() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
	    
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: ../result-list = 10", "/validation:validation/validation:when-validation/validation:list-type" +
						"[validation:list-id='1']");
	}
	
	@Test
	public void testValidWhenConditions() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
	    
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list.xml");
	}
	
	@Test
	public void testValidMustConstraint() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
        
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-must-constraint-list.xml");
	}
	
	@Test
	public void testViolateMustConstraint() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-list.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
				"data-value must be  64 .. 10000", "/validation:validation/validation:must-validation/validation:interface/validation:data[validation:data-id='3']");
	}
	
	@Test
	public void testViolateWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container-choicenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-list-casenode.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'failed'", "/validation:validation/validation:when-validation/validation:choicecase/validation:list-case-failed[validation:failed-id='1']");
	}

	
	@Test
	public void testValidWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list-casenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
		
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list-casenode.xml");		
	}
	
	@Test
    public void testWhenOnList() throws Exception {
	    getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                             "  <validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                             "      <arithmetic-validation> "+
                             "          <value1>15</value1> "+
                             "              <whenOnList>"+
                             "                  <key1>1</key1>"+
                             "              </whenOnList>"+
                             "              <whenOnList>"+
                             "                  <key1>2</key1>"+
                             "              </whenOnList>"+
                             "      </arithmetic-validation> "+
                             "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        String exptectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "<validation:arithmetic-validation>\n" + 
                "<validation:value1>15</validation:value1>\n" + 
                "<validation:whenOnList>\n" + 
                "<validation:key1>1</validation:key1>\n" + 
                "</validation:whenOnList>\n" + 
                "<validation:whenOnList>\n" + 
                "<validation:key1>2</validation:key1>\n" + 
                "</validation:whenOnList>\n" + 
                "</validation:arithmetic-validation>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(exptectedResponse);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "  <validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "      <arithmetic-validation> "+
                "          <value1>5</value1> "+ // Making it 5 to fail the when on list
                "      </arithmetic-validation> "+
                "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        exptectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "<validation:arithmetic-validation>\n" + 
                "<validation:value1>5</validation:value1>\n" + 
                "</validation:arithmetic-validation>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(exptectedResponse);
    }

	@Test
	public void testUniquenessOfDescendentNodesDirect() throws ModelNodeInitException {
		getModelNode();
		String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<descendent-unique-direct>"
				+    "<id>1</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf>hakuna matata</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "<descendent-unique-direct>"
				+    "<id>2</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf>hakuna</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<descendent-unique-direct>"
				+    "<id>3</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf>hakuna matata</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes="
				+ "{RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueTarget, "
				+ "(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueLeaf]}=hakuna matata}]");

	}

	@Test
	public void testUniquenessOfDescendentNodesInEditOperationWithDefaultEditAsMerge() throws ModelNodeInitException {
		getModelNode();
		String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<descendent-unique-direct>"
				+    "<id>1</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf>hakuna matata</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "<descendent-unique-direct>"
				+    "<id>2</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf>hakuna</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<descendent-unique-direct>"
				+    "<id>2</id>"
				+    "<uniqueTarget>"
				+    "<uniqueLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">hakuna matata</uniqueLeaf>"
				+    "</uniqueTarget>"
				+ "</descendent-unique-direct>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes="
				+ "{RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueTarget, "
				+ "(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueLeaf]}=hakuna matata}]");
	}

	@Test
    public void testUniquenessOfDescendentNodesInEditOperationWithDefaultEditAsNone() throws ModelNodeInitException {
	    getModelNode();
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-direct>"
                +    "<id>1</id>"
                +    "<uniqueTarget>"
                +    "<uniqueLeaf>hakuna matata</uniqueLeaf>"
                +    "</uniqueTarget>"
                + "</descendent-unique-direct>"
                + "<descendent-unique-direct>"
                +    "<id>2</id>"
                +    "<uniqueTarget>"
                +    "<uniqueLeaf>hakuna</uniqueLeaf>"
                +    "</uniqueTarget>"
                + "</descendent-unique-direct>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-direct>"
                +    "<id>2</id>"
                +    "<uniqueTarget>"
                +    "<uniqueLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">hakuna matata</uniqueLeaf>"
                +    "</uniqueTarget>"
                + "</descendent-unique-direct>"
                + "</validation>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false, EditConfigDefaultOperations.NONE);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes="
                + "{RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueTarget, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueLeaf]}=hakuna matata}]");
        
    }
	
	private void validateUniqueConstraint(NetconfRpcError error, String errorMsg){
	    assertEquals("data-not-unique", error.getErrorAppTag());
	    assertEquals(errorMsg, error.getErrorMessage());
	}
	
	@Test
    public void testUniquenessOfNodesInChoice() throws ModelNodeInitException {
        getModelNode();
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-choice>"
                +    "<id>3</id>"
                +    "<pumbaa>hakuna</pumbaa>"
                +    "<timon>matata</timon>"
                + "</unique-with-choice>"
                + "<unique-with-choice>"
                +    "<id>4</id>"
                +    "<pumbaa>hakuna</pumbaa>"
                +    "<timon>hakuna</timon>"
                + "</unique-with-choice>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-choice>"
                +    "<id>1</id>"
                +    "<pumbaa>hakuna</pumbaa>"
                +    "<timon>matata</timon>"
                + "</unique-with-choice>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={"
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, (urn:org:bbf2:pma:validation?revision=2015-12-14)pumbaa]}=hakuna, "
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, (urn:org:bbf2:pma:validation?revision=2015-12-14)timon]}=matata}]");
    }

	@Test
	public void testUniquenessOfNodesInChoiceOnEdit() throws ModelNodeInitException {
		getModelNode();
		String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<unique-with-choice>"
				+    "<id>3</id>"
				+    "<pumbaa>hakuna</pumbaa>"
				+    "<timon>matata</timon>"
				+ "</unique-with-choice>"
				+ "<unique-with-choice>"
				+    "<id>4</id>"
				+    "<pumbaa>hakuna</pumbaa>"
				+    "<timon>hakuna</timon>"
				+ "</unique-with-choice>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<unique-with-choice>"
				+    "<id>4</id>"
				+    "<timon xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">matata</timon>"
				+ "</unique-with-choice>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={"
				+ "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
				+ "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, (urn:org:bbf2:pma:validation?revision=2015-12-14)pumbaa]}=hakuna, "
				+ "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
				+ "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, (urn:org:bbf2:pma:validation?revision=2015-12-14)timon]}=matata}]");
	}
	
	@Test
    public void testUniquenessOfNodesInNestedChoice() throws ModelNodeInitException {
        getModelNode();
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-with-nested-choice>"
                +    "<id>1</id>"
                +    "<mufasa>hakuna</mufasa>"
                + " <innerContainer>"
                +    "<simba>matata</simba>"
                + " </innerContainer>"
                + "</descendent-unique-with-nested-choice>"
                + "<descendent-unique-with-nested-choice>"
                +    "<id>2</id>"
                +    "<mufasa>hakuna</mufasa>"
                + " <innerContainer>"
                +    "<simba>hakuna</simba>"
                + " </innerContainer>"
                + "</descendent-unique-with-nested-choice>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-with-nested-choice>"
                +    "<id>3</id>"
                +    "<mufasa>hakuna</mufasa>"
                + " <innerContainer>"
                +    "<simba>matata</simba>"
                + " </innerContainer>"
                + "</descendent-unique-with-nested-choice>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=["
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)outerChoice, (urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)innerChoice, (urn:org:bbf2:pma:validation?revision=2015-12-14)innerCase1, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)innerContainer, (urn:org:bbf2:pma:validation?revision=2015-12-14)simba]}=matata, "
                
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)outerChoice, (urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)mufasa]}=hakuna}]");
    }
	
	@Test
    public void testUniquenessOfNodesWithDefaults() throws ModelNodeInitException {
        getModelNode();
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-default>"
                +    "<id>1</id>"
                +    "<zazu>report</zazu>"
                +    "<rafiki>safeguard</rafiki>"
                + "</unique-with-default>"
                + "<unique-with-default>"
                +    "<id>2</id>"
                +    "<zazu>bird</zazu>"
                +    "<rafiki>arboreal</rafiki>"
                + "</unique-with-default>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-default>"
                +    "<id>3</id>"
                +    "<rafiki>Chinese Zodiac</rafiki>"
                + "</unique-with-default>"
                + "<unique-with-default>"
                +    "<id>4</id>"
                +    "<rafiki>Chinese Zodiac</rafiki>"
                + "</unique-with-default>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=["
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)rafiki]}=Chinese Zodiac, RelativeSchemaPath{path=["
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)zazu]}=fly}]");
    }
	
	@Test
    public void testUniquenessOfNodesWithDefaults2() throws ModelNodeInitException {
        getModelNode();
        
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-default>"
                +    "<id>5</id>"
                + "</unique-with-default>"
                + "<unique-with-default>"
                +    "<id>6</id>"
                + "</unique-with-default>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=["
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)rafiki]}=jump, "
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)zazu]}=fly}]");
	}
	
	@Test
    public void testUniquenessOfDescendentNodesInChoice() throws ModelNodeInitException {
        getModelNode();
        String requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-with-choice>"
                +    "<id>1</id>"
                +    "<mufasa>hakuna</mufasa>"
                +    "<uniqueTargetInChoice>"
                +    "<scar>matata</scar>"
                +    "</uniqueTargetInChoice>"
                + "</descendent-unique-with-choice>"
                + "<descendent-unique-with-choice>"
                +    "<id>2</id>"
                +    "<mufasa>hakuna</mufasa>"
                +    "<uniqueTargetInChoice>"
                +    "<scar>hakuna</scar>"
                +    "</uniqueTargetInChoice>"
                + "</descendent-unique-with-choice>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<descendent-unique-with-choice>"
                +    "<id>3</id>"
                +    "<mufasa>hakuna</mufasa>"
                +    "<uniqueTargetInChoice>"
                +    "<scar>matata</scar>"
                +    "</uniqueTargetInChoice>"
                + "</descendent-unique-with-choice>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        validateUniqueConstraint(error, "value already present: UniqueConstraintCheck [m_attributes={"
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)mufasa]}=hakuna, "
                + "RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)justChoice, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)justCase1, (urn:org:bbf2:pma:validation?revision=2015-12-14)uniqueTargetInChoice, "
                + "(urn:org:bbf2:pma:validation?revision=2015-12-14)scar]}=matata}]");
    }

	@Test
	public void testUniqueConstraintViolationException() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
		String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<single-unique>"
				+    "<id>1</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>2</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>3</id>"
				+    "<value>value2</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>4</id>"
				+    "<value>value2</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "</validation>";
		Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);
		String expected ="<error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
		        +"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:single-unique[validation:id='3']/validation:single-unique</non-unique>"
		        +"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:single-unique[validation:id='4']/validation:single-unique</non-unique>"
		        +"</error-info>";
		Element expectedErrorInfo = TestUtil.transformToElement(expected);
		testFailForUniqueType(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
				"value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)value]}=value2}]", "/validation:validation/validation:single-unique",expectedErrorInfo);
	}

    @Test
    public void testDefaultUniqueConstraintViolationException() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
        String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<single-unique>"
                +    "<id>1</id>"
                +    "<value>value1</value>"
                +    "<status>ok</status>"
                + "</single-unique>"
                + "<single-unique>"
                +    "<id>2</id>"
                +    "<value>value6</value>"
                +    "<status>ok</status>"
                + "</single-unique>"
                + "<single-unique>"
                +    "<id>3</id>"
                +    "<status>ok</status>"
                + "</single-unique>"
                + "</validation>";
        Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);
        String expected ="<error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:single-unique[validation:id='3']/validation:single-unique</non-unique>"
                + "</error-info>";
        Element expectedErrorInfo = TestUtil.transformToElement(expected);
                testFailForUniqueType(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
                "value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)value]}=value6}]", "/validation:validation/validation:single-unique",expectedErrorInfo);
    }
    
    @Test
    public void testMultiUniqueConstraintValidation_ConstraintViolation() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
    	String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
    			+ "<multi-unique-list>"
    			+    "<id>1</id>"
    			+    "<value>value1</value>"
    			+    "<status>ok</status>"
    			+ "</multi-unique-list>"
    			+ "<multi-unique-list>"
    			+    "<id>2</id>"
    			+    "<status>Nok</status>"
    			+ "</multi-unique-list>"
    			+ "<multi-unique-list>"
    			+    "<id>3</id>"
    			+    "<value>value6</value>"
    			+    "<status>Nok</status>"
    			+ "</multi-unique-list>"
    			+ "</validation>";
    	Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);

    	String expected ="<error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:multi-unique-list[validation:id='3']/validation:multi-unique-list</non-unique>"
    			+"</error-info>";
    	Element expectedErrorInfo = TestUtil.transformToElement(expected);
    	testFailForUniqueType(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
    			NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
    			"value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)status]}=Nok, RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)value]}=value6}]",
    			"/validation:validation/validation:multi-unique-list",expectedErrorInfo);
    }

    @Test
    public void testMultiUniqueConstraintViolation_UniqueConstaintViolation1() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
    	// create list with ipv4 container
    	String editRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
    			+ "<multi-unique-list>"
    			+    "<id>1</id>"
    			+    "<value>value1</value>"
    			+    "<status>ok</status>"
    			+    "<ip-version>ipv4</ip-version>"
    			+		"<ipv4>"
    			+			"<source-ipv4-address>2.2.2.2</source-ipv4-address>"
    			+			"<group-ipv4-address>15.15.15.15</group-ipv4-address>"
    			+		"</ipv4>"
    			+ "</multi-unique-list>"
    			+ "<multi-unique-list>"
    			+    "<id>2</id>"
    			+    "<status>Nok</status>"
    			+    "<ip-version>ipv4</ip-version>"
    			+		"<ipv4>"
    			+			"<source-ipv4-address>2.2.2.2</source-ipv4-address>"
    			+		"</ipv4>"
    			+ "</multi-unique-list>"
    			+ "</validation>";
    	Element editRequestElement = TestUtil.transformToElement(editRequest);
    	testPass(editRequestElement);

    	// verify the GET request
    	String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
    			"<data>"+
    			"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
    			"	<validation:multi-unique-list>"+
    			"		<validation:id>1</validation:id>"+
    			"		<validation:ip-version>ipv4</validation:ip-version>"+
    			"		<validation:ipv4>"+
    			"			<validation:group-ipv4-address>15.15.15.15</validation:group-ipv4-address>"+
    			"			<validation:source-ipv4-address>2.2.2.2</validation:source-ipv4-address>"+
    			"		</validation:ipv4>"+
    			"		<validation:status>ok</validation:status>"+
    			"		<validation:value>value1</validation:value>"+
    			"	</validation:multi-unique-list>"+
    			"	<validation:multi-unique-list>"+
    			"		<validation:id>2</validation:id>"+
    			"		<validation:ip-version>ipv4</validation:ip-version>"+
    			"		<validation:ipv4>"+
    			"			<validation:group-ipv4-address>10.10.10.10</validation:group-ipv4-address>"+ // default-leaf was created
    			"			<validation:source-ipv4-address>2.2.2.2</validation:source-ipv4-address>"+
    			"		</validation:ipv4>"+
    			"		<validation:status>Nok</validation:status>"+
    			"		<validation:value>value6</validation:value>"+ // default leaf was created
    			"	</validation:multi-unique-list>"+
    			"  </validation:validation>"+
    			"</data>"+
    			"</rpc-reply>";
    	verifyGet(expectedOutput);

    	// try to create list with ipv4 container (duplicate values which was exist in another list entry)
    	editRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
    			+ "<multi-unique-list>"
    			+    "<id>3</id>"
    			+    "<ip-version>ipv4</ip-version>"
    			+		"<ipv4>"
    			+			"<source-ipv4-address>2.2.2.2</source-ipv4-address>"
    			+			"<group-ipv4-address>10.10.10.10</group-ipv4-address>"
    			+		"</ipv4>"
    			+ "</multi-unique-list>"
    			+ "</validation>";
    	editRequestElement = TestUtil.transformToElement(editRequest);

    	// validate unique constraint violation error messages
    	String expected ="<error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:multi-unique-list[validation:id='3']/validation:multi-unique-list</non-unique>"
    			+"</error-info>";
    	Element expectedErrorInfo = TestUtil.transformToElement(expected);
    	testFailForUniqueType(editRequestElement, NetconfRpcErrorTag.OPERATION_FAILED,
    			NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
    			"value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)ipv4, (urn:org:bbf2:pma:validation?revision=2015-12-14)group-ipv4-address]}=10.10.10.10, RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)ipv4, (urn:org:bbf2:pma:validation?revision=2015-12-14)source-ipv4-address]}=2.2.2.2}]",
    			"/validation:validation/validation:multi-unique-list",expectedErrorInfo);
    }

    @Test
    public void testMultiUniqueConstraintValidation_UniqueConstaintViolation() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
    	// create list with ipv6 container
    	String editRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
    			+ "<multi-unique-list>"
    			+    "<id>1</id>"
    			+    "<value>value1</value>"
    			+    "<status>ok</status>"
    			+    "<ip-version>ipv6</ip-version>"
    			+		"<ipv6>"
    			+			"<source-ipv6-address>1:1:1:1:1:1:1:1</source-ipv6-address>"
    			+			"<group-ipv6-address>10:10:10:10:10:10:10:10</group-ipv6-address>"
    			+		"</ipv6>"
    			+ "</multi-unique-list>"
    			+ "<multi-unique-list>"
    			+    "<id>2</id>"
    			+    "<status>Nok</status>"
    			+    "<ip-version>ipv6</ip-version>"
    			+		"<ipv6>"
    			+			"<source-ipv6-address>2:2:2:2:2:2:2:2</source-ipv6-address>"
    			+			"<group-ipv6-address>0:0:0:0:0:0:0:0</group-ipv6-address>"
    			+		"</ipv6>"
    			+ "</multi-unique-list>"
    			+ "</validation>";
    	Element editRequestElement = TestUtil.transformToElement(editRequest);
    	testPass(editRequestElement);

    	// verify the GET request
    	String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
    			"<data>"+
    			"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
    			"	<validation:multi-unique-list>"+
    			"		<validation:id>1</validation:id>"+
    			"		<validation:ip-version>ipv6</validation:ip-version>"+
    			"		<validation:ipv6>"+
    			"			<validation:group-ipv6-address>10:10:10:10:10:10:10:10</validation:group-ipv6-address>"+
    			"			<validation:source-ipv6-address>1:1:1:1:1:1:1:1</validation:source-ipv6-address>"+
    			"		</validation:ipv6>"+
    			"		<validation:status>ok</validation:status>"+
    			"		<validation:value>value1</validation:value>"+
    			"	</validation:multi-unique-list>"+
    			"	<validation:multi-unique-list>"+
    			"		<validation:id>2</validation:id>"+
    			"		<validation:ip-version>ipv6</validation:ip-version>"+
    			"		<validation:ipv6>"+
    			"			<validation:group-ipv6-address>0:0:0:0:0:0:0:0</validation:group-ipv6-address>"+ 
    			"			<validation:source-ipv6-address>2:2:2:2:2:2:2:2</validation:source-ipv6-address>"+
    			"		</validation:ipv6>"+
    			"		<validation:status>Nok</validation:status>"+
    			"		<validation:value>value6</validation:value>"+ // default leaf was created
    			"	</validation:multi-unique-list>"+
    			"  </validation:validation>"+
    			"</data>"+
    			"</rpc-reply>";
    	verifyGet(expectedOutput);

    	// try to create list with ipv6 container (duplicate values which was exist in another list entry)
    	editRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
    			+ "<multi-unique-list>"
    			+    "<id>3</id>"
    			+    "<ip-version>ipv6</ip-version>"
    			+		"<ipv6>"
    			+			"<source-ipv6-address>2:2:2:2:2:2:2:2</source-ipv6-address>" // here default leaf 'group-ipv6-address' will be created with value "0:0:0:0:0:0:0:0"
    			+		"</ipv6>"
    			+ "</multi-unique-list>"
    			+ "</validation>";
    	editRequestElement = TestUtil.transformToElement(editRequest);

    	// validate unique constraint violation error messages
    	String expected ="<error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:multi-unique-list[validation:id='3']/validation:multi-unique-list</non-unique>"
    			+"</error-info>";
    	Element expectedErrorInfo = TestUtil.transformToElement(expected);
    	testFailForUniqueType(editRequestElement, NetconfRpcErrorTag.OPERATION_FAILED,
    			NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
    			"value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)ipv6, (urn:org:bbf2:pma:validation?revision=2015-12-14)group-ipv6-address]}=0:0:0:0:0:0:0:0, RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)ipv6, (urn:org:bbf2:pma:validation?revision=2015-12-14)source-ipv6-address]}=2:2:2:2:2:2:2:2}]",
    			"/validation:validation/validation:multi-unique-list",expectedErrorInfo);
    }

    @Test
    public void testMandatoryUniqueConstraintViolationException() throws ModelNodeInitException {
        String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<unique-with-mandatory>"
                +    "<id>1</id>"
                +    "<value>value1</value>"
                +    "<status>ok</status>"
                + "</unique-with-mandatory>"
                + "<unique-with-mandatory>"
                +    "<id>2</id>"
                +    "<value>value2</value>"
                +    "<status>ok</status>"
                + "</unique-with-mandatory>"
                + "<unique-with-mandatory>"
                +    "<id>3</id>"
                +    "<status>Nok</status>"
                + "</unique-with-mandatory>"
                + "</validation>";
        Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);

        testFail(singleUniqueElement, NetconfRpcErrorTag.DATA_MISSING,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                "Missing mandatory node - value", "/validation:validation/validation:unique-with-mandatory[validation:id='3']/validation:value");
    }

	@Test
	public void testForDuplicateKeyinList() throws ModelNodeInitException {
		String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<single-unique>"
				+    "<id>2</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>1</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>3</id>"
				+    "<value>value3</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>1</id>"
				+    "<value>value4</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "</validation>";
		Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);

		testFail(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, EditContainmentNode.DATA_NOT_UNIQUE,
				EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf2:pma:validation?revision=2015-12-14)single-unique", "/validation/single-unique[id='1']");
	}

	@Test
	public void testMultiUniqueConstraintViolationException() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {

		String multiUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<multi-unique>"
				+    "<id>1</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>2</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>3</id>"
				+    "<value>value3</value>"
				+    "<status>Nok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>4</id>"
				+    "<value>value3</value>"
				+    "<status>Nok</status>"
				+ "</multi-unique>"
				+ "</validation>";
		Element multiUniqueElement = TestUtil.transformToElement(multiUniqueRequest);
        String expected =" <error-info xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"<non-unique xmlns=\"urn:ietf:params:xml:ns:yang:1\">/validation:validation/validation:multi-unique[validation:id='4']/validation:multi-unique</non-unique>"
                +"</error-info>";
        Element expectedErrorInfo = TestUtil.transformToElement(expected);

		testFailForUniqueType(multiUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "data-not-unique",
				"value already present: UniqueConstraintCheck [m_attributes={RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)status]}=Nok, RelativeSchemaPath{path=[(urn:org:bbf2:pma:validation?revision=2015-12-14)value]}=value3}]", "/validation:validation/validation:multi-unique", expectedErrorInfo);
	}

    @Test
    public void testValidMultiUniqueConstraintWithOutReferencedLeaf() throws ModelNodeInitException {
        String multiUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<multi-unique>"
                +    "<id>1</id>"
                +    "<value>value1</value>"
                +    "<status>ok</status>"
                + "</multi-unique>"
                + "<multi-unique>"
                +    "<id>2</id>"
                +    "<value>value2</value>"
                + "</multi-unique>"
                + "<multi-unique>"
                +    "<id>3</id>"
                +    "<value>value2</value>"
                + "</multi-unique>"
                + "<multi-unique>"
                +    "<id>4</id>"
                +    "<value>value4</value>"
                + "</multi-unique>"
                + "</validation>";
        Element multiUniqueElement = TestUtil.transformToElement(multiUniqueRequest);
        testPass(multiUniqueElement);
    }

	@Test
	public void testValidValidUniqueConstraint() throws ModelNodeInitException {
		String request = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<single-unique>"
				+    "<id>1</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>2</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>3</id>"
				+    "<value>value3</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>4</id>"
				+    "<value>value4</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "</validation>";

		Element editRequestElement = TestUtil.transformToElement(request);              
		testPass(editRequestElement);           
	}

	@Test
	public void testValidMultiUniqueConstraintViolationException() throws ModelNodeInitException {

		String multiUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<multi-unique>"
				+    "<id>1</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>2</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>3</id>"
				+    "<value>value3</value>"
				+    "<status>Nok</status>"
				+ "</multi-unique>"
				+ "<multi-unique>"
				+    "<id>4</id>"
				+    "<value>value2</value>"
				+    "<status>Nok</status>"
				+ "</multi-unique>"
				+ "</validation>";
		Element multiUniqueElement = TestUtil.transformToElement(multiUniqueRequest);

		testPass(multiUniqueElement);           
	}
	
	@Test
    public void testMissingKeyExceptionInListHavingMust() throws ModelNodeInitException {

	    //key is not given
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<listWithMust>"
                +    "<one>dummy1</one>"
                +    "<two>dummy2</two>"
                + "</listWithMust>"
                + "</validation>";
        Element requestElement = TestUtil.transformToElement(requestInString);

        testFail(requestElement, NetconfRpcErrorTag.MISSING_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, null,
                "Expected list key(s) [id] is missing", "/validation:validation/validation:listWithMust");
        
        //key is given
        requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<listWithMust>"
                +    "<id>1</id>"
                +    "<one>dummy1</one>"
                +    "<two>dummy2</two>"
                + "</listWithMust>"
                + "</validation>";
        requestElement = TestUtil.transformToElement(requestInString);

        testPass(requestElement);
    }

    @Test
    public void testDefaultViolateMaxElements() throws ModelNodeInitException {
        String requestInString =  "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<list-range>"
                +        "<id>1</id>"
                +        "<value>value 1</value>"
                +    "</list-range>"
                +    "<list-range>"
                +        "<id>2</id>"
                +        "<value>value 2</value>"
                +    "</list-range>"
                +    "<list-range>"
                +        "<id>3</id>"
                +    "</list-range>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testFail(requestElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "too-many-elements",
                "Maximum elements allowed for list-range is 2.", "/validation:validation/validation:list-range");
    }

    @Test
    public void testDefaultValidMinElements() throws ModelNodeInitException {
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<list-range-with-min>"
                +        "<id>1</id>"
                +        "<inner-list>"
                +            "<index>index1</index>"
                +             "<value>value 1</value>"
                +        "</inner-list>"
                +        "<inner-list>"
                +            "<index>index2</index>"
                +        "</inner-list>"
                +    "</list-range-with-min>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testPass(requestElement);
    }


    @Test
    public void testDefaultViolateMinElements() throws ModelNodeInitException {
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<list-range-with-min>"
                +        "<id>1</id>"
                +        "<inner-list>"
                +            "<index>index1</index>"
                +        "</inner-list>"
                +    "</list-range-with-min>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testFail(requestElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "too-few-elements",
                "Minimum elements required for inner-list is 2.", "/validation:validation/validation:list-range-with-min[validation:id='1']/validation:inner-list");
    }


    @Test
    public void testDefaultValidMaxElements() throws ModelNodeInitException {
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<list-range>"
                +        "<id>1</id>"
                +        "<value>value 1</value>"
                +    "</list-range>"
                +    "<list-range>"
                +        "<id>2</id>"
                +    "</list-range>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testPass(requestElement);
    }

    @Test
    public void testValidMultiUniqueWithDefaultAndMandatory() throws ModelNodeInitException {
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<multi-unique-with-default-and-mandatory>"
                +        "<id>1</id>"
                +        "<value>value4</value>"
                +        "<test>yes</test>"
                +    "</multi-unique-with-default-and-mandatory>"
                +    "<multi-unique-with-default-and-mandatory>"
                +        "<id>2</id>"
                +        "<value>value4</value>"
                +        "<status>Nok</status>"
                +        "<test>no</test>"
                +    "</multi-unique-with-default-and-mandatory>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testPass(requestElement);
    }

    @Test
    public void testViolateWhenInMultiUniqueWithDefaultAndMandatory() throws ModelNodeInitException {
        String requestInString = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<multi-unique-with-default-and-mandatory>"
                +        "<id>1</id>"
                +        "<value>value4</value>"
                +        "<test>yes</test>"
                +    "</multi-unique-with-default-and-mandatory>"
                +    "<multi-unique-with-default-and-mandatory>"
                +        "<id>2</id>"
                +        "<value>value3</value>"
                +        "<status>Nok</status>"
                +        "<test>no</test>"
                +    "</multi-unique-with-default-and-mandatory>"
                +"</validation>"
                ;
        Element requestElement = TestUtil.transformToElement(requestInString);
        testFail(requestElement, NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../value = 'value4'", "/validation:validation/validation:multi-unique-with-default-and-mandatory[validation:id='2']/validation:status");
    }
    @Test
	public void testinvalidxmlinlist1() throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
		String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<single-unique>"
				+    "####<id>1</id>"
				+    "<value>value1</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>2</id>"
				+    "<value>value2</value>"
				+    "<status>ok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>3</id>"
				+    "<value>value2</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "<single-unique>"
				+    "<id>4</id>"
				+    "<value>value2</value>"
				+    "<status>Nok</status>"
				+ "</single-unique>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, singleUniqueRequest, false);
		assertEquals("Invalid XML Syntax reported in an element: single-unique", response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
	}
}
