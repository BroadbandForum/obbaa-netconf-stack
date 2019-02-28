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

package org.broadband_forum.obbaa.netconf.nc.stack.examples;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

/**
 * Simple Utility to demonstrate working with RPC requests/responses.
 */
public class ClientUtil {
    private static final Logger LOGGER = Logger.getLogger(ClientUtil.class);

    public static void runNetconfRequests(NetconfClientSession clientSession) {
        try {
            while (clientSession != null && clientSession.isOpen()) {
                Thread.sleep(1000);
                NetConfResponse response = clientSession.get(new GetRequest()).get();
                if(response != null){
                    LOGGER.info("Got response from server response: " + response.responseToString());
                    Thread.sleep(1000);
                }

                response = clientSession.getConfig(new GetConfigRequest().setSourceRunning()).get();
                LOGGER.info("Got response from server response: " + response.responseToString());

                Thread.sleep(1000);
                response = clientSession.editConfig(new EditConfigRequest().setTargetRunning().setConfigElement(new
                        EditConfigElement()
                        .addConfigElementContent(DocumentUtils.stringToDocument("<some-configuration-node/>")
                                .getDocumentElement()))).get();
                LOGGER.info("Got response from server response: " + response.responseToString());
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception", e);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error occured while sending response", e);
        } catch (ExecutionException e) {
            LOGGER.error("Error occured during execution ", e);
        }
    }

    public static void toggleTCPKA(NetconfClientSession session) {
        Scanner scanner = new Scanner(System.in);
        while(true){
            LOGGER.info("Set TCP KA Option ON/OFF: ");
            String option = scanner.nextLine().trim();
            if("ON".equals(option)){
                session.setTcpKeepAlive(true);
            }else {
                session.setTcpKeepAlive(false);
            }

        }
    }
}
