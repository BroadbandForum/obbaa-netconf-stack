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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MustReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class MustReferringNodeTest {

    private String accessPath;
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_mustReferringNodes = new DefaultConcurrentHashMap<>(new HashMap<SchemaPath, Expression>(), true);
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_mustReferringNodes1 = null;
    private static final SchemaPath referencedSchemaPath = SchemaPath.create(true, createQNameForMustConstraint("reference-container"));
    private static final SchemaPath nodeSchemaPath = SchemaPath.create(true, createQNameForMustConstraint("test-container"));
    MustReferringNode mustReferringNode = new MustReferringNode("test");
    MustReferringNode mustReferringNode1 = new MustReferringNode("test");

    static QName createQNameForMustConstraint(String localName) {
        return QName.create("must-constraint", "2021-04-13", localName);
    }

    @Test
    public void testEqualsToStringHashcode() {
        //testEquals
        assertEquals(mustReferringNode, mustReferringNode1);
        assertEquals(mustReferringNode.getComponentId(), "test");
        mustReferringNode.registerMustReferringNodes(referencedSchemaPath, nodeSchemaPath, accessPath);
        assertNotEquals(mustReferringNode, mustReferringNode1);
        mustReferringNode1.registerMustReferringNodes(referencedSchemaPath, nodeSchemaPath, accessPath);
        assertEquals(mustReferringNode, mustReferringNode1);
        MustReferringNode mustReferringNode2 = new MustReferringNode(null);
        assertNotEquals(mustReferringNode, mustReferringNode2);
        MustReferringNode mustReferringNode3 = new MustReferringNode(null);
        MustReferringNode mustReferringNode4 = new MustReferringNode("test");
        assertNotEquals(mustReferringNode3, mustReferringNode4);

        // testing ToSting()
        String toString = mustReferringNode.toString();
        String toString1 = mustReferringNode1.toString();
        assertEquals(toString, toString1);
        //testing Hashcode()
        Map<MustReferringNode, String> map = new HashMap<>();
        map.put(mustReferringNode, "dummy");
        Assert.assertEquals("dummy", map.get(mustReferringNode1));

    }
}
