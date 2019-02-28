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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class ActionRequestTest {

	private ActionRequest m_actionRequest = new ActionRequest();
	private Element m_inputElement;
	private QName m_qName;
	private SchemaPath m_schemaPath;

	@Before
	public void testUp() {
		m_qName = QName.create("test", "test");
		m_schemaPath = Mockito.mock(SchemaPath.class);
		m_inputElement = mock(Element.class);
	}

	@Test
	public void testSetAndGetActionQName() {
		m_actionRequest.setActionQName(m_qName);
		assertEquals(m_qName, m_actionRequest.getActionQName());
	}

	@Test
	public void testSetAndGetActionTreeElement() {
		m_actionRequest.setActionTreeElement(m_inputElement);
		assertEquals(m_inputElement, m_actionRequest.getActionTreeElement());
	}

	@Test
	public void testSetAndGetActionTargetpath() {
		m_actionRequest.setActionTargetpath(m_schemaPath);
		assertEquals(m_schemaPath, m_actionRequest.getActionTargetpath());
	}

	@Test
	public void testGetActionRequestDocument() throws NetconfMessageBuilderException {
		Element actionElement = DocumentUtils.createDocument().createElementNS("test", "test");
		m_actionRequest.setActionTreeElement(actionElement);
		Document document = m_actionRequest.getRequestDocument();
		Node rpcNode = document.getFirstChild();
		if (rpcNode instanceof Element) {
			assertTrue(((Element) rpcNode).getLocalName().equals("rpc"));
			Node actionNode = rpcNode.getFirstChild();
			if (actionNode instanceof Element) {
				assertTrue(((Element) actionNode).getLocalName().equals("action"));
				if ((actionNode).getFirstChild() instanceof Element) {
					assertTrue(((Element) (actionNode).getFirstChild()).getLocalName().equals("test"));
					assertTrue(((Element) (actionNode).getFirstChild()).getNamespaceURI().equals("test"));
				}
			}
		}
		assertNotNull(m_actionRequest.requestToString());
	}

	@Test(expected = NetconfMessageBuilderException.class)
	public void testGetActionRequestDocument_throwException() throws NetconfMessageBuilderException {
		m_actionRequest.setActionTreeElement(null);
		m_actionRequest.getRequestDocument();
		fail("NetconfMessageBuilderException is expected");
	}
}
