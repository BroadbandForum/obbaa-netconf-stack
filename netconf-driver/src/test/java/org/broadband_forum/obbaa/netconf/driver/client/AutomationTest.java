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

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to test notification automation drivers 
 * Created by nhtoan on 3/23/16.
 */
public class AutomationTest {
    private static final Logger LOGGER = Logger.getLogger(AutomationTest.class);

    public static void main(String[] args) {

        Set<String> caps = new HashSet<>();
        caps.add("urn:ietf:params:netconf:base:1.0");
        caps.add("urn:ietf:params:netconf:capability:writable-running:1.0");
        try {
            // client session
            NetconfClientSession clientSession = NetconfClientSessionFactory.createSSHClientSession("192.168.95.175", 9292, 0, "SSH",
                    "123456", caps);
            Set<String> serCaps = clientSession.getServerCapabilities();
            for (String serCap : serCaps) {
                LOGGER.info(serCap);
            }

            NetconfClientDriver netconfDriver = new NetconfClientDriverImpl(clientSession);

            // send create-subscription
            NotificationQueue queue = new NotificationQueue();
            Document createSubscriptionRequest = DocumentUtils.loadXmlDocument(AutomationTest.class
                    .getResourceAsStream("/create-subscription.xml"));
            String response = netconfDriver.sendCreateSubscriptionRequest(DocumentUtils.prettyPrint(createSubscriptionRequest), queue, null);
            System.out.println("Create-Subscription response: " + response);

            // send edit-config
            Document editRequest = DocumentUtils.loadXmlDocument(AutomationTest.class.getResourceAsStream("/edit-request.xml"));
            String editResponse = netconfDriver.sendEditConfigRequest(DocumentUtils.prettyPrint(editRequest), null);
            System.out.println("Edit Something: " + editResponse);

            // sleep 5s
            Thread.sleep(5000L);

            // check if the notification queue contains notification
            if (queue.retrieveAllNotification().isEmpty()) {
                LOGGER.info("Sorry! It is empty - no notification");
            } else {
                LOGGER.info("Notification in queue" + queue.retrieveNotification());
            }

            // send edit-config to remove
            Document removeRequest = DocumentUtils.loadXmlDocument(AutomationTest.class.getResourceAsStream("/remove-request.xml"));
            String removeResponse = netconfDriver.sendEditConfigRequest(DocumentUtils.prettyPrint(removeRequest), null);
            System.out.println("Remove the change: " + removeResponse);

            // check if the notification queue contains notification
            if (queue.retrieveAllNotification().isEmpty()) {
                LOGGER.info("Sorry! It is empty - no notification");
            } else {
                LOGGER.info("Notification in queue: \n" + queue.retrieveNotification());
            }

            // close session
            Document closeSessionRequest = DocumentUtils.loadXmlDocument(AutomationTest.class
                    .getResourceAsStream("/closeSessionRequest.xml"));
            String closeResponce = netconfDriver.sendCloseSessionRequest(DocumentUtils.prettyPrint(closeSessionRequest), null);
            if (null != closeResponce) {
                LOGGER.info("Close connection: " + closeResponce);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (NetconfConfigurationBuilderException e) {
            e.printStackTrace();
        } catch (NetconfClientDispatcherException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (NetconfMessageBuilderException e) {
            e.printStackTrace();
        }
    }

}
