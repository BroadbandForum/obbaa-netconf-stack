/**
 * 
 */
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

package org.broadband_forum.obbaa.netconf.driver.client;

import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * NetconfClientDriver provide xml stream level interface for sending netconf request as xml message and receiving response as xml message
 * 
 *
 * 
 */
public interface NetconfClientDriver {

    /**
     * Sends netconf get xml request and retrieve get reponse as xml
     * 
     * @param getXmlRequest
     * @return the response xml for get request
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendGetRequest(String getXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Sends netconf get xml request and retrieve get reponse as xml
     * 
     * @param getXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return the response xml for get request
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendGetRequest(String getXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Sends netconf get-config xml request and retrieve get-config response as xml
     * 
     * @param getConfigXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendGetConfigRequest(String getConfigXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Sends netconf get-config xml request and retrieve get-config response as xml
     * 
     * @param getConfigXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendGetConfigRequest(String getConfigXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Sends netconf edit-config xml request
     * 
     * @param editConfigXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendEditConfigRequest(String editConfigXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Sends netconf edit-config xml request
     * 
     * @param editConfigXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendEditConfigRequest(String editConfigXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf copy-config request
     * 
     * @param copyConfigXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCopyConfigRequest(String copyConfigXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf copy-config request
     * 
     * @param copyConfigXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCopyConfigRequest(String copyConfigXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf rpc request
     * 
     * @param rpcXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendRpcRequest(String rpcXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send netconf rpc request
     * 
     * @param rpcXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendRpcRequest(String rpcXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send netconf lock request
     * 
     * @param lockXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendLockRequest(String lockXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send netconf lock request
     * 
     * @param lockXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendLockRequest(String lockXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send netconf unlock request
     * 
     * @param unLockXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendUnLockRequest(String unLockXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf unlock request
     * 
     * @param unLockXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendUnLockRequest(String unLockXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf close session request
     * 
     * @param closeSessionXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCloseSessionRequest(String closeSessionXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf close session request
     * 
     * @param closeSessionXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCloseSessionRequest(String closeSessionXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf kill session request
     * 
     * @param killSessionXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendKillSessionRequest(String killSessionXmlRequest) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf kill session request
     * 
     * @param killSessionXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendKillSessionRequest(String killSessionXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException;

    /**
     * Send netconf create-subscription request
     * 
     * @param createSubscriptionXmlRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCreateSubscriptionRequest(String createSubscriptionXmlRequest, NotificationListener notificationListener)
            throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send netconf create-subscription request
     * 
     * @param createSubscriptionXmlRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendCreateSubscriptionRequest(String createSubscriptionXmlRequest, NotificationListener notificationListener, Long timeoutInMillis)
            throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send seamless netconf xml request and retrieve response xml Note:- send netconf xml request of any rpc supported
     * 
     * @param getRequest
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendRequest(String getRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Send seamless netconf xml request and retrieve response xml Note:- send netconf xml request of any rpc supported
     * 
     * @param getRequest
     * @param timeoutInMillis (null means the default timeout)
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NetconfMessageBuilderException
     */
    public String sendRequest(String getRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException;

    /**
     * Closes the actual session.   Note that after this you have to recreate the session, you cannot reuse it anymore.
     * 
     * @throws InterruptedException
     */
    void closeSession() throws InterruptedException, IOException;

}
