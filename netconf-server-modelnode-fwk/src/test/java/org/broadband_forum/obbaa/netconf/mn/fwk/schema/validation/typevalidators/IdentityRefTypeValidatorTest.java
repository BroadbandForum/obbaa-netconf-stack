package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

public class IdentityRefTypeValidatorTest extends AbstractTypeValidatorTest {
	@Test
	public void testValidIdentityRef() throws NetconfMessageBuilderException {
		testPass("identityrefvalidator/valid-identityref.xml");
		SchemaPath schemaPathForValidation = SchemaPath.create(true,
				QName.create("urn:org:bbf:pma", "2015-12-02", "validation"));
		SchemaPath schemaPathForTypeValidation = new SchemaPathBuilder().withParent(schemaPathForValidation)
				.appendLocalName("type-validation").build();
		SchemaPath schemaPathForId = new SchemaPathBuilder().withParent(schemaPathForTypeValidation)
				.appendLocalName("id").build();

		LeafSchemaNode leafNode = (LeafSchemaNode) c_schemaRegistry.getDataSchemaNode(schemaPathForId);
		TypeDefinition<?> typeDefinition = leafNode.getType();
		assertNotNull(c_schemaRegistry.getValidator(typeDefinition));
		testPass("identityrefvalidator/valid-identityref2.xml");
	}
	
	@Test
	public void testInvalidIdentityRef() throws NetconfMessageBuilderException {
		testFail("identityrefvalidator/invalid-identityref-1.xml", "Value \"yang1:english\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
		
		testFail("identityrefvalidator/invalid-identityref-2.xml", "Value \"yang1:english\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
		
		testFail("identityrefvalidator/invalid-identityref-3.xml", "Value \"yang:french\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
		
		testFail("identityrefvalidator/invalid-identityref-4.xml", "Value \"yang:french\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
		
		testFail("identityrefvalidator/invalid-identityref-5.xml", "Value \"english\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
		
		testFail("identityrefvalidator/invalid-identityref-6.xml", "Value \"\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);

		testFail("identityrefvalidator/invalid-identityref-7.xml", "Value \"english\" is not a valid identityref value.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:identityref-type", null);
	}
}
