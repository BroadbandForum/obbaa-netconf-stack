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

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getRpcRequest;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.DocumentBuilderFactoryWithoutDTD;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;

public class LogUtilTest {

    private static final String MESSAGE = "InputString : %s";
    private static final String DEBUG = "debug";
    private static final String TRACE = "trace";
    private static final String INFO = "info";
    private static final String EXPECTEDVALUE1 = "InputString : <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"/>\n";
    private static final String EXPECTEDVALUE2 = "InputString : <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <data>\n    <rpc-reply message-id=\"1\"/>\n  </data>\n</rpc-reply>\n";
    private static final String EXPECTEDVALUE3 = "InputString : TEST";
    private static final String EXPECTEDVALUE4 = "InputString : <null>";
    private static final String EXPECTEDVALUE5 = "InputString : <rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <retrieve-device-type-version xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">       <device-id>device-1</device-id>    </retrieve-device-type-version>\n</rpc>\n";
    
    private AdvancedLogger m_advancemocklogger = mock(AdvancedLogger.class);
    private Logger m_mocklogger = mock(Logger.class);
    private static final String INPUTSTRING = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
            + "    <retrieve-device-type-version xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">"
            + "       <device-id>device-1</device-id>" + "    </retrieve-device-type-version>" + "  </rpc>";

    private Document inputDocument() throws Exception {
        String InputString = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "</rpc-reply>";
        DocumentBuilderFactory dbf = DocumentBuilderFactoryWithoutDTD.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(InputString.getBytes("UTF-8"))); 
        return doc; 
    }

    private NetConfResponse inputNcResponse(Document document) throws Exception {
        NetConfResponse ncresponse = new NetConfResponse();
        return ncresponse.addDataContent(inputDocument().getDocumentElement());
    }
    
    private void validateLoggerType(Object object, String logtype, String expectedstring) {
        if (object instanceof AdvancedLogger) {
            if (logtype.equals(DEBUG)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_advancemocklogger).debug(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            } else if (logtype.equals(INFO)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_advancemocklogger).info(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            } else if (logtype.equals(TRACE)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_advancemocklogger).trace(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            }
        } else if (object instanceof Logger) {
            if (logtype.equals(DEBUG)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_mocklogger).debug(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            } else if (logtype.equals(INFO)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_mocklogger).info(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            } else if (logtype.equals(TRACE)) {
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(m_mocklogger).trace(captor.capture());
                assertEquals(expectedstring, captor.getValue());
            }
        }

    }
    
    @Test
    public void testAdvanceLoggerDebugforDocumentArgument() throws Exception {
        when(m_advancemocklogger.isDebugEnabled()).thenReturn(true);
        LogUtil.logDebug(m_advancemocklogger, MESSAGE, inputDocument());
        validateLoggerType(m_advancemocklogger,"DEBUG",EXPECTEDVALUE1);
    }

    @Test
    public void testLoggerDebugforDocumentArgument() throws Exception {
        when(m_mocklogger.isDebugEnabled()).thenReturn(true);
        LogUtil.logDebug(m_mocklogger, MESSAGE, inputDocument());
        validateLoggerType(m_mocklogger,"DEBUG",EXPECTEDVALUE1);
    }

    @Test
    public void testAdvanceLoggerInfoforDocumentArgument() throws Exception {
        when(m_advancemocklogger.isInfoEnabled()).thenReturn(true);
        LogUtil.logInfo(m_advancemocklogger, MESSAGE, inputDocument());
        validateLoggerType(m_advancemocklogger,"INFO",EXPECTEDVALUE1);

    }

    @Test
    public void testLoggerInfoforDocumentArgument() throws Exception {
        when(m_mocklogger.isInfoEnabled()).thenReturn(true);
        LogUtil.logInfo(m_mocklogger, MESSAGE, inputDocument());
        validateLoggerType(m_mocklogger,"INFO",EXPECTEDVALUE1);

    }

    @Test
    public void testAdvanceLoggerTraceforDocumentArgument() throws Exception {
        when(m_advancemocklogger.isTraceEnabled()).thenReturn(true);
        LogUtil.logTrace(m_advancemocklogger, MESSAGE, inputDocument());
        validateLoggerType(m_advancemocklogger,"TRACE",EXPECTEDVALUE1);

    }

    @Test
    public void testAdvanceLoggerDebugforResponseArgument() throws Exception {
        when(m_advancemocklogger.isDebugEnabled()).thenReturn(true);
        LogUtil.logDebug(m_advancemocklogger, MESSAGE, inputNcResponse(inputDocument()));
        validateLoggerType(m_advancemocklogger,"DEBUG",EXPECTEDVALUE2);

    }

    @Test
    public void testLoggerDebugforResponseArgument() throws Exception {
        when(m_mocklogger.isDebugEnabled()).thenReturn(true);
        LogUtil.logDebug(m_mocklogger, MESSAGE, inputNcResponse(inputDocument()));
        validateLoggerType(m_mocklogger,"DEBUG", EXPECTEDVALUE2);

    }

    @Test
    public void testAdvanceLoggerDebugforArgumentasString() {
        when(m_advancemocklogger.isDebugEnabled()).thenReturn(true);
        String test = "TEST";
        LogUtil.logDebug(m_advancemocklogger, MESSAGE, test);
        validateLoggerType(m_advancemocklogger,"DEBUG", EXPECTEDVALUE3);
    }

    @Test
    public void testLoggerDebugforArgumentasString() {
        when(m_mocklogger.isDebugEnabled()).thenReturn(true);
        String test = "TEST";
        LogUtil.logDebug(m_mocklogger, MESSAGE, test);
        validateLoggerType(m_mocklogger,"DEBUG", EXPECTEDVALUE3);
    }

    @Test
    public void testAdvanceLogerDebugforNegativecase2() {
        when(m_advancemocklogger.isDebugEnabled()).thenReturn(true);
        String test = null;
        LogUtil.logDebug(m_advancemocklogger, MESSAGE, test);
        validateLoggerType(m_advancemocklogger,"DEBUG", EXPECTEDVALUE4);
    }

    @Test
    public void testLogerDebugforNegativecase2() {
        when(m_mocklogger.isDebugEnabled()).thenReturn(true);
        String test = null;
        LogUtil.logDebug(m_mocklogger, MESSAGE, test);
        validateLoggerType(m_mocklogger,"DEBUG", EXPECTEDVALUE4);
    }

    @Test
    public void testAdvanceLoggerDebugforRequestArgument() throws NetconfMessageBuilderException {
        when(m_advancemocklogger.isDebugEnabled()).thenReturn(true);
        AbstractNetconfRequest request = getRpcRequest(stringToDocument(INPUTSTRING));
        LogUtil.logDebug(m_advancemocklogger, MESSAGE, request);
        validateLoggerType(m_advancemocklogger,"DEBUG", EXPECTEDVALUE5);
    }

    @Test
    public void testLoggerDebugforRequestArgument() throws NetconfMessageBuilderException {
        when(m_mocklogger.isDebugEnabled()).thenReturn(true);
        AbstractNetconfRequest request = getRpcRequest(stringToDocument(INPUTSTRING));
        LogUtil.logDebug(m_mocklogger, MESSAGE, request);
        validateLoggerType(m_mocklogger,"DEBUG", EXPECTEDVALUE5);

    }
}
