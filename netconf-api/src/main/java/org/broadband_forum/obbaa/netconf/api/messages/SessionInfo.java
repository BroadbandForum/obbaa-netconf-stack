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

package org.broadband_forum.obbaa.netconf.api.messages;


public class SessionInfo {

    private String m_userName;

    private int m_sessionId;

    private String m_sourceHostIpAddress;

    public String getUserName() {
        return m_userName;
    }

    public void setUserName(String userName) {
        this.m_userName = userName;
    }

    public int getSessionId() {
        return m_sessionId;
    }

    public void setSessionId(int sessionId) {
        this.m_sessionId = sessionId;
    }

    public String getSourceHostIpAddress() {
        return m_sourceHostIpAddress;
    }

    public void setSourceHostIpAddress(String sourceHostIpAddress) {
        this.m_sourceHostIpAddress = sourceHostIpAddress;
    }

}
