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

package org.broadband_forum.obbaa.netconf.api.client;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.broadband_forum.obbaa.netconf.api.ClosureReason;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

/**
 * A session given to netconf clients, can be used to send netconf messages to a netconf server. Following is an example on how to build a
 * lock request and send the message to the server.
 *
 * <pre>
 * NetconfClientSession session = ... //get the session using the client dispatcher. See implementations of {@link NetconfClientDispatcher} to find out how to get a NetconfClientSession.
 * LockRequest lockReq = new LockRequest().setTarget("running");
 * NetConfResponse res = session.lock(lockReq).get();
 *
 * 
 */
public interface NetconfClientSession {

    String GET_METHOD = "get";
    String TYPE = "type";
    String GET_CONFIG_METHOD = "getConfig";
    String EDIT_CONFIG_METHOD = "editConfig";
    String COPY_CONFIG_METHOD = "copyConfig";
    String RPC_METHOD = "rpc";
    String DEFAULT_CONNECT_TIMEOUT_MS = "15000";
    String DEFAULT_SOCKET_READ_TIMEOUT_MS = String.valueOf(Long.MAX_VALUE);

    void setMessageId(AbstractNetconfRequest request);

    /**
     * Perform Netconf <get-config> operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     *   <get-config>
     *       <source>
     *           <running />
     *       </source>
     *       <filter type="subtree">
     *           <top xmlns="http://example.com/schema/1.2/config">
     *               <users />
     *           </top>
     *       </filter>
     *   </get-config>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  GetConfigFilter filter = new GetConfigFilter()
     *                                   .setType("subtree")
     *                                   .setXmlFilters(DocumentUtils.getInstance().getElementByName(requestDocument, "top"));//Add the Element "top" here
     *  GetConfigRequest request = new GetConfigRequest()
     *                                   .setSourceRunning()
     *                                   .setFilter(filter);
     *  NetConfResponse response = session.getConfig(request).get();}
     * </pre>
     *
     * @param request {@link GetConfigRequest} containing request parameters.
     * @return A Future reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture getConfig(GetConfigRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <edit-config>}operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     *     <edit-config>
     *         <target>
     *             <running />
     *         </target>
     *         <default-operation>merge</default-operation>
     *         <test-option>test-then-set</test-option>
     *         <error-option>rollback-on-error</error-option>
     *         <config>
     *             <configuration xmlns="http://example.com/schema/1.2/config">
     *                 <protocols>
     *                     <rip>
     *                         <message-size operation="replace">255</message-size>
     *                     </rip>
     *                 </protocols>
     *             </configuration>
     *         </config>
     *     </edit-config>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  List<{@link EditConfigElement}> configElements = new ArrayList<>();
     *  {@link EditConfigElement} configElement = new EditConfigElement();
     *  configElement.setConfigElementContent(confgurationElement);//Set <configuration> org.w3c.dom.Element as content here
     *  configElements.add(configElement);
     *  {@link EditConfigRequest} request = new EditConfigRequest()
     *                                       .setTargetRunning()
     *                                       .setTestOption(EditConfigTestOptions.TEST_THEN_SET)
     *                                       .setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR)
     *                                       .setDefaultOperation(EditConfigDefaultOperations.MERGE)
     *                                       .setConfigElements(configElements);
     *  NetConfResponse response =  session.editConfig(request).get();}
     * </pre>
     *
     * @param request {@link EditConfigRequest} containing request parameters.
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException if the the request is not valid.
     */
    NetconfResponseFuture editConfig(EditConfigRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code<copy-config>} operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<copy-config>
     * 		<target>
     * 			<running />
     * 		</target>
     * 		<source>
     * 			<url>https://user:password@example.com/cfg/new.txt</url>
     * 		</source>
     * 	</copy-config>
     * </rpc>
     *
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  CopyConfigRequest request = new CopyConfigRequest()
     *                                       .setTargetRunning()
     *                                       .setSource("https://user:password@example.com/cfg/new.txt", true);
     *  NetConfResponse response = session.copyConfig(request).get();
     *  }
     *
     *  @param request {@link CopyConfigRequest}
     *  @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture copyConfig(CopyConfigRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code<delete-config>} operation. <b>Example:</b>
     *
     * <pre>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<delete-config>
     * 		<target>
     * 			<startup />
     * 		</target>
     * 	</delete-config>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     * DeleteConfigRequest request = new DeleteConfigRequest()
     *                                          .setTarget("startup");
     * NetConfResponse respone = session.deleteConfig(request).get();
     * }
     *  @param request {@link DeleteConfigRequest}
     *  @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture deleteConfig(DeleteConfigRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <lock>} operation
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<lock>
     * 		<target>
     * 			<candidate />
     * 		</target>
     * 	</lock>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     * LockRequest request = new LockRequest()
     *                                  .setTarget(DataStore.CANDIDATE);
     * NetConfResponse respone = session.lock(request).get();}
     * </pre>
     *
     * @param request {@link LockRequest}
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture lock(LockRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <unlock>} operation
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<unlock>
     * 		<target>
     * 			<candidate />
     * 		</target>
     * 	</unlock>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  UnLockRequest request = new UnLockRequest()
     *                                   .setTarget(DataStore.CANDIDATE);
     *  NetConfResponse respone = session.unlock(request).get();
     *  }
     *  @param request {@link UnLockRequest}
     *  @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture unlock(UnLockRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <get>} operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<get>
     * 		<filter type="subtree">
     * 			<top xmlns="http://example.com/schema/1.2/stats">
     * 				<interfaces>
     * 					<interface>
     * 						<ifName>eth0</ifName>
     * 					</interface>
     * 				</interfaces>
     * 			</top>
     * 		</filter>
     * 	</get>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  NetconfFilter filter = new NetconfFilter()
     *                                    .setType("subtree")
     *                                    .setXmlFilters(DocumentUtils.getInstance().getElementByName(requestDocument, "top"));
     *  GetRequest request = new GetRequest().setFilter(filter);
     *  NetConfResponse response = session.get(request).get();}
     * </pre>
     *
     * @param request {@link GetRequest}
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture get(GetRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <rpc>} operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<download-pma-configuration-to-device xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 		<device>device1</device>
     * 	</download-pma-configuration-to-device>
     * </rpc>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<upload-pma-configuration-to-device xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 		<device>device1</device>
     * 	</upload-pma-configuration-to-device>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  NetconfRpcRequest request = new NetconfRpcRequest();
     *  NetConfResponse response = session.rpc(request).get();}
     * </pre>
     *
     * @param request {@link NetconfRpcRequest}
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture rpc(NetconfRpcRequest request) throws NetconfMessageBuilderException;

    /**
     * Method to send a RPC Message directly without using Message POJOs.
     *
     * @param request
     * @return
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture sendRpc(AbstractNetconfRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <create-subscription>} operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * <create-subscription xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
     * 	      </create-subscription>
     *
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  CreateSubscriptionRequest request = new CreateSubscriptionRequest();
     *  NetConfResponse response = session.createSubscription(request).get();}
     * </pre>
     *
     * @param request
     * @return
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture createSubscription(CreateSubscriptionRequest request, NotificationListener notificationListener)
            throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <close-session> } operation.
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<close-session />
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     *  CloseSessionRequest request = new CloseSessionRequest();
     *  NetConfResponse response = session.closeSession(request).get();}
     * </pre>
     *
     * @param request {@link CloseSessionRequest}
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture closeSession(CloseSessionRequest request) throws NetconfMessageBuilderException;

    /**
     * Perform Netconf {@code <kill-session> } operation
     *
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC xml request
     * --------------------------------
     * <?xml version="1.0" encoding="UTF-8"?>
     * <rpc message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * 	<kill-session>
     * 		<session-id>4</session-id>
     * 	</kill-session>
     * </rpc>
     * --------------------------------
     * Corresponding NetconfRequest
     * --------------------------------
     * KillSessionRequest request = new KillSessionRequest()
     *                                       .setSessionId(4);
     * NetConfResponse response = session.killSession(request).get();}
     * </pre>
     *
     * @param request
     * @return A CompletableFuture reference to get the response.
     * @throws NetconfMessageBuilderException
     */
    NetconfResponseFuture killSession(KillSessionRequest request) throws NetconfMessageBuilderException;

    NetconfResponseFuture action(ActionRequest request) throws NetconfMessageBuilderException;

    /**
     * Get the capability of the connected server. The capabilities are exchanged during session establishment via netconf {@code <hello>}
     * message.
     *
     * <pre>
     * See RFC 6242 section-8.
     * </pre>
     *
     * @param capability - fully formatted capability name For example :
     *
     *                   <pre>
     *                   1. "urn:ietf:params:netconf:capability:startup:1.0"
     *                   2. "http://example.net/router/2.3/myfeature"
     *
     *                   </pre>
     * @return true if the server has the given capability, false otherwise.
     */
    boolean getServerCapability(String capability);

    /**
     * Get session identifier of the NETCONF session. This session identifier is set by the netconf server during
     * {@code <hello> message exchange.}
     *
     * @return current session id
     */
    int getSessionId();

    void addSessionListener(NetconfClientSessionListener listener);

    void addNotificationListener(NotificationListener listener);

    void sessionClosed();

    /**
     * call home device client session needs to provide remote address to map into deviceId configuration in pma
     *
     * @return
     */
    SocketAddress getRemoteAddress();

    /**
     * If call home device remote address does not resolve to device Id, then close session.
     * <p>
     * Note:- This is not closeSession or KillSession request. This is used reject call home device request from device
     *
     * @throws InterruptedException
     */
    void close() throws InterruptedException, IOException;

    void closeAsync();

    void closeAsync(ClosureReason closureReason);

    /**
     * Get the capabilities of the connected server. The capabilities are exchanged during session establishment via netconf {@code <hello>}
     * message.
     *
     * @return
     */
    Set<String> getServerCapabilities();

    boolean isOpen();

    long getCreationTime();

    /**
     * The time in milli seconds when the last data was sent.
     *
     * @return
     */
    long getIdleTimeStart();

    void setTcpKeepAlive(boolean keepAlive);

    void closeGracefully() throws IOException;

    NetconfClientSession getType();

    void setClosureReason(ClosureReason closureReason);
}
