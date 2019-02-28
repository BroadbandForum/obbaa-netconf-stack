package org.broadband_forum.obbaa.netconf.mn.fwk.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;

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

}
