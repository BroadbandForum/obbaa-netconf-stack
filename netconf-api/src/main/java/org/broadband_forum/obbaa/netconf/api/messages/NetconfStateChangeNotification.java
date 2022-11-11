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

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class NetconfStateChangeNotification extends NetconfNotification {
    
private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(StateChangeNotification.class, LogAppNames.NETCONF_LIB);
    
    public static final QName TYPE = QName.create(NetconfResources.NC_STACK_NS, "netconf-state-change");
    
    private List<StateChangeInfo> m_changesList = new ArrayList<StateChangeInfo>();
    
    
    public NetconfStateChangeNotification(List<StateChangeInfo> changesList) {
        this.m_changesList = changesList;
    }
    
    public NetconfStateChangeNotification(StateChangeInfo stateChangeInfo) {
        m_changesList.add(stateChangeInfo);
    }
    
    public List<StateChangeInfo> getChangesList() {
        return m_changesList;
    }

    public void setChangesList(List<StateChangeInfo> changesList) {
        this.m_changesList = changesList;
    }
    
    public void setChangesList(StateChangeInfo stateChangeInfo) {
        m_changesList.add(stateChangeInfo);
    }

    @Override
    public Element getNotificationElement() {
        Element nofElement = super.getNotificationElement();
        try {
            if (nofElement == null) {
                PojoToDocumentTransformer transformer = new PojoToDocumentTransformer();
                nofElement = transformer.getStateChangeNotificationElement(m_changesList);
                setNotificationElement(nofElement);
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while getting state-change notification element. ", e);
        }
        return nofElement;
    }
    
    @Override
    public QName getType() {
        return TYPE;
    }

}
