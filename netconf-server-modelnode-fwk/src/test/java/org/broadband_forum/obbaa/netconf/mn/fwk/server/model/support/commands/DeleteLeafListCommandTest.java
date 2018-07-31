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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class DeleteLeafListCommandTest {

    private DeleteLeafListCommand m_deleteLeafListCommand;
    private ChildLeafListHelper m_childLeafListHelper;
    private ModelNodeWithAttributes m_modelNodeWithAttributes;

    @Before
    public void setup() {
        m_childLeafListHelper = mock(ChildLeafListHelper.class);
        m_modelNodeWithAttributes = mock(ModelNodeWithAttributes.class);
        m_deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo(m_childLeafListHelper,
                m_modelNodeWithAttributes);
    }

    @Test
    public void testExecute() throws CommandExecutionException, ModelNodeDeleteException {

        m_deleteLeafListCommand.execute();
        verify(m_childLeafListHelper).removeAllChild(m_modelNodeWithAttributes);

        try {
            doThrow(new ModelNodeDeleteException("Deleting leaf-list failed")).when(m_childLeafListHelper)
                    .removeAllChild(m_modelNodeWithAttributes);
            m_deleteLeafListCommand.execute();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof CommandExecutionException);
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException: Deleting " +
                    "leaf-list failed", e.getMessage());
        }
    }
}
