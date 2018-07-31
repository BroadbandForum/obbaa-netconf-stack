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

package org.broadband_forum.obbaa.netconf.nc.stack.examples;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

@IntegrationTest
public class InvalidClientConfigurationTest {
    @Test
    public void testSSHNCClientWithInvalidTransport() throws Exception {
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        try {
            NetconfTransportFactory.makeNetconfTransport(transportOrder);
        } catch (NetconfConfigurationBuilderException e) {
            assertEquals("Invalid options server socket address is null ", e.getMessage());
        }
    }

}
