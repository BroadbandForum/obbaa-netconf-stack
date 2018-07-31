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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class NetconfRpcErrorTest {

    private NetconfRpcError m_rpcError;
    private Element m_error_info_null = null;

    @Test
    public void testGetUnknownElementError() {
        m_rpcError = NetconfRpcError.getUnknownElementError("unknown-element", NetconfRpcErrorType.RPC);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, m_rpcError.getErrorTag());
    }

    @Test
    public void testGetBadElementError() {
        m_rpcError = NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application);
        assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, m_rpcError.getErrorTag());
    }

    @Test
    public void testGetBadAttributeError() {
        m_rpcError = NetconfRpcError
                .getBadAttributeError("delete", NetconfRpcErrorType.Application, String.format("Bad delete attribute "))
                .setErrorAppTag("missing-instance");
        assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, m_rpcError.getErrorTag());
    }

    @Test
    public void testGetUnknownNamespaceError() {
        m_rpcError = NetconfRpcError.getUnknownNamespaceError("unknown-namespace", "local-name",
                NetconfRpcErrorType.Application);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, m_rpcError.getErrorTag());
    }

    @Test
    public void testGetMissingElementError() {
        m_rpcError = NetconfRpcError.getMissingElementError(Collections.singletonList("startTime"),
                NetconfRpcErrorType.Protocol);
        assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, m_rpcError.getErrorTag());
    }

    @Test
    public void testAddErrorInfoElements() {
        m_rpcError = NetconfRpcError.getMissingElementError(Collections.singletonList("startTime"),
                NetconfRpcErrorType.Protocol);
        assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, m_rpcError.getErrorTag());

        m_rpcError.setErrorInfo(m_error_info_null);
        List<Pair<String, String>> errorInfoElements = new ArrayList<>();
        errorInfoElements.add(new Pair<String, String>("version", "1"));
        errorInfoElements.add(new Pair<String, String>("interface-version", "1.0"));
        errorInfoElements.add(new Pair<String, String>("username", "test"));

        m_rpcError = m_rpcError.addErrorInfoElements(errorInfoElements);

        assertNotNull(m_rpcError.getErrorInfo());
    }

    @Test
    public void testGetMissingKeyError() {
        List<String> missingKeys = Arrays.asList("test1", "test2");
        String expectedErrorMsg = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISSING, missingKeys);
        m_rpcError = NetconfRpcError.getMissingKeyError(missingKeys, NetconfRpcErrorType.Protocol);
        assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, m_rpcError.getErrorTag());
        assertEquals(expectedErrorMsg, m_rpcError.getErrorMessage());
    }

    @Test
    public void testGetMisplacedKeyError() {
        List<String> missingKeys = Arrays.asList("test1", "test2");
        String expectedErrorMsg = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISPLACED, missingKeys);
        m_rpcError = NetconfRpcError.getMisplacedKeyError(missingKeys, NetconfRpcErrorType.Protocol);
        assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, m_rpcError.getErrorTag());
        assertEquals(expectedErrorMsg, m_rpcError.getErrorMessage());
    }
}
