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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteLeafCommandTest {
    private SchemaRegistry m_schemaRegistry;
    private DeleteLeafCommand m_deleteLeafCommand;

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource
                ("/deleteattributecommandtest/deleteattributecommandtest@2014-07-03.yang")), new NoLockService());
        m_deleteLeafCommand = new DeleteLeafCommand();
        ModelNode parentModelNode = mock(ModelNode.class);
        when(parentModelNode.getQName()).thenReturn(QName.create("unit:test:dact", "2014-07-03", "father"));
        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), null, parentModelNode, null,
                false);
    }

    @Test
    public void testDefaultValueSetOnConstraint() throws Exception {
        SchemaPath fatherSp = SchemaPathBuilder.fromString("(unit:test:dact?revision=2014-07-03),family,father");
        ModelNode parentModelNode = mock(ModelNode.class);
        when(parentModelNode.getModelNodeSchemaPath()).thenReturn(fatherSp);
        QName changeNodeQName = m_schemaRegistry.lookupQName("unit:test:dact", "dress");
        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), mock(ConfigAttributeHelper
                .class), parentModelNode, changeNodeQName, true);
        m_deleteLeafCommand.execute();
        assertFalse(m_deleteLeafCommand.isSetToDefault());
    }

}