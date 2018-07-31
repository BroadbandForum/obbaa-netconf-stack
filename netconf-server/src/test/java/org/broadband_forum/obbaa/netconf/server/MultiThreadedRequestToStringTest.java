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


import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class MultiThreadedRequestToStringTest {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadedRequestToStringTest.class);

    @Ignore("test can take time to run, not intended to eb used in CI")
    @Test
    public void testMultiThreadedRequestToStringDoesNotThrowExpcetion() throws NetconfMessageBuilderException,
            InterruptedException {
        EditConfigRequest request = new EditConfigRequest().setConfigElement(new EditConfigElement()
                .addConfigElementContent(DocumentUtils.stringToDocument("<anv:device-manager xmlns:anv=\"http://www.test.com/management-solutions/anv\" \n" +
                "                        xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> \n" +
                "      <adh:holder xmlns:adh=\"http://www.test-company.com/solutions/anv-holders\" " +
                        "\n" +
                "                         xc:operation=\"replace\"> \n" +
                "        <adh:name>TESTANV</adh:name> \n" +
                "      </adh:holder> \n" +
                "    </anv:device-manager> ").getDocumentElement()));

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Exception> errors = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                try {
                    request.requestToString();
                } catch (Exception e) {
                    errors.add(e);
                    //throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.SECONDS);
        if (errors.size() > 0) {
            fail("Un expected error occurred");
            LOGGER.error("Error : ", errors.get(0));
        }
    }
}
