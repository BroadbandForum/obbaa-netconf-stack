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

package org.broadband_forum.obbaa.netconf.api.client.util;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Before;
import org.w3c.dom.Document;

import io.netty.channel.Channel;

public class AbstractNetconfClientSessionTestSetUp {
    public AbstractNetconfClientSession m_abstractNetconfClientSession;
    public ExecutorService m_executorService;
    public Future<NetConfResponse> m_futureResponse;
    public String m_obtainedXmlString;
    public NotificationListener m_notificationListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {

        m_executorService = mock(ExecutorService.class);
        m_futureResponse = mock(Future.class);
        when(m_executorService.submit(any(Callable.class))).thenReturn(m_futureResponse);
        m_abstractNetconfClientSession = spy(new AbstractNetconfClientSession() {
            Channel serverChannel = mock(Channel.class);;

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public long getCreationTime() {
                return 0;
            }

            @Override
            public void setTcpKeepAlive(boolean keepAlive) {

            }

            @Override
            public SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void close() throws InterruptedException, IOException {

            }

            @Override
            public void closeAsync() {

            }

            @Override
            protected CompletableFuture<NetConfResponse> sendRpcMessage(final String currentMessageId, Document requestDocument,
                                                                        final long timoutMillis) {
                String xmlString = "";
                try {
                    xmlString = DocumentUtils.documentToString(requestDocument) + NetconfDelimiters.rpcEndOfMessageDelimiterString();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                m_obtainedXmlString = xmlString;
                serverChannel.writeAndFlush(xmlString);
                TimeoutFutureResponse future = new TimeoutFutureResponse(timoutMillis, TimeUnit.MILLISECONDS);
                m_responseFutures.put(currentMessageId, future);

                return future;
            }
        });
        m_notificationListener = mock(NotificationListener.class);
    }

    protected Document getDocumentFromFilePath(final String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = new File(url.getPath());
        return getDocFromFile(file);
    }
}