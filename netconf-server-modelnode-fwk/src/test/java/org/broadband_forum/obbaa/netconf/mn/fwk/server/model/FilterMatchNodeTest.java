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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class FilterMatchNodeTest {
    @Test
    public void testIsSameQName() {
        FilterMatchNode matchNode = new FilterMatchNode("device", "http://ns", "ONT1");
        assertTrue(matchNode.isSameQName(QName.create("http://ns", "device")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns1", "device")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns1", "device2")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns", "device2")));

    }
}
