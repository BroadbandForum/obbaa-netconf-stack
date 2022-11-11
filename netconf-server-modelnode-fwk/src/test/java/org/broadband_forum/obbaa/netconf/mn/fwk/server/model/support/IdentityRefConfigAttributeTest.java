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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static junit.framework.TestCase.assertEquals;

import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class IdentityRefConfigAttributeTest {

    @Test
    public void testEqualsWorksWhenPrefixesUsedAreDifferent(){
        IdentityRefConfigAttribute left = new IdentityRefConfigAttribute("urn:bbf:yang:bbf-xpon-if-type", "bbf-xponift",
                "type", "bbf-xponift:channel-group", "urn:ietf:params:xml:ns:yang:ietf-interfaces");
        IdentityRefConfigAttribute right = new IdentityRefConfigAttribute("urn:bbf:yang:bbf-xpon-if-type", "bbf-xponift2",
                "type", "bbf-xponift2:channel-group", "urn:ietf:params:xml:ns:yang:ietf-interfaces");

        assertEquals(left, right);
    }
}
