package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NoopSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class ActionValidatorSMTest extends AbstractSchemaMountTest {
	
	private NetConfResponse m_response = new ActionResponse().setMessageId("1");
	protected AddDefaultDataInterceptor m_addDefaultDataInterceptor;
	@Mock private SubSystem m_testActionSubSystem; 
	
	private void doSetup() throws Exception {
		YangUtils.deployInMemoryHelpers(getYang(), getSubSystem(), m_modelNodeHelperRegistry,m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry);
        addRootNodeHelpers();
        m_server.setRunningDataStore(m_dataStore);
        if (!m_rootModelNodeAggregator.getModelServiceRoots().isEmpty()){
            m_rootModelNode = m_rootModelNodeAggregator.getModelServiceRoots().get(0);
        }
        getSubSystemRegistry().register("test", SchemaPath.create(true, QName.create("urn:example:test-mount-action", "2018-01-03", "test-mount-action-container")), m_testActionSubSystem);
        when(m_testActionSubSystem.executeAction(any(ActionRequest.class))).thenReturn(getOutput());
    }
	
	private List<Element> getOutput() throws Exception {
		String response =  "<test:reset-finished-at xmlns:test=\"urn:example:test-mount-action\">2014-07-29T13:42:00Z</test:reset-finished-at>";
		Document document = DocumentUtils.stringToDocument(response);
		return Arrays.asList(document.getDocumentElement());
	}

	protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }
	
	protected SubSystem getSubSystem() {
        return new NoopSubSystem();
    }
	
	protected List<String> getYang() {
        List<String> fileList = new ArrayList<String>();
        fileList.add("/datastorevalidatortest/yangs/test-mount-action.yang");
    	return fileList;
    }

	@Test
    public void testValidActionInContainer() throws Exception {     
    	doSetup();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<validation xmlns=\"urn:org:bbf:pma:validation\">" +
    			"<xml-subtree>" +
    			"<plugType>PLUG-1.0</plugType>" +
    			"<schemaMountPoint>" +
    			"<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
    			"<test:mount-container-reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:mount-container-reset>"+
    			"</test:test-mount-action-container>"+
    			"</schemaMountPoint>" +
    			"</xml-subtree>" +
    			"</validation>" +
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
    }
	
	private Element getActionResponseElement() throws NetconfMessageBuilderException{
		String response =  "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
		                    "<test:reset-finished-at xmlns:test=\"urn:example:test-mount-action\">2014-07-29T13:42:00Z</test:reset-finished-at>" +
		                    "</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(response);
		return document.getDocumentElement();
	}
}
