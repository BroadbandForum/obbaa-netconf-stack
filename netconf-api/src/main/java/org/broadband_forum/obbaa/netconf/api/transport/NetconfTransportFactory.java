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

package org.broadband_forum.obbaa.netconf.api.transport;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;

public class NetconfTransportFactory {

    public static NetconfTransport makeNetconfTransport(NetconfTransportOrder transportOrder) throws
            NetconfConfigurationBuilderException {
        transportOrder.validate();

        if (transportOrder.getTransportType().equals(NetconfTransportProtocol.SSH.name())) {
            return new SshNetconfTransport().setSocketAddrress(transportOrder.getServerSocketAddress())
                    .setHostKeyPath(transportOrder.getServerSshHostKeyPath()).setHeartBeatInterval(transportOrder
                            .getHeartbeatInterval());
        } else {// validate() call above should take care of invalid transport protocols
            return new ReverseTlsNetconfTransport(transportOrder.getCallHomeIp(), transportOrder.getCallHomePort(),
                    transportOrder.getCallHomeListener(), transportOrder.isAllowSelfSigned(), transportOrder
                    .getTrustChain(),
                    transportOrder.getTrustManager(), transportOrder.getCertificateChain(), transportOrder
                    .getPrivateKey(),
                    transportOrder.getPrivateKeyPassword(), transportOrder.getKeyManager(), transportOrder
                    .getLocalAddress(),
                    transportOrder.isClientAuthenticationNeeded(), transportOrder.isTlsKeepalive(), transportOrder
                    .getSslProvider(),
                    transportOrder.getTlsHandshaketimeoutMillis());
        }
    }

}
