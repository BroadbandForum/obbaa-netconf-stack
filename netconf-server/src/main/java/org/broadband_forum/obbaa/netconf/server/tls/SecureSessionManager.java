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

package org.broadband_forum.obbaa.netconf.server.tls;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureSessionManager {
    private static SecureSessionManager m_instance = new SecureSessionManager();
    private Map<Integer, SecureNetconfServerHandler> m_serverHandlers = new ConcurrentHashMap<>();

    private SecureSessionManager() {
    }

    public static SecureSessionManager getInstance() {
        return m_instance;
    }

    public void killSesssion(Integer sessionId) {
        SecureNetconfServerHandler secureNetconfServerHandler = m_serverHandlers.get(sessionId);
        if (secureNetconfServerHandler != null) {
            secureNetconfServerHandler.close();
            m_serverHandlers.remove(sessionId);
        }
    }

    public void registerServerHandler(int sessionId, SecureNetconfServerHandler secureNetconfServerHandler) {
        m_serverHandlers.put(sessionId, secureNetconfServerHandler);
    }

}
