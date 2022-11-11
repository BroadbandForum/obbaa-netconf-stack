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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.mn.fwk.AggregatedTreeNodeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class AggregatedTreeNodeExceptionTest {

    @Test
    public void testATNException() {
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        ModelNodeId modelNodeId = new ModelNodeId();      
        modelNodeId.addRdn(ModelNodeRdn.CONTAINER, "urn:ietf:params:xml:ns:yang:ietf-interfaces", "interface");
        AggregatedTreeNodeException exception = new AggregatedTreeNodeException("dummy error message", modelNodeId, schemaRegistry);
        assertTrue(exception instanceof IllegalArgumentException);
        assertEquals("dummy error message", exception.getMessage());
        assertNotNull(exception.getErrorPath());
        assertNotNull(exception.getErrorPathNsByPrefix());
    }
}
