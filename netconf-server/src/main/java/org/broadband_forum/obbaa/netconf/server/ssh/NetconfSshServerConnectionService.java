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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.common.Service;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.session.AbstractServerSession;
import org.apache.sshd.server.session.ServerConnectionService;
import org.apache.sshd.server.session.ServerConnectionServiceFactory;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NetconfSshServerConnectionService extends ServerConnectionService {
    protected NetconfSshServerConnectionService(AbstractServerSession s) throws SshException {
        super(s);
        if (!(s instanceof ServerSession)) {
            throw new IllegalStateException("Server side service used on client side");
        }
        ServerSession session = s;
        if (session.isAuthenticated()) {
            startHeartBeat();
        } else {
            throw new SshException("Session is not authenticated");
        }
    }

    public static class Factory  extends ServerConnectionServiceFactory {
        public String getName() {
            return "ssh-connection";
        }

        @Override
        public Service create(Session session) throws IOException {
            ValidateUtils.checkTrue(session instanceof AbstractServerSession, "Not a server session: %s", session);
            NetconfSshServerConnectionService service = new NetconfSshServerConnectionService((AbstractServerSession) session);
            service.addPortForwardingEventListener(getPortForwardingEventListenerProxy());
            return service;
        }
    }

    protected void startHeartBeat() {
        String intervalStr = (String) getSession().getFactoryManager().getProperties().get(NetconfResources.HEARTBEAT_INTERVAL);
        try {
            int interval = intervalStr != null ? Integer.parseInt(intervalStr) : 0;
            if (interval > 0) {
                getSession().getFactoryManager().getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        sendHeartBeat();
                    }
                }, interval, interval, TimeUnit.MILLISECONDS);
            }
        } catch (NumberFormatException e) {
            log.warn("Ignoring bad heartbeat interval: {}", intervalStr);
        }
    }

    protected void sendHeartBeat() {
        try {
            Buffer buf = getSession().createBuffer(SshConstants.SSH_MSG_GLOBAL_REQUEST);
            String request = (String) getSession().getFactoryManager().getProperties().get(ClientFactoryManager.HEARTBEAT_REQUEST);
            if (request == null) {
                request = "keepalive@sshd.apache.org";
            }
            buf.putString(request);
            buf.putBoolean(false);
            getSession().writePacket(buf);
        } catch (IOException e) {
            log.info("Error sending keepalive message", e);
        }
    }

}
