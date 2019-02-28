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

package org.broadband_forum.obbaa.netconf.api.server.auth;

import java.io.Serializable;

public class AuthenticationResult {
    public static final String LOGIN_FAILURE_REASON = "Invalid credentials, user locked or authentication server not reachable";
    private static final AuthenticationResult FAILED_AUTH_RESULT = new AuthenticationResult(false, null, LOGIN_FAILURE_REASON);
    private final boolean m_authenticated;
    private final Serializable m_sessionId;
    private final String m_failureMessage;

    public AuthenticationResult(boolean authenticated, Serializable sessionId, String failureMessage) {
        m_authenticated = authenticated;
        m_sessionId = sessionId;
        m_failureMessage = failureMessage;
    }

    public AuthenticationResult(boolean authenticated, Serializable sessionId) {
        this(authenticated, sessionId, null);
    }

    public boolean isAuthenticated() {
        return m_authenticated;
    }

    public Serializable getSessionId() {
        return m_sessionId;
    }

    public String getFailureMessage() {
        return m_failureMessage;
    }

    public static AuthenticationResult failedAuthResult() {
        return FAILED_AUTH_RESULT;
    }
}
