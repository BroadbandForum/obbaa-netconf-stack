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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.ConstraintValidatorFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.ConstraintValidatorFactoryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafListValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ListValidator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Created by keshava on 11/23/15.
 */
public class ConstraintTypeValidatorFactoryTest {
    ConstraintValidatorFactory m_factory = ConstraintValidatorFactoryImpl.getInstance();

    @Test
    public void testFactoryMethod() {
        DataSchemaNode dataSchemaNode = mock(LeafSchemaNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        DSExpressionValidator expValidator = mock(DSExpressionValidator.class);
        SchemaNodeConstraintParser schemaNodeConstraintParser = m_factory.getConstraintNodeValidator(dataSchemaNode,
                schemaRegistry, expValidator);
        assertTrue(schemaNodeConstraintParser instanceof LeafValidator);
        assertEquals(dataSchemaNode, schemaNodeConstraintParser.getDataSchemaNode());

        dataSchemaNode = mock(LeafListSchemaNode.class);
        schemaNodeConstraintParser = m_factory.getConstraintNodeValidator(dataSchemaNode, schemaRegistry, expValidator);
        assertTrue(schemaNodeConstraintParser instanceof LeafListValidator);
        assertEquals(dataSchemaNode, schemaNodeConstraintParser.getDataSchemaNode());

        dataSchemaNode = mock(ListSchemaNode.class);
        schemaNodeConstraintParser = m_factory.getConstraintNodeValidator(dataSchemaNode, schemaRegistry, expValidator);
        assertTrue(schemaNodeConstraintParser instanceof ListValidator);
        assertEquals(dataSchemaNode, schemaNodeConstraintParser.getDataSchemaNode());
    }
}
