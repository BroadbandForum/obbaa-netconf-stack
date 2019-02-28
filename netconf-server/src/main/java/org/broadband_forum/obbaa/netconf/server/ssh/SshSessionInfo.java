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

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class SshSessionInfo {

    NetconfSubsystem subsystem;
    Integer sessionId;
    String user;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshSessionInfo.class, LogAppNames.NETCONF_LIB);

    public NetconfSubsystem getSubsystem() {
        return subsystem;
    }

    public SshSessionInfo setSubsystem(NetconfSubsystem subsystem) {
        this.subsystem = subsystem;
        return this;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public SshSessionInfo setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getUser() {
        return user;
    }

    public SshSessionInfo setUser(String user) {
        this.user = user;
        return this;
    }

    @Override
    public String toString() {
        return "SshSessionInfo [subsystem=" + subsystem + ", sessionId=" + LOGGER.sensitiveData(sessionId) +
                ", user=" + ((this.getUser() != null) ? LOGGER.sensitiveData(user) : user) + "]";
    }

}
