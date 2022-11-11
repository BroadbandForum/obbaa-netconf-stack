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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ModelNodeKeyTest {
    
   private static final String QOS_NAMESPACE = "http://www.test-company.com/solutions/rest-mediator/nwconfigapps/qos";
   private static final String QOS_REVISION = "2016-09-22";
   private static final QName DEVICE_QNAME = QName.create(QOS_NAMESPACE, QOS_REVISION, "device");
   private static final QName PORT_QNAME = QName.create(QOS_NAMESPACE, QOS_REVISION, "port");
   private static final QName PRIORITY_QNAME = QName.create(QOS_NAMESPACE, QOS_REVISION, "priority");

    @Test
    public void testEntrySetSorted() {
       Map<QName, String> keys = new LinkedHashMap<>();
       keys.put(PRIORITY_QNAME, "100");
       keys.put(PORT_QNAME, "1");
       keys.put(DEVICE_QNAME, "of:00000000000000002");
       ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
       assertEquals(keys.entrySet(), modelNodeKey.entrySet());
    }

}
