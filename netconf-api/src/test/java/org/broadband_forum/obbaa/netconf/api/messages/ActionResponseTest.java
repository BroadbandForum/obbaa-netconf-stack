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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ActionResponseTest {
	private ActionResponse m_response;
	private ActionDefinition m_actionDefinition = mock(ActionDefinition.class);
	private Element m_outputElement = null;
	private List<Element> m_outputElementList;

	@Before
	public void setup() throws NetconfMessageBuilderException {
		m_response = new ActionResponse();
		m_response.setMessageId("1");
		m_response.setOk(true);
		m_outputElementList = new ArrayList<Element>();
		m_outputElement = DocumentUtils.stringToDocument("<result xmlns=\"urn:example:rock\">SUCCESS</result>").getDocumentElement();
		m_outputElementList.add(m_outputElement);
		m_response.setActionOutputElements(m_outputElementList);
		m_response.setActionDefinition(m_actionDefinition);
	}

	@Test
	public void testRpcOutputElement() throws Exception {
		List<Element> rpcOutputElements = m_response.getActionOutputElements();
		assertEquals(1, rpcOutputElements.size());
		assertEquals(m_outputElement, rpcOutputElements.get(0));
		assertEquals(m_actionDefinition, m_response.getActionDefinition());
		String response = DocumentUtils.prettyPrint(m_response.getResponseDocument());
		assertEquals(getResponse(), response);
	}
	
	private String getResponse(){
		return "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
				+ "   <result xmlns=\"urn:example:rock\">SUCCESS</result>\n"
				+ "</rpc-reply>\n";
	}

	@Test
	public void testGetActionResponseDocument() throws NetconfMessageBuilderException {
		m_response = new ActionResponse();
		m_response.setMessageId("1");
		m_response.setOk(true);
		Document document = m_response.getResponseDocument();
		Node node = document.getFirstChild();
		if (node instanceof Element) {
			Element nodeElement = (Element) node;
			assertTrue(nodeElement.getLocalName().equals("rpc-reply"));
			assertTrue(((Element) (node).getFirstChild()).getLocalName().equals("ok"));
		}
	}

}
