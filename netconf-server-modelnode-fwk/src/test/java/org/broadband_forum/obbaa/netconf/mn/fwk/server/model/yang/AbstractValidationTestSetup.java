package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest.createRequestFromString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public abstract class AbstractValidationTestSetup {
    protected String m_componentId = "test";
    protected DataStore m_dataStore;
    protected NbiNotificationHelper m_nbiNotificationHelper;
    protected NetconfClientInfo m_clientInfo=getClientInfo();
    protected SchemaRegistry m_schemaRegistry;
    protected InMemoryDSM m_modelNodeDsm;
    protected ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    protected SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    protected DSExpressionValidator m_expValidator = null;
    public static final String YANG_PATH = TestUtil.class.getResource("/yangs/example-jukebox.yang").getPath();
    public static final String XML_PATH = TestUtil.class.getResource("/example-jukebox.xml").getPath();
   
    private static Logger LOGGER = Logger.getLogger(AbstractValidationTestSetup.class);
    @BeforeClass
    public static void before(){
        System.setProperty(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "true");
        RequestScope.setEnableThreadLocalInUT(true);
    }
    
    @AfterClass
    public static void after(){
        System.setProperty(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "false");
        RequestScope.setEnableThreadLocalInUT(false);
        RequestScope.resetScope();
    }
    
    @Before
    public void enableThreadLocal(){
        RequestScope.setEnableThreadLocalInUT(true);
        RequestScope.resetScope();
    }
    
    @After
    public void disableThreadLocal() {
        RequestScope.setEnableThreadLocalInUT(false);
    }
    
    public abstract void setup() throws Exception;
    
    protected InMemoryDSM getInMemoryDSM(){
        return m_modelNodeDsm;
    }
    
    protected  NetconfClientInfo getClientInfo() {
         return new NetconfClientInfo("unit-test", 1);
     }
    
    protected void assertXMLStringEquals(String expectedOutput, String actualOutput) throws SAXException, IOException {
        Element expectedElement = TestUtil.transformToElement(expectedOutput);
        Element actualElement = TestUtil.transformToElement(actualOutput);
        TestUtil.assertXMLEquals(expectedElement, actualElement);        
    }
        
    protected void verifyGet(NetconfServer server, NetconfClientInfo clientInfo, String expectedOutput) throws SAXException, IOException {
        GetRequest request = new GetRequest();
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        server.onGet(clientInfo, request, response);
        Element expectedElement = TestUtil.transformToElement(expectedOutput);
        Element actualElement = TestUtil.transformToElement(response.responseToString());
        TestUtil.assertXMLEquals(expectedElement, actualElement);
        try {
            LOGGER.info("received Xml:"+DocumentUtils.documentToPrettyString(actualElement));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("not able to print output xml",e);
        }
    }
    
    protected void verifyGet(NetconfServer server, NetconfClientInfo clientInfo, String requestFilterString, String expectedOutput) throws SAXException, IOException {
    	GetRequest request = new GetRequest();
    	NetconfFilter filter = null;
    	request.setMessageId("1");
    	if (requestFilterString != null && !requestFilterString.isEmpty()) {
    		filter = new NetconfFilter();
    		Element filterElement = TestUtil.transformToElement(requestFilterString);
    		filter.addXmlFilter(filterElement);
    	}
    	request.setFilter(filter);
    	NetConfResponse response = new NetConfResponse();
    	response.setMessageId("1");
    	server.onGet(clientInfo, request, response);
    	Element expectedElement = TestUtil.transformToElement(expectedOutput);
    	Element actualElement = TestUtil.transformToElement(response.responseToString());
    	TestUtil.assertXMLEquals(expectedElement, actualElement);
    }
	 
    protected NetConfResponse editConfig(NetconfServer server, NetconfClientInfo clientInfo, String requestXml1, boolean assertTrue) {
           RequestScope.resetScope();
           EditConfigRequest request1 = createRequestFromString(requestXml1);
           request1.setMessageId("1");
           NetConfResponse response1 = new NetConfResponse().setMessageId("1");
           server.onEditConfig(clientInfo, request1, response1);
           LOGGER.info(response1.responseToString());
           if (!response1.isOk()) {
               LOGGER.error(response1.responseToString());
           }
           if (assertTrue)
               assertTrue(response1.isOk());
           else{
               assertFalse(response1.isOk());
               LOGGER.error(response1.responseToString());
           }
           RequestScope.resetScope();
           return response1;
    }

    public static SchemaPath buildSchemaPath(SchemaPath parent, QName qname){
        SchemaPath schemaPath = new SchemaPathBuilder().withParent(parent).appendQName(qname).build();
        return schemaPath;
    }   
    
    protected List<YangTextSchemaSource> getYangs(){return Collections.EMPTY_LIST;}
    
    protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException {
        return new SchemaRegistryImpl(getYangs(), new NoLockService());
    }
}
