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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class MustPresenceValidationTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testMustFailsWhenContainerDoesNotExist() throws ModelNodeInitException {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<must-presence-validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <referring-container/>" +
                "</must-presence-validation>";

        getModelNode();
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);

        String expectedErrorMsg = "referred container must exist";
        assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:must-presence-validation/validation:referring-container",
                response.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustPassesWhenContainerExists() throws ModelNodeInitException {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<must-presence-validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <referring-container/>" +
                "    <referred-container/>" +
                "</must-presence-validation>";

        getModelNode();
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
    }
}
