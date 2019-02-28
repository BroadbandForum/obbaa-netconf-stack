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

package org.broadband_forum.obbaa.netconf.api.util;

import static junit.framework.TestCase.assertEquals;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

public class NetconfResourcesTest {

    @Test
    @Ignore("this is time zon dependent")
    public void testDateTmeFormatter(){
        assertEquals("1970-01-03T19:52:14.783Z", NetconfResources.parseDateTime("1970-01-03T19:52:14.783117+00:00").toString());
        assertEquals("1970-01-03T14:22:14.003Z", NetconfResources.parseDateTime("1970-01-03T19:52:14.003117+05:30").toString());
        assertEquals("2005-01-03T19:03:09.000Z", NetconfResources.parseDateTime("2005-01-03T19:03:09+00:00").toString());
    }

    @Test
    @Ignore("this is time zon dependent")
    public void testPrint(){
        DateTime dateTime = NetconfResources.parseDateTime("1970-01-03T19:52:14.783117+00:00");
        assertEquals("1970-01-03T19:52:14.783+00:00", NetconfResources.printWithMillis(dateTime));
        assertEquals("1970-01-03T19:52:14+00:00", NetconfResources.printWithoutMillis(dateTime));
    }


}
