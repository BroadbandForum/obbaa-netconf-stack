package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;


import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest.createRequestFromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class SchemaMountTest extends AbstractSchemaMountTest {
    @Mock private ModelNode m_modelNode;

	static QName createQName(String localName){
        return QName.create("schema-mount-test", "2018-01-03", localName);
    }
    
	private final static QName SCHEMA_MOUNT = createQName("schemaMount");
	private final static QName SCHEMA_MOUNT1 = createQName("schemaMount1");
	private final static QName INTERFACES = QName.create("test-interfaces", "interfaces");
    
    @BeforeClass
    public static void classSetup(){
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "true");
    }
    
    @AfterClass
    public static void classDestroy() {
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "false");
    }
    
    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
        SubSystem innerContainerSubsystem = new InnerContainerSubSystem();
        m_subSystemRegistry.register(COMPONENT_ID,INNER_CONTAINER_PATH,innerContainerSubsystem);
    }
    
    @After
    public void teardown(){
    	RequestScope.resetScope();
    }
    
    @Test
    public void testMountProviderInfo() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        
        Element requestElement = DocumentUtils.stringToDocument(request).getDocumentElement();
        MountProviderInfo mountProviderInfo = SchemaRegistryUtil.getMountProviderInfo(requestElement , m_schemaRegistry);
        DataSchemaNode mountPathDataSchemaNode = mountProviderInfo.getMountedDataSchemaNode();
        assertEquals("schemaMountPoint", mountPathDataSchemaNode.getQName().getLocalName());
        Node mountPathXmlNodeFromRequest = mountProviderInfo.getMountedXmlNodeFromRequest();
        assertEquals("schemaMountPoint", mountPathXmlNodeFromRequest.getLocalName());
        SchemaRegistry mountedRegistry = mountProviderInfo.getMountedRegistry();
        assertEquals("schemaMountPoint", mountedRegistry.getMountPath().getLastComponent().getLocalName());
    }
    
    @Test
    public void testProcessMissingDefaultData(){
    	when(m_modelNode.getModelNodeSchemaPath()).thenReturn(SCHEMA_MOUNT_POINT_PATH);
    	when(m_modelNode.hasSchemaMount()).thenReturn(true);
    	SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
    	when(m_modelNode.getMountRegistry()).thenReturn(mountRegistry);
    	AddDefaultDataInterceptor defaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry , m_schemaRegistry , m_expValidator);
    	defaultDataInterceptor.init();    	
    	EditContainmentNode editContainmentNode = new EditContainmentNode(SCHEMA_MOUNT_POINT, EditConfigOperations.CREATE);

    	EditContainmentNode interceptedContainmentNode = defaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
    	assertEquals(4, interceptedContainmentNode.getChildren().size());
    	assertNotNull(interceptedContainmentNode.getChildNode(SCHEMA_MOUNT));
    	assertNotNull(interceptedContainmentNode.getChildNode(SCHEMA_MOUNT1));
    	assertNotNull(interceptedContainmentNode.getChildNode(INTERFACES));    	
    }
    
    @Test
    public void testEditWithTwoMountRegistry() throws Exception {
    	 SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang")), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
         mountRegistry2.setName("PLUG-2.0");
         makeSetup(mountRegistry2);
         String request = 
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                 " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                 "  <xml-subtree>" +
                 "  <plugType>PLUG-1.0</plugType>" +
                 "   <schemaMountPoint>" +
                 "    <schemaMount xmlns=\"schema-mount-test\">" +
                 "     <leaf1>leaf1</leaf1>" +
                 "     <container1>" +
                 "      <list1> " +
                 "         <key>key</key>" +
                 "         <leafListMinElements>1</leafListMinElements>" +
                 "      </list1>" +
                 "     </container1>" +
                 "    </schemaMount>" +
                 "   </schemaMountPoint>" +
                 "  </xml-subtree>" +
                 "  <xml-subtree>" +
                 "  <plugType>PLUG-2.0</plugType>" +
                 "   <schemaMountPoint>" +
                 "    <schemaMount2nd xmlns=\"schema-2nd-mount-test\">" +
                 "     <leaf1>leaf1</leaf1>" +
                 "     <leaf2>false</leaf2>" +
                 "    </schemaMount2nd>" +
                 "   </schemaMountPoint>" +
                 "  </xml-subtree>" +
                 " </validation>"
                 ;
         editConfig(request);
    }
    
    @Test
    public void erorPathTest() throws Exception {
         String requestString = 
                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                 " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                 "  <xml-subtree>" +
                 "  <plugType>PLUG-1.0</plugType>" +
                 "   <schemaMountPoint>" +
                 "    <uk:unknown xmlns:uk=\"unknown-ns\">" +
                 "     <uk:leaf1>leaf1</uk:leaf1>" +
                 "    </uk:unknown>" +
                 "   </schemaMountPoint>" +
                 "  </xml-subtree>" +
                 " </validation>"
                 ;
         EditConfigRequest request = createRequestFromString(requestString);
         request.setMessageId("1");
         NetConfResponse response = new NetConfResponse().setMessageId("1");
         m_server.onEditConfig(m_clientInfo, request, response);
         String errorPath = response.getErrors().get(0).getErrorPath();
         assertEquals("/uk:unknown", errorPath);
    }
    
    @Test
    public void testDuplicateMountPoints() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf2</leaf1>" +
                "     <container1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        String errorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf:pma:validation?revision=2015-12-14)schemaMountPoint";
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals(EditContainmentNode.DATA_NOT_UNIQUE, error.getErrorAppTag());
    }
    
    @Test
    public void simpleEditGetTest() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
        
        RequestScope.resetScope();
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>leaf1</smt:leaf1>"
                        + "       <smt:container1>"
                        + "        <smt:list1>"
                        + "         <smt:key>key</smt:key>"
                        + "         <smt:leafListMinElements>1</smt:leafListMinElements>"
                        + "        </smt:list1>"
                        + "       </smt:container1>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testEnableContainerOnWhenCondition() throws Exception {
        ArgumentCaptor<EditConfigRequest> requestCaptor = ArgumentCaptor.forClass(EditConfigRequest.class);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf2>true</leaf2>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String internalEditRequest = 
                "<rpc message-id=\"internal_edit:1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "  <edit-config>"
                        + "   <target>"
                        + "    <running/>"
                        + "   </target>"
                        + "   <default-operation>merge</default-operation>"
                        + "   <test-option>set</test-option>"
                        + "   <error-option>stop-on-error</error-option>"
                        + "   <config>"
                        + "   <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + "    <xml-subtree>"
                        + "  <plugType>PLUG-1.0</plugType>" 
                        + "     <schemaMountPoint>"
                        + "      <schemaMount xmlns=\"schema-mount-test\">"
                        + "       <enableContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\"/>"
                        + "      </schemaMount>"
                        + "     </schemaMountPoint>"
                        + "    </xml-subtree>"
                        + "   </validation>"
                        + "   </config>"
                        + "  </edit-config>"
                        + " </rpc>"
                        ;
        Element internalEditRequestElement = DocumentUtils.stringToDocument(internalEditRequest).getDocumentElement();

        verify(m_integrityService, times(3)).createInternalEditRequests(requestCaptor.capture(), any(NetconfClientInfo.class));
        List<EditConfigRequest> editRequests = requestCaptor.getAllValues();
        for(EditConfigRequest editRequest : editRequests) {
            if(editRequest.getMessageId().equals("internal_edit:1")){
                TestUtil.assertXMLEquals(internalEditRequestElement, editRequest.getRequestDocument().getDocumentElement());
            }
        }
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:enableContainer/>"
                        + "      <smt:leaf2>true</smt:leaf2>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        //disabling the leaf2, so enableContainer will get deleted
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf2>false</leaf2>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:leaf2>false</smt:leaf2>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }
    
    @Test
    public void editGetWithConstraintTest() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leaf1>key</leaf1>" +
                "         <leaf2>20</leaf2>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
    }    
    
    @Test
    public void testLeafRefConstraint() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leaf3>20</leaf3>" +
                "         <leaf4>20</leaf4>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
        
        //verify response has the changes
           String response = 
        		"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +" <data>"
                +"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "   <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                +"      <smt:container1>"
                +"       <smt:list1>"
                +"        <smt:key>key</smt:key>"
                +"        <smt:leaf3>20</smt:leaf3>"
                +"        <smt:leaf4>20</smt:leaf4>"
                + "       <smt:leafListMinElements>1</smt:leafListMinElements>"
                +"       </smt:list1>"
                +"      </smt:container1>"
                +"     <smt:leaf1>leaf1</smt:leaf1>"
                +"    </smt:schemaMount>"
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>"
                +"</data>"
                +"</rpc-reply>";
        
        verifyGet(m_server, m_clientInfo, response);
    } 

    @SuppressWarnings("unchecked")
	@Test
    public void testGetOnStateData() throws Exception {
    	QName stateContainerQName = createQName("stateContainer");
		SchemaPath schemaPath = SchemaPath.create(true, stateContainerQName );
		SubSystem stateContainerSubSystem = mock(SubSystem.class);
		SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode, (Object)null);
		subSystemRegistry.register("test", schemaPath , stateContainerSubSystem );
		String result = 
			"<smt:stateContainer xmlns:smt=\"schema-mount-test\">"
	                +"       <smt:stateList>"
	                +"        <smt:keyLeaf>key</smt:keyLeaf>"
	                +"       </smt:stateList>"
	                +"      </smt:stateContainer>";
		Element resultElement = DocumentUtils.stringToDocument(result).getDocumentElement();
		doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {

			@Override
			public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
				Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation.getArguments()[0];
				ModelNodeId modelNodeId = map.keySet().iterator().next();
				Map<ModelNodeId, List<Element>> result = new HashMap<>();
				result.put(modelNodeId, Arrays.asList(resultElement));
				return result;
			}
		}).when(stateContainerSubSystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class));
    	String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leaf3>20</leaf3>" +
                "         <leaf4>20</leaf4>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
    	
    	String filter = "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"      <smt:stateContainer xmlns:smt=\"schema-mount-test\">"
                +"       <smt:stateList>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList>"
                +"      </smt:stateContainer>"
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>";
        
           String response = 
        		"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +" <data>"
                +"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                +"   <validation:xml-subtree>"
                +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"      <smt:stateContainer xmlns:smt=\"schema-mount-test\">"
                +"       <smt:stateList>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList>"
                +"      </smt:stateContainer>"
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>"
                +"</data>"
                +"</rpc-reply>";
        
        verifyGet(m_server, m_clientInfo, filter, response);
    }
    
    @Test
    public void testConstraintPredicatesOnDifferentRootNodes() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf3</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf2</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "      <forwarder>" +
                        "        <name>forwarder2</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port3</name>" +
                        "          <sub-interface>intf3</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        //verify response has the changes
        String response =  
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
                        " <data>" +
                        " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf3</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf2</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "      <forwarder>" +
                        "        <name>forwarder2</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port3</name>" +
                        "          <sub-interface>intf3</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"  +
                        "</data>"  +
                        "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }
    
    @Test
    public void testInvalidConstraintPredicatesOnDifferentRootNodes(){
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        
        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("A forwarder must have 1 port with usage network-port.", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:forwarding/smt:forwarder[smt:name='forwarder1']/smt:ports",
                ncResponse.getErrors().get(0).getErrorPath());
    }
    
    @Test
    public void testMustConstraintLeafrefOnSameRootNode() throws Exception{
    	String request = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
    					" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
    					"  <xml-subtree>" +
    					"  <plugType>PLUG-1.0</plugType>" +
    					"   <schemaMountPoint>" +
    					"    <schemaMount xmlns=\"schema-mount-test\">" +
    					"     <leaf1>leaf1</leaf1>" +
    					"     <container1>" +
    					"      <list1> " +
    					"         <key>key</key>" +
    					"         <leaf3>20</leaf3>" +
    					"         <leaf4>20</leaf4>" +
    					"         <leafListMinElements>1</leafListMinElements>" +
    					"         <type>test</type>" +
    					"      </list1>" +
    					"     </container1>" +
    					"     <channelpair>" +
    					"      <channelgroup-ref>key</channelgroup-ref>" +
    					"     </channelpair>" +
    					"    </schemaMount>" +
    					"   </schemaMountPoint>" +
    					"  </xml-subtree>" +
    					" </validation>"
    					;
    	editConfig(request);

    	//verify response has the changes
    	String response = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    					+" <data>"
    					+"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
    					+"   <validation:xml-subtree>"
    					+ "  <validation:plugType>PLUG-1.0</validation:plugType>" 
    					+"    <validation:schemaMountPoint>"
    					+"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
    					+"      <smt:container1>"
    					+"       <smt:list1>"
    					+"        <smt:key>key</smt:key>"
    					+"        <smt:leaf3>20</smt:leaf3>"
    					+"        <smt:leaf4>20</smt:leaf4>"
    					+"        <smt:leafListMinElements>1</smt:leafListMinElements>"
    					+"        <smt:type>test</smt:type>" 
    					+"       </smt:list1>"
    					+"      </smt:container1>"
    					+"      <smt:channelpair>" 
    					+"       <smt:channelgroup-ref>key</smt:channelgroup-ref>" 
    					+"      </smt:channelpair>" 
    					+"     <smt:leaf1>leaf1</smt:leaf1>"
    					+"    </smt:schemaMount>"
    					+"   </validation:schemaMountPoint>"
    					+"  </validation:xml-subtree>"
    					+" </validation:validation>"
    					+"</data>"
    					+"</rpc-reply>";

    	verifyGet(m_server, m_clientInfo, response);

    	request = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
    					" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
    					"  <xml-subtree>" +
    					"  <plugType>PLUG-1.0</plugType>" +
    					"   <schemaMountPoint>" +
    					"    <schemaMount xmlns=\"schema-mount-test\">" +
    					"     <leaf1>leaf1</leaf1>" +
    					"     <container1>" +
    					"      <list1> " +
    					"         <key>key</key>" +
    					"         <leaf3>20</leaf3>" +
    					"         <leaf4>20</leaf4>" +
    					"         <leafListMinElements>1</leafListMinElements>" +
    					"         <type>test</type>" +
    					"      </list1>" +
    					"     </container1>" +
    					"     <channelpair>" +
    					"      <channelgroup-ref>key1</channelgroup-ref>" +
    					"     </channelpair>" +
    					"    </schemaMount>" +
    					"   </schemaMountPoint>" +
    					"  </xml-subtree>" +
    					" </validation>"
    					;
    	NetConfResponse ncResponse = editConfigAsFalse(request);
    	assertFalse(ncResponse.isOk());
    	assertEquals("must reference a channelgroup", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:channelpair/smt:channelgroup-ref",
				ncResponse.getErrors().get(0).getErrorPath());
    }
    
    @Test
    public void testMustConstraintLeafrefOnOtherRootNode() throws Exception{
    	String request = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
    					" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
    					"  <xml-subtree>" +
    					"  <plugType>PLUG-1.0</plugType>" +
    					"   <schemaMountPoint>" +
    					"    <schemaMount xmlns=\"schema-mount-test\">" +
    					"     <leaf1>leaf1</leaf1>" +
    					"     <container1>" +
    					"      <list1> " +
    					"         <key>key</key>" +
    					"         <leaf3>20</leaf3>" +
    					"         <leaf4>20</leaf4>" +
    					"         <leafListMinElements>1</leafListMinElements>" +
    					"         <type>test</type>" +
    					"      </list1>" +
    					"     </container1>" +
    					"    </schemaMount>" +
    					"    <schemaMount1 xmlns=\"schema-mount-test\">" +
    					"      <channelgroup-ref1>key</channelgroup-ref1>" +
    					"    </schemaMount1>" +
    					"   </schemaMountPoint>" +
    					"  </xml-subtree>" +
    					" </validation>"
    					;
    	editConfig(request);

    	//verify response has the changes
    	String response = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    					+" <data>"
    					+"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
    					+"   <validation:xml-subtree>"
    					+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
    					+"    <validation:schemaMountPoint>"
    					+"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
    					+"      <smt:container1>"
    					+"       <smt:list1>"
    					+"        <smt:key>key</smt:key>"
    					+"        <smt:leaf3>20</smt:leaf3>"
    					+"        <smt:leaf4>20</smt:leaf4>"
    					+"        <smt:leafListMinElements>1</smt:leafListMinElements>"
    					+"        <smt:type>test</smt:type>" 
    					+"       </smt:list1>"
    					+"      </smt:container1>"
    					+"     <smt:leaf1>leaf1</smt:leaf1>"
    					+"    </smt:schemaMount>"
    					+"    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"
    					+"      <smt:channelgroup-ref1>key</smt:channelgroup-ref1>"
    					+"    </smt:schemaMount1>"
    					+"   </validation:schemaMountPoint>"
    					+"  </validation:xml-subtree>"
    					+" </validation:validation>"
    					+"</data>"
    					+"</rpc-reply>";

    	verifyGet(m_server, m_clientInfo, response);

    	request = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
    					" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
    					"  <xml-subtree>" +
    					"  <plugType>PLUG-1.0</plugType>" +
    					"   <schemaMountPoint>" +
    					"    <schemaMount xmlns=\"schema-mount-test\">" +
    					"     <leaf1>leaf1</leaf1>" +
    					"     <container1>" +
    					"      <list1> " +
    					"         <key>key</key>" +
    					"         <leaf3>20</leaf3>" +
    					"         <leaf4>20</leaf4>" +
    					"         <leafListMinElements>1</leafListMinElements>" +
    					"         <type>test</type>" +
    					"      </list1>" +
    					"     </container1>" +
    					"    </schemaMount>" +
    					"    <schemaMount1 xmlns=\"schema-mount-test\">" +
    					"      <channelgroup-ref1>key1</channelgroup-ref1>" +
    					"    </schemaMount1>" +
    					"   </schemaMountPoint>" +
    					"  </xml-subtree>" +
    					" </validation>"
    					;
    	NetConfResponse ncResponse = editConfigAsFalse(request);
    	assertFalse(ncResponse.isOk());
    	assertEquals("must reference a channelgroup", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount1/smt:channelgroup-ref1",
				ncResponse.getErrors().get(0).getErrorPath());
    }
    
    @Test
    public void testCreateInnerChildNodeContainer() throws Exception{
    	String request = 
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
    					" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
    					"  <xml-subtree>" +
    					"  <plugType>PLUG-1.0</plugType>" +
    					"   <schemaMountPoint>" +
    					"    <schemaMount xmlns=\"schema-mount-test\">" +
    					"     <leaf1>leaf1</leaf1>" +
    					"     <container1>" +
    					"      <list1> " +
    					"         <key>key</key>" +
    					"         <leaf3>20</leaf3>" +
    					"         <leaf4>20</leaf4>" +
    					"         <leafListMinElements>1</leafListMinElements>" +
    					"         <type>test</type>" +
    					"      </list1>" +
    					"     </container1>" +
    					"    </schemaMount>" +
    					"    <schemaMount1 xmlns=\"schema-mount-test\">" +
    					"      <channelgroup-ref1>key</channelgroup-ref1>" +
    					"      <innerSchemaMount1>"   +
    					"       <innerSchemaMountLeaf>test</innerSchemaMountLeaf>" +
    					"      </innerSchemaMount1>"   +
    					"    </schemaMount1>" +
    					"   </schemaMountPoint>" +
    					"  </xml-subtree>" +
    					" </validation>"
    					;
    	editConfig(request);
    	//verify response has the changes
    	String response = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    					+" <data>"
    					+"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
    					+"   <validation:xml-subtree>"
    					+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
    					+"    <validation:schemaMountPoint>"
    					+"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
    					+"      <smt:container1>"
    					+"       <smt:list1>"
    					+"        <smt:key>key</smt:key>"
    					+"        <smt:leaf3>20</smt:leaf3>"
    					+"        <smt:leaf4>20</smt:leaf4>"
    					+"        <smt:leafListMinElements>1</smt:leafListMinElements>"
    					+"        <smt:type>test</smt:type>" 
    					+"       </smt:list1>"
    					+"      </smt:container1>"
    					+"     <smt:leaf1>leaf1</smt:leaf1>"
    					+"    </smt:schemaMount>"
    					+"    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"
    					+"      <smt:channelgroup-ref1>key</smt:channelgroup-ref1>"
    					+"      <smt:innerSchemaMount1>"
    					+"       <smt:innerSchemaMountLeaf>test</smt:innerSchemaMountLeaf>"
    					+"      </smt:innerSchemaMount1>"
    					+"    </smt:schemaMount1>"
    					+"   </validation:schemaMountPoint>"
    					+"  </validation:xml-subtree>"
    					+" </validation:validation>"
    					+"</data>"
    					+"</rpc-reply>";

    	verifyGet(m_server, m_clientInfo, response);
    }
    
    @Test
    public void testMountPoint() {
        Collection<DataSchemaNode> rootNodes = m_schemaRegistry.getRootDataSchemaNodes();
        for (DataSchemaNode rootNode:rootNodes) {
            if (rootNode.getQName().getLocalName().contains("mountPointTest")) {
                assertTrue(AnvExtensions.MOUNT_POINT.isExtensionIn(rootNode));
            }

            if (rootNode.getQName().getLocalName().contains("mountPointTest1")) {
                assertTrue(AnvExtensions.MOUNT_POINT.isExtensionIn(rootNode));
            }
        }
    }

	@Test
	public void testAugmentValidationInSchemaMount() throws Exception {
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <leaf1>test1</leaf1>" + 
				"     <outer>" +
				"       <a>dummyValue</a>" + 
				"       <b>dummyValue</b>" + 
				"     </outer>" + 
				"    </schemaMount>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		NetConfResponse response = editConfigAsFalse(request);
		assertFalse(response.isOk());
		assertEquals("Violate when constraints: leaf1 = 'test'", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:outer",
				response.getErrors().get(0).getErrorPath());
		
		//positive case
		request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <leaf1>test</leaf1>" + 
				"     <outer>" +
				"       <a>dummyValue</a>" + 
				"       <b>dummyValue</b>" + 
				"     </outer>" + 
				"    </schemaMount>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		
		editConfig(request);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetRequest_FilterStateData() throws Exception {
		QName mountQName = createQName("schemaMount");
		SchemaPath mountSchemaPath = SchemaPath.create(true, mountQName);
		QName stateContainerQName = createQName("stateContainer1");
		SchemaPath stateContainerSchemaPath = AbstractValidationTestSetup.buildSchemaPath(mountSchemaPath,
				stateContainerQName);
		SubSystem stateContainerSubSystem = mock(SubSystem.class);
		SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode, (Object) null);
		subSystemRegistry.register("test", stateContainerSchemaPath, stateContainerSubSystem);
		String result =
					 "	<smt:stateContainer1 xmlns:smt=\"schema-mount-test\">"
	                +"       <smt:stateList1>"
	                +"        	<smt:keyLeaf>key</smt:keyLeaf>"
	                +"       </smt:stateList1>"
	                +"   </smt:stateContainer1>";
		Element resultElement = DocumentUtils.stringToDocument(result).getDocumentElement();
		doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {
			@Override
			public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
				Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation
						.getArguments()[0];
				ModelNodeId modelNodeId = map.keySet().iterator().next();
				Map<ModelNodeId, List<Element>> result = new HashMap<>();
				result.put(modelNodeId, Arrays.asList(resultElement));
				return result;
			}
		}).when(stateContainerSubSystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class));
    	String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leaf3>20</leaf3>" +
                "         <leaf4>20</leaf4>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
    	
    	String stateFilter = "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"    <smt:schemaMount xmlns:smt=\"schema-mount-test\">" 
                +"      <smt:stateContainer1>"
                +"       <smt:stateList1>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList1>"
                +"      </smt:stateContainer1>"
                +"    </smt:schemaMount>" 
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>";
        
           String response = 
        		"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +" <data>"
                +"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"    <smt:schemaMount xmlns:smt=\"schema-mount-test\">" 
                +"      <smt:stateContainer1 xmlns=\"schema-mount-test\">"
                +"       <smt:stateList1>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList1>"
                +"      </smt:stateContainer1>"
                +"    </smt:schemaMount>" 
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>"
                +"</data>"
                +"</rpc-reply>";
        
        verifyGet(m_server, m_clientInfo, stateFilter, response);
    }
	
	public void leafRefConstraintWithDifferentRootNodes_InvalidRefValue() throws Exception {
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <container1>" + 
				"       <profile>profile1</profile>" + 
				"	  </container1>"+
				"    </schemaMount>" +
				"    <schemaMount1 xmlns=\"schema-mount-test\">" + 
				"     	<profile>" +
				"       	<name>profile</name>" + 
				"     	</profile>" + 
				"    </schemaMount1>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		NetConfResponse response = editConfigAsFalse(request);
		assertFalse(response.isOk());
		assertEquals("Dependency violated, 'profile1' must exist", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:profile",
				response.getErrors().get(0).getErrorPath());
	}
	
	public void leafRefWithDifferentRootNodes() throws Exception {
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <container1>" + 
				"       <profile>testprofile</profile>" + 
				"	  </container1>"+
				"    </schemaMount>" +
				"    <schemaMount1 xmlns=\"schema-mount-test\">" + 
				"     	<profile>" +
				"       	<name>testprofile</name>" + 
				"     	</profile>" + 
				"    </schemaMount1>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		editConfig(request);
		
        String response = 
	        "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
	        " <data>"+
	        "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"+
	        "   <validation:xml-subtree>"+
	        "     <validation:plugType>PLUG-1.0</validation:plugType>"+ 
	        "    <schemaMountPoint xmlns=\"urn:org:bbf:pma:validation\">"+
	        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
	        "        <smt:container1>"+
	        "          <smt:profile>testprofile</smt:profile>"+
	        "        </smt:container1>"+
	        "     </smt:schemaMount>"+
	        "     <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"+
	        "        <smt:profile>"+
	        "          <smt:name>testprofile</smt:name>"+
	        "        </smt:profile>"+
	        "     </smt:schemaMount1>"+
	        "    </schemaMountPoint>"+
	        "   </validation:xml-subtree>"+
	        "  </validation:validation>"+
	        " </data>"+
	        "</rpc-reply>";
     		
		verifyGet(m_server, m_clientInfo, response);
		
		request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <container1 xc:operation=\"remove\">" + 
				"       <profile>testprofile</profile>" + 
				"	  </container1>"+
				"    </schemaMount>" +
				"    <schemaMount1 xmlns=\"schema-mount-test\">" + 
				"     	<profile xc:operation=\"remove\">" +
				"       	<name>testprofile</name>" + 
				"     	</profile>" + 
				"    </schemaMount1>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		editConfig(request);
	}
	
	public void leafRefWithDifferentRootNodes_RemoveWithoutRefValue() throws Exception {
		
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <container1>" + 
				"       <profile>testprofile</profile>" + 
				"	  </container1>"+
				"    </schemaMount>" +
				"    <schemaMount1 xmlns=\"schema-mount-test\">" + 
				"     	<profile>" +
				"       	<name>testprofile</name>" + 
				"     	</profile>" + 
				"    </schemaMount1>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		editConfig(request);
		
		request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount1 xmlns=\"schema-mount-test\">" + 
				"     	<profile xc:operation=\"remove\">" +
				"       	<name>testprofile</name>" + 
				"     	</profile>" + 
				"    </schemaMount1>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		
		NetConfResponse response = editConfigAsFalse(request);
		assertFalse(response.isOk());
		assertEquals("Dependency violated, 'testprofile' must exist", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:profile",
				response.getErrors().get(0).getErrorPath());
	}
	
    @Test
    public void testGetForStateContainers() throws Exception {
    	
    	//create element1 with some configuration under mount point
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <dummyList>" +
                "     <name>element1</name>" +
                "   </dummyList>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "     <container1>" +
                "      <list1> " +
                "         <key>key</key>" +
                "         <leafListMinElements>1</leafListMinElements>" +
                "      </list1>" +
                "     </container1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
        
        //create element2
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <dummyList>" +
                "     <name>element2</name>" +
                "   </dummyList>" +
                "  </xml-subtree>" +
                " </validation>"
                ;
        editConfig(request);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:dummyList>" 
                        + "      <validation:name>element1</validation:name>" 
                        + "      <validation:innerContainer>" 
                        + "       <validation:state>reachable</validation:state>" 
                        + "      </validation:innerContainer>" 
                        + "     </validation:dummyList>" 
                        + "     <validation:dummyList>" 
                        + "      <validation:name>element2</validation:name>" 
                        + "      <validation:innerContainer>" 
                        + "       <validation:state>notReachable</validation:state>" 
                        + "      </validation:innerContainer>" 
                        + "     </validation:dummyList>" 
                        + "     <schemaMountPoint xmlns=\"urn:org:bbf:pma:validation\">"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>leaf1</smt:leaf1>"
                        + "       <smt:container1>"
                        + "        <smt:list1>"
                        + "         <smt:key>key</smt:key>"
                        + "         <smt:leafListMinElements>1</smt:leafListMinElements>"
                        + "        </smt:list1>"
                        + "       </smt:container1>"
                        + "      </smt:schemaMount>"
                        + "     </schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }
    
	/*
	 * Added to test proper displaying of State data
	 */
	private class InnerContainerSubSystem extends AbstractSubSystem {
		@Override
		public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
				Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
			Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
			Document doc = DocumentUtils.createDocument();
			for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
				ModelNodeId modelNodeId = entry.getKey();
				List<Element> stateElements = new ArrayList<>();
				String elementName = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue();
				Element innerContainerElement = doc.createElementNS(VALIDATION_NS,
						getPrefixedLocalName("validation", INNER_CONTAINER_QNAME.getLocalName()));
				Element stateLeafElement = doc.createElementNS(VALIDATION_NS,
						getPrefixedLocalName("validation", "state"));
				if (elementName.equals("element1")) {
					stateLeafElement.setTextContent("reachable");
				} else if (elementName.equals("element2")) {
					stateLeafElement.setTextContent("notReachable");
				}
				innerContainerElement.appendChild(stateLeafElement);
				stateElements.add(innerContainerElement);
				stateInfo.put(modelNodeId, stateElements);
			}
			return stateInfo;
		}
	}

	private static String getPrefixedLocalName(String prefix, String localname) {
		return prefix + ":" + localname;
	}
	
	@Test
	public void testInternalEditRequest_WhenConstraintWithDefault() throws Exception {
		
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
				" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
				"  <xml-subtree>" + 
				"  <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <schemaMount xmlns=\"schema-mount-test\">" + 
				"     <container1>" + 
				"       <test-auth-fail>apple</test-auth-fail>" +
				"	  </container1>"+
				"    </schemaMount>" +
				"   </schemaMountPoint>" + 
				"  </xml-subtree>" + 
				" </validation>";
		editConfig(request);
		
        String response = 
    	        "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
    	        " <data>"+
    	        "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"+
    	        "   <validation:xml-subtree>"+
    	        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
    	        "    <schemaMountPoint xmlns=\"urn:org:bbf:pma:validation\">"+
    	        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
    	        "        <smt:container1>"+
    	        "			<smt:test-auth-fail>apple</smt:test-auth-fail>"+
    	        "				<smt:trap>"+
    	        "					<smt:auth-fail>test</smt:auth-fail>"+
    	        "				</smt:trap>"+
    	        "        </smt:container1>"+
    	        "     </smt:schemaMount>"+
    	        "    </schemaMountPoint>"+
    	        "   </validation:xml-subtree>"+
    	        "  </validation:validation>"+
    	        " </data>"+
    	        "</rpc-reply>";
         		
    		verifyGet(m_server, m_clientInfo, response);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetWithFilterOnListStateData() throws Exception {
		
		QName testStateList = createQName("testStateList");
		SchemaPath schemaPath = SchemaPath.create(true, testStateList );
		SubSystem testStateListSubsystem = mock(SubSystem.class);
		SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode, (Object)null);
		subSystemRegistry.register("dummyId", schemaPath , testStateListSubsystem );
		
		String entry1 =
				  "  <smt:testStateList xmlns:smt=\"schema-mount-test\">"
				+ "    <smt:name>testName1</smt:name>"
				+ "      <smt:dummy-leaf1>dummy1</smt:dummy-leaf1>"
				+ "  </smt:testStateList>";
		
		String entry2 =
				  "  <smt:testStateList xmlns:smt=\"schema-mount-test\">"
				+ "    <smt:name>testName2</smt:name>"
				+ "      <smt:dummy-leaf1>dummy2</smt:dummy-leaf1>"
				+ "  </smt:testStateList>";
		
		Element entry1Element = DocumentUtils.stringToDocument(entry1).getDocumentElement();
		Element entry2Element = DocumentUtils.stringToDocument(entry2).getDocumentElement();
		List<Element> resultElements = new ArrayList<>();
		resultElements.add(entry1Element);
		resultElements.add(entry2Element);
		
		doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {

			@Override
			public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
				Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation.getArguments()[0];
				ModelNodeId modelNodeId = map.keySet().iterator().next();
				Map<ModelNodeId, List<Element>> result = new HashMap<>();
				result.put(modelNodeId, resultElements);
				return result;
			}
		}).when(testStateListSubsystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class));
		
		String request =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf:pma:validation\">" +
						"  <xml-subtree>" +
						"  <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <schemaMount xmlns=\"schema-mount-test\">" +
						"     <leaf1>leaf1</leaf1>" +
						"     <container1>" +
						"      <list1> " +
						"         <key>key</key>" +
						"         <leaf3>20</leaf3>" +
						"         <leaf4>20</leaf4>" +
						"         <leafListMinElements>1</leafListMinElements>" +
						"      </list1>" +
						"     </container1>" +
						"    </schemaMount>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
				;
		editConfig(request);

		String filterOnStateListKey = 
				 " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
				+"   <validation:xml-subtree>"
				+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
				+"    <validation:schemaMountPoint>"
		        +      "<smt:testStateList xmlns:smt=\"schema-mount-test\">"
				+"        <smt:name />"
				+"      </smt:testStateList>"
				+"   </validation:schemaMountPoint>"
				+"  </validation:xml-subtree>"
				+" </validation:validation>";

		String response =
				        "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> " +
						" <data> " +
						"  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"> " +
						"   <validation:xml-subtree> " +
						"     <validation:plugType>PLUG-1.0</validation:plugType>" + 
						"    <schemaMountPoint xmlns=\"urn:org:bbf:pma:validation\"> " +
						"     <smt:testStateList xmlns:smt=\"schema-mount-test\"> " +
						"      <smt:dummy-leaf1>dummy1</smt:dummy-leaf1> " +
						"      <smt:name>testName1</smt:name> " +
						"     </smt:testStateList> " +
						"     <smt:testStateList xmlns:smt=\"schema-mount-test\"> " +
						"      <smt:dummy-leaf1>dummy2</smt:dummy-leaf1> " +
						"      <smt:name>testName2</smt:name> " +
						"     </smt:testStateList> " +
						"    </schemaMountPoint> " +
						"   </validation:xml-subtree> " +
						"  </validation:validation> " +
						" </data> " +
						"</rpc-reply>";

		verifyGet(m_server, m_clientInfo, filterOnStateListKey, response);
	}
}
