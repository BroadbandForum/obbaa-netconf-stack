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
    public void setup(){
        m_childLeafListHelper = mock(ChildLeafListHelper.class);
        m_modelNodeWithAttributes = mock(ModelNodeWithAttributes.class);
        m_deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo(m_childLeafListHelper,m_modelNodeWithAttributes);
    }
    @Test
    public void testExecute() throws CommandExecutionException, ModelNodeDeleteException {

        m_deleteLeafListCommand.execute();
        verify(m_childLeafListHelper).removeAllChild(m_modelNodeWithAttributes);

        try{
            doThrow(new ModelNodeDeleteException("Deleting leaf-list failed")).when(m_childLeafListHelper).removeAllChild(m_modelNodeWithAttributes);
            m_deleteLeafListCommand.execute();
            fail();
        }catch(Exception e){
            assertTrue(e instanceof CommandExecutionException);
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException: Deleting leaf-list failed",e.getMessage());
        }
    }
}
