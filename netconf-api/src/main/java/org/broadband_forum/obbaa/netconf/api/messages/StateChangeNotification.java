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

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * Created by nhtoan on 1/28/16.
 */
public class StateChangeNotification extends NetconfNotification {
    
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(StateChangeNotification.class, LogAppNames.NETCONF_LIB);
    
    public static final QName TYPE = QName.create(NetconfResources.NC_STACK_NS, "state-change-notification");

    private String m_target;
    private String m_value;

    public StateChangeNotification() {
        super();
    }
    
    public StateChangeNotification(String target, String value) {
        super();
        this.m_target = target;
        this.m_value = value;
    }

    public String getTarget() {
        return m_target;
    }

    public String getValue() {
        return m_value;
    }

    @Override
    public Element getNotificationElement() {
        try {
            Element nofElement = super.getNotificationElement();
            if (nofElement == null) {
                PojoToDocumentTransformer transformer = new PojoToDocumentTransformer();
                nofElement = transformer.getStateChangeNotificationElement(m_target, m_value);
                setNotificationElement(nofElement);
            }
            return nofElement;
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while getting state-change-notification element. ", e);
        }
        return null;
    }
    
    @Override
    public QName getType() {
        return TYPE;
    }
}
