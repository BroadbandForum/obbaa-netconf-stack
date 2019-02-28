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

package org.broadband_forum.obbaa.netconf.server;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractResponseChannel implements ResponseChannel {
    private boolean m_isSessionClosed = false;
    private CompletableFuture<Boolean> m_closeFuture = new CompletableFuture<>();

    @Override
    public void markSessionClosed() {
        m_isSessionClosed = true;
        m_closeFuture.complete(Boolean.TRUE);
    }

    @Override
    public boolean isSessionClosed() {
        return m_isSessionClosed;
    }

    @Override
    public void sendNotification(Notification notification) {
    }

    @Override
    public CompletableFuture<Boolean> getCloseFuture() {
        return m_closeFuture;
    }
}
