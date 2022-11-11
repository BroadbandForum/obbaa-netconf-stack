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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom;

import static org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions.Constants.EXT_NS;
import static org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions.Constants.EXT_REV;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class EncryptDecryptUtilTest {

    @Test
    public void testIsPassword_NetconfExtension(){
        ExtensionDefinition extension = mock(ExtensionDefinition.class);
        when(extension.getQName()).thenReturn(QName.create(URI.create(EXT_NS),EXT_REV, "is-password"));

        mockAndVerifyIsPassword(extension);
    }

    @Test
    public void testIsPassword_YinAnnotation(){
        DataSchemaNode schemaNode = mock(DataSchemaNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        when(schemaRegistry.getName()).thenReturn("G.FAST|1.0");
        when(schemaNode.getUnknownSchemaNodes()).thenReturn(Collections.emptyList());
        YinAnnotationService yinAnnotationService = mock(YinAnnotationService.class);
        when(schemaRegistry.getYinAnnotationService()).thenReturn(yinAnnotationService);
        when(yinAnnotationService.isPassword(schemaNode,"G.FAST|1.0")).thenReturn(true);

        assertTrue(EncryptDecryptUtil.isPassword(schemaNode,schemaRegistry));
    }

    private void mockAndVerifyIsPassword(ExtensionDefinition extension) {
        DataSchemaNode schemaNode = mock(DataSchemaNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);

        List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        UnknownSchemaNode unknownSN = mock(UnknownSchemaNode.class);
        unknownSchemaNodes.add(unknownSN);

        when(unknownSN.getExtensionDefinition()).thenReturn(extension);
        when(schemaNode.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        assertTrue(EncryptDecryptUtil.isPassword(schemaNode, schemaRegistry));
    }
}
