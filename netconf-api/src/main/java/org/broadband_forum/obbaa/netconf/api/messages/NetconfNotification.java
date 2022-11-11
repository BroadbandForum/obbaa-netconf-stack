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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_WITH_TZ_DOT_WITHOUT_MS_PATTERN;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS_PATTERN;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_WITH_TZ_WITH_MS_PATTERN;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;


/**
 * An abstract class providing implementation for netconf notification. RFC5277
 * 
 *
 * 
 */
public class NetconfNotification implements Notification {
    
    private static final DateTimeFormatter ISO_DATA_TIME_FORMATTER = ISODateTimeFormat.dateTime();
    
    private CompletableFuture<String> m_messageSentFuture = new CompletableFuture<>();
    
    private String m_eventTime;
    private Element m_notificationElement;
    private String m_notificationString;
    private WeakReference<Element> m_notificationElementWR;
    private QName m_type;
    
    public NetconfNotification() {
        this.m_eventTime = NetconfResources.DATE_TIME_WITH_TZ.print(new DateTime(System.currentTimeMillis()));
    }
    
    /*
     * Parsing element to eventTime, notificationElement and type
     */
    public NetconfNotification(Document notifElement) throws NetconfMessageBuilderException {
        Element element = notifElement.getDocumentElement();
        if (element != null) {
            NodeList childNodes = element.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (NetconfResources.EVENT_TIME.equals(child.getNodeName())) {
                            String eventTime ="";
                            try {
                                eventTime = child.getTextContent();
                                setEventTime(eventTime);
                            } catch (DOMException | NetconfMessageBuilderException e) {
                                throw new NetconfMessageBuilderException("Invalid event time format: "+ eventTime);
                            }
                        } else {
                            m_type = QName.create(child.getNamespaceURI(), child.getLocalName());
                            m_notificationElement = (Element) child;
                        }
                    }
                }
            }
        }
    }

    public NetconfNotification(Document notifElement, String notificationString) throws NetconfMessageBuilderException {
        Element element = notifElement.getDocumentElement();
        if (element != null) {
            NodeList childNodes = element.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (NetconfResources.EVENT_TIME.equals(child.getNodeName())) {
                            String eventTime ="";
                            try {
                                eventTime = child.getTextContent();
                                setEventTime(eventTime);
                            } catch (DOMException | NetconfMessageBuilderException e) {
                                throw new NetconfMessageBuilderException("Invalid event time format: "+ eventTime);
                            }
                        } else {
                            m_type = QName.create(child.getNamespaceURI(), child.getLocalName());
                            m_notificationElementWR = new WeakReference<>((Element) child);
                            m_notificationString = notificationString;
                        }
                    }
                }
            }
        }
    }

    public Document getNotificationDocument() throws NetconfMessageBuilderException {
        synchronized (this){
            Document doc = new PojoToDocumentTransformer().newNetconfNotificationDocument(getEventTime(), getNotificationElement()).build();
            return doc;
        }
    }

    public String getEventTime() {
        if (m_eventTime == null) {
            m_eventTime = NetconfResources.DATE_TIME_WITH_TZ.print((new DateTime(System.currentTimeMillis())));
        }
        return m_eventTime;
    }

    public void setEventTime(long eventTime) {
        this.m_eventTime = NetconfResources.DATE_TIME_WITH_TZ.print(new DateTime(eventTime));
    }

    public void setEventTime(String eventTime) throws NetconfMessageBuilderException{
        eventTime = processEventTime(eventTime);
        if(DATE_TIME_WITH_TZ_WITHOUT_MS_PATTERN.matcher(eventTime).matches()
                || DATE_TIME_WITH_TZ_WITH_MS_PATTERN.matcher(eventTime).matches()) {
            setEventTime(NetconfResources.parseDateTime(eventTime).getMillis());
        } else {
            throw new NetconfMessageBuilderException("Invalid event time format: "+ eventTime);
        }
    }

    private String processEventTime(String eventTime) {
        // if eventTime is invalid with trailing dot and without ms, then just ignore the trailing dot
        boolean match = DATE_TIME_WITH_TZ_DOT_WITHOUT_MS_PATTERN.matcher(eventTime).matches();
        if (match) {
            return eventTime.replace(".", "");
        }
        return eventTime;
    }

    public Element getNotificationElement() {
        if (m_notificationElement != null) {
            return this.m_notificationElement;
        } else {
            if (m_notificationElementWR != null && m_notificationElementWR.get() != null) {
                return m_notificationElementWR.get();
            } else {
                try {
                    if (m_notificationString != null) {
                        return getChildNode(m_notificationString);
                    }
                } catch (NetconfMessageBuilderException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private Element getChildNode(String notificationString) throws NetconfMessageBuilderException {
        Element childNode = null;
        Element element = DocumentUtils.stringToDocumentElement(notificationString);
        if (element != null) {
            NodeList childNodes = element.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (!NetconfResources.EVENT_TIME.equals(child.getNodeName())) {
                            childNode = (Element) child;
                        }
                    }
                }
            }
        }
        return childNode;
    }

    public void setNotificationElement(Element notificationElement) {
        this.m_notificationElement = notificationElement;
    }

    public String notificationToString() {
        if(m_notificationString != null) {
            return m_notificationString;
        } else {
            try {
                return DocumentUtils.documentToString(getNotificationDocument());
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String notificationToPrettyString() {
        try {
            return DocumentUtils.documentToPrettyString(getNotificationDocument());
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public String getNotificationString() {
        return m_notificationString;
    }

    @VisibleForTesting
    public WeakReference<Element> getNotificationElementWR() {
        return m_notificationElementWR;
    }

    @VisibleForTesting
    public void setNotificationElementWR(WeakReference<Element> notificationElementWR) {
        m_notificationElementWR = notificationElementWR;
    }

    @Override
    public QName getType() {
        return m_type;
    }

    @Override
    public CompletableFuture<String> getMessageSentFuture() {
        return m_messageSentFuture;
    }

    @Override
    public String toString() {
        return notificationToPrettyString();
    }
    
}
