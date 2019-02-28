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

import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getNetConfResponses;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElement;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElementWithDeviations;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElementWithDeviationsOnModuleWithEmptyRevision;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElementWithSimYang;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionTestSetUp;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class AbstractNetconfClientSessionGetServerCapabilityTest extends AbstractNetconfClientSessionTestSetUp {
    final String m_feature;
    final String m_deviation;
    final int m_numberOfCapabilities;
    final CompletableFuture<NetConfResponse> m_response;

    public AbstractNetconfClientSessionGetServerCapabilityTest(final CompletableFuture<NetConfResponse> response, final String feature, final String deviation, final int numberOfCapabilities) {
        this.m_response = response;
        this.m_feature = feature;
        this.m_deviation = deviation;
        this.m_numberOfCapabilities = numberOfCapabilities;
    }

    @Test
    public void testOnHelloMessageYang1_1AttributesAreUpdatedInCache() throws NetconfMessageBuilderException {
        Mockito.doReturn(m_response).when(m_abstractNetconfClientSession).get(Mockito.any());
        m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("hello_yang1.1.xml"));
        assertTrue(m_abstractNetconfClientSession.getServerCapabilities().toString().contains(m_feature));
        assertTrue(m_abstractNetconfClientSession.getServerCapabilities().toString().contains(m_deviation));
        assertEquals(m_abstractNetconfClientSession.getServerCapabilities().size(), m_numberOfCapabilities);
        m_abstractNetconfClientSession.sessionClosed();
    }

    @Parameterized.Parameters (name = "{index}: testOnHelloMessageYang1_1AttributesAreUpdatedinCache")
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][] {
               { getNetConfResponses(getYangModulesResponseElementWithDeviationsOnModuleWithEmptyRevision()), "features=if-mib", "deviations=test-ietf-interfaces-dev", 16 },
               { getNetConfResponses(getYangModulesResponseElementWithDeviations()), "features=if-mib", "deviations=test-ietf-interfaces-dev", 12 },
               { getNetConfResponses(getYangModulesResponseElement()), "features=entity-sensor,entity-mib", "features=tag-rewrites", 14 },
               { getNetConfResponses(getYangModulesResponseElementWithSimYang()), "entity-sensor,entity-mib", "features=tag-rewrites", 15 }
        });
    }
}