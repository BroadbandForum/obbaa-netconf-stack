package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteLeafCommandTest {
    private SchemaRegistry m_schemaRegistry;
    private DeleteLeafCommand m_deleteLeafCommand;

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/deleteattributecommandtest/deleteattributecommandtest@2014-07-03.yang")), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_deleteLeafCommand = new DeleteLeafCommand();
        HelperDrivenModelNode parentModelNode = mock(HelperDrivenModelNode.class);
        when(parentModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(parentModelNode.getQName()).thenReturn(QName.create("unit:test:dact", "2014-07-03", "father"));
        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), null, parentModelNode, null, false);
    }
    
    @Test
    public void testDefaultValueSetOnConstraint() throws Exception{
        SchemaPath fatherSp = SchemaPathBuilder.fromString("(unit:test:dact?revision=2014-07-03),family,father");
        HelperDrivenModelNode parentModelNode = mock(HelperDrivenModelNode.class);
        when(parentModelNode.getModelNodeSchemaPath()).thenReturn(fatherSp);
        when(parentModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        QName changeNodeQName = m_schemaRegistry.lookupQName("unit:test:dact", "dress");
        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class)	, mock(ConfigAttributeHelper.class), parentModelNode, changeNodeQName, true);
        m_deleteLeafCommand.execute();
        assertFalse(m_deleteLeafCommand.isSetToDefault());
    }

}