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

package org.broadband_forum.obbaa.netconf.api.logger;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class NetconfExtensionsTest {

	private static final String NAME_SPACE = "http://www.test-company.com/solutions/anv-yang-extensions";
	private static final String LOCAL_NAME = "treat-as-relative-path";
	private static final String REVISION = "2016-01-07";

	private List<UnknownSchemaNode> m_listUnknownSchema;
	private UnknownSchemaNode m_unknownSchemaNode;
	private ExtensionDefinition m_extDef;
	private QName m_qName;

	@Before
	public void setUp() {
		m_qName = QName.create(NAME_SPACE, REVISION, LOCAL_NAME);
		m_unknownSchemaNode = Mockito.mock(UnknownSchemaNode.class);
		m_extDef = Mockito.mock(ExtensionDefinition.class);
		m_listUnknownSchema = new ArrayList<UnknownSchemaNode>();
		m_listUnknownSchema.add(m_unknownSchemaNode);
		Mockito.when(m_unknownSchemaNode.getExtensionDefinition()).thenReturn(m_extDef);
		Mockito.when(m_extDef.getQName()).thenReturn(m_qName);
	}

	@Test
	public void testIsExtensionIn_TypeDefinition() {
		TypeDefinition typeDefinition = Mockito.mock(TypeDefinition.class);
		Mockito.when(typeDefinition.getUnknownSchemaNodes()).thenReturn(m_listUnknownSchema);
		boolean result = NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(typeDefinition);
		Assert.assertTrue(result);
		//Return false if extension path is different
		result = NetconfExtensions.IS_PASSWORD.isExtensionIn(typeDefinition);
		Assert.assertFalse(result);
		typeDefinition = null;
		result = NetconfExtensions.IS_PASSWORD.isExtensionIn(typeDefinition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testIsExtensionIn_DataSchemaNode() {
		DataSchemaNode dataSchemaNode = Mockito.mock(DataSchemaNode.class);
		Mockito.when(dataSchemaNode.getUnknownSchemaNodes()).thenReturn(m_listUnknownSchema);
		boolean result = NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(dataSchemaNode);
		Assert.assertTrue(result);
		//Return false if extension path is different
		result = NetconfExtensions.IS_PASSWORD.isExtensionIn(dataSchemaNode);
		Assert.assertFalse(result);
	}

}