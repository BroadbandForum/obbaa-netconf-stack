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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

@RunWith(RequestScopeJunitRunner.class)
public class LeafRefDSValidatorTest extends AbstractDataStoreValidatorTest {

	@Test
	public void testNegativeCasesInDeploymentStage() throws ModelNodeFactoryException, SchemaBuildException {
		List<String> yangs = getYang();
		yangs.add("/datastorevalidatortest/yangs/datastore-validator-leafref-negative-tests.yang");
		List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(yangs);
		SchemaRegistryImpl schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		try {
			YangUtils.deployInMemoryHelpers(yangs, getSubSystem(), m_modelNodeHelperRegistry,
					m_subSystemRegistry, schemaRegistry, m_modelNodeDsm, null, null);
			fail("Expecting exception here");
		} catch(RuntimeException e) {
			assertEquals(
					"Configuration leafref node : AbsoluteSchemaPath{path=[(urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)rootNodeNegative, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)leafRefHolder, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)lr]} "
							+ "path /lrNegative:rootNodeNegative/lrNegative:leafRefTarget[id=current()/../id]/lrNegative:name is pointing to state data node AbsoluteSchemaPath{path=[(urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)rootNodeNegative, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)leafRefTarget, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)name]}"
							+ "\n\nLeafref AbsoluteSchemaPath{path=[(urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)rootNodeNegative, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)leafRefHolder, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)lrPointingToContainer]} "
							+ "target path is not referring to leaf/leaflist node. Target path - AbsoluteSchemaPath{path=[(urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)rootNodeNegative, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)leafRefHolder, (urn:org:bbf2:pma:leafref:negative:validation?revision=2019-02-11)testContainer]}",
					e.getMessage());
		}
	}
	
	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_1() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
        		+"  <lrPoints>"
        		+"  <index>1</index>"
        		+"  <lrPointsToSibling>1</lrPointsToSibling>"
                +"  </lrPoints>"
        		+"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
        		+ "  <data>"
        		+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
        		+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
        		+ "      <lrValidation:lrPoints>"
        		+ "        <lrValidation:index>1</lrValidation:index>"
                + "        <lrValidation:index2>10</lrValidation:index2>"
        		+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
        		+ "      </lrValidation:lrPoints>"
        		+ "    </lrValidation:rootNode>"
        		+ "  </data>"
        		+ "</rpc-reply>";
		verifyGet(expectedOutput);
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
        		+"  <lrPoints>"
        		+"  <index>1</index>"
        		+"  <lrPointsToSibling>2</lrPointsToSibling>"
                +"  </lrPoints>"
        		+"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); // Wrong lr value
        checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:lrPointsToSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);
        
	}

	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_2() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "  <data>"
				+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "      <lrValidation:lrPoints>"
				+ "        <lrValidation:index>1</lrValidation:index>"
				+ "        <lrValidation:index2>10</lrValidation:index2>"
				+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
				+ "      </lrValidation:lrPoints>"
				+ "    </lrValidation:rootNode>"
				+ "  </data>"
				+ "</rpc-reply>";
		verifyGet(expectedOutput);
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
//				+"  <lrPoints>"
//				+"  <index>1</index>"
//				+"  <lrPointsToSibling>2</lrPointsToSibling>"
//				+"  </lrPoints>"
//				+"</rootNode>" ;
//		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); // Wrong lr value
//		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:lrPointsToSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<otherRootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrTarget>5</lrTarget>"
				+"</otherRootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// Pointing to parent node Sibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <otherParent>"
				+"  	<otherIndex1>1</otherIndex1>"
				+"  	<otherIndex2>10</otherIndex2>"
				+"  </otherParent>"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>2</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false); // negative case - lrPointsToSibling
		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:lrPointsToSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);
	}

	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_3() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "  <data>"
				+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "      <lrValidation:lrPoints>"
				+ "        <lrValidation:index>1</lrValidation:index>"
				+ "        <lrValidation:index2>10</lrValidation:index2>"
				+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
				+ "      </lrValidation:lrPoints>"
				+ "    </lrValidation:rootNode>"
				+ "  </data>"
				+ "</rpc-reply>";
		verifyGet(expectedOutput);
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
//				+"  <lrPoints>"
//				+"  <index>1</index>"
//				+"  <lrPointsToSibling>2</lrPointsToSibling>"
//				+"  </lrPoints>"
//				+"</rootNode>" ;
//		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); // Wrong lr value
//		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:lrPointsToSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<otherRootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrTarget>5</lrTarget>"
				+"</otherRootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// Pointing to parent node Sibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <otherParent>"
				+"  	<otherIndex1>1</otherIndex1>"
				+"  	<otherIndex2>10</otherIndex2>"
				+"  </otherParent>"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
//				+"  <lrPoints>"
//				+"  <index>1</index>"
//				+"  <lrPointsToSibling>2</lrPointsToSibling>"
//				+"  </lrPoints>"
//				+"</rootNode>" ;
//		response = editConfig(m_server, m_clientInfo, requestXml1, false); // negative case - lrPointsToSibling
//		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:lrPointsToSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>2</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>3</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>6</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false); // negative case
		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:deep1/lrValidation:lrPointintToParentNodeSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);

	}

	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_4() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "  <data>"
				+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "      <lrValidation:lrPoints>"
				+ "        <lrValidation:index>1</lrValidation:index>"
				+ "        <lrValidation:index2>10</lrValidation:index2>"
				+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
				+ "      </lrValidation:lrPoints>"
				+ "    </lrValidation:rootNode>"
				+ "  </data>"
				+ "</rpc-reply>";
		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<otherRootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrTarget>5</lrTarget>"
				+"</otherRootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// Pointing to parent node Sibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <otherParent>"
				+"  	<otherIndex1>1</otherIndex1>"
				+"  	<otherIndex2>10</otherIndex2>"
				+"  </otherParent>"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>2</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);// negative case - lrPointintToParentNodeSibling
		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:deep1/lrValidation:lrPointintToParentNodeSibling", "Dependency violated, '2' must exist", NetconfRpcErrorTag.DATA_MISSING);

	}

	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_5() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "  <data>"
				+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "      <lrValidation:lrPoints>"
				+ "        <lrValidation:index>1</lrValidation:index>"
				+ "        <lrValidation:index2>10</lrValidation:index2>"
				+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
				+ "      </lrValidation:lrPoints>"
				+ "    </lrValidation:rootNode>"
				+ "  </data>"
				+ "</rpc-reply>";
		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<otherRootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrTarget>5</lrTarget>"
				+"</otherRootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// Pointing to parent node Sibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <otherParent>"
				+"  	<otherIndex1>1</otherIndex1>"
				+"  	<otherIndex2>10</otherIndex2>"
				+"  </otherParent>"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>3</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>1</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);// negative case - lrPointintToOtherParentNodeSibling
		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:deep1/lrValidation:lrPointintToOtherParentNodeSibling", "Dependency violated, '3' must exist", NetconfRpcErrorTag.DATA_MISSING);

	}

	@Test
	public void testLeafRefPathPointingToDifferentLevelsOfLeafs_6() throws ModelNodeInitException, SAXException, IOException,
			NetconfMessageBuilderException {
		getModelNode();
		// Pointing to Sibling
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "  <data>"
				+ "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "      <lrValidation:lrPoints>"
				+ "        <lrValidation:index>1</lrValidation:index>"
				+ "        <lrValidation:index2>10</lrValidation:index2>"
				+ "        <lrValidation:lrPointsToSibling>1</lrValidation:lrPointsToSibling>"
				+ "      </lrValidation:lrPoints>"
				+ "    </lrValidation:rootNode>"
				+ "  </data>"
				+ "</rpc-reply>";
		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<otherRootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrTarget>5</lrTarget>"
				+"</otherRootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// Pointing to parent node Sibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <otherParent>"
				+"  	<otherIndex1>1</otherIndex1>"
				+"  	<otherIndex2>10</otherIndex2>"
				+"  </otherParent>"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>5</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case


//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
//				+"  <lrPoints>"
//				+"  <index>1</index>"
//				+"  <lrPointsToSibling>1</lrPointsToSibling>"
//				+"    <deep1>"
//				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
//				+"    <lrPointintToOtherParentNodeSibling>3</lrPointintToOtherParentNodeSibling>"
//				+"    <lrPointintToOtherRootNodeSibling>1</lrPointintToOtherRootNodeSibling>"
//				+"    </deep1>"
//				+"  </lrPoints>"
//				+"</rootNode>" ;
//		response = editConfig(m_server, m_clientInfo, requestXml1, false);// negative case - lrPointintToOtherParentNodeSibling
//		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:deep1/lrValidation:lrPointintToOtherParentNodeSibling", "Dependency violated, '3' must exist", NetconfRpcErrorTag.DATA_MISSING);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <lrPoints>"
				+"  <index>1</index>"
				+"  <lrPointsToSibling>1</lrPointsToSibling>"
				+"    <deep1>"
				+"    <lrPointintToParentNodeSibling>1</lrPointintToParentNodeSibling>"
				+"    <lrPointintToOtherParentNodeSibling>1</lrPointintToOtherParentNodeSibling>"
				+"    <lrPointintToOtherRootNodeSibling>6</lrPointintToOtherRootNodeSibling>"
				+"    </deep1>"
				+"  </lrPoints>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);// negative case - lrPointintToOtherRootNodeSibling
		checkErrors(response, "/lrValidation:rootNode/lrValidation:lrPoints[lrValidation:index='1']/lrValidation:deep1/lrValidation:lrPointintToOtherRootNodeSibling", "Dependency violated, '6' must exist", NetconfRpcErrorTag.DATA_MISSING);
	}
	
	private void checkErrors(NetConfResponse response, String errorPath, String errorMessage, NetconfRpcErrorTag tag) {
		List<NetconfRpcError> errors = response.getErrors();
		assertEquals(1, errors.size());
		NetconfRpcError error = errors.get(0);
		assertEquals(errorPath, error.getErrorPath());
		assertEquals(errorMessage, error.getErrorMessage());
		assertEquals(tag, error.getErrorTag());
		assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
	}

	@Test
	public void testLeafRefPathWithMultiPredicates() throws ModelNodeInitException {
		getModelNode();
		// Pointing to Sibling
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
        		+"  <multiPredicatesExample>"
        		+"  <index>1</index>"
        		+"    <deep1>"
        		+"    <deep1Index>1</deep1Index>"
        		+"       <deep2>"
        		+"       	<deep2Index>1</deep2Index>"
        		+"       	<lrTarget>abc</lrTarget>"
        		+"       	<multiPredicateLR>abc</multiPredicateLR>"
                +"       </deep2>"
                +"    </deep1>"
                +"  </multiPredicatesExample>"
        		+"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true); // Fair case
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
        		+"  <multiPredicatesExample>"
        		+"  <index>1</index>"
        		+"    <deep1>"
        		+"    <deep1Index>1</deep1Index>"
        		+"       <deep2>"
        		+"       	<deep2Index>1</deep2Index>"
        		+"       	<lrTarget>abc</lrTarget>"
        		+"       	<multiPredicateLR>xyz</multiPredicateLR>"
                +"       </deep2>"
                +"    </deep1>"
                +"  </multiPredicatesExample>"
        		+"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); // negative case
        checkErrors(response, "/lrValidation:rootNode/lrValidation:multiPredicatesExample[lrValidation:index='1']/lrValidation:deep1[lrValidation:deep1Index='1']/lrValidation:deep2[lrValidation:deep2Index='1']/lrValidation:multiPredicateLR", "Dependency violated, 'xyz' must exist", NetconfRpcErrorTag.DATA_MISSING);
	}
	
	@Test
	public void testLeafRefWithMust() throws ModelNodeInitException {
		getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <leafRefHolder>"
        		+"  <id>1</id>"
        		+"  <lrWithMust>1</lrWithMust>"
                +"  </leafRefHolder>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:leafRefHolder[lrValidation:id='1']/lrValidation:lrWithMust", "Violate must constraints: ../lrValidation:id > '5'", NetconfRpcErrorTag.OPERATION_FAILED);
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <leafRefHolder>"
        		+"  <id>6</id>"
        		+"  <lrWithMust>6</lrWithMust>"
                +"  </leafRefHolder>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
	}
	
	@Test
    public void testMustWithPredicateAsImpactNodeAbsPath() throws ModelNodeInitException {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <child-scheduler-nodes>"
                +"      <name>two</name>"
                +"  </child-scheduler-nodes>"
                +"  <scheduler-node>"
                +"      <name>two</name>"
                +"      <child-scheduler-nodes>"
                +"          <name>one</name>"
                +"      </child-scheduler-nodes>"
                +"  </scheduler-node>"
                +"  <mustWithPredicatesAsImpactNode_AbsPath-target>"
                +"      <name>one</name>"
                +"  </mustWithPredicatesAsImpactNode_AbsPath-target>"
                +"  <mustWithPredicatesAsImpactNode_AbsPath-source>"
                +"      <name>one</name>"
                +"      <egress-tm-objects>"
                +"          <scheduler-node-name>one</scheduler-node-name>"
                +"      </egress-tm-objects>"
                +"  </mustWithPredicatesAsImpactNode_AbsPath-source>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
		/**
		 * This part of the UT has been commented out as part of FNMS-49373. We are skipping validation here as the same issue as FNMS-49373 applies for the
		 * scheduler-node list in this model. The UT works so far as there is only a single entry of the list node in the request.
		 */
        /*requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <scheduler-node>"
                +"      <name>two</name>"
                +"      <child-scheduler-nodes xc:operation=\"remove\">"
                +"          <name>one</name>"
                +"      </child-scheduler-nodes>"
                +"  </scheduler-node>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:mustWithPredicatesAsImpactNode_AbsPath-source[lrValidation:name='one']/lrValidation:egress-tm-objects/lrValidation:scheduler-node-name", "Count failed with multi predicates", NetconfRpcErrorTag.OPERATION_FAILED);*/
    }
	
	@Test
    public void testMustWithPredicatesAsImpactNodesRelativePath() throws ModelNodeInitException {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <leafRefHolder>"
                +"      <id>1</id>"
                +"      <testInt>1</testInt>"
                +"  </leafRefHolder>"
                +"  <multiPredicatesExample>"
                +"      <index>1</index>"
                +"      <deep1>"
                +"          <deep1Index>1</deep1Index>"
                +"      </deep1>"
                +"  </multiPredicatesExample>"
                +"  <mustWithMultiplePredicatesAsImpactNodes_RelativePath>"
                +"      <name>one</name>"
                +"      <scheduler-node-name-leafref>1</scheduler-node-name-leafref>"
                +"  </mustWithMultiplePredicatesAsImpactNodes_RelativePath>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <leafRefHolder xc:operation=\"remove\">"
                +"      <id>1</id>"
                +"  </leafRefHolder>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:mustWithMultiplePredicatesAsImpactNodes_RelativePath[lrValidation:name='one']/lrValidation:scheduler-node-name-leafref", "Count failed", NetconfRpcErrorTag.OPERATION_FAILED);
    }
		
	@Test
	public void testLeafRefReferringToLeafList() throws ModelNodeInitException {
		getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <leafRefPointingToLeafList>"
        		+"  <index>1</index>"
        		+"  <minEleLeafList>20</minEleLeafList>"
        		+"  <minEleLeafList>30</minEleLeafList>"
        		+"  <lrPointingToMinEleLeafList>20</lrPointingToMinEleLeafList>"
                +"  </leafRefPointingToLeafList>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true); 
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        		+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <leafRefPointingToLeafList>"
        		+"  <index>1</index>"
        		+"  <minEleLeafList>20</minEleLeafList>"
        		+"  <minEleLeafList>30</minEleLeafList>"
        		+"  <lrPointingToMinEleLeafList>40</lrPointingToMinEleLeafList>"
                +"  </leafRefPointingToLeafList>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml1, false); 
	}
	   
    @Test
    public void testChoiceWithLeafRef1() throws Exception {
        
        /**
         * 1/ Create with LF1 and A1 - should pass
         * 2/ Now update the choice  to case B, that is give value to B1 - should fail with instance required
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF1>A1Present</LF1>"
                +"  <A1>A1Present</A1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:LF1>A1Present</lrValidation:LF1>"
                + "        <lrValidation:A1>A1Present</lrValidation:A1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <B1>B1Present</B1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF1", "Dependency violated, 'A1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
    
    @Test
    public void testChoiceWithLeafRef2() throws Exception {
        /**
         * 3/ Removing LF1 and selecting case B should pass
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF1>A1Present</LF1>"
                +"  <A1>A1Present</A1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <LF1 xc:operation=\"remove\"/>"
                +"  <B1>B1Present</B1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:B1>B1Present</lrValidation:B1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
    }
    
    @Test
    public void testChoiceWithLeafRef3() throws Exception {
        /**
         * 4/ Creating LF2 (Case C) and L1 (outside the choice) should pass
         * 5/ Removing L1 should fail.
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF2>L1Present</LF2>"
                +"  <L1>L1Present</L1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:LF2>L1Present</lrValidation:LF2>"
                + "        <lrValidation:L1>L1Present</lrValidation:L1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <L1 xc:operation=\"remove\"/>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF2", "Dependency violated, 'L1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
    
    @Test
    public void testChoiceWithLeafRef4() throws Exception {
        /**
         * 6/ Selecting other case (say case B) and removing L1 should pass
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF2>L1Present</LF2>"
                +"  <L1>L1Present</L1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:LF2>L1Present</lrValidation:LF2>"
                + "        <lrValidation:L1>L1Present</lrValidation:L1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <L1 xc:operation=\"remove\"/>"
                +"  <B1>B1Present</B1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:B1>B1Present</lrValidation:B1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
    }
    
    @Test
    public void testChoiceWithLeafRef5() throws Exception {
        /**
         * 7/ Selecting LF3 and D1 should pass
         * 8/ Removing D1 should fail
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF3>D1Present</LF3>"
                +"  <D1>D1Present</D1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:LF3>D1Present</lrValidation:LF3>"
                + "        <lrValidation:D1>D1Present</lrValidation:D1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <D1 xc:operation=\"remove\"/>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF3", "Dependency violated, 'D1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
    
    @Test
    public void testChoiceWithLeafRef6() throws Exception {
        /**
         * 9/ Selecting other case (say Case B) should not cause any problem
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF3>D1Present</LF3>"
                +"  <D1>D1Present</D1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:LF3>D1Present</lrValidation:LF3>"
                + "        <lrValidation:D1>D1Present</lrValidation:D1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <B1>B1Present</B1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:B1>B1Present</lrValidation:B1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <LF2>L1RequiredButNotPresent</LF2>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF2", "Dependency violated, 'L1RequiredButNotPresent' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
    
    @Test
    public void testChoiceWithLeafRef7() throws Exception {
        /**
         *  Testing Nested choices
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <innerLF1>E1Present</innerLF1>"
                +"  <E1>E1Present</E1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:E1>E1Present</lrValidation:E1>"
                + "        <lrValidation:innerLF1>E1Present</lrValidation:innerLF1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        // Switching cases
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <B1>B1Present</B1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "    <lrValidation:rootNode xmlns:lrValidation=\"urn:org:bbf2:pma:leafref:validation\">"
                + "      <lrValidation:choiceWithLeafref>"
                + "        <lrValidation:B1>B1Present</lrValidation:B1>"
                + "      </lrValidation:choiceWithLeafref>"
                + "    </lrValidation:rootNode>"
                + "  </data>"
                + "</rpc-reply>";
        verifyGet(expectedOutput);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <innerLF1>E1Present</innerLF1>"
                +"  <E1>E1Present</E1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <E1 xc:operation=\"remove\"/>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:innerLF1", "Dependency violated, 'E1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
    
    @Test
    public void testChoiceWithLeafRef8() throws Exception {
        /**
         *  Testing Nested choices
         */
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF4>innerB1Present</LF4>"
                +"  <innerB1>innerB1Present</innerB1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <innerB1 xc:operation=\"remove\"/>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF4", "Dependency violated, 'innerB1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
                +"  <choiceWithLeafref>"
                +"  <LF4>innerB1Present</LF4>"
                +"  <innerB1>innerB1Present</innerB1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        
        // Switching cases
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                +"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <choiceWithLeafref>"
                +"  <D1>D1Present</D1>"
                +"  </choiceWithLeafref>"
                +"</rootNode>" ;
        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/lrValidation:rootNode/lrValidation:choiceWithLeafref/lrValidation:LF4", "Dependency violated, 'innerB1Present' must exist", NetconfRpcErrorTag.DATA_MISSING);
    }
	
	@Test
    public void testCircularDependencyWithMustImpactNodePath() throws Exception {
        String interfaces_file = "/datastorevalidatortest/yangs/test-interfaces.yang";
        String interfaces_usage_file = "/datastorevalidatortest/yangs/mountRegistry2/bbf-interface-usage.yang";
        String interfaces_usage_dev_file = "/datastorevalidatortest/yangs/mountRegistry2/bbf-interface-usage-dev.yang";
        String sub_interfaces_file = "/datastorevalidatortest/yangs/mountRegistry2/bbf-sub-interfaces.yang";
		String bbf_xpon_if_type_file = "/datastorevalidatortest/yangs/mountRegistry2/bbf-xpon-if-type.yang";
        YangTextSchemaSource interfaceYangResource = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(interfaces_file));
        YangTextSchemaSource interfaceUsageYangResource = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(interfaces_usage_file));
        YangTextSchemaSource interfaceUsageDevYangResource = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(interfaces_usage_dev_file));
        YangTextSchemaSource subInterfaceYangResource = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(sub_interfaces_file));
        YangTextSchemaSource bbfXponIfTypeYangResource =
				YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(bbf_xpon_if_type_file));
        Set<QName> feature_set = new HashSet<QName>();
        Map<QName, Set<QName>> deviationMap = new HashMap<>();
        QName moduleQName = QName.create("bbf-interface-usage", "bbf-interface-usage");
        QName deviationQName = QName.create("bbf-interface-usage-dev", "bbf-interface-usage-dev");
        Set<QName> deviations = new HashSet<>();
        deviations.add(deviationQName);
        deviationMap.put(moduleQName, deviations);
        SchemaRegistryImpl schemaRegistry = new SchemaRegistryImpl(Arrays.asList(interfaceYangResource, interfaceUsageYangResource,
				interfaceUsageDevYangResource, subInterfaceYangResource, bbfXponIfTypeYangResource), feature_set,
                deviationMap, new NoLockService());
        m_modelNodeDsm = spy(new InMemoryDSM(schemaRegistry));
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(schemaRegistry);
        YangUtils.deployInMemoryHelpers(Arrays.asList(interfaces_file, interfaces_usage_file, interfaces_usage_dev_file, sub_interfaces_file), new LocalSubSystem(), m_modelNodeHelperRegistry, m_subSystemRegistry, schemaRegistry, m_modelNodeDsm, feature_set, deviationMap);
	}

	@Ignore
    public void testCircular() throws Exception {
        String leafref_yang_file = "/datastorevalidatortest/yangs/leafref-test.yang";        
        YangTextSchemaSource featuretest_yang = YangParserUtil.getYangSource(
                SchemaRegistryImplTest.class.getResource(leafref_yang_file));
        QName test_feature1 = QName.create("urn:org:bbf2:pma:leafref:test", "2015-12-14", "test-feature-1");
        Set<QName> feature_set = new HashSet<QName>();
        feature_set.add(test_feature1);
        SchemaRegistryImpl schemaRegistry = new SchemaRegistryImpl(Arrays.asList(featuretest_yang), feature_set,
                Collections.emptyMap(), new NoLockService());
        m_modelNodeDsm = spy(new InMemoryDSM(schemaRegistry));
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(schemaRegistry);
        Set<String> errorList = getErrorList();        
        try {
            YangUtils.deployInMemoryHelpers(Arrays.asList(leafref_yang_file), new LocalSubSystem(),
                    m_modelNodeHelperRegistry, m_subSystemRegistry, schemaRegistry, m_modelNodeDsm, feature_set, null, true);
            fail("Exception expected");
        } catch (Exception e) {
            String message = e.getMessage();
            String[] errors = message.split("\n");
            Set<String> leafrefsWithCircularDependencies = Sets.newHashSet(errors);
            Set<String> unexpectedErrors = Sets.difference(leafrefsWithCircularDependencies, errorList);
            Set<String> missingErrors = Sets.difference(errorList, leafrefsWithCircularDependencies);
            assertTrue("Unexpected errors: " + unexpectedErrors + ", missing errors: " + missingErrors, unexpectedErrors.isEmpty() && missingErrors.isEmpty());
        }
    }

	@Test
	public void testMustwithMultiplePredicatesFailurecase1() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "  <list1>"
				+ "      <name>two</name>"
				+ "      <list3>"
				+ "          <listid>one</listid>"
				+ "      </list3>"
				+ "      <caselist1>"
				+ "          <name1>one</name1>"
				+ "      </caselist1>"
				+ "  </list1>"
				+ "  <list2>"
				+ "      <name2>one</name2>"
				+ "      <tm-object>"
				+ "          <groupleaf1>one</groupleaf1>"
				+ "      </tm-object>"
				+ "  </list2>"
				+ "</rootNode>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicatesFailurecase2() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+ "  <list1>"
				+ "      <name>two</name>"
				+ "      <caselist1>"
				+ "          <name1>one</name1>"
				+ "      </caselist1>"
				+ "  </list1>"
				+ "  <list2>"
				+ "      <name2>one</name2>"
				+ "      <tm-object>"
				+ "          <groupleaf1>one</groupleaf1>"
				+ "          <leaf-must>one</leaf-must>"
				+ "      </tm-object>"
				+ "  </list2>"
				+ "</rootNode>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicatesFailurecase3() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf1>one</groupleaf1>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}
	@Test
	public void testMustwith_MultiplePredicates_RelativePathDeletingLeafListOfImpactedNode1() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf1>one</groupleaf1>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <caselist1 xc:operation=\"remove\">"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_RelativePath_DeletingImpactedNode() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf1>one</groupleaf1>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <leaf-must xc:operation=\"remove\"/>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_RelativePath_ModifyingImpactedNode() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf1>one</groupleaf1>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <leaf-must xc:operation=\"merge\">two</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_RelativePath_DeletingLeafListOfImpactedNode3() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf1>one</groupleaf1>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3 xc:operation =\"remove\">"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf1", "Count Validation in multiple predicates using relative path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}
	@Test
	public void testMustwithMultiplePredicates_AbsolutePath_DeletingLeafListOfImpactedNode1() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf2>one</groupleaf2>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <caselist1 xc:operation=\"remove\">"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf2", "Count Validation in multiple predicates using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_AbsolutePath_DeletingImpactedNode1() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf2>one</groupleaf2>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <leaf-must xc:operation=\"remove\"/>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf2", "Count Validation in multiple predicates using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_AbsolutePath_ModifyingImpactedNode2() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf2>one</groupleaf2>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <leaf-must xc:operation=\"merge\">two</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf2", "Count Validation in multiple predicates using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_AbsolutePath_DeletingLeafListOfImpactedNode3() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf2>one</groupleaf2>"
				+"          <leaf-must>one</leaf-must>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3 xc:operation =\"remove\">"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf2", "Count Validation in multiple predicates using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}
	@Test
	public void testMustwithMultiplePredicates_MultipleList_AbsolutePath_DeletingLeafListOfImpactedNode() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf3>one</groupleaf3>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <list3 xc:operation =\"remove\">"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf3", "Count Validation in multiple predicates with multiple list using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}
	@Test
	public void testMustwithMultiplePredicates_MultipleList_AbsolutePath_DeletingListOfImpactedNode2() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf3>one</groupleaf3>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <caselist1 xc:operation=\"remove\">"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf3", "Count Validation in multiple predicates with multiple list using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}

	@Test
	public void testMustwithMultiplePredicates_MultipleList_AbsolutePath_DeletingLeafListOfImpactedNode3() throws ModelNodeInitException {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"</list1>"
				+"  <list1>"
				+"      <name>two</name>"
				+"      <list3>"
				+"          <listid>one</listid>"
				+"      </list3>"
				+"      <caselist1>"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"  <list2>"
				+"      <name2>one</name2>"
				+"      <tm-object>"
				+"          <groupleaf3>one</groupleaf3>"
				+"      </tm-object>"
				+"  </list2>"
				+"</rootNode>" ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<rootNode xmlns=\"urn:org:bbf2:pma:leafref:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <list1>"
				+"      <name>one</name>"
				+"      <caselist1 xc:operation=\"remove\">"
				+"          <name1>one</name1>"
				+"      </caselist1>"
				+"  </list1>"
				+"</rootNode>" ;
		response = editConfig(m_server, m_clientInfo, requestXml1, false);
		checkErrors(response, "/lrValidation:rootNode/lrValidation:list2[lrValidation:name2='one']/lrValidation:tm-object/lrValidation:groupleaf3", "Count Validation in multiple predicates with multiple list using absolute path fails", NetconfRpcErrorTag.OPERATION_FAILED);
	}


    private Set<String> getErrorList() {
        Set<String> errorList = new HashSet<String>();
        errorList.add("Leafref with circular dependency: leafref-to-when");
        errorList.add("Leafref with circular dependency: leafref-if-feature1");
        errorList.add("Leafref with circular dependency: leaf-list-with-leafref1");
        errorList.add("Leafref with circular dependency: leaf-for-choice");
        errorList.add("Leafref with circular dependency: leaflist-to-leaf");
        errorList.add("Leafref with circular dependency: leafref-to-leafref2");
        errorList.add("Leafref with circular dependency: leafref-under-case2-for-when");
        errorList.add("Leafref with circular dependency: leafref-to-leafref1");
        errorList.add("Leafref with circular dependency: leafref-inside-list");
        errorList.add("Leafref with circular dependency: leaf-list-with-leafref2");
        errorList.add("Leafref with circular dependency: leafref-to-must");
        errorList.add("Leafref with circular dependency: leafref-under-case1");
        errorList.add("Leafref with circular dependency: leafref-c2b-c2a-leafref");
        errorList.add("Leafref with circular dependency: leafref-c2b-c2a-must");
        errorList.add("Leafref with circular dependency: leafref-c2a-c2b-leafref");
        errorList.add("Leafref with circular dependency: leafref-c2a-c2b-when");
        errorList.add("Leafref with circular dependency: leaf-for-leafref-inside-list");
        errorList.add("Leafref with circular dependency: leafref-container-when");
        errorList.add("Leafref with circular dependency: leafref-under-case2-for-must");
        errorList.add("Leafref with circular dependency: leafref-for-union-type2");
        errorList.add("Leafref with circular dependency: leafref-for-union-type3");
        errorList.add("Leafref with circular dependency: leafref-to-leaf-with-union");
        errorList.add("Leafref with circular dependency: leafref-for-union-type");
        errorList.add("Leafref with circular dependency: leaf-for-leafref");
        errorList.add("Leafref with circular dependency: leafref-container-when2");
        errorList.add("Leafref with circular dependency: leafref-with-must-referring-to-self4");
        return errorList;
    }
}
