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

package org.broadband_forum.obbaa.netconf.api.logger.ual;


import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.w3c.dom.Document;

public class NetconfUALLog {
    private String m_username;
    private String m_session;
    private String m_invocationTime;
    private String m_applications;
    private String m_operation;
    private String m_containerName;
    private String m_containerId;
    private String m_arguments;
    private String m_payload;
    private String m_result;
    private String m_delegate_user;
    private String m_delegate_session;
    private AbstractNetconfRequest m_netconfRequest;
    private Document m_requestDocument;

    public NetconfUALLog() {

    }

    public NetconfUALLog(String username, String session, String invocationTime, String applications, String operation,
                         String containerName, String containerId, String arguments, String payload, String result,
                         String delegate_user, String delegate_session) {
        this.m_username = username;
        this.m_session = session;
        this.m_invocationTime = invocationTime;
        this.m_applications = applications;
        this.m_operation = operation;
        this.m_containerName = containerName;
        this.m_containerId = containerId;
        this.m_arguments = arguments;
        this.m_payload = payload;
        this.m_result = result;
        this.m_delegate_user = delegate_user;
        this.m_delegate_session = delegate_session;
    }

    public AbstractNetconfRequest getNetconfRequest() {
        return m_netconfRequest;
    }

    public NetconfUALLog setNetconfRequest(AbstractNetconfRequest netconfRequest) {
        m_netconfRequest = netconfRequest;
        return this;
    }

    public String getUsername() {
        return m_username;
    }

    public NetconfUALLog setUsername(String username) {
        m_username = username;
        return this;
    }

    public String getSessionId() {
        return m_session;
    }

    public NetconfUALLog setSessionId(String session) {
        m_session = session;
        return this;
    }

    public String getInvocationTime() {
        return m_invocationTime;
    }

    public NetconfUALLog setInvocationTime(long invocationTime) {
        m_invocationTime = String.valueOf(invocationTime);
        return this;
    }

    public String getApplications() {
        return m_applications;
    }

    public NetconfUALLog setApplications(String applications) {
        m_applications = applications;
        return this;
    }

    public String getOperation() {
        return m_operation;
    }

    public NetconfUALLog setOperation(String operation) {
        m_operation = operation;
        return this;
    }

    public String getContainerName() {
        return m_containerName;
    }

    public void setContainerName(String containerName) {
        m_containerName = containerName;
    }

    public String getContainerId() {
        return m_containerId;
    }

    public void setContainerId(String containerId) {
        m_containerId = containerId;
    }

    public String getArguments() {
        return m_arguments;
    }

    public void setArguments(String arguments) {
        m_arguments = arguments;
    }

    public String getPayload() {
        return m_payload;
    }

    public void setPayload(String payload) {
        m_payload = payload;
    }

    public String getResult() {
        return m_result;
    }

    public NetconfUALLog setResult(String result) {
        m_result = result;
        return this;
    }

    public String getDelegateUser() {
        return this.m_delegate_user;
    }

    public NetconfUALLog setDelegateUser(String delegateUser) {
        this.m_delegate_user = delegateUser;
        return this;
    }

    public String getDelegateSession() {
        return this.m_delegate_session;
    }

    public NetconfUALLog setDelegateSession(String delegateSession) {
        this.m_delegate_session = delegateSession;
        return this;
    }

    public NetconfUALLog setRequestDocument(Document netconfRequestDocument) {
        this.m_requestDocument = netconfRequestDocument;
        return this;
    }
    public Document getRequestDocument(){
        return this.m_requestDocument;
    }

    @Override
    public String toString() {
        return "NetconfUALLog{" +
                "m_username='" + m_username + '\'' +
                ", m_session='" + m_session + '\'' +
                ", m_invocationTime='" + m_invocationTime + '\'' +
                ", m_applications='" + m_applications + '\'' +
                ", m_operation='" + m_operation + '\'' +
                ", m_containerName='" + m_containerName + '\'' +
                ", m_containerId='" + m_containerId + '\'' +
                ", m_arguments='" + m_arguments + '\'' +
                ", m_payload='" + m_payload + '\'' +
                ", m_result='" + m_result + '\'' +
                ", m_delegate_user='" + m_delegate_user + '\'' +
                ", m_delegate_session='" + m_delegate_session +
                '}';
    }
}
