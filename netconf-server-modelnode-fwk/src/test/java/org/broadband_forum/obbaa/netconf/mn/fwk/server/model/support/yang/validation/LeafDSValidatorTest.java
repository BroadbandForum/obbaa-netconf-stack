package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

public class LeafDSValidatorTest extends AbstractDataStoreValidatorTest {

	@Test
	public void testNotMatchedLeafRefValue() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-leaf-ref.xml", NetconfRpcErrorTag.DATA_MISSING,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
				"Dependency violated, 'ABC' must exist", "/validation:validation/validation:leaf-ref/validation:album[validation:name='Album1']/validation:song[validation:name='Circus']/validation:artist-name");
	}

	@Test
	public void testMatchedLeafRefValue() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-ref.xml");
	}

	@Test
	public void testNotMatchedLeafRefValueWithCurrent() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-reference-leaf-ref.xml", NetconfRpcErrorTag.DATA_MISSING,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
				"Dependency violated, 'Invalid_SONG' must exist", "/validation:validation/validation:leaf-ref/validation:music/validation:favourite-song");
	}

	@Test
	public void testMatchedLeafRefValueWithCurrent() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-reference-leaf-ref.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());
	}

	@Test
	public void testAugmentedWhen() throws Exception{

		String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/AugmentedWhen.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity3</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:default-leaf>0</validation11:default-leaf>"
						+ "   </validation11:leaf-ref-yang11>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

	}

	@Test
	public void testChildImpactNodeValidationForList() throws Exception {
		String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation2.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation",
				"Violate when constraints: ../parent-list[current()]/inner-list1[current()]/inner-list2[current()]/type = 'test'",
				"/validation11:validation-yang11/validation11:parent/validation11:when-leaf");

		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation5.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation",
				"Violate when constraints: ../parent-list[current()]/inner-list1[current()]/inner-list2[current()]/type = 'test'",
				"/validation11:validation-yang11/validation11:parent/validation11:when-leaf");

	}

	@Test
	public void testChildImpactValidationForContainers() throws Exception{
		String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation3.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation4.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation",
				"Violate when constraints: ../../child-container1/child-container2/type = 'test'",
				"/validation11:validation-yang11/validation11:parent/validation11:child-container12/validation11:when-leaf2");

		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation6.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation",
				"Violate when constraints: ../parent-list[current()]/inner-list1[current()]/inner-list2[current()]/inner-container1/inner-container2/last-leaf = 'last'",
				"/validation11:validation-yang11/validation11:parent/validation11:when-leaf4");

		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation7.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation",
				"Violate when constraints: ../parent-list[current()]/inner-list1[current()]/inner-list2[current()]/inner-container1/inner-container2/last-leaf = 'last'",
				"/validation11:validation-yang11/validation11:parent/validation11:when-leaf4");

	}

	@Test
	public void testNotLeaf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>abc</validation>" +
				"  <notLeaf>10</notLeaf>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>hello</validation>" +
				"</validation>                                                 " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/validation:validation/validation:notLeaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: not(../validation = 'hello')", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testDeleteofContainer() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				" <when-validation>" +
				"   <result-container>12</result-container>" +
				"   <result-list>10</result-list>" +
				" </when-validation>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-validation>"
						+ "    <validation:result-container>12</validation:result-container>"
						+ "    <validation:result-list>10</validation:result-list>"
						//Below empty container needs to be removed via FNMS-24459
						+ "    <validation:container-type/>"
						+ "   </validation:when-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				" <when-validation>" +
				"   <container-type/>" +
				"   <list-type>" +
				"     <list-id>a</list-id>" +
				"   </list-type>" +
				" </when-validation>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-validation>"
						+ "    <validation:result-container>12</validation:result-container>"
						+ "    <validation:result-list>10</validation:result-list>"
						+ "    <validation:container-type/>"
						+ "    <validation:list-type>"
						+ "     <validation:list-id>a</validation:list-id>"
						+ "    </validation:list-type>"
						+ "   </validation:when-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				" <when-validation>" +
				"   <result-container>9</result-container>" +
				"   <result-list>9</result-list>" +
				" </when-validation>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-validation>"
						+ "    <validation:result-container>9</validation:result-container>"
						+ "    <validation:result-list>9</validation:result-list>"
						+ "   </validation:when-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testContainsLeaf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>hello</validation>" +
				"  <containsLeaf>10</containsLeaf>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>abc</validation>" +
				"</validation>                                                 " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/validation:validation/validation:containsLeaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: contains(../validation,'hello')", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenConstraintLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-validation1>" +
				"  		<when-leaf>hello</when-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</when-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-validation1>"
						+ "    <validation:when-leaf>hello</validation:when-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:when-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-validation1>" +
				"  		<enabled>false</enabled>" +
				"	</when-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-validation1>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "   </validation:when-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(responseXml);
	}

	@Test
	public void testMustConstraintLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<must-validation1>" +
				"  		<must-leaf>hello</must-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</must-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:must-validation1>"
						+ "    <validation:must-leaf>hello</validation:must-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:must-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<must-validation1>" +
				"  		<enabled>false</enabled>" +
				"	</must-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:must-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenConstraintLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-validation1>" +
				"  		<when-leaf>hello</when-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</when-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-validation1/validation:when-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustConstraintLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<must-validation1>" +
				"  		<must-leaf>hello</must-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</must-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:must-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenMandatoryLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-mandatory-validation/validation:when-mandatory-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenMandatoryLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-mandatory-validation>"
						+ "    <validation:when-mandatory-leaf>hello</validation:when-mandatory-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:when-mandatory-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-mandatory-validation>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "   </validation:when-mandatory-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(responseXml);
	}

	@Test
	public void testWhenMandatoryLeafRef() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"		<profiles>"+
				"			<profile>"+
				"				<name>hello</name>"+
				"			</profile>"+
				"		</profiles>"+
				"	</when-mandatory-leafref-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-mandatory-leafref-validation>"
						+ "    <validation:when-mandatory-leaf>hello</validation:when-mandatory-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "    <validation:profiles>"
						+ "    	<validation:profile>"
						+ "    		<validation:name>hello</validation:name>"
						+ "    	</validation:profile>"
						+ "    	</validation:profiles>"
						+ "   </validation:when-mandatory-leafref-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-leafref-validation>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:when-mandatory-leafref-validation>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "    <validation:profiles>"
						+ "    	<validation:profile>"
						+ "    		<validation:name>hello</validation:name>"
						+ "    	</validation:profile>"
						+ "    	</validation:profiles>"
						+ "   </validation:when-mandatory-leafref-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(responseXml);
	}

	@Test
	public void testWhenMandatoryWithLeafRef_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<when-mandatory-leaf>hello1</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"		<profiles>"+
				"			<profile>"+
				"				<name>hello</name>"+
				"			</profile>"+
				"		</profiles>"+
				"	</when-mandatory-leafref-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, response.getErrors().get(0).getErrorTag());
		assertEquals("instance-required", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-mandatory-leafref-validation/validation:when-mandatory-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'hello1' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testDuplicateLeafs() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>hello</validation>" +
				"  <containsLeaf>10</containsLeaf>" +
				"  <containsLeaf>15</containsLeaf>" +
				"</validation>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		String errorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf:pma:validation?revision=2015-12-14)containsLeaf";
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals(errorMsg, error.getErrorMessage());
		assertEquals(EditContainmentNode.DATA_NOT_UNIQUE, error.getErrorAppTag());

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>hello</validation>" +
				"  <containsLeaf>10</containsLeaf>" +
				"  <inner-validation>"+
				"  <containsLeaf>10</containsLeaf>" +
				"  </inner-validation>"+
				"</validation>" ;
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
	}


	@Test
	public void testImpactLeafRefOnDelete() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<validation>abc</validation>" +
				"  <leafref-validation>abc</leafref-validation>" +
				"</validation>													" ;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"	<validation xc:operation=\"delete\">abc</validation>" +
				"</validation>													" ;

		NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);
		assertFalse(response2.isOk());
		assertEquals("/validation:validation/validation:leafref-validation",response2.getErrors().get(0).getErrorPath());

	}

	@Test
	public void testChoiceLeaf() throws Exception {
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <choicecase>"+
				"    <list1-type>"+
				"      <list-key>key</list-key>"+
				"      <case-leaf1>10</case-leaf1>"+
				"    </list1-type>"+
				"  </choicecase>" +
				"</validation>                                                 " ;

		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);
		RequestScope.setEnableThreadLocalInUT(false);
	}
	@Test
	public void testImpactListOnChange() throws Exception{
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <when-validation>"+
				"      <result-list>10</result-list>"+
				"      <list-type>" +
				"         <list-id>id</list-id>"+
				"         <list-value>value</list-value>"+
				"      </list-type>"+
				"  </when-validation>" +
				"</validation>                                                 " ;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <when-validation>"+
				"      <result-list>9</result-list>"+
				"  </when-validation>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml2, true);
		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "<data>"
						+ " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "  <validation:when-validation>"
						+ "   <validation:result-list>9</validation:result-list>"
						+ "  </validation:when-validation>"
						+ " </validation:validation>"
						+ "</data>"
						+"</rpc-reply>"
				;
		verifyGet(expectedOutput);

		RequestScope.setEnableThreadLocalInUT(false);
	}
	@Test
	public void testImpactLeafOnChange() throws ModelNodeInitException, NetconfMessageBuilderException, SchemaBuildException, SAXException, IOException {
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<arithmetic-validation>"+
				"		<value1>5</value1>"+
				"		<mod-leaf>10</mod-leaf>"+
				"	</arithmetic-validation>" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"	<arithmetic-validation>"+
				"		<value1>4</value1>"+
				"	</arithmetic-validation>" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		String expectedOutput =
				"<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:arithmetic-validation>" +
						"<validation:value1>4</validation:value1>" +
						"</validation:arithmetic-validation>" +
						"</validation:validation>"+
						"</data>"+
						"</rpc-reply>"
				;

		verifyGet(expectedOutput);
		RequestScope.setEnableThreadLocalInUT(false);

	}

	@Test
	public void testValidLeafRefWithCurrentAlone() throws ModelNodeInitException {
		testMatchedLeafRefValueWithCurrent();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\"> 		" +
				"	<leaf-ref> 													" +
				"    	<artist> 												" +
				"    		<name>LENY</name> 									" +
				"    	</artist> 												" +
				"    	<album> 												" +
				"    		<name>Album1</name> 								" +
				"	    	<song>												" +
				"	    		<name>Last Christmas</name>						" +
				"	    		<artist-name>LENY</artist-name>					" +
				"	    	</song>												" +
				" 			<song-count>20</song-count>							" +
				"       </album>												" +
				"      <music>													" +
				"     		<kind>Balad</kind>									" +
				"    		<favourite-album>Album1</favourite-album>			" +
				"   		<favourite-song>Last Christmas</favourite-song>		" +
				"  	</music>												" +
				"		<current-alone>											" +
				"			<current-leaf>Album1</current-leaf>					" +
				"			<current-alone-leaf>Album1</current-alone-leaf>		" +
				"		</current-alone>										" +
				"	</leaf-ref>													" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref>												" +
				"		<current-alone> 										" +
				"			<current-parent-leaf>Album1</current-parent-leaf>	" +
				"			<current-leaf-list>Test1</current-leaf-list>		" +
				"			<current-leaf-list>Test2</current-leaf-list>		" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
	}

	@Test
	public void testCurrent_LeafWithWhenConstraints() throws Exception {

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref>												" +
				"    	<album> 												" +
				"    		<name>Test</name> 								" +
				" 			<song-count>20</song-count>							" +
				"       </album>												" +
				"		<current-alone> 										" +
				"			<current-leaf1>Test</current-leaf1>		" +
				"			<current-leaf2>Test2</current-leaf2>		" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
	}

	@Test
	public void testCurrentLeaf_FailWhenConstraints() throws Exception {

		getModelNode();
		/**
		 * current-leaf1 is not available in request as well as DS, so when constraints failed for current-leaf2
		 */
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>		" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref>												" +
				"		<current-alone> 										" +
				"			<current-leaf2>Test2</current-leaf2>				" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>													" ;

		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());

		/**
		 * current-leaf1 value is Test, but album list is empty, so when constraints failed for current-leaf2
		 */
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>		" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref>												" +
				"		<current-alone> 										" +
				"			<current-leaf2>Test2</current-leaf2>				" +
				"			<current-leaf1>Test</current-leaf1>					" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>													" ;

		request = createRequestFromString(requestXml);
		request.setMessageId("1");
		response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());

		/**
		 * current-leaf1 value is Test, but album list does not have any key with 'Test' so when constraints failed for current-leaf2
		 */

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>				" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref>												" +
				"    	<album> 												" +
				"    		<name>Album1</name> 									" +
				" 			<song-count>20</song-count>							" +
				"       </album>												" +
				"		<current-alone> 										" +
				"			<current-leaf1>Test</current-leaf1>					" +
				"			<current-leaf2>Test2</current-leaf2>				" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>	";

		request = createRequestFromString(requestXml);
		request.setMessageId("1");
		response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testValidCount() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<count-validation> "+
				"		<countable>8</countable>"+
				"		<count-list>"+
				"			<leaf1>10</leaf1>"+
				"		</count-list> "+
				"      <count-list>"+
				"          <leaf1>20</leaf1>"+
				"      </count-list> "+
				"	</count-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertFalse(response1.isOk());
		assertNull(response1.getData());
		assertEquals("Violate when constraints: count(count-list) = 1", response1.getErrors().get(0).getErrorMessage());
		String expectedPath1 = "/validation:validation/validation:count-validation/validation:count-list[validation:leaf1='10']/validation:leaf1";
		String expectedPath2 = "/validation:validation/validation:count-validation/validation:count-list[validation:leaf1='20']/validation:leaf1";
		String errorPath = response1.getErrors().get(0).getErrorPath();
		// changeNodeMap contains a set of EditContainmentNodes, so which one will be used for the error is unpredictable
		assertTrue(expectedPath1.equals(errorPath) || expectedPath2.equals(errorPath));
	}

	@Test
	public void testValidCount1() throws Exception {
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <count-validation> "+
				"      <countable>8</countable>"+
				"      <value2>0</value2>"+
				"      <count-list>"+
				"          <leaf1>11</leaf1>"+
				"      </count-list> "+
				"  </count-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());
		assertNull(response2.getData());
		assertEquals("Violate when constraints: count(countable) = 0", response2.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenDeleteNode() throws Exception {
		getModelNode();
		m_dataStore.enableUTSupport();
		List<AbstractNetconfRequest> requestList = m_dataStore.getRequestListForTest();
		assertEquals(0, requestList.size());

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <count-validation> "+
				"      <value2>0</value2>"+
				"      <count-list>"+
				"          <leaf1>11</leaf1>"+
				"      </count-list> "+
				"  </count-validation> "+
				"</validation>"
				;

		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		requestList = m_dataStore.getRequestListForTest();
		assertEquals(1, requestList.size());

		EditConfigRequest req = (EditConfigRequest)requestList.get(0);
		assertXMLStringEquals(request1.requestToString(), req.requestToString());

		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <count-validation> "+
				"    <countable>8</countable>"+
				"  </count-validation> "+
				"</validation>"
				;

		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		requestList = m_dataStore.getRequestListForTest();
		assertEquals(3, requestList.size());

		req = (EditConfigRequest)requestList.get(1);
		assertXMLStringEquals(request2.requestToString(), req.requestToString());

		req = (EditConfigRequest)requestList.get(2);

		String implicitDelete = "<rpc message-id=\"internal_edit:1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> "
				+ "  <edit-config> "
				+ "    <target> "
				+ "      <running/> "
				+ "    </target> "
				+ "    <default-operation>merge</default-operation> "
				+ "    <test-option>set</test-option> "
				+ "    <error-option>stop-on-error</error-option> "
				+ "    <config> "
				+ "      <validation xmlns=\"urn:org:bbf:pma:validation\"> "
				+ "        <count-validation> "
				+ "          <value2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">0</value2> "
				+ "        </count-validation> "
				+ "      </validation> "
				+ "    </config> "
				+ "  </edit-config> "
				+ "</rpc>";
		assertXMLStringEquals(implicitDelete, req.requestToString());

	}

	@Test
	public void testSelfCountList() throws ModelNodeInitException{
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<count-validation> "+
				"		<count-list>"+
				"			<leaf1>10</leaf1>"+
				"		</count-list> "+
				"	</count-validation> "+
				"</validation>"
				;

		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<count-validation> "+
				"		<count-list>"+
				"			<leaf1>10</leaf1>"+
				"			<leaf2>11</leaf2>"+
				"		</count-list> "+
				"	</count-validation> "+
				"</validation>"
				;

		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml2, true);


	}

	@Test
	public void testIdentity() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"+
				"<validation:identity-validation>"+
				"		<validation:leaf1>identity1</validation:leaf1> "+
				"	</validation:identity-validation> " +
				" </validation:validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"<identity-validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<leaf2>1</leaf2> "+
				"	</identity-validation> " +
				" </validation>"
				;

		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
	}

	@Test
	public void testIdentity2() throws ModelNodeInitException{

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"+
				"	<validation:identity-validation>"+
				"		<validation:leaf3 xmlns:validation11=\"urn:org:bbf:pma:validation-yang11\">validation11:identity3</validation:leaf3> "+
				"	</validation:identity-validation> " +
				"</validation:validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"+
				"	<validation:identity-validation>"+
				"		<validation:leaf3 xmlns:validation11=\"urn:org:bbf:pma:validation-yang11\">validation11:identity2</validation:leaf3> "+
				"	</validation:identity-validation> " +
				"</validation:validation>"
				;

		request1 = createRequestFromString(requestXml2);
		request1.setMessageId("1");
		response1 = new NetConfResponse().setMessageId("1");
		try{
			m_server.onEditConfig(m_clientInfo, request1, response1);
		}catch(ValidationException e){
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.INVALID_VALUE, rpcError.getErrorTag());
			assertEquals("Value \"validation11:identity2\" is not a valid identityref value.", rpcError.getErrorMessage());
			assertEquals("/validation:validation/validation:identity-validation/validation:leaf3", rpcError.getErrorPath());
		}
	}

	@Test
	public void testNotEqualOperator2() throws ModelNodeInitException {
		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<when-validation> "+
				"		<result-leaf>10</result-leaf> "+
				"	</when-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertTrue(response3.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<when-validation> "+
				"		<not-equal>test1</not-equal> "+
				"	</when-validation> "+
				"</validation>"
				;


		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());
		assertTrue(response2.getErrors().get(0).getErrorMessage().contains("Violate when constraints"));
		assertTrue(response2.getErrors().get(0).getErrorPath()
				.equals("/validation:validation/validation:when-validation/validation:not-equal"));

	}

	@Test
	public void testArthimeticFailOperation() throws ModelNodeInitException {
		/// Add first leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  	<arithmetic-validation> "+
				"		 	<value1>15</value1> "+
				"	    </arithmetic-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		// add leaf to check + operator
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<fail-must-leaf >15</fail-must-leaf > "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());
		assertTrue(response2.getErrors().get(0).getErrorMessage().contains("Violate must constraints: ../value1 + 10 < 0"));
		assertNull(response2.getData());

	}
	@Test
	public void testArthimeticOperation() throws ModelNodeInitException {
		/// Add first leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  	<arithmetic-validation> "+
				"		 	<value1>15</value1> "+
				"	    </arithmetic-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		// add leaf to check + operator
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<value2>15</value2> "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		// add leaf to check - operator
		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<value3>15</value3> "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertTrue(response3.isOk());

		// add leaf to check * operator
		String requestXml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<value4>45</value4> "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request4 = createRequestFromString(requestXml4);
		request4.setMessageId("1");
		NetConfResponse response4 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request4, response4);
		assertTrue(response4.isOk());

		// add leaf to check div operator
		String requestXml5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<value5>15</value5> "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request5 = createRequestFromString(requestXml5);
		request5.setMessageId("1");
		NetConfResponse response5 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request5, response5);
		assertTrue(response5.isOk());


	}

	@Test
	public void testAllArithmeticOperator() throws ModelNodeInitException {
		/// Add first leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  	<arithmetic-validation> "+
				"		 	<value1>15</value1> "+
				"	    </arithmetic-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		//add leaf to check mod operator
		String requestXml6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"	<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"		<arithmetic-validation> "+
				"			<all-arith-leaf>10</all-arith-leaf>"+
				"			<all-must-leaf>10</all-must-leaf>"+
				"			<abs-leaf>10</abs-leaf>"+
				"			<mod-leaf>15</mod-leaf> "+
				"		</arithmetic-validation> "+
				"  </validation>"
				;

		getModelNode();
		EditConfigRequest request6 = createRequestFromString(requestXml6);
		request6.setMessageId("1");
		NetConfResponse response6 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request6, response6);
		assertTrue(response6.isOk());

	}

	@Test
	public void testCrossReferencePath() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"  <validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"    <validation>validation</validation>" +
				"    <crossConstant>10</crossConstant>"+
				"    <constantCheck>10</constantCheck>"+
				"</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNotEqualOperator() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<when-validation> "+
				"		<result-leaf>15</result-leaf> "+
				"	</when-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<when-validation> "+
				"		<not-equal>test1</not-equal> "+
				"	</when-validation> "+
				"</validation>"
				;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
		assertNull(response2.getData());

	}

	@Test
	public void testInvalidLeafRefWithCurrentAlone() throws ModelNodeInitException {
		testMatchedLeafRefValueWithCurrent();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
				"    <leaf-ref> 												" +
				"    	<album> 												" +
				"    		<name>Album2</name> 								" +
				"	    	<song>												" +
				"	    		<name>Last Christmas</name>						" +
				"	    		<artist-name>LENY</artist-name>					" +
				"	    	</song>												" +
				"	 		<song-count>0</song-count>							" +
				"       </album>												" +
				"	  </leaf-ref> 												" +
				"</validation>													" ;
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				"<validation xmlns=\"urn:org:bbf:pma:validation\"> 		" +
				"    <leaf-ref> 												" +
				"		<current-alone>											" +
				"			<current-parent-leaf>Album2</current-parent-leaf>	" +
				"			<current-leaf-list>Test1</current-leaf-list>		" +
				"			<current-leaf-list>Test2</current-leaf-list>		" +
				"		</current-alone> 										" +
				"	  </leaf-ref>												" +
				"</validation>" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());
	}

	@Test
	public void testInvalidInstanceIdentifierForNotExistingContainer() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-container.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/abc", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}

	@Test
	public void testValidInstanceIdentifierWithContainerPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-container.xml");
	}

	@Test
	public void testValidInstanceIdentifierForListPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-list.xml");
	}

	@Test
	public void testValidInstanceIdentifierForListLeafPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf-of-list.xml");
	}

	@Test
	public void testInvalidInstanceIdentifierForLeafInList() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf-of-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[subject-id = 'SJ001']/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}

	@Test
	public void testInvalidInstanceIdentifierWithRequireInstance() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-with-require-instance.xml",
				NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"instance-required",
				"Missing required element /validation/instance-identifier-example/address/national[national-id=BN]",
				"/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}

	@Test
	public void testValidInstanceIdentifierListIncludedMultipleKeys() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-list-with-two-keys.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());

		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/create-instance-identifier-list-with-two-keys.xml");
	}

	@Test
	public void testValidInstanceIdentifierForLeaf() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf.xml");
	}

	@Test
	public void testInvalidInstanceIdentifierForLeaf() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application,  NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[1]/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}

	@Test
	public void testValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-index-list.xml");
	}

	@Test
	public void testInValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-index-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application,  NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[2]/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}

	@Test
	public void testViolateWhenconstraints() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaf.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: ../result-leaf >= 10", "/validation:validation/validation:when-validation/validation:leaf-type");
	}

	@Test
	public void testValidWhenconstraints() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml");
	}

	@Test
	public void testValidWhenconstraintsOnAbsoluteLeaf() throws ModelNodeInitException {

		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		// single condition
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf2.xml");

		//multiple condition and
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf.xml");

		//multiple condition or
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf3.xml");
	}

	@Test
	public void testValidMustConstraint() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-leaf.xml");
	}

	@Test
	public void testViolateMustConstraint() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-leaf.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
				"An MTU must be  100 .. 5000", "/validation:validation/validation:must-validation/validation:leaf-type");
	}


	@Test
	public void testViolateWhenConstraintForLeafCaseNode() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf-casenode-1.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-1.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'success'", "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-success");

		requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf-casenode-2.xml";
		getModelNode();
		request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-2.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'failed'", "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-failed");
	}


	@Test
	public void testValidWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-1.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());

		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-1.xml");
	}

	@Test
	public void testValidWhenConstraintForFailLeafCase() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-2.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-2.xml");
	}

	@Test
	public void testValidateLeafWithSameName() throws ModelNodeInitException {
		String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-with-same-name.xml";
		getModelNode();
		EditConfigRequest request = createRequest(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);

		assertTrue(response.isOk());
		testPass("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leafref-with-same-name.xml");
	}

	@Test
	public void testInvalidLeafRefWithSameName() throws ModelNodeInitException {
		String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-with-same-name.xml";
		getModelNode();
		EditConfigRequest request = createRequest(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);

		assertTrue(response.isOk());
		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-invalid-leafref-with-same-name.xml",
				NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
				"Dependency violated, 'validation1' must exist", "/validation:validation/validation:leafref-validation");
	}

	@Override
	protected void initialiseInterceptor() {
		m_addDefaultDataInterceptor = null;
	}

	@Test
	public void testValidAbsoluteWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-1.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());

		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-1.xml");
	}

	@Test
	public void testValidAbsoluteWhenConstraintReferringACase() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-abs-leaflist-casenode-1.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());

		testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-2.xml");
	}

	@Test
	public void testInValidAbsoluteWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-2.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		assertTrue(response1.isOk());

		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-1.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application,NetconfRpcErrorSeverity.Error,
				"when-violation", "Violate when constraints: /validation/when-validation/choicecase/result-choice = 'success'",
				"/validation:validation/validation:when-validation/validation:choicecase/validation:absolute-case-success");
	}

	@Test
	public void testValidWhenconstraintsOnNotEqualsLeaf() throws ModelNodeInitException {

		String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
		getModelNode();
		EditConfigRequest request1 = createRequest(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);

		// single condition
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation1.xml");

		//multiple condition and
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation2.xml");

		//multiple condition or
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation3.xml",
				NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				"when-violation","Violate when constraints: ../result-leaf != 0 and /validation/when-validation/result-leaf != 15",
				"/validation:validation/validation:when-validation/validation:NotEqualLeaf3");
	}

	@Test
	public void testRootContinerOnListAddition() throws Exception{
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation1 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <leaf1>leaf1</leaf1>"+
				"</validation1>"
				;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation1 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"  </list1>"+
				"</validation1>"
				;

		editConfig(m_server, m_clientInfo, requestXml2, true);

		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation1 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key2</key1>"+
				"  </list1>"+
				"</validation1>"
				;
		NetConfResponse response3 = editConfig(m_server, m_clientInfo, requestXml3, false);

		assertFalse(response3.isOk());
		assertEquals("Violate must constraints: count(list1) <= 1", response3.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation1", response3.getErrors().get(0).getErrorPath());

		RequestScope.setEnableThreadLocalInUT(false);
	}

	@Test
	public void testMustConstraintOnListChildNode() throws Exception{
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation2 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <enabled>true</enabled>"+
				"  </list1>"+
				"</validation2>"
				;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "  <validation:validation2 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "    <validation:list1>"
				+ "     <validation:key1>key1</validation:key1>"
				+ "     <validation:enabled>true</validation:enabled>"
				+ "    </validation:list1>"
				+ "  </validation:validation2>"
				+ " </data>"
				+ "</rpc-reply>"
				;
		verifyGet(response);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation2 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key2</key1>"+
				"    <enabled>true</enabled>"+
				"  </list1>"+
				"</validation2>"
				;

		NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);

		assertFalse(response2.isOk());
		assertEquals("Violate must constraints: count(list1[enabled='true']) <= 1", response2.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation2", response2.getErrors().get(0).getErrorPath());

		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation2 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key2</key1>"+
				"    <enabled>false</enabled>"+
				"  </list1>"+
				"</validation2>"
				;
		editConfig(m_server, m_clientInfo, requestXml3, true);

		RequestScope.setEnableThreadLocalInUT(false);
	}

	@Test
	public void testMustConstraintOnListChildNodeDepthCase() throws Exception{
		RequestScope.setEnableThreadLocalInUT(true);
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container1>"+
				"    <list2>"+
				"    <key2>key2</key2>"+
				"    <enabled>true</enabled>"+
				"    </list2>"+
				"    </container1>"+
				"  </list1>"+
				"</validation3>"
				;
		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "    <validation:list1>"
				+ "     <validation:key1>key1</validation:key1>"
				+ "     <validation:container1>"
				+ "      <validation:list2>"
				+ "       <validation:key2>key2</validation:key2>"
				+ "       <validation:enabled>true</validation:enabled>"
				+ "      </validation:list2>"
				+ "     </validation:container1>"
				+ "    </validation:list1>"
				+ "  </validation:validation3>"
				+ " </data>"
				+ "</rpc-reply>"
				;
		verifyGet(response);

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container1>"+
				"    <list2>"+
				"    <key2>key3</key2>"+
				"    <enabled>true</enabled>"+
				"    </list2>"+
				"    </container1>"+
				"  </list1>"+
				"</validation3>"
				;

		NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);

		assertFalse(response2.isOk());
		assertEquals("Violate must constraints: count(../container1/list2[enabled='true']) <= 1", response2.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation3/validation:list1[validation:key1='key1']/validation:container1", response2.getErrors().get(0).getErrorPath());

		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container1>"+
				"    <list2>"+
				"    <key2>key3</key2>"+
				"    <enabled>false</enabled>"+
				"    </list2>"+
				"    </container1>"+
				"  </list1>"+
				"</validation3>"
				;
		editConfig(m_server, m_clientInfo, requestXml3, true);

		RequestScope.setEnableThreadLocalInUT(false);
	}

	@Test
	public void testDefaultLeafCreationOnWhen() throws Exception {

		RequestScope.setEnableThreadLocalInUT(true);
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <validation>default</validation>"+
				"</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "   <validation:validation>default</validation:validation>"
				+ "   <validation:defaultLeaf>1</validation:defaultLeaf>"
				+ "  </validation:validation>"
				+ " </data>"
				+ "</rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <validation>default1</validation>"+
				"</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "   <validation:validation>default1</validation:validation>"
				+ "   <validation:defaultLeaf1>2</validation:defaultLeaf1>"
				+ "  </validation:validation>"
				+ " </data>"
				+ "</rpc-reply>"
		;
		verifyGet(response);

	}

	@Test
	public void testCurrentAloneInMultiList() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "<leaf-ref>"
				+ " <current-alone>"
				+ "  <current-alone-list>"
				+ "    <key>10</key>"
				+ "    <current-alone>11</current-alone>"
				+ "  </current-alone-list>"
				+ " </current-alone>"
				+ "</leaf-ref>"
				+ "</validation>"
				;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate when constraints: ../../current-alone-list[current()]/current-alone = .",
				response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testCurrentAloneInMissingList() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "<leaf-ref>"
				+ " <current-alone>"
				+ "  <current-alone-list-leaf>10</current-alone-list-leaf>"
				+ " </current-alone>"
				+ "</leaf-ref>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testSymbolLeaf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>validation</validation>"
				+ " <symbol-leaf-1.1_2>validation</symbol-leaf-1.1_2>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>validation1</validation>"
				+ " <symbol-leaf-1.1_2>validation</symbol-leaf-1.1_2>"
				+ "</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testDefaultWhenCreationNonPresenceContainer() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>default-when</validation>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:default-when-validation>"
						+ "    <validation:class>10</validation:class>"
						+ "   </validation:default-when-validation>"
						+ "  <validation:validation>default-when</validation:validation>"
						+ " </validation:validation>"
						+ "</data>"
						+ "</rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ " <validation xc:operation='delete'>default-when</validation>"
				+ "</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ " </validation:validation>"
						+ "</data>"
						+ "</rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testMandatoryLeafInNonPresenceContainer() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>mandatory</validation>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mandatory-validation-container/>"
				+ "</validation>"
		;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Missing mandatory node", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:mandatory-validation-container/validation:leafValidation/validation:leaf1",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mandatory-validation-container>"
				+ "   <leafValidation>"
				+ "     <leaf1>0</leaf1>"
				+ "   </leafValidation>"
				+ " </mandatory-validation-container>"
				+ "</validation>"
		;
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:mandatory-validation-container>"
						+ "    <validation:leafValidation>"
						+ "     <validation:leaf1>0</validation:leaf1>"
						+ "     <validation:leafDefault>0</validation:leafDefault>"
						+ "    </validation:leafValidation>"
						+ "   </validation:mandatory-validation-container>"
						+ "  <validation:validation>mandatory</validation:validation>"
						+ " </validation:validation>"
						+ "</data>"
						+ "</rpc-reply>"
				;
		verifyGet(response);

	}

	@Test
	public void testForParentContainerDeletionWithAttributes() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validate-parent-container-on-when-deletion>"
				+ "  <leaf1>10</leaf1>"
				+ "  <for-leaf>"
				+ "   <leaf1>0</leaf1>"
				+ "   <leaf2>0</leaf2>"
				+ "   <innerContainer>"
				+ "    <leaf1>0</leaf1>"
				+ "   </innerContainer>"
				+ "  </for-leaf>"
				+ " </validate-parent-container-on-when-deletion>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ " <validate-parent-container-on-when-deletion>"
				+ "  <leaf1>0</leaf1>"
				+ "  <for-leaf>"
				+ "   <leaf2 xc:operation='delete'>0</leaf2>"
				+ "  </for-leaf>"
				+ " </validate-parent-container-on-when-deletion>"
				+ "</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "  <validation:validate-parent-container-on-when-deletion>"
						+ "   <validation:for-leaf>"
						+ "    <validation:leaf1>0</validation:leaf1>"
						+ "   </validation:for-leaf>"
						+ "   <validation:leaf1>0</validation:leaf1>"
						+ "  </validation:validate-parent-container-on-when-deletion>"
						+ " </validation:validation>"
						+ "</data>"
						+ "</rpc-reply>"
				;
		verifyGet(response);

	}

	@Test
	public void testDeleteOnLeafRef() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>name</name>"
				+ "  </album>"
				+ "  <current-alone>"
				+ "   <current-leaf>name</current-leaf>"
				+ "  </current-alone>"
				+ " </leaf-ref>"
				+ "</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ " <leaf-ref>"
				+ "  <album xc:operation='delete'>"
				+ "   <name>name</name>"
				+ "  </album>"
				+ " </leaf-ref>"
				+ "</validation>"
		;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());

	}

	@Test
	public void testTypeValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "<valueCheck>4294967298</valueCheck>"
				+ "</validation>"
				;
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:valueCheck", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals("The argument is out of bounds <-128, 127>",ncResponse.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "<valueCheck1>429</valueCheck1>"
				+ "</validation>"
		;
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:valueCheck1", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals("The argument is out of bounds <-128, 127>",ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testCurrentWithMultipleParentBothCases() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>Album1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>Album1</current-some-leaf>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <current-album-list-leaf>20</current-album-list-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//NegativeCase
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>Album1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>Album1</current-some-leaf>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <current-album-list-leaf>21</current-album-list-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name-list[validation:name='TestName']/validation:current-album-list-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals("Dependency violated, '21' must exist", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMultipleCurrentSameLevelBothCases() throws Exception {
		getModelNode();

		//PositiveCase for path "current()/../../name-list[name = current()/../../current-some-leaf]/name-count"
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>Album1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>Album1</current-some-leaf>"
				+ "    <name-list>"
				+"      <name>Album1</name>"
				+"      <name-count>1</name-count>"
				+ "    </name-list>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <current-album-list-leaf>20</current-album-list-leaf>"
				+ "        <two-current-leaf>1</two-current-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//NegativeCase
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>Album1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>Album1</current-some-leaf>"
				+ "    <name-list>"
				+"      <name>Album1</name>"
				+"      <name-count>1</name-count>"
				+ "    </name-list>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <current-album-list-leaf>20</current-album-list-leaf>"
				+ "        <two-current-leaf>2</two-current-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(
				"/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name-list[validation:name='TestName']/validation:two-current-leaf",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals("Dependency violated, '2' must exist", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testNestedCurrentSameLevelBothCases() throws Exception {
		getModelNode();

		//PositiveCase (nested current) for path:
		//"../../../album[name = current()/../../name-list[name = current()/../../current-some-leaf]/name-count]/song-count"
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>InnerAlbum1</current-some-leaf>"
				+ "    <name-list>"
				+"      <name>InnerAlbum1</name>"
				+"      <name-count>1</name-count>"
				+ "    </name-list>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <nested-current-leaf>20</nested-current-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//NegativeCase
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <leaf-ref>"
				+ "  <album>"
				+ "   <name>1</name>"
				+ "   <song-count>20</song-count>"
				+ "  </album>"
				+ "  <current-multi-parent>"
				+ "    <current-some-leaf>InnerAlbum1</current-some-leaf>"
				+ "    <name-list>"
				+"      <name>InnerAlbum1</name>"
				+"      <name-count>1</name-count>"
				+ "    </name-list>"
				+ "    <album-name-list>"
				+ "        <name>TestName</name>"
				+ "        <nested-current-leaf>21</nested-current-leaf>"
				+ "    </album-name-list>"
				+ "  </current-multi-parent>"
				+ " </leaf-ref>"
				+ "</validation>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(
				"/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name-list[validation:name='TestName']/validation:nested-current-leaf",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals("Dependency violated, '21' must exist", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testAbsPathAtContainerSameAsRootName() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <validation>sameNodeAsRootName</validation>"
				+ "  <sameContainerAsRoot>"
				+ "   <validation>"
				+ "    <validation1>sameNodeAsRootName</validation1>"
				+ "  </validation>"
				+ " </sameContainerAsRoot>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testSum() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someList>"
				+ "  <key>key1</key>"
				+ "  <sumValue>4</sumValue>"
				+ " </someList>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum(../someList/sumValue) > 5",response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someList>"
				+ "  <key>key1</key>"
				+ "  <sumValue>11</sumValue>"
				+ " </someList>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someList>"
				+ "  <key>key2</key>"
				+ "  <sumValue>111</sumValue>"
				+ " </someList>"
				+ "</validation>";
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum(../someList/sumValue) > 5",response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testEnumValueWithParentPattern() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <alarm-list>"
				+ "  <key>key1</key>"
				+ "  <severity>critical</severity>"
				+ "  <test-enum-value-with-parent-pattern>hello</test-enum-value-with-parent-pattern>"
				+ " </alarm-list>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <alarm-list>"
				+ "  <key>key2</key>"
				+ "  <severity>minor</severity>"
				+ "  <test-enum-value-with-parent-pattern>hello</test-enum-value-with-parent-pattern>"
				+ " </alarm-list>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: enum-value(../severity) >= 5",response.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:alarm-list[validation:key='key2']/validation:test-enum-value-with-parent-pattern",
				response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testEnumValueOnNonExistingLeaf() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf>hello</test-enum-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: enum-value(../../test-container-one/leaf1) > 1",response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testEnumValueWithAbsoluteXpath() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "   <list1>"
				+ "    <key>key1</key>"
				+ "    <valueLeaf>one</valueLeaf>"
				+ "   </list1>"
				+ "   <list1>"
				+ "    <key>key2</key>"
				+ "    <valueLeaf>two</valueLeaf>"
				+ "   </list1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		//negative case
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-with-absolute-path>key1</test-enum-leaf-with-absolute-path>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: enum-value(/validation/test-enum-value-container/test-container-one/list1[key = current()]/valueLeaf) > 1",response.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-with-absolute-path",
				response.getErrors().get(0).getErrorPath());

		//positive case
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-with-absolute-path>key2</test-enum-leaf-with-absolute-path>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String ncResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:test-enum-value-container>"
						+ "    <validation:test-container-one>"
						+ "       <validation:list1>"
						+ "       <validation:key>key1</validation:key>"
						+ "       <validation:valueLeaf>one</validation:valueLeaf>"
						+ "       </validation:list1>"
						+ "       <validation:list1>"
						+ "       <validation:key>key2</validation:key>"
						+ "       <validation:valueLeaf>two</validation:valueLeaf>"
						+ "       </validation:list1>"
						+ "    </validation:test-container-one>"
						+ "    <validation:test-container-two>"
						+ "       <validation:test-enum-leaf-with-absolute-path>key2</validation:test-enum-leaf-with-absolute-path>"
						+ "    </validation:test-container-two>"
						+ "   </validation:test-enum-value-container>"
						+ "  </validation:validation>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);
	}

	@Test
	public void testEnumValueWithNoArguments() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <alarm-list>"
				+ "  <key>key1</key>"
				+ "  <severity>minor</severity>"
				+ "  <test-enum-value-with-empty-node-set>hello</test-enum-value-with-empty-node-set>"
				+ " </alarm-list>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals(
				"Missing argument in enum-value function. You need to provide one argument in the enum-value() function : enum-value() in node : test-enum-value-with-empty-node-set",
				response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testEnumValueWithArgumentsNegativeCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <leaf1>one</leaf1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf>hello</test-enum-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(../../test-container-one/leaf1) > 1",
				response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testEnumValueWithArgumentsPositiveCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <leaf1>two</leaf1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf>hello</test-enum-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String ncResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:test-enum-value-container>"
						+ "    <validation:test-container-one>"
						+ "       <validation:leaf1>two</validation:leaf1>"
						+ "    </validation:test-container-one>"
						+ "    <validation:test-container-two>"
						+ "       <validation:test-enum-leaf>hello</validation:test-enum-leaf>"
						+ "    </validation:test-container-two>"
						+ "   </validation:test-enum-value-container>"
						+ "  </validation:validation>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);

	}

	@Test
	public void testEnumValueForLeafListPositiveCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <leaflist1>three</leaflist1>"
				+ "  <leaflist1>four</leaflist1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-for-leaf-list>hello</test-enum-leaf-for-leaf-list>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String ncResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:test-enum-value-container>"
						+ "    <validation:test-container-one>"
						+ "       <validation:leaflist1>three</validation:leaflist1>"
						+ "       <validation:leaflist1>four</validation:leaflist1>"
						+ "    </validation:test-container-one>"
						+ "    <validation:test-container-two>"
						+ "       <validation:test-enum-leaf-for-leaf-list>hello</validation:test-enum-leaf-for-leaf-list>"
						+ "    </validation:test-container-two>"
						+ "   </validation:test-enum-value-container>"
						+ "  </validation:validation>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);

	}

	@Test
	public void testEnumValueForLeafListNegativeCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <leaflist1>one</leaflist1>"
				+ "  <leaflist1>two</leaflist1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-for-leaf-list>hello</test-enum-leaf-for-leaf-list>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-for-leaf-list", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(../../test-container-one/leaflist1) > 2",
				response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testEnumValueForNonEnumLeafAndLeafList() throws Exception {
		getModelNode();

		//leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <non-enum-leaf1>one</non-enum-leaf1>"
				+ "  <non-enum-leaf-list1>two</non-enum-leaf-list1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-for-non-enum-leaf>hello</test-for-non-enum-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(
				"Violate must constraints: enum-value(/validation/test-enum-value-container/test-container-one/non-enum-leaf1) > 1",
				response.getErrors().get(0).getErrorMessage());

		//leaf-list
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <non-enum-leaf-list1>one</non-enum-leaf-list1>"
				+ "  <non-enum-leaf-list1>two</non-enum-leaf-list1>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-for-non-enum-leaf-list>hello</test-for-non-enum-leaf-list>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(
				"Violate must constraints: enum-value(../../test-container-one/non-enum-leaf-list1) > 2",
				response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testEnumValueWithCurrentLeafListOrLeaf() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-list-with-dot-operator>one</test-enum-leaf-list-with-dot-operator>"
				+ "  <test-enum-leaf-list-with-dot-operator>two</test-enum-leaf-list-with-dot-operator>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-list-with-dot-operator", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(.) > 2",
				response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-with-dot-operator>two</test-enum-leaf-with-dot-operator>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-with-dot-operator>one</test-enum-leaf-with-dot-operator>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-with-dot-operator", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(.) > 1",
				response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testEnumValueWithTargetLeafInChoiceCase() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-one>"
				+ "  <leaf22>one</leaf22>"
				+ "  </test-container-one>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-in-choice-case>hello</test-enum-leaf-in-choice-case>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-in-choice-case", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(../../test-container-one/leaf22) > 1",
				response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testEnumValueWithXpathFromCurrentNode() throws Exception {
		getModelNode();

		//Negative Case
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-leaf>one</test-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-xpath-from-dot>one</test-enum-leaf-xpath-from-dot>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:test-enum-value-container/validation:test-container-two/validation:test-enum-leaf-xpath-from-dot", response.getErrors().get(0).getErrorPath());
		assertEquals(
				"Violate must constraints: enum-value(./../test-leaf) > enum-value(.)",
				response.getErrors().get(0).getErrorMessage());

		//Positive Case
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-leaf>three</test-leaf>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <test-enum-value-container>"
				+ "  <test-container-two>"
				+ "  <test-enum-leaf-xpath-from-dot>two</test-enum-leaf-xpath-from-dot>"
				+ "  </test-container-two>"
				+ " </test-enum-value-container>"
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String ncResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:test-enum-value-container>"
						+ "    <validation:test-container-two>"
						+ "       <validation:test-leaf>three</validation:test-leaf>"
						+ "       <validation:test-enum-leaf-xpath-from-dot>two</validation:test-enum-leaf-xpath-from-dot>"
						+ "    </validation:test-container-two>"
						+ "   </validation:test-enum-value-container>"
						+ "  </validation:validation>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);
	}

	@Test
	public void testMustAsLeafMandatory() throws Exception {
		getModelNode();
		// test violation of must(./) on a leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ " </mustMandatory>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: ./mandatoryLeaf",response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mandatoryLeaf>4</mandatoryLeaf>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		// test violation of must(./) on a container
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer/>"
				+ " </mustMandatory>"
				+ "</validation>";
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: ./mandatoryContainer", response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer>"
				+ "    <mandatoryContainer>"
				+ "      <mandatoryList>"
				+ "        <key>key</key>"
				+ "      </mandatoryList>"
				+ "    </mandatoryContainer>"
				+ "  </mustMandatoryContainer>"
				+ " </mustMandatory>"
				+ "</validation>";
		response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// test violation of must(./) on a leafList
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer>"
				+ "    <mandatoryContainer>"
				+ "      <anotherLeaf>leafList</anotherLeaf>"
				+ "    </mandatoryContainer>"
				+ "  </mustMandatoryContainer>"
				+ " </mustMandatory>"
				+ "</validation>";
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate when constraints: ../mandatoryLeafList", response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer>"
				+ "    <mandatoryContainer>"
				+ "      <anotherLeaf>leafList</anotherLeaf>"
				+ "    </mandatoryContainer>"
				+ "  </mustMandatoryContainer>"
				+ " </mustMandatory>"
				+ "</validation>";
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate when constraints: ../mandatoryLeafList", response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer>"
				+ "    <mandatoryContainer>"
				+ "      <mandatoryLeafList>mandatory</mandatoryLeafList>"
				+ "      <anotherLeaf>leafList</anotherLeaf>"
				+ "    </mandatoryContainer>"
				+ "  </mustMandatoryContainer>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		// delete a leaf and the impacted when(./) condition must also be removed/deleted
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer>"
				+ "    <mandatoryContainer xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "      <mandatoryLeafList xc:operation=\"delete\">mandatory</mandatoryLeafList>"
				+ "    </mandatoryContainer>"
				+ "  </mustMandatoryContainer>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String ncResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:mustMandatory>"
						+ "    <validation:key>key1</validation:key>"
						+ "    <validation:mandatoryLeaf>4</validation:mandatoryLeaf>"
						+ "    <validation:mustMandatoryContainer>"
						+ "     <validation:mandatoryContainer>"
						+ "      <validation:mandatoryList>"
						+ "       <validation:key>key</validation:key>"
						+ "      </validation:mandatoryList>"
						+ "     </validation:mandatoryContainer>"
						+ "    </validation:mustMandatoryContainer>"
						+ "   </validation:mustMandatory>"
						+ "  </validation:validation>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);
	}

	@Test
	public void testBooleanCurrent() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <booleanCurrent>"
				+ "  <key>key1</key>"
				+ "  <leaf1>leaf1</leaf1>"
				+ "  <leaf2>true</leaf2>"
				+ " </booleanCurrent>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testOtherTreeRoot() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>123</validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <otherTreeRelativePath>123</otherTreeRelativePath>"
				+ "</validation3>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ " <validation xc:operation=\"delete\">123</validation>"
				+ "</validation>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Dependency violated, '123' must exist", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testOtherRootFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>Other</validation>"
				+ " <booleanCurrent>"
				+ "  <key>someKey</key>"
				+ " </booleanCurrent>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someLeaf>someLeaf</someLeaf>"
				+ "</validation3>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: count(../../validation/booleanCurrent) > 0 "
						+ "and not(contains(../../validation/validation, 'Other'))",
				response.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>other</validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someLeaf>someLeaf</someLeaf>"
				+ "</validation3>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>Other</validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, false);

	}

	@Test
	public void testOtherRootWhen() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>otherRoot</validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <someLeaf1>otherRoot</someLeaf1>"
				+ "</validation3>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:validation>otherRoot</validation:validation>"
						+ "  </validation:validation>"
						+ "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:someLeaf1>otherRoot</validation:someLeaf1>"
						+ "  </validation:validation3>"
						+ " </data>"
						+ "</rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ " <validation>otherRoot1</validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
						+ "   <validation:validation>otherRoot1</validation:validation>"
						+ "  </validation:validation>"
						+ "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
						+ " </data>"
						+ "</rpc-reply>"
		;
		verifyGet(response);

	}

	@Test
	public void testWhenOnParentLocalName() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <when-parent-local-name-leaf>test</when-parent-local-name-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testLocalNameWithNoArgs() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <when-no-arg-local-name-leaf>test</when-no-arg-local-name-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testWhenOnParentLocalNameChoiceCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <choicecase/>"
				+ "   <when-parent-local-name-container-leaf>test</when-parent-local-name-container-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testWhenOnParentName() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <when-parent-name-leaf>test</when-parent-name-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testWhenOnParentNameChoiceCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <choicecase/>"
				+ "   <when-parent-name-container-leaf>test</when-parent-name-container-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testWhenOnParentNamespace() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testNamespaceUriWithNoArgs() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <when-validation>"
				+ "   <when-no-arg-namespace-leaf>test</when-no-arg-namespace-leaf>"
				+ " </when-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	// This is not working yet, so only the above trivial case currently works (tracked by FNMS-20720)
//    @Test
//    public void testWhenOnParentNamespaceChoiceCase() throws Exception {
//        getModelNode();
//        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
//                + "  <when-validation>"
//                + "   <choicecase/>"
//                + "   <when-parent-namespace-container-leaf>test</when-parent-namespace-container-leaf>"
//                + " </when-validation>"
//                + "</validation>";
//        editConfig(m_server, m_clientInfo, requestXml1, true);
//
	@Test
	public void testStringFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <number1>41</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <number1>41</number1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		//no-arg string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testConcatFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>B</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testStartsWithFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>DSL1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testContainsFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>TestDSL1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testSubstringFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testSubstringBeforeFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testSubstringAfterFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testStringLengthFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		//no-arg string-length function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <no-arg-string-length-function-leaf>test</no-arg-string-length-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNormalizeSpaceFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		//no-arg normalize-space function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <no-arg-normalize-space-function-leaf>test</no-arg-normalize-space-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testTranslateFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>-bar-</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </string-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

//        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
//                + "  <string-function-validation>"
//                + "    <string1>dsl</string1>"
//                + "    <translate-function-leaf>test</translate-function-leaf>"
//                + "  </string-function-validation>"
//                + "</validation>";
//        // should fail
//        editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "  </string-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testBooleanFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNotFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testTrueFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <true-function-leaf>test</true-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testFalseFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <false-function-leaf>test</false-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNotFunctionWithNoArgs() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <no-arg-not-function-leaf>test</no-arg-not-function-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Invalid JXPath syntax: Incorrect number of arguments: not()",response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testBooleanStringConversion() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <boolean-string-conversion-leaf>test</boolean-string-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-string-conversion-leaf>test</boolean-string-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanStringConversionInAnd() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1></string1>"
				+ "    <boolean-string-conversion-in-and-leaf>test</boolean-string-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-string-conversion-in-and-leaf>test</boolean-string-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanStringConversionInOr() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1></string1>"
				+ "    <boolean-string-conversion-in-or-leaf>test</boolean-string-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-string-conversion-in-or-leaf>test</boolean-string-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	// Not working properly because GenericConfigAttribute always returns a string, even
	// for numeric leafs, so JXPath doesn't know it's a number
	// This would require significant rework
	// Ticket to track: FNMS-23850
	// @Test
	public void testBooleanNumberConversion() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>0</number1>"
				+ "    <boolean-number-conversion-leaf>test</boolean-number-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "    <boolean-number-conversion-leaf>test</boolean-number-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanNumberConversionInAnd() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>0</number1>"
				+ "    <boolean-number-conversion-in-and-leaf>test</boolean-number-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "    <boolean-number-conversion-in-and-leaf>test</boolean-number-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanNumberConversionInOr() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>0</number1>"
				+ "    <boolean-number-conversion-in-or-leaf>test</boolean-number-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "    <boolean-number-conversion-in-or-leaf>test</boolean-number-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <number1>20</number1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanNodesetConversion() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <boolean-nodeset-conversion-leaf>test</boolean-nodeset-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "    <boolean-nodeset-conversion-leaf>test</boolean-nodeset-conversion-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanNodesetConversionInAnd() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <boolean-nodeset-conversion-in-and-leaf>test</boolean-nodeset-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "    <boolean-nodeset-conversion-in-and-leaf>test</boolean-nodeset-conversion-in-and-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testBooleanNodesetConversionInOr() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <boolean-nodeset-conversion-in-or-leaf>test</boolean-nodeset-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "    <boolean-nodeset-conversion-in-or-leaf>test</boolean-nodeset-conversion-in-or-leaf>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <boolean-function-validation>"
				+ "    <nodeset1></nodeset1>"
				+ "  </boolean-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNumberFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <string1>41</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <string1>41</string1>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		//no args number function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <no-arg-number-function-leaf>1</no-arg-number-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testFloorFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testCeilingFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testRoundFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.72</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </number-function-validation>"
				+ "</validation>";
		// should fail
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

	@Test
	public void testMustValidationDuringDeleteOfReferencedNode() throws Exception {
		m_schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-augment-test", "/validation:validation/validation:test-interfaces", mock(SchemaPath.class));
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>intName</name>"
				+ "     <type>sampleType1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>intName</name>"
				+ "     <type>sampleType1</type>"
				+ "    </test-interface>"
				+ "     <use-test-interface xmlns=\"urn:opendaylight:datastore-validator-augment-test\">"
				+ "      <channel-leaf>sampleChannel</channel-leaf>"
				+ "      <sample-leaf>intName</sample-leaf>"
				+ "     </use-test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "  <test-interfaces>"
				+ "    <test-interface xc:operation=\"remove\">"
				+ "     <name>intName</name>"
				+ "     <type>sampleType1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("must reference a channel-leaf",response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustNotValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>" +
						"<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:list1>" +
						"<validation:container2>" +
						"<validation:address>" +
						"<validation:ip>1.1.1.1</validation:ip>" +
						"</validation:address>" +
						"</validation:container2>" +
						"<validation:key1>key1</validation:key1>" +
						"</validation:list1>" +
						"</validation:validation3>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    <address>"+
				"    <ip>1.1.1.2</ip>"+
				"    </address>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>" +
						"<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:list1>" +
						"<validation:container2>" +
						"<validation:address>" +
						"<validation:ip>1.1.1.1</validation:ip>" +
						"</validation:address>" +
						"<validation:address>" +
						"<validation:ip>1.1.1.2</validation:ip>" +
						"</validation:address>" +
						"</validation:container2>" +
						"<validation:key1>key1</validation:key1>" +
						"</validation:list1>" +
						"</validation:validation3>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>" +
						"<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:list1>" +
						"<validation:container2>" +
						"<validation:address>" +
						"<validation:ip>1.1.1.1</validation:ip>" +
						"</validation:address>" +
						"<validation:address>" +
						"<validation:ip>1.1.1.2</validation:ip>" +
						"</validation:address>" +
						"</validation:container2>" +
						"<validation:key1>key1</validation:key1>" +
						"</validation:list1>" +
						"</validation:validation3>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
		;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);

		String expectedErrorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf:pma:validation?revision=2015-12-14)address";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation3/list1[key1='key1']/container2/address[ip='1.1.1.1']",
				response.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustOnCurrentExtensionFunction() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    <refClass1>test</refClass1>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testMustOnCurrentExpressionPath() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <list1>"+
				"    <key1>key1</key1>"+
				"    <container2>"+
				"    <address>"+
				"    <ip>1.1.1.1</ip>"+
				"    </address>"+
				"    <refClass1>test</refClass1>"+
				"    <refClass2>test</refClass2>"+
				"    </container2>"+
				"  </list1>"+
				"</validation3>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testMustCountWithMultipleCurrent_Fail() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<current-validation>"+
				"  		<list1>"+
				"    		<key>key1</key>"+
				"	 		<type>type1</type>"+
				"	 		<value>10</value>"+
				"  		</list1>"+
				"		<leaf1>key2</leaf1>"+
				"	</current-validation>"+
				"</validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		String expectedErrorMsg = "Violate must constraints: count(/validation/current-validation/list1[key = current() and /validation/current-validation/list1[key = 'key1']/value = /validation/current-validation/list1[key = current()]/value]) = 1";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:current-validation/validation:leaf1",
				response.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustCountWithMultipleCurrent() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"	<current-validation>"+
				"  		<list1>"+
				"    		<key>key1</key>"+
				"	 		<type>type1</type>"+
				"	 		<value>10</value>"+
				"  		</list1>"+
				"		<leaf1>key1</leaf1>"+
				"	</current-validation>"+
				"</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+"		<validation:current-validation>"
				+"			<validation:leaf1>key1</validation:leaf1>"
				+"			<validation:list1>"
				+"				<validation:key>key1</validation:key>"
				+"				<validation:type>type1</validation:type>"
				+"				<validation:value>10</validation:value>"
				+"			</validation:list1>"
				+"		</validation:current-validation>"
				+"	</validation:validation>"
				+" </data>"
				+"</rpc-reply>";

		verifyGet(expectedOutput);
	}

	@Test
	public void testGroupReferenceWithoutPrefixes() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <validation>GroupPrefixes</validation>" +
				"  <group-validation-without-prefixes>" +
				"   <groupContainer2>" +
				"    <groupContainerLeaf2>GroupPrefixes</groupContainerLeaf2>" +
				"   </groupContainer2>" +
				"  </group-validation-without-prefixes>" +
				"</validation>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	@Test
	public void testCasesWithSameLeafName() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"   +
				" <routing xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">" +
				"  <control-plane-protocols>" +
				"   <control-plane-protocol>" +
				"    <type>identity2</type>" +
				"    <name>1111</name>"+
				"    <static-routes>"+
				"     <ipv4>"+
				"      <route>"+
				"       <destination-prefix>0.0.0.0/0</destination-prefix>"+
				"       <next-hop>"+
				"        <next-hop-address>135.249.41.1</next-hop-address>"+
				"       </next-hop>"+
				"      </route>"+
				"     </ipv4>"+
				"    </static-routes>"+
				"   </control-plane-protocol>"+
				"  </control-plane-protocols>"+
				" </routing>" +
				"</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testMustWithDerivedFromOrLeaf() throws Exception {

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation5 xmlns=\"urn:org:bbf:pma:validation\">"+
				"    <type>identity1</type>" +
				"</validation5>";

		getModelNode();
		NetConfResponse  response = editConfig(m_server, m_clientInfo, requestXml, false);

		String expectedErrorMsg = "Violate must constraints: derived-from-or-self(/validation5/type, 'validation:identity2')";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation5/validation:must-with-derived-from-or-self",
				response.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation5 xmlns=\"urn:org:bbf:pma:validation\">"+
				"    <type>identity2</type>" +
				"</validation5>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "	<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "		<validation:validation5 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "			<validation:must-with-derived-from-or-self>"
				+ "				<validation:mustLeaf>must</validation:mustLeaf>"
				+ "			</validation:must-with-derived-from-or-self>"
				+ "			<validation:type>validation:identity2</validation:type>"
				+ "		</validation:validation5>"
				+ "	</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);
	}

	@Test
	public void testAugmentWhenWithMandatoryLeaf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"   +
				"  <augmentContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">" +
				"  </augmentContainer>" +
				"</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testNonExistentLeaf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"   +
				"  <nonExistantLeaf>test</nonExistantLeaf>"+
				"</validation>"
				;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);

		String expectedErrorMsg = "Violate when constraints: 1 = 1 and iamImpactNode != 'iAmNotNull'";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:nonExistantLeaf",
				response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testNullOnFunctionValue() throws Exception{
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"<containsLeaf>1</containsLeaf>" +
				"</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation:validation/validation:containsLeaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: contains(../validation,'hello')", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testImpactNodesWithMultipleLeafRefNodes() throws Exception{
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <classifiers>"+
				"   <classifier-entry>" +
				"    <name>class1</name>" +
				"    <match-criteria>" +
				"     <dscp-range>11</dscp-range>" +
				"    </match-criteria>" +
				"   </classifier-entry>" +
				"   <classifier-entry>" +
				"    <name>class2</name>" +
				"    <match-criteria>" +
				"     <dscp-range>11</dscp-range>" +
				"    </match-criteria>" +
				"   </classifier-entry>" +
				"  </classifiers>"+
				"  <policies>"+
				"   <policy>" +
				"    <name>policy1</name>" +
				"    <classifiers>" +
				"     <name>class1</name>" +
				"    </classifiers>" +
				"    <classifiers>" +
				"     <name>class2</name>" +
				"    </classifiers>" +
				"   </policy>" +
				"  </policies>"+
				"  <qos-policy-profiles>"+
				"   <policy-profile>" +
				"    <name>qospolicy1</name>" +
				"    <policy-list>" +
				"     <name>policy1</name>" +
				"    </policy-list>" +
				"   </policy-profile>" +
				"  </qos-policy-profiles>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>" +
						"<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:classifiers>" +
						"<validation:classifier-entry>" +
						"<validation:match-criteria>" +
						"<validation:dscp-range>11</validation:dscp-range>" +
						"</validation:match-criteria>" +
						"<validation:name>class1</validation:name>" +
						"</validation:classifier-entry>" +
						"<validation:classifier-entry>" +
						"<validation:match-criteria>" +
						"<validation:dscp-range>11</validation:dscp-range>" +
						"</validation:match-criteria>" +
						"<validation:name>class2</validation:name>" +
						"</validation:classifier-entry>" +
						"</validation:classifiers>" +
						"<validation:policies>" +
						"<validation:policy>" +
						"<validation:classifiers>" +
						"<validation:name>class1</validation:name>" +
						"</validation:classifiers>" +
						"<validation:classifiers>" +
						"<validation:name>class2</validation:name>" +
						"</validation:classifiers>" +
						"<validation:name>policy1</validation:name>" +
						"</validation:policy>" +
						"</validation:policies>" +
						"<validation:qos-policy-profiles>" +
						"<validation:policy-profile>" +
						"<validation:name>qospolicy1</validation:name>" +
						"<validation:policy-list>" +
						"<validation:name>policy1</validation:name>" +
						"</validation:policy-list>" +
						"</validation:policy-profile>" +
						"</validation:qos-policy-profiles>" +
						"</validation:validation3>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <classifiers>"+
				"   <classifier-entry>" +
				"    <name>class3</name>" +
				"    <match-criteria>" +
				"     <dscp-range>11</dscp-range>" +
				"    </match-criteria>" +
				"   </classifier-entry>" +
				"  </classifiers>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>" +
						"<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:classifiers>" +
						"<validation:classifier-entry>" +
						"<validation:match-criteria>" +
						"<validation:dscp-range>11</validation:dscp-range>" +
						"</validation:match-criteria>" +
						"<validation:name>class1</validation:name>" +
						"</validation:classifier-entry>" +
						"<validation:classifier-entry>" +
						"<validation:match-criteria>" +
						"<validation:dscp-range>11</validation:dscp-range>" +
						"</validation:match-criteria>" +
						"<validation:name>class2</validation:name>" +
						"</validation:classifier-entry>" +
						"<validation:classifier-entry>" +
						"<validation:match-criteria>" +
						"<validation:dscp-range>11</validation:dscp-range>" +
						"</validation:match-criteria>" +
						"<validation:name>class3</validation:name>" +
						"</validation:classifier-entry>" +
						"</validation:classifiers>" +
						"<validation:policies>" +
						"<validation:policy>" +
						"<validation:classifiers>" +
						"<validation:name>class1</validation:name>" +
						"</validation:classifiers>" +
						"<validation:classifiers>" +
						"<validation:name>class2</validation:name>" +
						"</validation:classifiers>" +
						"<validation:name>policy1</validation:name>" +
						"</validation:policy>" +
						"</validation:policies>" +
						"<validation:qos-policy-profiles>" +
						"<validation:policy-profile>" +
						"<validation:name>qospolicy1</validation:name>" +
						"<validation:policy-list>" +
						"<validation:name>policy1</validation:name>" +
						"</validation:policy-list>" +
						"</validation:policy-profile>" +
						"</validation:qos-policy-profiles>" +
						"</validation:validation3>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);
	}

	/**
	 * Test Identity ref validation which is defined in different module with default identity value
	 * @throws Exception
	 */
	@Test
	public void testIdentityRefDifferentModule_withDefault() throws Exception {
		getModelNode();

		// Validate identity ref validation and created default identity ref
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"+
				"    <load-balancing>round-robin</load-balancing>"+
				"</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:identityContainer>" +
						"<validation:load-balancer>" +
						"<validation:distribution-algorithm xmlns:lb=\"urn:org:bbf:pma:load-balancer\">lb:round-robin</validation:distribution-algorithm>" +
						"</validation:load-balancer>" +
						"</validation:identityContainer>" +
						"<validation:load-balancing>round-robin</validation:load-balancing>"+
						"</validation:validation>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		// Remove the container which has identity ref
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"<identityContainer xc:operation=\"remove\"/>" +
				"</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:load-balancing>round-robin</validation:load-balancing>"+
						"</validation:validation>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput);

		// Again the default identity ref will be added
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"<load-balancing xc:operation=\"replace\">round-robin</load-balancing>"+
				"</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput1=
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"<data>" +
						"<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
						"<validation:identityContainer>" +
						"<validation:load-balancer>" +
						"<validation:distribution-algorithm xmlns:lb=\"urn:org:bbf:pma:load-balancer\">lb:round-robin</validation:distribution-algorithm>" +
						"</validation:load-balancer>" +
						"</validation:identityContainer>" +
						"<validation:load-balancing>round-robin</validation:load-balancing>"+
						"</validation:validation>" +
						"</data>" +
						"</rpc-reply>";

		verifyGet(expectedOutput1);
	}

	@Test
	public void testMustConstraint_LeafViolation() throws Exception{

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <must-validation>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"  </must-validation>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "<validation:must-validation>"
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation>"
				+ "</validation:validation3>"
				+ "</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"  <must-validation>"+
				"    <key1>key</key1>" +
				"    <value xc:operation=\"delete\">test</value>" +
				"  </must-validation>"+
				"</validation3>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals("/validation:validation3/validation:must-validation[validation:key1='key']", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: value ='test' ", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustConstraint_KeyandLeafViolation() throws Exception{

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"  </must-validation1>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "<validation:must-validation1>"
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation1>"
				+ "</validation:validation3>"
				+ "</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value xc:operation=\"delete\">test</value>" +
				"  </must-validation1>"+
				"</validation3>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals("/validation:validation3/validation:must-validation1[validation:key1='key']", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: key1 = 'key' and value ='test' ", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testValidateChildCache() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"   +
				"  <dummy-interfaces xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"  <dummy-interface>" +
				"  <dummy-name>int1</dummy-name>" +
				"  <dummy-type>traps</dummy-type>" +
				"  <dummy-traps>" +
				"  <trap-leaf>dummyTrap</trap-leaf>" +
				"  </dummy-traps>" +
				"  </dummy-interface>" +
				"  </dummy-interfaces>" +
				"</validation>"
				;
		editConfig(m_server, m_clientInfo, requestXml1, true);
		assertFalse(m_datastoreValidator.getValidatedChildCacheHitStatus());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf:pma:validation\">"   +
				"  <dummy-interfaces xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"  <dummy-interface>" +
				"  <dummy-name>int2</dummy-name>" +
				"  <dummy-type>forwarding</dummy-type>" +
				"  </dummy-interface>" +
				"  </dummy-interfaces>" +
				"</validation>"
		;
		editConfig(m_server, m_clientInfo, requestXml1, true);
		assertTrue(m_datastoreValidator.getValidatedChildCacheHitStatus());
	}

	@Test
	public void testMustConstraint_LeafListViolation() throws Exception{

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <must-validation2>"+
				"    <key1>key</key1>" +
				"    <values>test</values>" +
				"  </must-validation2>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "<validation:must-validation2>"
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:values>test</validation:values>"
				+ "</validation:must-validation2>"
				+ "</validation:validation3>"
				+ "</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"  <must-validation2>"+
				"    <key1>key</key1>" +
				"    <values xc:operation=\"delete\">test</values>" +
				"  </must-validation2>"+
				"</validation3>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals("/validation:validation3/validation:must-validation2[validation:key1='key']", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: key1 = 'key' and values ='test' ", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenConstraint_LeafListViolation() throws Exception{

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\">"+
				"  <when-validation>"+
				"    <key1>key</key1>" +
				"    <values>test</values>" +
				"  </when-validation>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+ "<validation:when-validation>"
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:values>test</validation:values>"
				+ "</validation:when-validation>"
				+ "</validation:validation3>"
				+ "</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"  <when-validation>"+
				"    <key1>key</key1>" +
				"    <values xc:operation=\"delete\">test</values>" +
				"  </when-validation>"+
				"</validation3>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals("/validation:validation3/validation:when-validation[validation:key1='key']", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: key1 = 'key' and values = 'test'", response.getErrors().get(0).getErrorMessage());
	}
	@After
	public void teardown() {
		m_dataStore.disableUTSupport();
		m_datastoreValidator.setValidatedChildCacheHitStatus(false);
	}
}


