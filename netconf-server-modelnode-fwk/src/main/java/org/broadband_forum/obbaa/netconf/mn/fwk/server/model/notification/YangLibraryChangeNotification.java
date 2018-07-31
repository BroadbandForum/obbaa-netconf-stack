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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class YangLibraryChangeNotification extends NetconfNotification {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(YangLibraryChangeNotification.class,
            "netconf-stack", "DEBUG", "GLOBAL");
    public static final String IETF_YANG_LIBRARY_NS = "urn:ietf:params:xml:ns:yang:ietf-yang-library";
    private static final QName TYPE = QName.create(IETF_YANG_LIBRARY_NS, "yang-library-change");
    private static final String YANG_LIBRARY_CHANGE = "yang-library-change";
    private static final String MODULE_SET_ID = "module-set-id";
    private static final String COLON = ":";
    private String m_moduleSetIdValue;
    private String m_prefix;

    public YangLibraryChangeNotification(String prefix, String moduleSetIdValue) {
        super();
        this.m_moduleSetIdValue = moduleSetIdValue;
        this.m_prefix = prefix;
    }

    public String getValue() {
        return m_moduleSetIdValue;
    }

    @Override
    public Element getNotificationElement() {
        try {
            Element notificationElement = super.getNotificationElement();
            if (notificationElement == null) {
                notificationElement = buildYangLibraryChangeNotificationElement(m_prefix, m_moduleSetIdValue);
                setNotificationElement(notificationElement);
            }
            return notificationElement;
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while getting yang-library-change-notification element. ", e);
        }
        return null;
    }

    @Override
    public QName getType() {
        return TYPE;
    }

    public Element buildYangLibraryChangeNotificationElement(String prefix, String moduleSetId) throws
            NetconfMessageBuilderException {
        Document doc = DocumentUtils.createDocument();
        Element yangLibraryChangeNotificationElement = doc.createElementNS(IETF_YANG_LIBRARY_NS,
                buildLocalName(prefix, YANG_LIBRARY_CHANGE));

        Element moduleSetIdElement = doc.createElementNS(IETF_YANG_LIBRARY_NS, buildLocalName(prefix, MODULE_SET_ID));
        moduleSetIdElement.setTextContent(moduleSetId);
        yangLibraryChangeNotificationElement.appendChild(moduleSetIdElement);
        return yangLibraryChangeNotificationElement;
    }

    private String buildLocalName(String prefix, String localName) {
        if (prefix == null) {
            return localName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(COLON).append(localName);
        return sb.toString();
    }
}
