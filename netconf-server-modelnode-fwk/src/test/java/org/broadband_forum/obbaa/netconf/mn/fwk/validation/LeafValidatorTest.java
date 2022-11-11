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

package org.broadband_forum.obbaa.netconf.mn.fwk.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Element;

public class LeafValidatorTest {
	private SchemaRegistry m_schemaRegistry;
	private LeafSchemaNode m_leafSchemaNode;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private DSExpressionValidator m_expValidator;
	private LeafValidator m_leafValidator;

	@Before
	public void setUp() throws SchemaBuildException {
		m_schemaRegistry = mock(SchemaRegistry.class);
		m_leafSchemaNode = mock(LeafSchemaNode.class);
		m_expValidator = mock(DSExpressionValidator.class);
		m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
		m_leafValidator = new LeafValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_leafSchemaNode,
				m_expValidator);
	}

	@Test
	public void testValidateException() throws Exception {
		try {
			Element dataNode = mock(Element.class);
			RequestType requestType = RequestType.EDIT_CONFIG;
			when(dataNode.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION))
					.thenReturn(EditConfigOperations.DELETE);
			when(m_leafSchemaNode.isMandatory()).thenReturn(true);
			when(dataNode.getNodeName()).thenReturn("slicing:group-name");
			m_leafValidator.validate(dataNode, requestType);
			fail("Expected exception");
		} catch (ValidationException e) {
			assertEquals("Cannot delete the mandatory attribute 'slicing:group-name'",
					e.getRpcError().getErrorMessage());
		}

	}

	@Test
	public void testValidate_PasswordLeaf() throws Exception{

		String keyFilePath = getClass().getResource("/domvisitortest/keyfile.plain").getPath();
		CryptUtil2 cryptUtil2 = new CryptUtil2();
		cryptUtil2.setKeyFilePathForTest(keyFilePath);
		cryptUtil2.initFile();

		when(m_schemaRegistry.getName()).thenReturn("mountpoint");
		YinAnnotationService yinRegistry = mock(YinAnnotationService.class);
		when(m_schemaRegistry.getYinAnnotationService()).thenReturn(yinRegistry);
		when(yinRegistry.isPassword(m_leafSchemaNode,"mountpoint")).thenReturn(true);
		TypeValidator typeValidator = mock(TypeValidator.class);
		when(m_schemaRegistry.getValidator(Mockito.any())).thenReturn(typeValidator);

		Element dataNode = DocumentUtils.getDocumentElement("<password-leaf xmlns=\"ns\">$-0$uhq0MlYHxJPk9n5glfzm+kRKrXzYdhZY/QXlzgq/x9w=</password-leaf>");
		m_leafValidator.validate(dataNode,RequestType.EDIT_CONFIG);
		ArgumentCaptor<Element> elementArgumentCaptor = ArgumentCaptor.forClass(Element.class);
		verify(typeValidator).validate(elementArgumentCaptor.capture(),Mockito.eq(false),Mockito.eq(null));
		Element actualElement = elementArgumentCaptor.getValue();
		assertEquals("<password-leaf xmlns=\"ns\">testdata</password-leaf>",
				DocumentUtils.documentToPrettyString(actualElement).trim());


		when(yinRegistry.isPassword(m_leafSchemaNode,"mountpoint")).thenReturn(true);
		Mockito.reset(typeValidator);

		dataNode = DocumentUtils.getDocumentElement("<password-leaf xmlns=\"ns\">testdata</password-leaf>");
		m_leafValidator.validate(dataNode,RequestType.EDIT_CONFIG);
		verify(typeValidator).validate(elementArgumentCaptor.capture(),Mockito.eq(false),Mockito.eq(null));
		actualElement = elementArgumentCaptor.getValue();
		assertEquals("<password-leaf xmlns=\"ns\">testdata</password-leaf>",
				DocumentUtils.documentToPrettyString(actualElement).trim());

		when(yinRegistry.isPassword(m_leafSchemaNode,"mountpoint")).thenReturn(false);
		Mockito.reset(typeValidator);

		dataNode = DocumentUtils.getDocumentElement("<password-leaf xmlns=\"ns\">testdata</password-leaf>");
		m_leafValidator.validate(dataNode,RequestType.EDIT_CONFIG);
		verify(typeValidator).validate(elementArgumentCaptor.capture(),Mockito.eq(false),Mockito.eq(null));
		actualElement = elementArgumentCaptor.getValue();
		assertEquals("<password-leaf xmlns=\"ns\">testdata</password-leaf>",
				DocumentUtils.documentToPrettyString(actualElement).trim());
	}

}
