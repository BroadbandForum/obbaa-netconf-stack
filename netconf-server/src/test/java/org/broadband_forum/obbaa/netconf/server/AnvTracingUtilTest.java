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


import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class AnvTracingUtilTest {

    @Test
    public void testPollingRequest() throws NetconfMessageBuilderException {
        GetConfigRequest request = new GetConfigRequest();
        NetconfFilter filter = new NetconfFilter();
        request.setFilter(filter);
        boolean isPollingRequest = AnvTracingUtil.isEmptyRequest(request);
        Assert.assertTrue(isPollingRequest);

        GetRequest getRequest = new GetRequest();
        getRequest.setFilter(filter);
        isPollingRequest = AnvTracingUtil.isEmptyRequest(getRequest);
        Assert.assertFalse(isPollingRequest);

        Document reqDoc = DocumentUtils.stringToDocument(getRequestMessage());
        isPollingRequest = AnvTracingUtil.isEmptyRequest(reqDoc, NetconfResources.GET_CONFIG);
        Assert.assertTrue(isPollingRequest);

        isPollingRequest = AnvTracingUtil.isEmptyRequest(reqDoc, NetconfResources.GET);
        Assert.assertFalse(isPollingRequest);
    }

    private String getRequestMessage() {
        return "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"13\"><get-config><source><running/></source><filter/></get-config></rpc>";
    }

}
